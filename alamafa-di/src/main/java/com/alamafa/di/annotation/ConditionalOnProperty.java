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
    /** 配置 key 名称。 */
    String name();
    /** 期望的属性值，留空时仅判断存在。 */
    String havingValue() default "";
    /** 属性缺失时是否视为匹配。 */
    boolean matchIfMissing() default false;
}
