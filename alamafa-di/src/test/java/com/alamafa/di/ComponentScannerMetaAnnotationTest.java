package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.ConditionalOnProperty;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentScannerMetaAnnotationTest {

    @Test
    void resolvesMetaAnnotationsTransitively() {
        ApplicationContext context = new ApplicationContext();
        context.put("test.meta.component", "true");
        BeanRegistry registry = new BeanRegistry(context);

        registry.scanPackages(getClass().getPackageName());
        registry.registerConfigurations();

        assertTrue(MetaComponent.class.getAnnotation(SecondLevelComponent.class).primary());
        assertCandidatePrimary();
        List<BeanDefinition<?>> definitions = registry.definitionsFor(MetaComponent.class);
        assertEquals(1, definitions.size());
        BeanDefinition<?> definition = definitions.get(0);
        assertTrue(definition.primary(), "meta component should be primary");
        assertEquals(BeanDefinition.Scope.PROTOTYPE, definition.scope(), "scope override should be honoured");
        assertTrue(definition.lazy(), "lazy override should be honoured");

        MetaComponent component = registry.get(MetaComponent.class);
        assertEquals("meta", component.name());
    }

    interface MetaContract {
        String name();
    }

    @SecondLevelComponent(scope = BeanDefinition.Scope.PROTOTYPE, primary = true, lazy = true)
    @ConditionalOnProperty(prefix = "test", name = "meta.component", havingValue = "true")
    static class MetaComponent implements MetaContract {
        @Override
        public String name() {
            return "meta";
        }
    }

    @Component
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    public @interface FirstLevelComponent {
        BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;
        boolean primary() default false;
        boolean lazy() default false;
    }

    @FirstLevelComponent
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    public @interface SecondLevelComponent {
        BeanDefinition.Scope scope() default BeanDefinition.Scope.SINGLETON;
        boolean primary() default false;
        boolean lazy() default false;
    }

    private void assertCandidatePrimary() {
        try {
            Class<?> scannerClass = Class.forName("com.alamafa.di.internal.ComponentScanner");
            Annotation secondLevel = MetaComponent.class.getAnnotation(SecondLevelComponent.class);
            Method direct = secondLevel.annotationType().getDeclaredMethod("primary");
            direct.setAccessible(true);
            Object directValue = direct.invoke(secondLevel);
            assertTrue(directValue instanceof Boolean && Boolean.TRUE.equals(directValue));
            Method extractBoolean = scannerClass.getDeclaredMethod("extractBooleanAttribute", Annotation.class, String.class);
            extractBoolean.setAccessible(true);
            Object optionalPrimary = extractBoolean.invoke(null, secondLevel, "primary");
            assertTrue(optionalPrimary instanceof java.util.Optional<?> opt && opt.isPresent() && Boolean.TRUE.equals(opt.get()));
            java.lang.reflect.Constructor<?> constructor = scannerClass.getDeclaredConstructor(ClassLoader.class);
            constructor.setAccessible(true);
            Object scanner = constructor.newInstance(Thread.currentThread().getContextClassLoader());
            Method scan = scannerClass.getDeclaredMethod("scan", String.class);
            scan.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<?> candidates = (Set<?>) scan.invoke(scanner, getClass().getPackageName());
            Object candidate = candidates.stream()
                .filter(c -> {
                    try {
                        Method type = c.getClass().getDeclaredMethod("type");
                        type.setAccessible(true);
                        return type.invoke(c) == MetaComponent.class;
                    } catch (ReflectiveOperationException ex) {
                        throw new AssertionError("Failed to inspect candidate", ex);
                    }
                })
                .findFirst()
                .orElseThrow();
            Method primary = candidate.getClass().getDeclaredMethod("primary");
            primary.setAccessible(true);
            Boolean value = (Boolean) primary.invoke(candidate);
            assertTrue(Boolean.TRUE.equals(value));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to inspect candidate metadata", ex);
        }
    }
}
