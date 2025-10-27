package com.alamafa.core.logging;

import org.slf4j.Logger;

import java.util.Objects;

/**
 * 将 SLF4J Logger 适配为 {@link AlamafaLogger} 的内部实现。
 */
record Slf4jLoggerAdapter(Logger delegate) implements AlamafaLogger {
    /**
     * 使用指定 SLF4J 实例进行适配。
     */
    Slf4jLoggerAdapter(Logger delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void trace(String message, Object... args) {
        delegate.trace(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        delegate.debug(message, args);
    }

    @Override
    public void info(String message, Object... args) {
        delegate.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        delegate.warn(message, args);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        delegate.warn(message, throwable);
    }

    @Override
    public void error(String message, Object... args) {
        delegate.error(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        delegate.error(message, throwable);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }
}
