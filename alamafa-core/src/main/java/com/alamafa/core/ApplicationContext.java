package com.alamafa.core;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简应用上下文，作为键值注册表使用，后续可扩展为具备作用域、懒加载与事件能力的 Bean 容器。
 */
public class ApplicationContext {
    private final Map<String, Object> registry = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> typedRegistry = new ConcurrentHashMap<>();

    /**
     * 以字符串 key 形式向上下文写入对象。
     */
    public void put(String key, Object value) { registry.put(key, value); }

    @SuppressWarnings("unchecked")
    /**
     * 根据字符串 key 获取对象，调用方需自行保证类型安全。
     */
    public <T> T get(String key) { return (T) registry.get(key); }

    /**
     * 判断是否存在指定 key。
     */
    public boolean contains(String key) { return registry.containsKey(key); }

    /**
     * 返回当前所有字符串 key 的不可变快照，便于调试。
     */
    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(Map.copyOf(registry));
    }

    /**
     * 通过类型作为 key 写入对象，可避免硬编码字符串。
     */
    public <T> void put(Class<T> type, T value) {
        Objects.requireNonNull(type, "type");
        typedRegistry.put(type, value);
    }

    /**
     * 通过类型查找对象；不存在时返回 null。
     */
    public <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object value = typedRegistry.get(type);
        return value == null ? null : type.cast(value);
    }

    /**
     * 判断是否使用类型 key 注册过对象。
     */
    public boolean contains(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return typedRegistry.containsKey(type);
    }

    /**
     * 返回类型 key 注册表的不可变快照。
     */
    public Map<Class<?>, Object> typedSnapshot() {
        return Collections.unmodifiableMap(Map.copyOf(typedRegistry));
    }
}
