package com.alamafa.core;

/**
 * 应用生命周期协议，按 init → start → stop 顺序执行，可由具体实现覆盖对应方法。
 */
public interface Lifecycle {
    /**
     * No-op lifecycle instance for scenarios where application logic is provided via participants.
     */
    Lifecycle NO_OP = new Lifecycle() { };

    /**
     * 初始化阶段，适合完成上下文装配与资源准备。
     */
    default void init(ApplicationContext ctx) throws Exception { }

    /**
     * 启动阶段，通常负责展示 UI 或启动后台任务。
     */
    default void start(ApplicationContext ctx) throws Exception { }

    /**
     * 停止阶段，用于释放资源并终止任务。
     */
    default void stop(ApplicationContext ctx) throws Exception { }
}
