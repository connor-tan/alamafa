package com.alamafa.core.health;

import com.alamafa.core.ApplicationContext;

/**
 * 代表一个简单的健康检查函数，返回组件当前状态。
 */
@FunctionalInterface
public interface HealthCheck {
    /**
     * 执行健康检查并返回结果，允许抛出异常以指示失败。
     */
    HealthStatus check(ApplicationContext context) throws Exception;
}
