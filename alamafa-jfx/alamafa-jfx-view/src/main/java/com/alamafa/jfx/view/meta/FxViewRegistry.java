package com.alamafa.jfx.view.meta;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory registry storing {@link FxViewDescriptor} instances keyed by bean type or name.
 */
public final class FxViewRegistry {
    private final Map<Class<?>, FxViewDescriptor> byType = new ConcurrentHashMap<>();
    private final Map<String, FxViewDescriptor> byName = new ConcurrentHashMap<>();

    public void register(FxViewDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        byType.putIfAbsent(descriptor.type(), descriptor);
        if (descriptor.name() != null && !descriptor.name().isBlank()) {
            byName.putIfAbsent(descriptor.name(), descriptor);
        }
    }

    public Optional<FxViewDescriptor> find(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable(byType.get(type));
    }

    public Optional<FxViewDescriptor> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byName.get(name.trim()));
    }

    public Optional<FxViewDescriptor> primaryDescriptor() {
        return byType.values().stream()
                .filter(FxViewDescriptor::primary)
                .findFirst();
    }
}
