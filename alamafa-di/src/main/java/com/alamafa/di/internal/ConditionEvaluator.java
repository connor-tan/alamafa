package com.alamafa.di.internal;


import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.ConditionalOnClass;
import com.alamafa.di.annotation.ConditionalOnProperty;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Optional;
import com.alamafa.config.Configuration;

/**
 * 评估条件注解（目前支持 {@link ConditionalOnProperty}）。
 */
public final class ConditionEvaluator {
    private ConditionEvaluator() {
    }

    /**
     * 判断给定元素是否满足条件注解。
     */
    public static boolean matches(ApplicationContext context, AnnotatedElement element) {
        Objects.requireNonNull(context, "context");
        return matchesProperty(context, element) && matchesOnClass(element);
    }

    private static boolean matchesProperty(ApplicationContext context, AnnotatedElement element) {
        ConditionalOnProperty conditional = element.getAnnotation(ConditionalOnProperty.class);
        if (conditional == null) {
            return true;
        }
        String propertyName = conditional.name();
        String expected = conditional.havingValue();
        boolean matchIfMissing = conditional.matchIfMissing();

        String value = resolveProperty(context, propertyName);
        if (value == null) {
            return matchIfMissing;
        }
        if (expected.isEmpty()) {
            return true;
        }
        return expected.equals(value);
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
