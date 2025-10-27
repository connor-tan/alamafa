package com.alamafa.core;

import java.util.Objects;

/**
 * 表示某个生命周期阶段执行失败，用于包装原始异常并携带阶段与目标信息。
 */
public final class LifecycleExecutionException extends Exception {
    private final LifecyclePhase phase;
    private final String target;

    /**
     * 创建异常实例，记录失败的阶段、目标描述以及原始原因。
     */
    public LifecycleExecutionException(LifecyclePhase phase, String target, Throwable cause) {
        super(buildMessage(phase, target, cause), Objects.requireNonNull(cause, "cause"));
        this.phase = Objects.requireNonNull(phase, "phase");
        this.target = target == null ? "unknown" : target;
    }

    /**
     * 返回出错的生命周期阶段。
     */
    public LifecyclePhase phase() {
        return phase;
    }

    /**
     * 返回发生异常的目标描述。
     */
    public String target() {
        return target;
    }

    /**
     * 构造标准化异常消息，包含阶段、目标与原因。
     */
    private static String buildMessage(LifecyclePhase phase, String target, Throwable cause) {
        String subject = target == null ? "unknown target" : target;
        String reason = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        return "Lifecycle phase " + phase + " failed while executing " + subject + ": " + reason;
    }
}
