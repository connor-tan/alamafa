package com.alamafa.jfx.viewmodel.annotation;

import com.alamafa.di.BeanDefinition;
import com.alamafa.di.annotation.Component;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a JavaFX ViewModel managed by Alamafa DI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component(scope = BeanDefinition.Scope.PROTOTYPE)
public @interface FxViewModelSpec {
    /**
     * Optional bean name override.
     */
    String value() default "";

    /**
     * Whether this view model should be created lazily.
     */
    boolean lazy() default false;

    /**
     * Defines the scope in which the view-model instance should live.
     */
    FxViewModelScope scope() default FxViewModelScope.VIEW;
}
