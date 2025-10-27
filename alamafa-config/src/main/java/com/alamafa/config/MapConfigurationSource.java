package com.alamafa.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 直接基于现有 Map 的配置源，通常用于测试或编程式注入。
 */
final class MapConfigurationSource implements ConfigurationSource {
    private final Map<String, String> values;

    /**
     * 使用给定 Map 构造不可变配置源。
     */
    MapConfigurationSource(Map<String, String> values) {
        this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
    }

    @Override
    /**
     * 直接返回预先保存的键值对。
     */
    public Map<String, String> load() {
        return values;
    }
}
