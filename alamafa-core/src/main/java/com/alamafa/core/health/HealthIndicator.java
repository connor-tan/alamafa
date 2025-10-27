package com.alamafa.core.health;

import java.time.Instant;
import java.util.Objects;

/**
 * 描述某个组件在特定时间点的健康状态快照。
 */
public final class HealthIndicator {
    private final String name;
    private final com.alamafa.core.health.HealthStatus status;
    private final Instant timestamp;
    private final String details;

    /**
     * 创建健康指示器，记录名称、状态以及可选说明。
     */
    public HealthIndicator(String name, HealthStatus status, String details) {
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.timestamp = Instant.now();
        this.details = details;
    }

    /**
     * 返回指标名称。
     */
    public String name() {
        return name;
    }

    /**
     * 返回健康状态。
     */
    public HealthStatus status() {
        return status;
    }

    /**
     * 返回生成快照的时间戳。
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * 返回额外的状态描述，可为空。
     */
    public String details() {
        return details;
    }
}
