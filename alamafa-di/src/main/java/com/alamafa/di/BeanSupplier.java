package com.alamafa.di;

/**
 * Bean 创建函数式接口，允许抛出受检异常。
 */
@FunctionalInterface
public interface BeanSupplier<T> {
    /**
     * 生成 Bean 实例。
     */
    T get() throws Exception;
}
