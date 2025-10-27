package com.alamafa.core.health;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 中央健康检查注册表，统一管理健康检查并生成聚合快照。
 */
public final class HealthRegistry {
    public static final String CONTEXT_KEY = "alamafa.health.registry";

    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(HealthRegistry.class);

    private final ApplicationContext context;
    private final ConcurrentMap<String, HealthCheck> checks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, HealthIndicator> lastIndicators = new ConcurrentHashMap<>();

    /**
     * 绑定指定应用上下文，健康检查执行时可以读取上下文数据。
     */
    public HealthRegistry(ApplicationContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * 注册新的健康检查条目。
     */
    public void register(String name, HealthCheck check) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(check, "check");
        checks.put(name, check);
        LOGGER.debug("Registered health check {}", name);
    }

    /**
     * 移除已注册的健康检查。
     */
    public void unregister(String name) {
        checks.remove(name);
        lastIndicators.remove(name);
        LOGGER.debug("Unregistered health check {}", name);
    }

    /**
     * 执行所有健康检查并返回不可变快照。
     */
    public Map<String, HealthIndicator> snapshot() {
        Map<String, HealthIndicator> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, HealthCheck> entry : checks.entrySet()) {
            String name = entry.getKey();
            HealthCheck check = entry.getValue();
            snapshot.put(name, evaluate(name, check));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * 返回最近一次执行的健康指示器，用于缓存命中。
     */
    public HealthIndicator lastIndicator(String name) {
        return lastIndicators.get(name);
    }

    /**
     * 调用具体检查，并在失败时记录异常信息。
     */
    private HealthIndicator evaluate(String name, HealthCheck check) {
        try {
            HealthStatus status = check.check(context);
            if (status == null) {
                status = HealthStatus.UNKNOWN;
            }
            HealthIndicator indicator = new HealthIndicator(name, status, null);
            lastIndicators.put(name, indicator);
            return indicator;
        } catch (Exception ex) {
            HealthIndicator indicator = new HealthIndicator(name, HealthStatus.DOWN, ex.getMessage());
            lastIndicators.put(name, indicator);
            LOGGER.warn("Health check {} reported DOWN: {}", name, ex.getMessage());
            return indicator;
        }
    }
}
