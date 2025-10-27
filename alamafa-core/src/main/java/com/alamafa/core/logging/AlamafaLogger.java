package com.alamafa.core.logging;

/**
 * Elamafa 模块内统一的轻量日志门面，默认由 SLF4J 实现。
 */
public interface AlamafaLogger {
    /** 输出 trace 级别日志（支持占位符）。 */
    void trace(String message, Object... args);

    /** 输出 debug 级别日志。 */
    void debug(String message, Object... args);

    /** 输出 info 级别日志。 */
    void info(String message, Object... args);

    /** 输出 warn 级别日志。 */
    void warn(String message, Object... args);

    /** 输出 warn 级别日志并附带异常。 */
    void warn(String message, Throwable throwable);

    /** 输出 error 级别日志。 */
    void error(String message, Object... args);

    /** 输出 error 级别日志并附带异常。 */
    void error(String message, Throwable throwable);

    /** 判断是否开启 trace。 */
    boolean isTraceEnabled();

    /** 判断是否开启 debug。 */
    boolean isDebugEnabled();

    /** 判断是否开启 info。 */
    boolean isInfoEnabled();

    /** 判断是否开启 warn。 */
    boolean isWarnEnabled();

    /** 判断是否开启 error。 */
    boolean isErrorEnabled();
}
