package com.alamafa.di.internal;

import com.alamafa.di.BeanDefinition;
import com.alamafa.di.BeanResolutionException;
import com.alamafa.di.annotation.PostConstruct;
import com.alamafa.di.annotation.PreDestroy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 缓存并提供基于注解的生命周期方法解析结果，供组件扫描与 @Bean 工厂复用。
 */
final class LifecycleMethodCollector {
    private static final ConcurrentMap<Class<?>, List<Method>> POST_CONSTRUCT_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, List<Method>> PRE_DESTROY_CACHE = new ConcurrentHashMap<>();

    private LifecycleMethodCollector() {
    }

    /**
     * 返回类型上标记的 @PostConstruct 方法（包含父类），封装为不可变集合。
     */
    static List<Method> postConstructMethods(Class<?> type) {
        return POST_CONSTRUCT_CACHE.computeIfAbsent(type, clazz ->
                collectLifecycleMethods(clazz, PostConstruct.class));
    }

    /**
     * 返回类型上标记的 @PreDestroy 方法（包含父类），封装为不可变集合。
     */
    static List<Method> preDestroyMethods(Class<?> type) {
        return PRE_DESTROY_CACHE.computeIfAbsent(type, clazz ->
                collectLifecycleMethods(clazz, PreDestroy.class));
    }

    /**
     * 验证作用域与 @PreDestroy 的兼容性。
     */
    static void ensureScopeSupportsLifecycle(BeanDefinition.Scope scope, Class<?> type) {
        if (!preDestroyMethods(type).isEmpty() && scope != BeanDefinition.Scope.SINGLETON) {
            throw new BeanResolutionException("@PreDestroy methods are only supported on singleton scope components");
        }
    }

    private static List<Method> collectLifecycleMethods(Class<?> type, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        for (Method method : collectMethods(type)) {
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }
            validateLifecycleMethod(method);
            method.setAccessible(true);
            methods.add(method);
        }
        return List.copyOf(methods);
    }

    private static void validateLifecycleMethod(Method method) {
        if (method.getParameterCount() != 0) {
            throw new BeanResolutionException("Lifecycle method " + method + " must not declare parameters");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new BeanResolutionException("Lifecycle method " + method + " must not be static");
        }
    }

    private static Collection<Method> collectMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = type;
        while (current != null && !current.equals(Object.class)) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }
        return methods;
    }
}
