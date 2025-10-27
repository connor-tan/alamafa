package com.alamafa.config;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 从 classpath 中读取 .properties 文件的配置源，可配置资源路径及是否强制存在。
 */
public class ClasspathPropertiesSource implements ConfigurationSource {

    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ClasspathPropertiesSource.class);

    private final String resource;
    private final boolean required;
    private final ClassLoader classLoader;

    /**
     * 使用当前线程类加载器读取指定资源。
     */
    ClasspathPropertiesSource(String resource, boolean required) {
        this(resource, required, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 允许传入自定义类加载器，便于插件等场景。
     */
    ClasspathPropertiesSource(String resource, boolean required, ClassLoader classLoader) {
        this.resource = Objects.requireNonNull(resource, "resource");
        this.required = required;
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * 从 classpath 读取 properties，必要时抛出异常或返回空 Map。
     */
    @Override
    public java.util.Map<String, String> load() {
        InputStream stream = classLoader.getResourceAsStream(resource);
        if (stream == null) {
            if (required) {
                throw new IllegalStateException("Required configuration resource not found: " + resource);
            }
            LOGGER.debug("Configuration resource {} not found, skipping", resource);
            return Map.of();
        }
        try (InputStream input = stream) {
            Properties properties = new Properties();
            properties.load(input);
            Map<String, String> values = new HashMap<>();
            for (String name : properties.stringPropertyNames()) {
                values.put(name, properties.getProperty(name));
            }
            return values;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration resource: " + resource, e);
        }
    }
}
