package com.alamafa.bootstrap;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable description of an Alamafa boot application derived from {@link AlamafaBootApplication}.
 */
public final class BootApplicationDescriptor {
    private final Class<?> primarySource;
    private final Set<String> basePackages;
    private final Set<Class<?>> moduleClasses;

    BootApplicationDescriptor(Class<?> primarySource,
                              Set<String> basePackages,
                              Set<Class<?>> moduleClasses) {
        this.primarySource = Objects.requireNonNull(primarySource, "primarySource");
        this.basePackages = Collections.unmodifiableSet(new LinkedHashSet<>(basePackages));
        this.moduleClasses = Collections.unmodifiableSet(new LinkedHashSet<>(moduleClasses));
    }

    public Class<?> primarySource() { return primarySource; }
    public Set<String> basePackages() { return basePackages; }
    public Set<Class<?>> moduleClasses() { return moduleClasses; }
}

