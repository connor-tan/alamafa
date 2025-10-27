package com.alamafa.di;

/**
 * 描述 Bean 的创建方式、类型以及作用域。
 */
public record BeanDefinition<T>(Class<T> type,
                                BeanSupplier<T> supplier,
                                Scope scope,
                                boolean primary,
                                boolean lazy) {
    public BeanDefinition(Class<T> type, BeanSupplier<T> supplier, Scope scope) {
        this(type, supplier, scope, false, false);
    }

    public BeanDefinition(Class<T> type, BeanSupplier<T> supplier) {
        this(type, supplier, Scope.SINGLETON, false, false);
    }

    public BeanDefinition {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (supplier == null) {
            throw new IllegalArgumentException("supplier must not be null");
        }
        if (scope == null) {
            scope = Scope.SINGLETON;
        }
        if (scope == Scope.PROTOTYPE && primary) {
            // prototype primary is allowed but nothing to check
        }
    }

    /**
     * Bean 作用域枚举。
     */
    public enum Scope {
        SINGLETON,
        PROTOTYPE
    }
}
