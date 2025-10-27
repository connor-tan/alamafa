package com.alamafa.di.internal;


import com.alamafa.di.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 表示扫描阶段发现的组件候选信息。
 */
final class ComponentCandidate {
    private final Class<?> type;
    private final String beanName;
    private final BeanDefinition.Scope scope;
    private final boolean primary;
    private final boolean lazy;
    private final Class<? extends Annotation> stereotype;
    private final boolean sharedView;

    ComponentCandidate(Class<?> type,
                       String beanName,
                       BeanDefinition.Scope scope,
                       boolean primary,
                       boolean lazy,
                       Class<? extends Annotation> stereotype,
                       boolean sharedView) {
        this.type = Objects.requireNonNull(type, "type");
        this.beanName = beanName;
        this.scope = Objects.requireNonNull(scope, "scope");
        this.primary = primary;
        this.lazy = lazy;
        this.stereotype = stereotype;
        this.sharedView = sharedView;
    }

    /** 返回候选类。 */
    Class<?> type() {
        return type;
    }

    /** Bean 名称。 */
    String beanName() {
        return beanName;
    }

    /** 作用域信息。 */
    BeanDefinition.Scope scope() {
        return scope;
    }

    /** 是否首选。 */
    boolean primary() {
        return primary;
    }

    /** 是否延迟加载。 */
    boolean lazy() {
        return lazy;
    }

    /** 返回原始的立场注解类型。 */
    Class<? extends Annotation> stereotype() {
        return stereotype;
    }

    /** 对于视图，是否共享实例。 */
    boolean sharedView() {
        return sharedView;
    }
}
