package com.alamafa.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 映射 JVM System Properties 的配置源。
 */
final class SystemPropertiesSource implements ConfigurationSource {
    /**
     * 将系统属性复制到 Map 中。
     */
    @Override
    public Map<String, String> load() {
        Map<String, String> values = new HashMap<>();
        Properties properties = System.getProperties();
        for (String name : properties.stringPropertyNames()) {
            values.put(name, properties.getProperty(name));
        }
        return values;
    }
}
