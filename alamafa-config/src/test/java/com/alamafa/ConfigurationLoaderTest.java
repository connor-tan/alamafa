package com.alamafa;

import com.alamafa.config.Configuration;
import com.alamafa.config.ConfigurationLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {

    @Test
    void mergesSourcesWithOverride() {
        ConfigurationLoader loader = ConfigurationLoader.create()
                .addProperties(Map.of("key1", "value1", "shared", "base"))
                .addProperties(Map.of("shared", "override", "key2", "value2"));

        Configuration configuration = loader.load();

        assertEquals("value1", configuration.get("key1").orElseThrow());
        assertEquals("override", configuration.get("shared").orElseThrow());
        assertEquals("value2", configuration.get("key2").orElseThrow());
    }

    @Test
    void supportsTypedAccessors() {
        Configuration configuration = ConfigurationLoader.create()
                .addProperties(Map.of("flag", "true", "size", "42"))
                .load();

        assertTrue(configuration.getBoolean("flag", false));
        assertEquals(42, configuration.getInt("size", 1));
    }

    @Test
    void defaultLoaderReadsClasspathResource() {
        Configuration configuration = ConfigurationLoader.withDefaults()
                .addClasspathResource("test-override.properties", false)
                .load();

        assertEquals("world", configuration.get("sample.greeting").orElseThrow());
        assertEquals("override", configuration.get("sample.override").orElseThrow());
    }

    @Test
    void priorityControlsOverrideOrder() {
        ConfigurationLoader loader = ConfigurationLoader.create()
                .addSource(() -> Map.of("value", "lowest"), ConfigurationLoader.Priority.LOWEST)
                .addSource(() -> Map.of("value", "normal"), ConfigurationLoader.Priority.NORMAL)
                .addSource(() -> Map.of("value", "highest"), ConfigurationLoader.Priority.HIGHEST);

        Configuration configuration = loader.load();

        assertEquals("highest", configuration.get("value").orElseThrow());
    }

    @Test
    void validatesRequiredKeys() {
        ConfigurationLoader loader = ConfigurationLoader.create()
                .addProperties(Map.of("present", "ok"))
                .requireKeys("present", "missing");

        IllegalStateException ex = assertThrows(IllegalStateException.class, loader::load);
        assertTrue(ex.getMessage().contains("missing"));
    }
}
