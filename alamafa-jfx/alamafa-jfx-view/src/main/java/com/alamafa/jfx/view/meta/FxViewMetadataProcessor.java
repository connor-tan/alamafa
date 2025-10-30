package com.alamafa.jfx.view.meta;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanPostProcessor;
import com.alamafa.jfx.view.annotation.FxViewSpec;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Captures metadata from beans annotated with {@link FxViewSpec} and stores them in {@link FxViewRegistry}.
 */
public final class FxViewMetadataProcessor implements BeanPostProcessor {
    private final FxViewRegistry registry;
    private final Set<Class<?>> processed = ConcurrentHashMap.newKeySet();

    public FxViewMetadataProcessor(FxViewRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void postProcess(Object bean, ApplicationContext context) {
        Class<?> type = bean.getClass();
        if (processed.contains(type)) {
            return;
        }
        FxViewSpec annotation = type.getAnnotation(FxViewSpec.class);
        if (annotation == null) {
            return;
        }
        FxViewDescriptor descriptor = FxViewDescriptor.of(
                type,
                annotation.value(),
                annotation.fxml(),
                annotation.styles(),
                annotation.bundle(),
                annotation.viewModel(),
                annotation.shared(),
                annotation.primary(),
                annotation.title(),
                annotation.width(),
                annotation.height(),
                annotation.resizable());
        registry.register(descriptor);
        processed.add(type);
    }
}
