package com.alamafa.di.annotation;


import com.alamafa.di.BeanDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个工厂方法，用于生成 Bean。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {
    /** Bean 名称，默认为方法名。 */
    String name() default "";
    /** Bean 作用域，默认单例。 */
    BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;
    /** 是否标记为首选 Bean。 */
    boolean primary() default false;
    /** 是否延迟初始化。 */
    boolean lazy() default false;
}
