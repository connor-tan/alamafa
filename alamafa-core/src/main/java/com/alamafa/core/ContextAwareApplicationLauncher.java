package com.alamafa.core;

/**
 * {@link ApplicationLauncher} 的增强版本，能够暴露底层 {@link ApplicationContext}，
 * 方便引导阶段先对上下文进行填充再执行生命周期。
 */
public interface ContextAwareApplicationLauncher extends ApplicationLauncher {
    /**
     * 返回当前启动器使用的应用上下文。
     */
    ApplicationContext getContext();
}
