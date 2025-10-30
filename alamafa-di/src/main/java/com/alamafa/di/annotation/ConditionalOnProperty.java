package com.alamafa.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当配置属性满足特定值时才装配 Bean 的条件注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnProperty {
    /**
     * 属性名前缀，可选。
     */
    String prefix() default "";

    /**
     * 属性名称列表，与 {@link #name()} 等价写法。
     */
    String[] value() default {};

    /**
     * 属性名称列表（兼容写法）。
     */
    String[] name() default {};

    /**
     * 期望的属性值，留空时仅判断存在与否。
     */
    String havingValue() default "";

    /**
     * 属性缺失时是否视为匹配。
     */
    boolean matchIfMissing() default false;
}
