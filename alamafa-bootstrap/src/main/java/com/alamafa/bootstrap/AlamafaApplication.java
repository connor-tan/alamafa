package com.alamafa.bootstrap;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.bootstrap.autoconfigure.AutoConfigurationLoader;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationBootstrap;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ContextAwareApplicationLauncher;
import com.alamafa.core.DefaultApplicationLauncher;
import com.alamafa.core.Lifecycle;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.DiRuntimeBootstrap;
import com.alamafa.di.annotation.Configuration;

import java.lang.StackWalker;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point similar to Spring Boot's {@code SpringApplication}, orchestrating the Alamafa runtime.
 */
public final class AlamafaApplication {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(AlamafaApplication.class);

    private AlamafaApplication() {
    }

    public static void run(String... args) {
        Class<?> primarySource = locatePrimarySource();
        run(primarySource, args);
    }

    public static void run(Class<?> primarySource, String... args) {
        Objects.requireNonNull(primarySource, "primarySource");
        AlamafaBootApplication metadata = primarySource.getAnnotation(AlamafaBootApplication.class);
        if (metadata == null) {
            throw new IllegalArgumentException(
                    primarySource.getName() + " must be annotated with @AlamafaBootApplication");
        }
        ContextAwareApplicationLauncher launcher = instantiateLauncher(metadata.launcher());
        ApplicationBootstrap bootstrap = new ApplicationBootstrap(launcher);
        ApplicationContext context = bootstrap.getContext();

        ApplicationArguments applicationArguments = new ApplicationArguments(args);
        context.put(ApplicationArguments.class, applicationArguments);

        LinkedHashSet<String> scanPackages = new LinkedHashSet<>(resolveBasePackages(metadata, primarySource));
        LinkedHashSet<Class<?>> configurationClasses = new LinkedHashSet<>();
        addConfigurationIfPresent(configurationClasses, primarySource);

        ClassLoader classLoader = determineClassLoader(primarySource);

        AlamafaBootstrapContext moduleContext = new AlamafaBootstrapContext(
                bootstrap,
                context,
                primarySource,
                configurationClasses,
                scanPackages);
        registerAutoConfigurations(moduleContext, classLoader);
        configureModules(metadata.modules(), moduleContext);

        registerDiBootstrap(bootstrap, moduleContext, metadata, primarySource);

        LOGGER.info("Starting {} with {} argument(s)", primarySource.getSimpleName(), applicationArguments.asList().size());
        bootstrap.launch(Lifecycle.NO_OP);
    }

    private static void registerDiBootstrap(ApplicationBootstrap bootstrap,
                                            AlamafaBootstrapContext moduleContext,
                                            AlamafaBootApplication metadata,
                                            Class<?> primarySource) {
        DiRuntimeBootstrap.Builder builder = DiRuntimeBootstrap.builder();
        Set<Class<?>> configurationClasses = new LinkedHashSet<>(moduleContext.configurationClasses());
        if (!configurationClasses.isEmpty()) {
            builder.withConfigurations(configurationClasses.toArray(Class[]::new));
        }
        Set<String> packages = new LinkedHashSet<>(moduleContext.scanPackages());
        if (packages.isEmpty()) {
            packages = resolveBasePackages(metadata, primarySource);
        }
        if (!packages.isEmpty()) {
            builder.scanPackages(packages.toArray(String[]::new));
        }
        bootstrap.addLifecycleParticipant(builder.build());
    }

    private static void configureModules(Class<?>[] moduleClasses, AlamafaBootstrapContext context) {
        if (moduleClasses == null) {
            return;
        }
        for (Class<?> moduleClass : moduleClasses) {
            if (moduleClass == null) {
                continue;
            }
            if (moduleClass.isAnnotationPresent(Configuration.class)) {
                context.addConfiguration(moduleClass);
            }
            boolean requiresInstantiation = AlamafaBootstrapModule.class.isAssignableFrom(moduleClass)
                    || Lifecycle.class.isAssignableFrom(moduleClass);
            if (!requiresInstantiation) {
                continue;
            }
            Object instance = instantiateModule(moduleClass);
            if (instance instanceof AlamafaBootstrapModule module) {
                module.configure(context);
            }
            if (instance instanceof Lifecycle lifecycle) {
                context.addLifecycleParticipant(lifecycle);
            }
        }
    }

    private static Object instantiateModule(Class<?> moduleClass) {
        try {
            return moduleClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to instantiate module " + moduleClass.getName(), ex);
        }
    }

    private static LinkedHashSet<String> resolveBasePackages(AlamafaBootApplication metadata, Class<?> primarySource) {
        Stream<String> explicitPackages = Arrays.stream(metadata.scanBasePackages())
                .filter(pkg -> pkg != null && !pkg.isBlank())
                .map(String::trim);
        Stream<String> classBasedPackages = Arrays.stream(metadata.scanBasePackageClasses())
                .filter(Objects::nonNull)
                .map(Class::getPackageName)
                .filter(pkg -> !pkg.isBlank());
        LinkedHashSet<String> packages = explicitPackages
                .collect(Collectors.toCollection(LinkedHashSet::new));
        classBasedPackages.forEach(packages::add);
        if (packages.isEmpty()) {
            String primaryPackage = primarySource.getPackageName();
            if (!primaryPackage.isBlank()) {
                packages.add(primaryPackage);
            }
        }
        return packages;
    }

    private static void registerAutoConfigurations(AlamafaBootstrapContext context, ClassLoader classLoader) {
        for (String className : AutoConfigurationLoader.loadAutoConfigurations(classLoader)) {
            try {
                Class<?> candidate = Class.forName(className, false, classLoader);
                if (!candidate.isAnnotationPresent(AutoConfiguration.class)) {
                    LOGGER.warn("Auto configuration {} is missing @AutoConfiguration", className);
                    continue;
                }
                context.addConfiguration(candidate);
            } catch (ClassNotFoundException ex) {
                LOGGER.warn("Auto configuration class {} not found", className, ex);
            }
        }
    }

    private static ClassLoader determineClassLoader(Class<?> primarySource) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            return loader;
        }
        return primarySource.getClassLoader();
    }

    private static void addConfigurationIfPresent(LinkedHashSet<Class<?>> configurationClasses,
                                                  Class<?> candidate) {
        if (candidate.isAnnotationPresent(Configuration.class)) {
            configurationClasses.add(candidate);
        }
    }

    private static Class<?> locatePrimarySource() {
        try {
            return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .walk(stream -> stream
                            .map(StackWalker.StackFrame::getDeclaringClass)
                            .filter(clazz -> clazz != AlamafaApplication.class)
                            .filter(clazz -> clazz.isAnnotationPresent(AlamafaBootApplication.class))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Unable to determine @AlamafaBootApplication declaring class")));
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to locate @AlamafaBootApplication class", ex);
        }
    }

    private static ContextAwareApplicationLauncher instantiateLauncher(Class<? extends ContextAwareApplicationLauncher> launcherType) {
        Class<? extends ContextAwareApplicationLauncher> type = launcherType == null
                ? DefaultApplicationLauncher.class
                : launcherType;
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to instantiate launcher " + type.getName(), ex);
        }
    }
}
