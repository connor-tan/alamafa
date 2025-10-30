package com.alamafa.di.internal;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.BeanResolutionException;
import com.alamafa.di.annotation.Inject;
import com.alamafa.di.annotation.OptionalDependency;
import com.alamafa.di.annotation.Qualifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 反射相关的内部工具，用于配置实例化与方法参数解析。
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * 创建配置类实例，支持多参数与依赖注入。
     */
    public static Object instantiateConfiguration(Class<?> configClass,
                                                  ApplicationContext context,
                                                  BeanRegistry registry) {
        Constructor<?> constructor = selectConstructor(configClass);
        constructor.setAccessible(true);
        Object[] arguments = resolveParameters(constructor, context, registry);
        try {
            return constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to instantiate configuration class "
                    + configClass.getName(), ex);
        }
    }

    /**
     * 根据参数类型解析方法调用所需的参数数组。
     */
    public static Object[] resolveMethodArguments(Method method, ApplicationContext context, BeanRegistry registry) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return new Object[0];
        }
        DependencyResolver resolver = new DependencyResolver(context, registry);
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            InjectionPoint point = buildInjectionPoint(parameters[i],
                    method.getDeclaringClass().getName() + " method " + method.getName() + " argument " + i);
            args[i] = resolver.resolve(point);
        }
        return args;
    }

    private static Constructor<?> selectConstructor(Class<?> configClass) {
        Constructor<?>[] constructors = configClass.getDeclaredConstructors();
        List<Constructor<?>> injectable = java.util.Arrays.stream(constructors)
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();
        if (injectable.size() > 1) {
            throw new IllegalStateException("Multiple constructors annotated with @Inject in "
                    + configClass.getName());
        }
        if (injectable.size() == 1) {
            return injectable.get(0);
        }
        if (constructors.length == 1) {
            return constructors[0];
        }
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        throw new IllegalStateException("No suitable constructor found for configuration class "
                + configClass.getName());
    }

    private static Object[] resolveParameters(Constructor<?> constructor,
                                              ApplicationContext context,
                                              BeanRegistry registry) {
        Parameter[] parameters = constructor.getParameters();
        if (parameters.length == 0) {
            return new Object[0];
        }
        DependencyResolver resolver = new DependencyResolver(context, registry);
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            InjectionPoint point = buildInjectionPoint(parameters[i],
                    constructor.getDeclaringClass().getName() + " constructor argument " + i);
            args[i] = resolver.resolve(point);
        }
        return args;
    }

    private static InjectionPoint buildInjectionPoint(Parameter parameter, String description) {
        boolean optional = parameter.isAnnotationPresent(OptionalDependency.class);
        String qualifier = findQualifier(parameter);
        return createInjectionPoint(parameter.getParameterizedType(), parameter.getType(), optional, qualifier, description);
    }

    private static String findQualifier(Parameter parameter) {
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (qualifier == null) {
            return null;
        }
        String value = qualifier.value().trim();
        return value.isEmpty() ? null : value;
    }

    private static InjectionPoint createInjectionPoint(Type declaredType,
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

    private static boolean isOptional(Class<?> type) {
        return Optional.class.equals(type);
    }

    private static Type resolveOptionalType(Type declaredType, String description) {
        if (declaredType instanceof ParameterizedType parameterizedType) {
            Type[] actual = parameterizedType.getActualTypeArguments();
            if (actual.length == 1) {
                return actual[0];
            }
        }
        throw new BeanResolutionException("Failed to resolve Optional generic type for " + description);
    }

    private static Class<?> resolveCollectionRawType(Type type, String description) {
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

    private static Type resolveCollectionElementType(Type type, String description) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actual = parameterizedType.getActualTypeArguments();
            if (actual.length == 1) {
                return actual[0];
            }
        }
        throw new BeanResolutionException("Failed to resolve collection element type for " + description);
    }

    private static final class DependencyResolver {
        private final ApplicationContext context;
        private final BeanRegistry registry;

        private DependencyResolver(ApplicationContext context, BeanRegistry registry) {
            this.context = context;
            this.registry = registry;
        }

        Object resolve(InjectionPoint point) {
            try {
                Object resolved = doResolve(point);
                if (point.wrapsOptional()) {
                    return Optional.ofNullable(resolved);
                }
                if (resolved == null && !point.optional()) {
                    throw new BeanResolutionException("Unsatisfied dependency "
                            + describeTarget(point) + " for " + point.description());
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
                throw new BeanResolutionException("Unsatisfied dependency "
                        + describeTarget(point) + " for " + point.description(), ex);
            }
        }

        private Object doResolve(InjectionPoint point) {
            Class<?> rawType = point.rawType();
            if (ApplicationContext.class.equals(rawType)) {
                return context;
            }
            if (BeanRegistry.class.equals(rawType)) {
                return registry;
            }
            Class<?> collectionRaw = point.collectionRawType();
            if (collectionRaw != null) {
                return resolveCollection(point, collectionRaw);
            }
            Class<?> dependencyClass = resolveClass(point.elementType(), point.description());
            if (point.qualifier() != null) {
                return resolveByQualifier(point.qualifier(), dependencyClass, point);
            }
            return registry.get(dependencyClass);
        }

        private Object resolveCollection(InjectionPoint point, Class<?> collectionRaw) {
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
            if (collectionRaw.isAssignableFrom(java.util.Collection.class)) {
                return List.copyOf(beans);
            }
            throw new BeanResolutionException("Unsupported collection type " + collectionRaw.getName()
                    + " for " + point.description());
        }

        private Object resolveByQualifier(String qualifier, Class<?> expectedType, InjectionPoint point) {
            Object bean = registry.get(qualifier);
            if (!expectedType.isInstance(bean)) {
                throw new BeanResolutionException("Bean '" + qualifier + "' is not of required type "
                        + expectedType.getName() + " for " + point.description());
            }
            return bean;
        }

        private Class<?> resolveClass(Type type, String description) {
            if (type instanceof Class<?> clazz) {
                return clazz;
            }
            if (type instanceof ParameterizedType parameterized) {
                Type raw = parameterized.getRawType();
                if (raw instanceof Class<?> rawClass) {
                    return rawClass;
                }
            }
            throw new BeanResolutionException("Unsupported dependency type " + type + " for " + description);
        }

        private String describeTarget(InjectionPoint point) {
            if (point.collectionRawType() != null) {
                Class<?> elementClass = resolveClass(point.elementType(), point.description());
                return point.collectionRawType().getName() + "<" + elementClass.getName() + ">";
            }
            return resolveClass(point.elementType(), point.description()).getName();
        }
    }
}
