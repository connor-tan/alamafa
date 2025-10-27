package com.alamafa.di.internal;


import com.alamafa.di.BeanDefinition;
import com.alamafa.di.annotation.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 负责扫描指定包下的组件，识别 @Component 及其派生注解。
 */
final class ComponentScanner {
    private final ClassLoader classLoader;

    ComponentScanner(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * 扫描并返回所有组件候选。
     */
    Set<ComponentCandidate> scan(String basePackage) {
        Set<ComponentCandidate> candidates = new LinkedHashSet<>();
        Set<Class<?>> types = ClassPathScanner.findClasses(classLoader, basePackage, this::isPotentialComponent);
        for (Class<?> type : types) {
            resolveMetadata(type).ifPresent(metadata -> {
                String beanName = metadata.name;
                if (beanName == null || beanName.isBlank()) {
                    beanName = defaultBeanName(type);
                } else {
                    beanName = beanName.trim();
                }
                candidates.add(new ComponentCandidate(type, beanName, metadata.scope,
                        metadata.primary, metadata.lazy, metadata.stereotype, metadata.shared));
            });
        }
        return candidates;
    }

    /** 判断类型是否带有组件元数据。 */
    private boolean isPotentialComponent(Class<?> type) {
        return resolveMetadata(type).isPresent();
    }

    /**
     * 解析组件注解或其元注解，返回统一的 Metadata。
     */
    private Optional<Metadata> resolveMetadata(Class<?> type) {
        if (type.isAnnotation() || type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return Optional.empty();
        }
        Component component = type.getAnnotation(Component.class);
        if (component != null) {
            return Optional.of(new Metadata(component.scope(), component.value(),
                    component.primary(), component.lazy(), Component.class, false));
        }
        for (Annotation annotation : type.getAnnotations()) {
            Component meta = annotation.annotationType().getAnnotation(Component.class);
            if (meta == null) {
                continue;
            }
            BeanDefinition.Scope scope = extractScopeOverride(annotation).orElse(meta.scope());
            boolean primary = extractBooleanAttribute(annotation, "primary").orElse(meta.primary());
            boolean lazy = extractBooleanAttribute(annotation, "lazy").orElse(meta.lazy());
            String name = extractValueAttribute(annotation);
            boolean shared = extractBooleanAttribute(annotation, "shared").orElse(false);
            return Optional.of(new Metadata(scope, name, primary, lazy, annotation.annotationType(), shared));
        }
        return Optional.empty();
    }

    /** 读取注解中的 value 属性（若存在）。 */
    private static String extractValueAttribute(Annotation annotation) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod("value");
            if (method.getReturnType().equals(String.class)) {
                Object result = method.invoke(annotation);
                return result != null ? result.toString() : null;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    /** 读取 scope 属性以覆盖默认作用域。 */
    private static Optional<BeanDefinition.Scope> extractScopeOverride(Annotation annotation) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod("scope");
            if (method.getReturnType().equals(BeanDefinition.Scope.class)) {
                Object result = method.invoke(annotation);
                if (result instanceof BeanDefinition.Scope scope) {
                    return Optional.of(scope);
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }

    /** 读取布尔型属性。 */
    private static Optional<Boolean> extractBooleanAttribute(Annotation annotation, String attribute) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attribute);
            if (method.getReturnType().equals(boolean.class)) {
                Object result = method.invoke(annotation);
                if (result instanceof Boolean booleanValue) {
                    return Optional.of(booleanValue);
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }

    /**
     * 退化时根据类名生成默认 Bean 名称。
     */
    private static String defaultBeanName(Class<?> type) {
        String simpleName = type.getSimpleName();
        if (simpleName.isEmpty()) {
            return type.getName();
        }
        if (simpleName.length() == 1) {
            return simpleName.toLowerCase();
        }
        if (Character.isUpperCase(simpleName.charAt(0)) && Character.isUpperCase(simpleName.charAt(1))) {
            return simpleName;
        }
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private record Metadata(BeanDefinition.Scope scope,
                            String name,
                            boolean primary,
                            boolean lazy,
                            Class<? extends Annotation> stereotype,
                            boolean shared) {
    }
}
