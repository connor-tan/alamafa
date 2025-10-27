package com.alamafa.starter;

import com.alamafa.bootstrap.BootstrapProfile;
import com.alamafa.bootstrap.BootApplicationDescriptor;
import com.alamafa.bootstrap.BootApplicationParser;
import com.alamafa.core.ApplicationBootstrap;
import com.alamafa.core.ContextAwareApplicationLauncher;
import com.alamafa.core.Lifecycle;
import com.alamafa.di.DiRuntimeBootstrap;
import com.alamafa.di.annotation.Configuration;

import java.lang.reflect.Constructor;

/**
 * Unified entry point similar to SpringApplication for Alamafa.
 */
public final class AlamafaLauncher {
    private AlamafaLauncher() {}

    /**
     * Launch application using provided primary source; returns ApplicationContext after startup.
     */
    public static com.alamafa.core.ApplicationContext run(Class<?> primarySource, String... args) {
        BootApplicationDescriptor descriptor = BootApplicationParser.parse(primarySource);
        ContextAwareApplicationLauncher launcher = new SimpleConsoleLauncher();
        ApplicationBootstrap bootstrap = new ApplicationBootstrap(launcher);

        // basic context initializers (args, primary class)
        bootstrap.addContextInitializer(ctx -> {
            ctx.put(String[].class, args != null ? args : new String[0]);
            ctx.put(Class.class, primarySource); // primary marker
        });

        // Prepare DI builder (defer build until modules processed)
        DiRuntimeBootstrap.Builder diBuilder = DiRuntimeBootstrap.builder()
                .scanPackages(descriptor.basePackages().toArray(String[]::new));

        // Process modules
        for (Class<?> moduleClass : descriptor.moduleClasses()) {
            attachModule(bootstrap, diBuilder, moduleClass);
        }

        // Build DI runtime and register lifecycle participant last
        DiRuntimeBootstrap diRuntime = diBuilder.build();
        bootstrap.addLifecycleParticipant(diRuntime);

        bootstrap.launch(Lifecycle.NO_OP);
        return launcher.getContext();
    }

    private static void attachModule(ApplicationBootstrap bootstrap, DiRuntimeBootstrap.Builder diBuilder, Class<?> moduleClass) {
        if (moduleClass.isAnnotationPresent(Configuration.class)) {
            diBuilder.withConfigurations(moduleClass);
            return;
        }
        if (Lifecycle.class.isAssignableFrom(moduleClass)) {
            bootstrap.addLifecycleParticipant((Lifecycle) instantiate(moduleClass));
            return;
        }
        if (BootstrapProfile.class.isAssignableFrom(moduleClass)) {
            BootstrapProfile profile = (BootstrapProfile) instantiate(moduleClass);
            profile.apply(bootstrap); // profile may add lifecycle participants
            return;
        }
        // Unknown module type: ignore silently for now (could log later)
    }

    private static Object instantiate(Class<?> type) {
        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate module " + type.getName(), e);
        }
    }

    /** Simple console launcher variant (non-blocking stop) */
    private static final class SimpleConsoleLauncher implements ContextAwareApplicationLauncher {
        private final com.alamafa.core.ApplicationContext context = new com.alamafa.core.ApplicationContext();
        @Override
        public com.alamafa.core.ApplicationContext getContext() { return context; }
        @Override
        public void launch(Lifecycle lifecycle) {
            try {
                lifecycle.init(context);
                lifecycle.start(context);
                // No automatic stop here; caller or shutdown hook should trigger when added in future.
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start application", e);
            }
        }
    }
}
