package com.alamafa.di.annotation;


import com.alamafa.di.BeanDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记类为可通过包扫描发现的 DI 管理组件。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Component {
    /**
     * 可选的显式 Bean 名称。
     */
    String value() default "";

    /**
     * 指定组件的作用域，默认单例。
     */
    BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;

    /**
     * 当存在多个同类型 Bean 时标记为首选，预留给后续限定符逻辑。
     */
    boolean primary() default false;

    /**
     * 是否延迟初始化，后续可用于性能优化。
     */
    boolean lazy() default false;
}
