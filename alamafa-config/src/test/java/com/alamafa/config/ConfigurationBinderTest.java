package com.alamafa.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationBinderTest {

    @Test
    void shouldBindCamelCaseKeys() {
        Configuration configuration = new Configuration(new HashMap<>(Map.of(
                "api.baseUrl", "http://example.com",
                "api.connectTimeoutSeconds", "15",
                "api.requestTimeoutSeconds", "45"
        )));

        ApiSampleProperties properties = ConfigurationBinder.bind(configuration, ApiSampleProperties.class);

        assertEquals("http://example.com", properties.getBaseUrl());
        assertEquals(15, properties.getConnectTimeoutSeconds());
        assertEquals(45, properties.getRequestTimeoutSeconds());
    }

    @Test
    void shouldBindKebabCaseKeys() {
        Configuration configuration = new Configuration(new HashMap<>(Map.of(
                "api.base-url", "https://sample",
                "api.connect-timeout-seconds", "20",
                "api.request-timeout-seconds", "60"
        )));

        ApiSampleProperties properties = ConfigurationBinder.bind(configuration, ApiSampleProperties.class);

        assertEquals("https://sample", properties.getBaseUrl());
        assertEquals(20, properties.getConnectTimeoutSeconds());
        assertEquals(60, properties.getRequestTimeoutSeconds());
    }

    @ConfigurationProperties(prefix = "api")
    static class ApiSampleProperties {
        private String baseUrl = "http://localhost";
        private int connectTimeoutSeconds = 5;
        private int requestTimeoutSeconds = 10;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }

        public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        public int getRequestTimeoutSeconds() {
            return requestTimeoutSeconds;
        }

        public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
            this.requestTimeoutSeconds = requestTimeoutSeconds;
        }
    }
}
