package com.alamafa.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明配置绑定前缀，将 {@link Configuration} 中的键值映射到对象属性。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationProperties {

    /** 绑定的属性前缀，可为空。 */
    String prefix() default "";
}

