package com.alamafa.starter;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.DiRuntimeBootstrap;

/**
 * Convenience factory utilities for quickly bootstrapping Alamafa framework components.
 */
public final class Alamafa {
    private Alamafa() {}

    /**
     * Create a new ApplicationContext.
     */
    public static ApplicationContext context() {
        return new ApplicationContext();
    }

    /**
     * Create a BeanRegistry bound to a new ApplicationContext.
     */
    public static BeanRegistry registry() {
        return new BeanRegistry(context());
    }

    /**
     * Create a BeanRegistry bound to an existing context.
     */
    public static BeanRegistry registry(ApplicationContext ctx) {
        return new BeanRegistry(ctx);
    }

    /**
     * 创建一个 DI 运行时引导器，可直接作为 {@link com.alamafa.core.ApplicationBootstrap} 的生命周期参与者。
     */
    public static DiRuntimeBootstrap diBootstrap(Class<?>... configurations) {
        return DiRuntimeBootstrap.builder()
                .withConfigurations(configurations)
                .build();
    }

}
