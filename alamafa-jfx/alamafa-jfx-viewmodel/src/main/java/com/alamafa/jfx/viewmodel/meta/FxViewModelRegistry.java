package com.alamafa.jfx.viewmodel.meta;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.BeanRegistry;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of view-model descriptors plus scope-aware retrieval helpers.
 */
public final class FxViewModelRegistry {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(FxViewModelRegistry.class);

    private final Map<Class<?>, FxViewModelDescriptor> byType = new ConcurrentHashMap<>();
    private final Map<String, FxViewModelDescriptor> byName = new ConcurrentHashMap<>();
    private final Map<Class<?>, FxViewModel> applicationCache = new ConcurrentHashMap<>();
    private final Set<String> scannedPackages = ConcurrentHashMap.newKeySet();

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
        if (beanRegistry != null) {
            ensureRegistered(beanRegistry, type);
            if (beanRegistry.hasBeanDefinition(type)) {
                return beanRegistry.get(type);
            }
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

    private void ensureRegistered(BeanRegistry registry, Class<?> type) {
        if (registry.hasBeanDefinition(type)) {
            return;
        }
        FxViewModelSpec spec = type.getAnnotation(FxViewModelSpec.class);
        if (spec == null) {
            return;
        }
        String packageName = type.getPackageName();
        if (packageName == null || packageName.isBlank()) {
            return;
        }
        boolean firstScan = scannedPackages.add(packageName);
        try {
            registry.scanPackages(packageName);
        } catch (Exception ex) {
            LOGGER.warn("Failed to scan package {} for view-model {}", packageName, type.getName(), ex);
        }
        if (!registry.hasBeanDefinition(type) && firstScan) {
            LOGGER.debug("View-model {} not registered after scanning {}; will rely on reflection instantiation",
                    type.getName(), packageName);
        }
    }
}
