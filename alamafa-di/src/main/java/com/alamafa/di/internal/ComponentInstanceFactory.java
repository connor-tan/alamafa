package com.alamafa.di.internal;


import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.BeanResolutionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 根据 {@link ComponentDefinition} 生成实例并完成字段注入。
 */
final class ComponentInstanceFactory {
    private final BeanRegistry registry;
    private final ComponentDefinition definition;

    ComponentInstanceFactory(BeanRegistry registry, ComponentDefinition definition) {
        this.registry = registry;
        this.definition = definition;
    }

    /**
     * 按定义创建实例并注入字段。
     */
    Object createInstance() throws Exception {
        Object instance = instantiateConstructor();
        injectFields(instance);
        return instance;
    }

    /** 使用解析好的构造器实例化 Bean。 */
    private Object instantiateConstructor() throws Exception {
        Constructor<?> constructor = definition.constructor();
        Object[] args = resolveArguments(definition.constructorArguments());
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException ex) {
            throw new BeanResolutionException("Failed to instantiate component " +
                    definition.candidate().type().getName(), ex);
        }
    }

    /** 将注入点解析为构造器参数。 */
    private Object[] resolveArguments(java.util.List<InjectionPoint> points) {
        Object[] args = new Object[points.size()];
        for (int i = 0; i < points.size(); i++) {
            args[i] = resolveDependency(points.get(i));
        }
        return args;
    }

    /** 遍历字段注入依赖。 */
    private void injectFields(Object instance) {
        for (ComponentDefinition.FieldInjection injection : definition.fieldInjections()) {
            Object dependency = resolveDependency(injection.dependency());
            Field field = injection.field();
            try {
                field.set(instance, dependency);
            } catch (IllegalAccessException ex) {
                throw new BeanResolutionException("Failed to inject field " + field + " on " +
                        definition.candidate().type().getName(), ex);
            }
        }
    }

    /**
     * 统一解析依赖，支持 ApplicationContext、BeanRegistry、集合以及 Optional。
     */
    private Object resolveDependency(InjectionPoint point) {
        try {
            Object resolved = doResolve(point);
            if (point.wrapsOptional()) {
                return Optional.ofNullable(resolved);
            }
            if (resolved == null && !point.optional()) {
                throw new BeanResolutionException("Unsatisfied dependency "
                        + describeTarget(point) + " for " + definition.candidate().type().getName()
                        + " (" + point.description() + ")");
            }
            return resolved;
        } catch (BeanResolutionException ex) {
            if (point.optional()) {
                return point.wrapsOptional() ? Optional.empty() : null;
            }
            throw ex;
        } catch (IllegalArgumentException ex) {
            if (point.optional()) {
                return point.wrapsOptional() ? Optional.empty() : null;
            }
            throw new BeanResolutionException("Unsatisfied dependency " + describeTarget(point)
                    + " for " + definition.candidate().type().getName() + " (" + point.description() + ")", ex);
        }
    }

    private Object doResolve(InjectionPoint point) {
        Class<?> rawType = point.rawType();
        if (ApplicationContext.class.equals(rawType)) {
            return registry.context();
        }
        if (BeanRegistry.class.equals(rawType)) {
            return registry;
        }

        Class<?> collectionRaw = point.collectionRawType();
        if (collectionRaw != null) {
            return resolveCollectionDependency(point, collectionRaw);
        }

        Class<?> dependencyClass = resolveClass(point.elementType(), point.description());
        if (point.qualifier() != null) {
            return resolveByQualifier(point.qualifier(), dependencyClass, point);
        }
        return registry.get(dependencyClass);
    }

    private Object resolveCollectionDependency(InjectionPoint point, Class<?> collectionRaw) {
        Class<?> elementClass = resolveClass(point.elementType(), point.description());
        List<?> beans;
        if (point.qualifier() != null) {
            Object bean = resolveByQualifier(point.qualifier(), elementClass, point);
            beans = List.of(bean);
        } else {
            beans = registry.getBeansOfType(elementClass);
        }
        if (collectionRaw.isAssignableFrom(List.class)) {
            return List.copyOf(beans);
        }
        if (collectionRaw.isAssignableFrom(Set.class)) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(beans));
        }
        if (collectionRaw.isAssignableFrom(Collection.class)) {
            return List.copyOf(beans);
        }
        throw new BeanResolutionException("Unsupported collection type " + collectionRaw.getName() +
                " for " + point.description());
    }

    private Object resolveByQualifier(String qualifier, Class<?> expectedType, InjectionPoint point) {
        try {
            Object bean = registry.get(qualifier);
            if (!expectedType.isInstance(bean)) {
                throw new BeanResolutionException("Bean '" + qualifier + "' is not of required type "
                        + expectedType.getName() + " for " + point.description());
            }
            return bean;
        } catch (IllegalArgumentException ex) {
            throw new BeanResolutionException("No bean named '" + qualifier + "' defined for "
                    + definition.candidate().type().getName() + " (" + point.description() + ")", ex);
        }
    }

    private String describeTarget(InjectionPoint point) {
        Class<?> collectionRaw = point.collectionRawType();
        if (collectionRaw != null) {
            return collectionRaw.getName() + "<" + resolveClass(point.elementType(), point.description()).getName() + ">";
        }
        return resolveClass(point.elementType(), point.description()).getName();
    }

    /** 将 Type 转为具体的 Class。 */
    private Class<?> resolveClass(Type type, String description) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof java.lang.reflect.ParameterizedType parameterized) {
            Type raw = parameterized.getRawType();
            if (raw instanceof Class<?> rawClass) {
                return rawClass;
            }
        }
        throw new BeanResolutionException("Unsupported dependency type " + type + " for " + description);
    }
}
