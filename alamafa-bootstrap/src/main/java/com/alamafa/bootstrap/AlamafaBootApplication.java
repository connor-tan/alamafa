package com.alamafa.bootstrap;

import com.alamafa.core.ContextAwareApplicationLauncher;
import com.alamafa.core.DefaultApplicationLauncher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares application-level metadata used to bootstrap runtime modules.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AlamafaBootApplication {
    String[] scanBasePackages() default {};

    Class<?>[] scanBasePackageClasses() default {};

    Class<?>[] modules() default {};

    Class<? extends ContextAwareApplicationLauncher> launcher() default DefaultApplicationLauncher.class;
}
