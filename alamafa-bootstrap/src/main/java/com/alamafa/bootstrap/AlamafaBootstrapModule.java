package com.alamafa.bootstrap;

/**
 * Allows external modules to customize the bootstrap process by contributing additional
 * configuration classes, lifecycle participants or context initializers.
 */
@FunctionalInterface
public interface AlamafaBootstrapModule {

    /**
     * Invoked during application bootstrap to register module contributions.
     */
    void configure(AlamafaBootstrapContext context);
}

