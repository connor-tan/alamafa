package com.alamafa.config;


import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.*;

/**
 * 聚合多个配置源并按优先级合并，最终生成不可变的 {@link Configuration}。
 */
public final class ConfigurationLoader {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private final List<Entry> sources = new ArrayList<>();
    private final List<String> requiredKeys = new ArrayList<>();

    /**
     * 创建空的配置加载器，需手动添加数据源。
     */
    public static ConfigurationLoader create() {
        return new ConfigurationLoader();
    }

    /**
     * 提供默认策略：application.properties → profile 文件 → 环境变量 → 系统属性。
     */
    public static ConfigurationLoader withDefaults() {
        ConfigurationLoader loader = new ConfigurationLoader();
        loader.addClasspathResource("application.properties", Priority.LOW, false);
        String profile = resolveProfile();
        if (profile != null) {
            loader.addClasspathResource("application-" + profile + ".properties", Priority.NORMAL, false);
        }
        loader.includeEnvironmentVariables("ALAMAFA_", Priority.HIGH);
        loader.includeSystemProperties(Priority.HIGHEST);
        return loader;
    }

    private static String resolveProfile() {
        String profile = System.getProperty("alamafa.profile");
        if (profile == null || profile.isBlank()) {
            profile = System.getenv("ALAMAFA_PROFILE");
        }
        return (profile == null || profile.isBlank()) ? null : profile;
    }

    /**
     * 添加一个默认优先级的配置源。
     */
    public ConfigurationLoader addSource(ConfigurationSource source) {
        return addSource(source, Priority.NORMAL);
    }

    /**
     * 按指定优先级添加配置源。
     */
    public ConfigurationLoader addSource(ConfigurationSource source, Priority priority) {
        sources.add(new Entry(Objects.requireNonNull(source, "source"),
                Objects.requireNonNull(priority, "priority"),
                false));
        return this;
    }

    /**
     * 以默认优先级加载 classpath 上的 properties 资源。
     */
    public ConfigurationLoader addClasspathResource(String resource, boolean required) {
        return addClasspathResource(resource, Priority.NORMAL, required);
    }

    /**
     * 指定优先级加载 classpath 资源，可配置是否必需存在。
     */
    public ConfigurationLoader addClasspathResource(String resource, Priority priority, boolean required) {
        sources.add(new Entry(new ClasspathPropertiesSource(resource, required),
                Objects.requireNonNull(priority, "priority"),
                required));
        return this;
    }

    /**
     * 直接合并 Map 中的键值，默认使用较高优先级覆盖。
     */
    public ConfigurationLoader addProperties(Map<String, String> values) {
        sources.add(new Entry(new MapConfigurationSource(values), Priority.HIGH, false));
        return this;
    }

    /**
     * 加载环境变量，支持前缀过滤和自定义优先级。
     */
    public ConfigurationLoader includeEnvironmentVariables(String prefix, Priority priority) {
        sources.add(new Entry(new EnvironmentVariablesSource(prefix),
                Objects.requireNonNull(priority, "priority"),
                false));
        return this;
    }

    /**
     * 将系统属性作为配置来源。
     */
    public ConfigurationLoader includeSystemProperties(Priority priority) {
        sources.add(new Entry(new SystemPropertiesSource(),
                Objects.requireNonNull(priority, "priority"),
                false));
        return this;
    }

    /**
     * 声明必须存在且非空的配置 key，加载后统一校验。
     */
    public ConfigurationLoader requireKeys(String... keys) {
        if (keys == null) {
            return this;
        }
        for (String key : keys) {
            if (key != null && !key.isBlank()) {
                requiredKeys.add(key.trim());
            }
        }
        return this;
    }

    /**
     * 按优先级排序并依次加载所有配置，返回聚合后的结果。
     */
    public Configuration load() {
        Map<String, String> merged = new LinkedHashMap<>();
        sources.sort(Comparator.comparingInt(entry -> entry.priority().order));
        for (Entry sourceEntry : sources) {
            Map<String, String> contribution = sourceEntry.source().load();
            if (!contribution.isEmpty()) {
                LOGGER.debug("Loaded {} configuration entries from {} [{}]",
                        contribution.size(),
                        sourceEntry.source().getClass().getSimpleName(),
                        sourceEntry.priority());
                merged.putAll(contribution);
            }
        }
        Configuration configuration = new Configuration(merged);
        validate(configuration);
        return configuration;
    }

    /**
     * 校验必需的 key 是否存在。
     */
    private void validate(Configuration configuration) {
        for (String key : requiredKeys) {
            boolean present = configuration.get(key)
                    .filter(value -> !value.isBlank())
                    .isPresent();
            if (!present) {
                throw new IllegalStateException("Required configuration key missing or blank: " + key);
            }
        }
    }

    /**
     * 配置源优先级，值越大越靠后覆盖。
     */
    public enum Priority {
        LOWEST(0),
        LOW(10),
        NORMAL(50),
        HIGH(90),
        HIGHEST(100);

        private final int order;

        Priority(int order) {
            this.order = order;
        }
    }

    /**
     * 记录配置源及其元数据，方便排序与调试。
     */
    private record Entry(ConfigurationSource source, Priority priority, boolean required) { }
}
