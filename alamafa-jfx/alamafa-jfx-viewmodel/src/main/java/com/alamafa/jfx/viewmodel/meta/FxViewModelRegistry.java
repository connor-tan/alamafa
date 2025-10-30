package com.alamafa.jfx.viewmodel.meta;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of view-model descriptors plus scope-aware retrieval helpers.
 */
public final class FxViewModelRegistry {
    private final Map<Class<?>, FxViewModelDescriptor> byType = new ConcurrentHashMap<>();
    private final Map<String, FxViewModelDescriptor> byName = new ConcurrentHashMap<>();
    private final Map<Class<?>, FxViewModel> applicationCache = new ConcurrentHashMap<>();

    public void register(FxViewModelDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        byType.putIfAbsent(descriptor.type(), descriptor);
        if (descriptor.name() != null && !descriptor.name().isBlank()) {
            byName.putIfAbsent(descriptor.name(), descriptor);
        }
    }

    public Optional<FxViewModelDescriptor> find(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable(byType.get(type));
    }

    public Optional<FxViewModelDescriptor> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byName.get(name.trim()));
    }

    public Optional<FxViewModelScope> scopeOf(Class<?> type) {
        return find(type).map(FxViewModelDescriptor::scope);
    }

    public <T extends FxViewModel> T obtain(ApplicationContext context, BeanRegistry beanRegistry, Class<T> type) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(type, "type");
        FxViewModelDescriptor descriptor = find(type).orElse(null);
        FxViewModelScope scope = descriptor != null ? descriptor.scope() : FxViewModelScope.APPLICATION;
        if (scope == FxViewModelScope.APPLICATION) {
            return type.cast(applicationCache.computeIfAbsent(type, key -> instantiate(context, beanRegistry, type)));
        }
        return instantiate(context, beanRegistry, type);
    }

    private <T extends FxViewModel> T instantiate(ApplicationContext context, BeanRegistry beanRegistry, Class<T> type) {
        if (beanRegistry != null && beanRegistry.hasBeanDefinition(type)) {
            return beanRegistry.get(type);
        }
        T fromContext = context.get(type);
        if (fromContext != null) {
            return fromContext;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to instantiate view model " + type.getName(), ex);
        }
    }
}
