package com.alamafa.di;

/**
 * Bean 解析或创建失败时抛出的运行时异常。
 */
public final class BeanResolutionException extends RuntimeException {
    /**
     * 使用错误描述构造异常。
     */
    public BeanResolutionException(String message) {
        super(message);
    }

    /**
     * 同时包含原始异常的构造函数。
     */
    public BeanResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
