package com.alamafa.core.logging;

import java.util.Objects;
import java.util.function.Function;

/**
 * 获取日志实例的入口，默认使用 SLF4J，亦可在测试或其他运行时覆盖提供者。
 */
public final class LoggerFactory {
    private static final Function<String, AlamafaLogger> DEFAULT_PROVIDER =
            name -> new Slf4jLoggerAdapter(org.slf4j.LoggerFactory.getLogger(name));

    private static volatile Function<String, AlamafaLogger> provider = DEFAULT_PROVIDER;

    private LoggerFactory() {
    }

    /**
     * 根据类型名获取日志实例。
     */
    public static AlamafaLogger getLogger(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return getLogger(type.getName());
    }

    /**
     * 根据自定义名称获取日志实例。
     */
    public static AlamafaLogger getLogger(String name) {
        Objects.requireNonNull(name, "name");
        return provider.apply(name);
    }

    /**
     * 重写日志提供者，常用于测试或定制运行时。
     */
    public static void setProvider(Function<String, AlamafaLogger> customProvider) {
        provider = Objects.requireNonNull(customProvider, "customProvider");
    }

    /**
     * Restores the default provider backed by SLF4J/Logback.
     */
    public static void resetProvider() {
        provider = DEFAULT_PROVIDER;
    }
}
