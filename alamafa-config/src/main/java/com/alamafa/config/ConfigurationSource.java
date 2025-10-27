package com.alamafa.config;

import java.util.Map;

/**
 * 提供配置数据的来源，实现需返回键值对供最终配置合并。
 */
public interface ConfigurationSource {
    /**
     * 加载配置并返回键值 Map。
     */
    Map<String, String> load();
}
