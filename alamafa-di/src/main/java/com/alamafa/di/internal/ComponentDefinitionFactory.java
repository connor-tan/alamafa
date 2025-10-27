package com.alamafa.di.internal;


import com.alamafa.di.BeanDefinition;
import com.alamafa.di.BeanResolutionException;
import com.alamafa.di.annotation.Inject;
import com.alamafa.di.annotation.OptionalDependency;
import com.alamafa.di.annotation.Qualifier;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 根据扫描到的 {@link ComponentCandidate} 生成可供实例化的详细定义。
 */
final class ComponentDefinitionFactory {
    /**
     * 解析构造器、字段和生命周期方法，构造组件定义。
     */
    ComponentDefinition create(ComponentCandidate candidate) {
        Class<?> type = candidate.type();
        Constructor<?> constructor = selectConstructor(type);
        constructor.setAccessible(true);
        List<InjectionPoint> constructorPoints = resolveParameters(constructor);
        List<ComponentDefinition.FieldInjection> fieldInjections = resolveFields(type);
        List<Method> postConstructMethods = LifecycleMethodCollector.postConstructMethods(type);
        List<Method> preDestroyMethods = LifecycleMethodCollector.preDestroyMethods(type);
        LifecycleMethodCollector.ensureScopeSupportsLifecycle(candidate.scope(), type);
        return new ComponentDefinition(candidate, constructor, constructorPoints, fieldInjections,
                postConstructMethods, preDestroyMethods);
    }

    /**
     * 按照 @Inject 标记或默认策略选择构造器。
     */
    private Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        List<Constructor<?>> injectConstructors = Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .toList();
        if (injectConstructors.size() > 1) {
            throw new BeanResolutionException("Multiple constructors annotated with @Inject in " + type.getName());
        }
        if (injectConstructors.size() == 1) {
            return injectConstructors.get(0);
        }
        if (constructors.length == 1) {
            return constructors[0];
        }
        try {
            Constructor<?> noArgs = type.getDeclaredConstructor();
            return noArgs;
        } catch (NoSuchMethodException e) {
            throw new BeanResolutionException("No suitable constructor found for component " + type.getName());
        }
    }

    /**
     * 解析构造器参数对应的注入点。
     */
    private List<InjectionPoint> resolveParameters(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        List<InjectionPoint> points = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            boolean optional = parameter.isAnnotationPresent(OptionalDependency.class);
            String qualifier = findQualifier(parameter);
            InjectionPoint point = buildInjectionPoint(parameter.getParameterizedType(), parameter.getType(),
                    optional, qualifier, constructor.getDeclaringClass().getName() + " constructor argument " + i);
            points.add(point);
        }
        return points;
    }

    /**
     * 收集所有需要注入的字段。
     */
    private List<ComponentDefinition.FieldInjection> resolveFields(Class<?> type) {
        List<ComponentDefinition.FieldInjection> injections = new ArrayList<>();
        for (Field field : collectFields(type)) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            boolean optional = field.isAnnotationPresent(OptionalDependency.class);
            String qualifier = findQualifier(field);
            InjectionPoint point = buildInjectionPoint(field.getGenericType(), field.getType(), optional,
                    qualifier, type.getName() + " field " + field.getName());
            field.setAccessible(true);
            injections.add(new ComponentDefinition.FieldInjection(field, point));
        }
        return injections;
    }

    /**
     * 根据声明类型创建 InjectionPoint，处理 Optional 情况。
     */
    private InjectionPoint buildInjectionPoint(Type declaredType,
                                                                       Class<?> rawType,
                                                                       boolean optional,
                                                                       String qualifier,
                                                                       String description) {
        boolean wrapsOptional = false;
        Type effectiveType = declaredType;
        if (isOptional(rawType)) {
            wrapsOptional = true;
            optional = true;
            effectiveType = resolveOptionalType(declaredType, description);
        }
        Class<?> collectionRawType = resolveCollectionRawType(effectiveType, description);
        Type elementType = collectionRawType != null
                ? resolveCollectionElementType(effectiveType, description)
                : effectiveType;
        return new InjectionPoint(declaredType, elementType, rawType, collectionRawType,
                optional, wrapsOptional, qualifier, description);
    }

    private String findQualifier(AnnotatedElement element) {
        Qualifier qualifier = element.getAnnotation(Qualifier.class);
        if (qualifier == null) {
            return null;
        }
        String value = qualifier.value().trim();
        return value.isEmpty() ? null : value;
    }

    private Class<?> resolveCollectionRawType(Type type, String description) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type raw = parameterizedType.getRawType();
            if (raw instanceof Class<?> rawClass && java.util.Collection.class.isAssignableFrom(rawClass)) {
                return rawClass;
            }
        } else if (type instanceof Class<?> rawClass && java.util.Collection.class.isAssignableFrom(rawClass)) {
            throw new BeanResolutionException("Collection injection point must declare generic element type for "
                    + description);
        }
        return null;
    }

    private Type resolveCollectionElementType(Type type, String description) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actual = parameterizedType.getActualTypeArguments();
            if (actual.length == 1) {
                return actual[0];
            }
        }
        throw new BeanResolutionException("Failed to resolve collection element type for " + description);
    }

    /** 是否为 Optional 类型。 */
    private static boolean isOptional(Class<?> type) {
        return Optional.class.equals(type);
    }

    /**
     * 获取 Optional 的泛型参数。
     */
    private Type resolveOptionalType(Type declaredType, String description) {
        if (declaredType instanceof ParameterizedType parameterizedType) {
            Type[] actual = parameterizedType.getActualTypeArguments();
            if (actual.length == 1) {
                return actual[0];
            }
        }
        throw new BeanResolutionException("Failed to resolve Optional generic type for " + description);
    }

    /**
     * 遍历类层次收集字段。
     */
    private Collection<Field> collectFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && !current.equals(Object.class)) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 确保 @PreDestroy 仅用于单例作用域。
     */
    // collectMethods removed; only field collection retained
}
