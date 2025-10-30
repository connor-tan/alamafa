package com.alamafa.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当容器中不存在指定 Bean 时才装配的条件注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnMissingBean {
    /**
     * 以类型形式指定需要检查的 Bean。
     */
    Class<?>[] value() default {};

    /**
     * 以名称形式指定需要检查的 Bean。
     */
    String[] name() default {};
}

