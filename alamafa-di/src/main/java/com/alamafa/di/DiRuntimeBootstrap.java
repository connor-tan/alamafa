package com.alamafa.di;

import com.alamafa.config.Configuration;
import com.alamafa.config.ConfigurationLoader;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLifecycle;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.Lifecycle;
import com.alamafa.core.LifecycleExecutionException;
import com.alamafa.core.LifecyclePhase;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.core.runner.ApplicationRunner;
import com.alamafa.core.runner.CommandLineRunner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 负责在 {@link com.alamafa.core.ApplicationBootstrap} 生命周期内构建 DI 环境，
 * 包括加载配置、初始化 {@link BeanRegistry} 以及管理 {@link ApplicationLifecycle} Bean。
 */
public final class DiRuntimeBootstrap implements Lifecycle {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(DiRuntimeBootstrap.class);

    private final List<Class<?>> configurationClasses;
    private final List<String> scanPackages;
    private final ConfigurationLoader configurationLoader;

    private BeanRegistry registry;
    private List<ApplicationLifecycle> lifecycleBeans = List.of();
    private List<ApplicationRunner> applicationRunners = List.of();
    private List<CommandLineRunner> commandLineRunners = List.of();

    private DiRuntimeBootstrap(List<Class<?>> configurationClasses,
                               List<String> scanPackages,
                               ConfigurationLoader configurationLoader) {
        this.configurationClasses = configurationClasses;
        this.scanPackages = scanPackages;
        this.configurationLoader = configurationLoader;
    }

