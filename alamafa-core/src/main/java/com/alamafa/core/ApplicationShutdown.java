package com.alamafa.core;

/**
 * 允许运行时实现提供统一的关闭钩子，业务代码可调用 {@link #requestShutdown()} 来触发退出流程。
 */
@FunctionalInterface
public interface ApplicationShutdown {
    /**
     * 请求应用执行优雅关闭。
     */
    void requestShutdown();
}
