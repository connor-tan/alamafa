package com.alamafa.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationBinderTest {

    @Test
    void bindsConfigurationPropertiesWithPrefix() {
        Configuration config = ConfigurationLoader.create()
                .addProperties(Map.of(
                        "app.name", "demo",
                        "app.port", "8080",
                        "app.enabled", "true"
                ))
                .load();

        AppProperties properties = ConfigurationBinder.bind(config, AppProperties.class);

        assertEquals("demo", properties.getName());
        assertEquals(8080, properties.getPort());
        assertTrue(properties.isEnabled());
    }

    @Test
    void bindsEnumProperties() {
        Configuration config = ConfigurationLoader.create()
                .addProperties(Map.of("enum.mode", "BETA"))
                .load();

        EnumProperties properties = ConfigurationBinder.bind(config, EnumProperties.class);

        assertEquals(Mode.BETA, properties.getMode());
    }

    @Test
    void throwsOnUnsupportedType() {
        Configuration config = ConfigurationLoader.create()
                .addProperties(Map.of("unsupported.duration", "PT10S"))
                .load();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ConfigurationBinder.bind(config, UnsupportedProperties.class));
        assertTrue(ex.getMessage().contains("duration"));
    }

    @ConfigurationProperties(prefix = "app")
    static class AppProperties {
        private String name;
        private int port;
        private boolean enabled;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    @ConfigurationProperties(prefix = "enum")
    static class EnumProperties {
        private Mode mode;

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }
    }

    enum Mode {
        ALPHA, BETA
    }

    @ConfigurationProperties(prefix = "unsupported")
    static class UnsupportedProperties {
        private java.time.Duration duration;

        public java.time.Duration getDuration() {
            return duration;
        }

        public void setDuration(java.time.Duration duration) {
            this.duration = duration;
        }
    }
}
