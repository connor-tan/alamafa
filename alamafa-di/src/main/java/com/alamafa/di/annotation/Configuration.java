package com.alamafa.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明一个配置类，可向 {@code BeanRegistry} 注册 Bean。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    /**
     * 需要额外扫描的基础包，用于发现更多 {@link Configuration}。
     */
    String[] scanBasePackages() default {};

    /**
     * 手动导入的配置类列表。
     */
    Class<?>[] imports() default {};
}
