package com.alamafa.jfx.view.annotation;

import com.alamafa.di.BeanDefinition;
import com.alamafa.di.annotation.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JavaFX view component. The annotated class will be registered as a DI bean
 * (prototype by default) and can expose additional metadata for view loading.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component(scope = BeanDefinition.Scope.PROTOTYPE)
public @interface FxViewSpec {
    /**
     * Optional bean name override.
     */
    String value() default "";

    /**
     * FXML resource path to load for this view. If empty, the view class is expected to build its UI manually.
     */
    String fxml() default "";

    /**
     * Optional stylesheet resources applied after loading.
     */
    String[] styles() default {};

    /**
     * Optional resource bundle base name for i18n.
     */
    String bundle() default "";

    /**
     * Associated view-model type. Defaults to {@code Object.class} meaning "unspecified".
     */
    Class<?> viewModel() default Object.class;

    /**
     * Whether the view instance can be shared across multiple scenes.
     */
    boolean shared() default false;

    /**
     * Marks this view as the primary view to mount on the application's primary stage.
     */
    boolean primary() default false;

    /**
     * Optional window title when used as a primary or standalone window.
     */
    String title() default "";

    /**
     * Preferred stage width when used as a primary or managed window. Non-positive values are ignored.
     */
    double width() default -1;

    /**
     * Preferred stage height when used as a primary or managed window. Non-positive values are ignored.
     */
    double height() default -1;

    /**
     * Whether the window should be resizable. Only applies when the framework creates the stage.
     */
    boolean resizable() default true;
}
