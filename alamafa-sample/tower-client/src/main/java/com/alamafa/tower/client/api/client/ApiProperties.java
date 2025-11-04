package com.alamafa.tower.client.api.client;

import com.alamafa.config.ConfigurationProperties;
import com.alamafa.di.annotation.Component;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * 配置API访问的基础参数，例如Base URL与超时时间。
 */
@Component
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String baseUrl = "http://localhost:8080";
    private int connectTimeoutSeconds = 5;
    private int requestTimeoutSeconds = 10;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        if (connectTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("connectTimeoutSeconds must be greater than 0");
        }
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public Duration getConnectTimeout() {
        return Duration.ofSeconds(connectTimeoutSeconds);
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        if (requestTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("requestTimeoutSeconds must be greater than 0");
        }
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public Duration getRequestTimeout() {
        return Duration.ofSeconds(requestTimeoutSeconds);
    }

    public URI resolve(String path) {
        String base = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        String normalizedPath = path == null ? "" : path.trim();
        if (normalizedPath.isEmpty()) {
            return URI.create(base);
        }
        boolean baseEndsWithSlash = base.endsWith("/");
        boolean pathStartsWithSlash = normalizedPath.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash) {
            return URI.create(base + normalizedPath.substring(1));
        }
        if (!baseEndsWithSlash && !pathStartsWithSlash) {
            return URI.create(base + "/" + normalizedPath);
        }
        return URI.create(base + normalizedPath);
    }
}
