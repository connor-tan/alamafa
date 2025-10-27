package com.alamafa.di.internal;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanDefinition;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.BeanSupplier;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.di.annotation.Import;
import com.alamafa.di.internal.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 处理 @Configuration 类与组件扫描，将定义注册到 {@link BeanRegistry}。
 */
public final class ConfigurationProcessor {
    private final BeanRegistry registry;
    private final ApplicationContext context;
    private final Set<Class<?>> processed = new HashSet<>();
    private final Set<String> scannedPackages = new HashSet<>();
    private final Set<ComponentCandidate> componentCandidates = new LinkedHashSet<>();
    private final Set<Class<?>> registeredComponentTypes = new HashSet<>();

    public ConfigurationProcessor(BeanRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.context = registry.context();
    }

    /**
     * 扫描多个基础包，发现配置类及组件。
     */
    public void scanPackages(String... basePackages) {
        if (basePackages == null) {
            return;
        }
        for (String basePackage : basePackages) {
            if (basePackage == null) {
                continue;
            }
            String trimmed = basePackage.trim();
            if (!trimmed.isEmpty()) {
                scanAndProcess(trimmed);
            }
        }
    }

    /**
     * 解析单个 @Configuration 类，处理导入、扫描及 @Bean 方法。
     */
    public void processConfiguration(Class<?> configClass) {
        if (!processed.add(configClass)) {
            return;
        }
        if (!configClass.isAnnotationPresent(Configuration.class)) {
            throw new IllegalArgumentException(configClass.getName() + " is not annotated with @Configuration");
        }
        if (!ConditionEvaluator.matches(context, configClass)) {
            return;
        }
        Object instance = ReflectionUtils.instantiateConfiguration(configClass, context, registry);
        Package pkg = configClass.getPackage();
        if (pkg != null) {
            scanComponents(pkg.getName());
        }

        Configuration configAnnotation = configClass.getAnnotation(Configuration.class);
        if (configAnnotation != null) {
            for (Class<?> imported : configAnnotation.imports()) {
                processConfiguration(imported);
            }
            for (String basePackage : configAnnotation.scanBasePackages()) {
                scanAndProcess(basePackage);
            }
        }
        Import importAnnotation = configClass.getAnnotation(Import.class);
        if (importAnnotation != null) {
            Arrays.stream(importAnnotation.value()).forEach(this::processConfiguration);
        }
        registerBeanMethods(configClass, instance);
    }

    /**
     * 执行组件扫描并递归处理扫描到的配置类。
     */
    private void scanAndProcess(String basePackage) {
        if (basePackage == null || basePackage.isBlank()) {
            return;
        }
        scanComponents(basePackage);
        Set<Class<?>> candidates = ClassPathScanner.findConfigurationClasses(getClassLoader(), basePackage);
        for (Class<?> candidate : candidates) {
            processConfiguration(candidate);
        }
    }

    /**
     * 获取用于扫描的类加载器。
     */
    private ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null ? loader : getClass().getClassLoader();
    }

    /**
     * 遍历配置类中的 @Bean 方法并注册为 BeanDefinition。
     */
    private void registerBeanMethods(Class<?> configClass, Object instance) {
        for (Method method : getUniqueDeclaredMethods(configClass)) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean == null) {
                continue;
            }
            if (!ConditionEvaluator.matches(context, method)) {
                continue;
            }
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(void.class)) {
                throw new IllegalStateException("@Bean method must return a value: " + method);
            }
            method.setAccessible(true);
            BeanSupplier<?> supplier = () -> {
                Object[] args = ReflectionUtils.resolveMethodArguments(method, context, registry);
                try {
                    Object beanInstance = method.invoke(instance, args);
                    if (beanInstance == null) {
                        throw new IllegalStateException("@Bean method returned null: " + method);
                    }
                    invokeLifecycleCallbacks(bean.scope(), beanInstance);
                    return beanInstance;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to invoke @Bean method " + method, e);
                }
            };
            @SuppressWarnings("unchecked")
            Class<Object> beanType = (Class<Object>) returnType;
            @SuppressWarnings("unchecked")
            BeanSupplier<Object> typedSupplier = (BeanSupplier<Object>) supplier;
            BeanDefinition<Object> definition = new BeanDefinition<>(beanType, typedSupplier, bean.scope(),
                    bean.primary(), bean.lazy());
            boolean named = bean.name() != null && !bean.name().isBlank();
            if (!named || registry.definitionsFor(beanType).isEmpty()) {
                registry.register(beanType, definition);
            }
            if (named) {
                registry.register(bean.name(), definition);
            }
        }
    }

    private void invokeLifecycleCallbacks(BeanDefinition.Scope scope, Object beanInstance) throws Exception {
        Class<?> type = beanInstance.getClass();
        LifecycleMethodCollector.ensureScopeSupportsLifecycle(scope, type);
        for (Method postConstruct : LifecycleMethodCollector.postConstructMethods(type)) {
            postConstruct.invoke(beanInstance);
        }
        if (scope == BeanDefinition.Scope.SINGLETON) {
            List<Method> preDestroyMethods = LifecycleMethodCollector.preDestroyMethods(type);
            if (!preDestroyMethods.isEmpty()) {
                registry.registerPreDestroy(beanInstance, preDestroyMethods);
            }
        }
    }

    /**
     * 获取类层次结构中唯一的方法集合，避免重复。
     */
    private static Set<Method> getUniqueDeclaredMethods(Class<?> type) {
        Set<Method> methods = new LinkedHashSet<>();
        for (Method method : type.getDeclaredMethods()) {
            methods.add(method);
        }
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            methods.addAll(getUniqueDeclaredMethods(superclass));
        }
        return methods;
    }

    /** 返回已发现的组件候选只读集合。 */
    public Set<ComponentCandidate> componentCandidates() {
        return Set.copyOf(componentCandidates);
    }

    /** 将尚未注册的组件候选交给 {@link ComponentRegistrar}。 */
    public void registerComponents() {
        if (componentCandidates.isEmpty()) {
            return;
        }
        ComponentRegistrar registrar = new ComponentRegistrar(registry);
        for (ComponentCandidate candidate : componentCandidates) {
            if (registeredComponentTypes.add(candidate.type())) {
                registrar.register(candidate);
            }
        }
    }

    /**
     * 对指定包执行组件扫描，避免重复。
     */
    private void scanComponents(String basePackage) {
        String trimmed = basePackage.trim();
        if (!scannedPackages.add(trimmed)) {
            return;
        }
        ComponentScanner scanner = new ComponentScanner(getClassLoader());
        componentCandidates.addAll(scanner.scan(trimmed));
    }
}
