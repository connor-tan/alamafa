package com.alamafa.di.internal;


import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 反射相关的内部工具，用于配置实例化与方法参数解析。
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * 寻找合适构造器创建配置类实例，支持无参/注入 ApplicationContext 或 BeanRegistry。
     */
    public static Object instantiateConfiguration(Class<?> configClass, ApplicationContext context, BeanRegistry registry) {
        Constructor<?>[] constructors = configClass.getDeclaredConstructors();
        Constructor<?> target = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                target = constructor;
                break;
            }
        }
        if (target == null) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 1 &&
                        (constructor.getParameterTypes()[0].equals(ApplicationContext.class)
                                || constructor.getParameterTypes()[0].equals(BeanRegistry.class))) {
                    target = constructor;
                    break;
                }
            }
        }
        if (target == null) {
            throw new IllegalStateException("No suitable constructor found for configuration class " + configClass.getName());
        }
        target.setAccessible(true);
        try {
            if (target.getParameterCount() == 0) {
                return target.newInstance();
            }
            Class<?> paramType = target.getParameterTypes()[0];
            Object arg = paramType.equals(ApplicationContext.class) ? context : registry;
            return target.newInstance(arg);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate configuration class " + configClass.getName(), e);
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
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            if (type.equals(ApplicationContext.class)) {
                args[i] = context;
            } else if (type.equals(BeanRegistry.class)) {
                args[i] = registry;
            } else {
                args[i] = registry.get(type);
            }
        }
        return args;
    }
}
