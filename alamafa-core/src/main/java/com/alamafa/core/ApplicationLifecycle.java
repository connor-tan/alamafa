package com.alamafa.core;

/**
 * Marker interface for 应用级生命周期 Bean，由 {@link com.alamafa.di.DiRuntimeBootstrap}
 * 在应用启动/关闭时统一调度。
 */
public interface ApplicationLifecycle extends Lifecycle {

    /**
     * Ordering hint for coordinating multiple application lifecycle beans. Lower values execute first.
     */
    default int getOrder() {
        return 0;
    }
}
