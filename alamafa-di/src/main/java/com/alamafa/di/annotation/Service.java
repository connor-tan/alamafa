package com.alamafa.di.annotation;

import com.alamafa.di.BeanDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记业务服务组件，等价于 {@link Component} 的派生注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Service {
    /**
     * 可选的显式 Bean 名称。
     */
    String value() default "";

    /**
     * 指定作用域，默认单例。
     */
    BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;

    /**
     * 当存在多个候选时指定首选项。
     */
    boolean primary() default false;

    /**
     * 是否延迟初始化。
     */
    boolean lazy() default false;
}

