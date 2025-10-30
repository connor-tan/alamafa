package com.alamafa.bootstrap.autoconfigure;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 基于类路径的自动配置加载器，参考 Spring Boot 的 spring.factories 机制。
 */
public final class AutoConfigurationLoader {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(AutoConfigurationLoader.class);
    private static final String FACTORIES_RESOURCE = "META-INF/alamafa.factories";

    private AutoConfigurationLoader() {
    }

    public static List<String> loadAutoConfigurations(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");
        Set<String> result = new LinkedHashSet<>();
        try {
            Enumeration<java.net.URL> resources = classLoader.getResources(FACTORIES_RESOURCE);
            while (resources.hasMoreElements()) {
                java.net.URL url = resources.nextElement();
                loadResource(url, result);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load " + FACTORIES_RESOURCE, ex);
        }
        return List.copyOf(result);
    }

    private static void loadResource(java.net.URL url, Set<String> result) {
        try (InputStream stream = url.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder currentLine = new StringBuilder();
            String raw;
            while ((raw = reader.readLine()) != null) {
                String processed = stripComment(raw).trim();
                if (processed.isEmpty()) {
                    continue;
                }
                currentLine.append(processed);
                if (processed.endsWith("\\")) {
                    currentLine.setLength(currentLine.length() - 1); // remove trailing escape
                    continue;
                }
                handleEntry(currentLine.toString(), result);
                currentLine.setLength(0);
            }
            if (currentLine.length() > 0) {
                handleEntry(currentLine.toString(), result);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to read auto-configuration resource {}", url, ex);
        }
    }

    private static String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx >= 0 ? line.substring(0, idx) : line;
    }

    private static void handleEntry(String line, Set<String> result) {
        int idx = line.indexOf('=');
        if (idx < 0) {
            LOGGER.warn("Ignoring malformed auto-configuration entry '{}'", line);
            return;
        }
        String key = line.substring(0, idx).trim();
        if (!key.isEmpty() && !"com.alamafa.bootstrap.AutoConfiguration".equals(key)) {
            return;
        }
        String value = line.substring(idx + 1).trim();
        if (value.isEmpty()) {
            return;
        }
        for (String entry : value.split(",")) {
            String candidate = entry.trim();
            if (!candidate.isEmpty()) {
                result.add(candidate);
            }
        }
    }
}
