package com.alamafa.di.annotation;


import com.alamafa.di.BeanDefinition;
import com.alamafa.di.annotation.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * {@link Component} 的别名，专门用于插件暴露的服务。
 */
@Documented
@Component(scope = BeanDefinition.Scope.SINGLETON)
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface PluginComponent {
    /** 插件 Bean 名称。 */
    String value() default "";

    /** 默认单例作用域。 */
    BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;

    /** 是否为首选。 */
    boolean primary() default false;

    /** 是否延迟加载。 */
    boolean lazy() default false;
}
