package com.alamafa.bootstrap;

import com.alamafa.core.ApplicationBootstrap;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.Lifecycle;
import com.alamafa.di.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shared context passed to {@link AlamafaBootstrapModule} implementations so they can
 * contribute to the bootstrap sequence without having to know the internals.
 */
public final class AlamafaBootstrapContext {
    private final ApplicationBootstrap bootstrap;
    private final ApplicationContext context;
    private final Class<?> primarySource;
    private final LinkedHashSet<Class<?>> configurationClasses;
    private final LinkedHashSet<String> scanPackages;

    AlamafaBootstrapContext(ApplicationBootstrap bootstrap,
                            ApplicationContext context,
                            Class<?> primarySource,
                            LinkedHashSet<Class<?>> configurationClasses,
                            LinkedHashSet<String> scanPackages) {
        this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
        this.context = Objects.requireNonNull(context, "context");
        this.primarySource = Objects.requireNonNull(primarySource, "primarySource");
        this.configurationClasses = Objects.requireNonNull(configurationClasses, "configurationClasses");
        this.scanPackages = Objects.requireNonNull(scanPackages, "scanPackages");
    }

    public ApplicationBootstrap bootstrap() {
        return bootstrap;
    }

    public ApplicationContext context() {
        return context;
    }

    public Class<?> primarySource() {
        return primarySource;
    }

    public void addConfiguration(Class<?> configurationClass) {
        if (configurationClass == null) {
            return;
        }
        if (!configurationClass.isAnnotationPresent(Configuration.class)) {
            throw new IllegalArgumentException("Configuration class "
                    + configurationClass.getName()
                    + " must be annotated with @Configuration");
        }
        configurationClasses.add(configurationClass);
    }

    public void addScanPackage(String basePackage) {
        if (basePackage == null) {
            return;
        }
        String trimmed = basePackage.trim();
        if (!trimmed.isEmpty()) {
            scanPackages.add(trimmed);
        }
    }

    public void addLifecycleParticipant(Lifecycle lifecycle) {
        if (lifecycle == null) {
            return;
        }
        bootstrap.addLifecycleParticipant(lifecycle);
    }

    public void addContextInitializer(Consumer<ApplicationContext> initializer) {
        if (initializer == null) {
            return;
        }
        bootstrap.addContextInitializer(initializer);
    }

    Set<Class<?>> configurationClasses() {
        return Set.copyOf(configurationClasses);
    }

    Set<String> scanPackages() {
        return Set.copyOf(scanPackages);
    }
}
