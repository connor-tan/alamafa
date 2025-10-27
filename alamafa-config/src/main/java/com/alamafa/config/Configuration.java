package com.alamafa.config;

import java.util.*;

/**
 * 不可变的配置视图，提供常用类型的读取方法。
 */
public final class Configuration {
    private final Map<String, String> values;

    /**
     * 通过 Map 构造配置副本，外部修改不会影响内部状态。
     */
    Configuration(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    /**
     * 根据 key 返回可选值。
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    /**
     * 根据 key 返回字符串，若不存在则使用默认值。
     */
    public String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    /**
     * 读取整数配置，若缺失返回默认值，若格式非法则抛异常。
     */
    public int getInt(String key, int defaultValue) {
        String raw = values.get(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Configuration key " + key + " is not an int: " + raw, ex);
        }
    }

    /**
     * 读取布尔配置，兼容 `true/false` 字符串。
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String raw = values.get(key);
        return raw == null ? defaultValue : Boolean.parseBoolean(raw.trim());
    }

    /**
     * 返回内部配置的不可变视图。
     */
    public Map<String, String> snapshot() {
        return values;
    }

    /**
     * 生成一个新的配置实例，优先采用另一个配置中的值。
     */
    Configuration merged(Configuration other) {
        Objects.requireNonNull(other, "other");
        Map<String, String> merged = new HashMap<>(values);
        merged.putAll(other.values);
        return new Configuration(merged);
    }
}
