package com.alamafa.di.internal;


import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.BeanDefinition;
import com.alamafa.di.BeanRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 负责将扫描到的组件注册为 BeanDefinition，并处理接口别名。
 */
final class ComponentRegistrar {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ComponentRegistrar.class);

    private final BeanRegistry registry;
    private final ComponentDefinitionFactory definitionFactory = new ComponentDefinitionFactory();

    ComponentRegistrar(BeanRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * 将组件候选注册到 BeanRegistry，包含作用域检查与命名处理。
     */
    void register(ComponentCandidate candidate) {
        Class<?> type = candidate.type();
        if (registry.hasBeanDefinition(type)) {
            LOGGER.debug("Skipping component {} because a bean definition already exists", type.getName());
            return;
        }
        String beanName = candidate.beanName();
        if (beanName != null && !beanName.isBlank() && registry.hasBeanName(beanName)) {
            LOGGER.debug("Skipping component {} because bean name '{}' already exists", type.getName(), beanName);
            return;
        }
        if (!ConditionEvaluator.matches(registry.context(), type)) {
            LOGGER.debug("Skipping component {} because condition not satisfied", type.getName());
            return;
        }
        if (candidate.sharedView() && candidate.scope() != BeanDefinition.Scope.SINGLETON) {
            LOGGER.warn("Shared view {} is registered with non-singleton scope {}", type.getName(), candidate.scope());
        }
        ComponentDefinition definition = definitionFactory.create(candidate);
        BeanDefinition<Object> beanDefinition = new BeanDefinition<>(
                cast(type),
                () -> instantiate(definition),
                definition.scope(),
                candidate.primary(),
                candidate.lazy());
        registry.register(cast(type), beanDefinition);
        if (beanName != null && !beanName.isBlank()) {
            registry.register(beanName, beanDefinition);
        }
        registerInterfaces(type, beanDefinition);
    }

    /**
     * 通过 ComponentInstanceFactory 创建实例并触发生命周期回调。
     */
    private Object instantiate(ComponentDefinition definition) throws Exception {
        ComponentInstanceFactory factory = new ComponentInstanceFactory(registry, definition);
        Object instance = factory.createInstance();
        invokePostConstruct(definition.postConstructMethods(), instance);
        registerPreDestroy(definition, instance);
        return instance;
    }

    /** 调用 @PostConstruct 方法。 */
    private void invokePostConstruct(List<Method> methods, Object instance) throws Exception {
        for (Method method : methods) {
            method.invoke(instance);
        }
    }

    /** 根据 scope 注册 @PreDestroy 回调。 */
    private void registerPreDestroy(ComponentDefinition definition, Object instance) {
        if (!definition.preDestroyMethods().isEmpty() && definition.scope() == BeanDefinition.Scope.SINGLETON) {
            registry.registerPreDestroy(instance, definition.preDestroyMethods());
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<Object> cast(Class<?> type) {
        return (Class<Object>) type;
    }

    /**
     * 为实现的接口也注册相同的 BeanDefinition，便于通过接口注入。
     */
    private void registerInterfaces(Class<?> type, BeanDefinition<Object> definition) {
        for (Class<?> iface : type.getInterfaces()) {
            if (iface.getName().startsWith("java.")) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<Object> ifaceType = (Class<Object>) iface;
            registry.register(ifaceType, definition);
        }
    }
}