    /**
     * 创建新的构建器，用于声明需要导入的配置类与扫描包。
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void init(ApplicationContext context) throws Exception {
        Objects.requireNonNull(context, "context");
        registry = ensureRegistry(context);
        ensureConfiguration(context, registry);
        registerBootstrapBeans(context, registry);
        if (!configurationClasses.isEmpty()) {
            registry.registerConfigurations(configurationClasses.toArray(Class<?>[]::new));
        }
        if (!scanPackages.isEmpty()) {
            registry.scanPackages(scanPackages.toArray(String[]::new));
        }
        registry.initializeSingletonPostProcessors();
        lifecycleBeans = registry.getBeansOfType(ApplicationLifecycle.class)
                .stream()
                .sorted(Comparator.comparingInt(ApplicationLifecycle::getOrder))
                .toList();
        applicationRunners = registry.getBeansOfType(ApplicationRunner.class)
                .stream()
                .sorted(Comparator.comparingInt(ApplicationRunner::getOrder))
                .toList();
        commandLineRunners = registry.getBeansOfType(CommandLineRunner.class)
                .stream()
                .sorted(Comparator.comparingInt(CommandLineRunner::getOrder))
                .toList();
        for (ApplicationLifecycle lifecycle : lifecycleBeans) {
            try {
                lifecycle.init(context);
            } catch (Exception ex) {
                LOGGER.error("ApplicationLifecycle {} failed during init", lifecycle.getClass().getName(), ex);
                throw wrap(lifecycle, LifecyclePhase.INIT, ex);
            }
        }
    }

    @Override
    public void start(ApplicationContext context) throws Exception {
        for (ApplicationLifecycle lifecycle : lifecycleBeans) {
            try {
                lifecycle.start(context);
            } catch (Exception ex) {
                LOGGER.error("ApplicationLifecycle {} failed during start", lifecycle.getClass().getName(), ex);
                throw wrap(lifecycle, LifecyclePhase.START, ex);
            }
        }
        runRunners(context);
        if (lifecycleBeans.isEmpty()) {
            attemptAutoShutdown(context);
        }
    }

    @Override
    public void stop(ApplicationContext context) throws Exception {
        LifecycleExecutionException firstError = null;
        for (int i = lifecycleBeans.size() - 1; i >= 0; i--) {
            ApplicationLifecycle lifecycle = lifecycleBeans.get(i);
            try {
                lifecycle.stop(context);
            } catch (Exception ex) {
                LOGGER.error("ApplicationLifecycle {} failed during stop", lifecycle.getClass().getName(), ex);
                LifecycleExecutionException wrapped = wrap(lifecycle, LifecyclePhase.STOP, ex);
                if (firstError == null) {
                    firstError = wrapped;
                } else {
                    firstError.addSuppressed(wrapped);
                }
            }
        }
        BeanRegistry activeRegistry = registry;
        if (activeRegistry != null) {
            activeRegistry.destroySingletons();
        }
        lifecycleBeans = List.of();
        applicationRunners = List.of();
        commandLineRunners = List.of();
        registry = null;
        if (firstError != null) {
            throw firstError;
        }
    }

    private LifecycleExecutionException wrap(ApplicationLifecycle lifecycle, LifecyclePhase phase, Exception ex) {
        if (ex instanceof LifecycleExecutionException execution && execution.phase() == phase) {
            return execution;
        }
        return new LifecycleExecutionException(phase, lifecycle.getClass().getName(), ex);
    }

    private LifecycleExecutionException wrap(Object runner, Exception ex) {
        if (ex instanceof LifecycleExecutionException execution && execution.phase() == LifecyclePhase.START) {
            return execution;
        }
        String target = runner == null ? "unknown runner" : runner.getClass().getName();
        return new LifecycleExecutionException(LifecyclePhase.START, target, ex);
    }

    private BeanRegistry ensureRegistry(ApplicationContext context) {
        BeanRegistry existing = context.get(BeanRegistry.class);
        if (existing != null) {
            return existing;
        }
        BeanRegistry created = new BeanRegistry(context);
        context.put(BeanRegistry.class, created);
        return created;
    }

    private void ensureConfiguration(ApplicationContext context, BeanRegistry registry) {
        Configuration configuration = context.get(Configuration.class);
        if (configuration == null) {
            ConfigurationLoader loader = configurationLoader != null
                    ? configurationLoader
                    : ConfigurationLoader.withDefaults();
            configuration = loader.load();
            context.put(Configuration.class, configuration);
        }
        if (!registry.hasBeanDefinition(Configuration.class)) {
            registry.register(Configuration.class,
                    new BeanDefinition<>(Configuration.class,
                            () -> context.get(Configuration.class),
                            BeanDefinition.Scope.SINGLETON,
                            true,
                            false));
        }
    }

    private void registerBootstrapBeans(ApplicationContext context, BeanRegistry registry) {
        if (!registry.hasBeanDefinition(BeanRegistry.class)) {
            registry.register(BeanRegistry.class,
                    new BeanDefinition<>(BeanRegistry.class, () -> registry, BeanDefinition.Scope.SINGLETON,
                            true, false));
        }
        context.put(BeanRegistry.class, registry);
    }

    private void runRunners(ApplicationContext context) throws Exception {
        if (applicationRunners.isEmpty() && commandLineRunners.isEmpty()) {
            return;
        }
        ApplicationArguments arguments = context.get(ApplicationArguments.class);
        for (ApplicationRunner runner : applicationRunners) {
            try {
                runner.run(context, arguments);
            } catch (Exception ex) {
                LOGGER.error("ApplicationRunner {} failed", runner.getClass().getName(), ex);
                throw wrap(runner, ex);
            }
        }
        for (CommandLineRunner runner : commandLineRunners) {
            try {
                runner.run(arguments);
            } catch (Exception ex) {
                LOGGER.error("CommandLineRunner {} failed", runner.getClass().getName(), ex);
                throw wrap(runner, ex);
            }
        }
    }

    private void attemptAutoShutdown(ApplicationContext context) {
        if (applicationRunners.isEmpty() && commandLineRunners.isEmpty()) {
            return;
        }
        ApplicationShutdown shutdown = context.get(ApplicationShutdown.class);
        if (shutdown != null) {
            shutdown.requestShutdown();
        } else {
            LOGGER.warn("Application runners completed but ApplicationShutdown is unavailable; JVM will remain running.");
        }
    }

    /**
     * 构建器，用于配置 DI 引导行为。
     */
    public static final class Builder {
        private final List<Class<?>> configurationClasses = new ArrayList<>();
        private final List<String> scanPackages = new ArrayList<>();
        private ConfigurationLoader configurationLoader;

        private Builder() {
        }

        /**
         * 导入一个或多个 @Configuration 类。
         */
        public Builder withConfigurations(Class<?>... configurations) {
            if (configurations == null) {
                return this;
            }
            for (Class<?> configuration : configurations) {
                if (configuration != null) {
                    configurationClasses.add(configuration);
                }
            }
            return this;
        }

        /**
         * 指定需要扫描的基础包。
         */
        public Builder scanPackages(String... packages) {
            if (packages == null) {
                return this;
            }
            for (String basePackage : packages) {
                if (basePackage != null && !basePackage.isBlank()) {
                    scanPackages.add(basePackage.trim());
                }
            }
            return this;
        }

        /**
         * 自定义配置加载器；未提供时默认采用 {@link ConfigurationLoader#withDefaults()}。
         */
        public Builder withConfigurationLoader(ConfigurationLoader loader) {
            this.configurationLoader = loader;
            return this;
        }

        public DiRuntimeBootstrap build() {
            return new DiRuntimeBootstrap(List.copyOf(configurationClasses),
                    List.copyOf(scanPackages),
                    configurationLoader);
        }
    }
}
