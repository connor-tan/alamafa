package com.alamafa.core;

/**
 * 生命周期阶段出现异常时的回调接口，可用于日志或恢复。
 */
@FunctionalInterface
public interface LifecycleErrorHandler {
    /**
     * 处理指定阶段抛出的异常。
     */
    void onError(LifecyclePhase phase, Exception exception);
}
