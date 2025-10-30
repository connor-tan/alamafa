package com.alamafa.jfx.viewmodel.meta;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanPostProcessor;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects {@link FxViewModelSpec} metadata during bean post-processing.
 */
public final class FxViewModelMetadataProcessor implements BeanPostProcessor {
    private final FxViewModelRegistry registry;
    private final Set<Class<?>> processed = ConcurrentHashMap.newKeySet();

    public FxViewModelMetadataProcessor(FxViewModelRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void postProcess(Object bean, ApplicationContext context) {
        Class<?> type = bean.getClass();
        if (processed.contains(type)) {
            return;
        }
        FxViewModelSpec annotation = type.getAnnotation(FxViewModelSpec.class);
        if (annotation == null) {
            return;
        }
        FxViewModelDescriptor descriptor = new FxViewModelDescriptor(type, annotation.value(), annotation.lazy(), annotation.scope());
        registry.register(descriptor);
        processed.add(type);
    }
}
