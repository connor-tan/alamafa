package com.alamafa.core;

/**
 * 表示针对特定运行时或 UI 环境的启动器，负责在线程模型（如 Swing EDT）下正确触发生命周期：
 * init → start →（JVM 退出）→ stop。
 */
public interface ApplicationLauncher {
    /**
     * 启动传入的生命周期，典型实现需要：
     * 1. 同步执行 lifecycle.init(context)。
     * 2. 根据 UI 环境选择线程执行 lifecycle.start(context)。
     * 3. 注册关闭钩子以调用 lifecycle.stop(context)。
     */
    void launch(Lifecycle lifecycle);
}
