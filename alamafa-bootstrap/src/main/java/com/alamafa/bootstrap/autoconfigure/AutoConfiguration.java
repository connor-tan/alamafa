package com.alamafa.bootstrap.autoconfigure;

import com.alamafa.di.annotation.Configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记类为自动配置候选，类似 Spring Boot 的 @AutoConfiguration。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration
public @interface AutoConfiguration {
}

