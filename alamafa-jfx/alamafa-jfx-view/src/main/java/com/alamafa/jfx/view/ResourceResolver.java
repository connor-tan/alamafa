package com.alamafa.jfx.view;

import java.net.URL;
import java.util.Objects;

/**
 * Strategy interface for resolving view assets such as FXML or stylesheets.
 */
@FunctionalInterface
public interface ResourceResolver {
    URL resolve(String path);

    static ResourceResolver classpath() {
        return classLoaderResource(Thread.currentThread().getContextClassLoader());
    }

    static ResourceResolver classLoaderResource(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");
        return path -> {
            if (path == null || path.isBlank()) {
                return null;
            }
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            return classLoader.getResource(normalized);
        };
    }
}
