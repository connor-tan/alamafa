package com.alamafa.bootstrap;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable description of an Alamafa boot application derived from {@link AlamafaBootApplication}.
 */
public record BootApplicationDescriptor(Class<?> primarySource, Set<String> basePackages, Set<Class<?>> moduleClasses) {
    public BootApplicationDescriptor(Class<?> primarySource,
                                     Set<String> basePackages,
                                     Set<Class<?>> moduleClasses) {
        this.primarySource = Objects.requireNonNull(primarySource, "primarySource");
        this.basePackages = Collections.unmodifiableSet(new LinkedHashSet<>(basePackages));
        this.moduleClasses = Collections.unmodifiableSet(new LinkedHashSet<>(moduleClasses));
    }
}

