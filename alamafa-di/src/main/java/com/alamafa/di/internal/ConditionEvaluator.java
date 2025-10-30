package com.alamafa.di.internal;


import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.annotation.ConditionalOnClass;
import com.alamafa.di.annotation.ConditionalOnMissingBean;
import com.alamafa.di.annotation.ConditionalOnProperty;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Optional;
import com.alamafa.config.Configuration;

/**
 * 评估条件注解（支持 {@link ConditionalOnProperty}、{@link ConditionalOnClass}、
 * {@link com.alamafa.di.annotation.ConditionalOnMissingBean} 等）。
 */
public final class ConditionEvaluator {
    private ConditionEvaluator() {
    }

    /**
     * 判断给定元素是否满足条件注解。
     */
    public static boolean matches(ApplicationContext context, AnnotatedElement element) {
        Objects.requireNonNull(context, "context");
        return matchesProperty(context, element)
                && matchesOnClass(element)
                && matchesOnMissingBean(context, element);
    }

    private static boolean matchesProperty(ApplicationContext context, AnnotatedElement element) {
        ConditionalOnProperty conditional = element.getAnnotation(ConditionalOnProperty.class);
        if (conditional == null) {
            return true;
        }
        String[] propertyNames = resolvePropertyNames(conditional);
        if (propertyNames.length == 0) {
            return true;
        }
        String expected = conditional.havingValue();
        boolean matchIfMissing = conditional.matchIfMissing();
        String prefix = conditional.prefix();

        for (String name : propertyNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String fullName = buildPropertyName(prefix, name.trim());
            String value = resolveProperty(context, fullName);
            if (value == null) {
                if (!matchIfMissing) {
                    return false;
                }
                continue;
            }
            if (!expected.isEmpty() && !expected.equals(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesOnClass(AnnotatedElement element) {
        ConditionalOnClass conditional = element.getAnnotation(ConditionalOnClass.class);
        if (conditional == null) {
            return true;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ConditionEvaluator.class.getClassLoader();
        }
        for (String className : conditional.value()) {
            if (className == null || className.trim().isEmpty()) {
                continue;
            }
            if (!isClassPresent(className.trim(), loader)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesOnMissingBean(ApplicationContext context, AnnotatedElement element) {
        ConditionalOnMissingBean conditional = element.getAnnotation(ConditionalOnMissingBean.class);
        if (conditional == null) {
            return true;
        }
        BeanRegistry registry = context.get(BeanRegistry.class);
        if (registry == null) {
            return true;
        }
        for (Class<?> type : conditional.value()) {
            if (type == null) {
                continue;
            }
            if (registry.hasBeanDefinition(type)) {
                return false;
            }
        }
        for (String name : conditional.name()) {
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (registry.hasBeanName(trimmed)) {
                return false;
            }
        }
        return true;
    }

    private static String[] resolvePropertyNames(ConditionalOnProperty conditional) {
        String[] values = conditional.value();
        String[] names = conditional.name();
        if ((values == null || values.length == 0) && (names == null || names.length == 0)) {
            return new String[0];
        }
        if (values == null || values.length == 0) {
            return names;
        }
        if (names == null || names.length == 0) {
            return values;
        }
        String[] combined = new String[values.length + names.length];
        System.arraycopy(values, 0, combined, 0, values.length);
        System.arraycopy(names, 0, combined, values.length, names.length);
        return combined;
    }

    private static String buildPropertyName(String prefix, String name) {
        if (prefix == null || prefix.isBlank()) {
            return name;
        }
        if (name.isEmpty()) {
            return prefix;
        }
        if (prefix.endsWith(".")) {
            return prefix + name;
        }
        return prefix + "." + name;
    }

    private static boolean isClassPresent(String className, ClassLoader loader) {
        try {
            Class.forName(className, false, loader);
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    /** 从上下文或配置对象解析属性值。 */
    private static String resolveProperty(ApplicationContext context, String name) {
        if (context.contains(name)) {
            Object value = context.get(name);
            return value != null ? value.toString() : null;
        }
        Configuration configuration = context.get(Configuration.class);
        if (configuration != null) {
            Optional<String> opt = configuration.get(name);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return null;
    }
}
