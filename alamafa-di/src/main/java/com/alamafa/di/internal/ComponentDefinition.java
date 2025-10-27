package com.alamafa.di.internal;


import com.alamafa.di.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 描述通过注解扫描得到的组件定义，包含构造器、注入点以及生命周期方法。
 */
final class ComponentDefinition {
    private final ComponentCandidate candidate;
    private final Constructor<?> constructor;
    private final List<InjectionPoint> constructorArguments;
    private final List<FieldInjection> fieldInjections;
    private final List<Method> postConstructMethods;
    private final List<Method> preDestroyMethods;

    ComponentDefinition(ComponentCandidate candidate,
                        Constructor<?> constructor,
                        List<InjectionPoint> constructorArguments,
                        List<FieldInjection> fieldInjections,
                        List<Method> postConstructMethods,
                        List<Method> preDestroyMethods) {
        this.candidate = Objects.requireNonNull(candidate, "candidate");
        this.constructor = Objects.requireNonNull(constructor, "constructor");
        this.constructorArguments = List.copyOf(constructorArguments);
        this.fieldInjections = List.copyOf(fieldInjections);
        this.postConstructMethods = List.copyOf(postConstructMethods);
        this.preDestroyMethods = List.copyOf(preDestroyMethods);
    }

    /** 返回原始候选信息。 */
    ComponentCandidate candidate() {
        return candidate;
    }

    /** 获取用于实例化的构造器。 */
    Constructor<?> constructor() {
        return constructor;
    }

    /** 构造器参数注入点列表。 */
    List<InjectionPoint> constructorArguments() {
        return constructorArguments;
    }

    /** 字段注入点。 */
    List<FieldInjection> fieldInjections() {
        return fieldInjections;
    }

    /** PostConstruct 方法集合。 */
    List<Method> postConstructMethods() {
        return postConstructMethods;
    }

    /** PreDestroy 方法集合。 */
    List<Method> preDestroyMethods() {
        return preDestroyMethods;
    }

    /** 返回 Bean 作用域。 */
    BeanDefinition.Scope scope() {
        return candidate.scope();
    }

    static final class FieldInjection {
        private final Field field;
        private final InjectionPoint dependency;

        FieldInjection(Field field, InjectionPoint dependency) {
            this.field = Objects.requireNonNull(field, "field");
            this.dependency = Objects.requireNonNull(dependency, "dependency");
        }

        /** 需要注入的字段。 */
        Field field() {
            return field;
        }

        /** 字段依赖描述。 */
        InjectionPoint dependency() {
            return dependency;
        }
    }
}
