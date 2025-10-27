package com.alamafa.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 读取环境变量并转换为配置键的来源，可选支持前缀过滤。
 */
public class EnvironmentVariablesSource implements ConfigurationSource{
    private final String prefix;

    /**
     * 构造时指定前缀，null 将视为无前缀。
     */
    EnvironmentVariablesSource(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    /**
     * 遍历系统环境变量，过滤前缀并规范化键后返回。
     */
    @Override
    public Map<String, String> load() {
        Map<String, String> resolved = new HashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                continue;
            }
            if (!prefix.isEmpty()) {
                if (!key.startsWith(prefix)) {
                    continue;
                }
                key = key.substring(prefix.length());
                if (key.isEmpty()) {
                    continue;
                }
            }
            resolved.put(normalise(key), value);
        }
        return resolved;
    }

    /**
     * 将环境变量名称统一转换为小写并使用点号作为分隔。
     */
    private String normalise(String key) {
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return trimmed.toLowerCase(Locale.ROOT).replace('_', '.');
    }
}
