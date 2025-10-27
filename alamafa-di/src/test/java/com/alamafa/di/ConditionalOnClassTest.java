package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.ConditionalOnClass;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.di.annotation.Bean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalOnClassTest {

    @Test
    void componentConditionalOnClass() throws Exception {
        ApplicationContext context = new ApplicationContext();
        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .scanPackages(getClass().getPackageName())
                .withConfigurations(TestConfig.class)
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        assertTrue(registry.hasBeanDefinition(PresentComponent.class));
        assertThrows(IllegalArgumentException.class, () -> registry.get(MissingComponent.class));
        assertTrue(registry.definition(ConditionalBean.class).isEmpty());
    }

    @Component
    @ConditionalOnClass("java.util.Random")
    static class PresentComponent { }

    @Component
    @ConditionalOnClass("com.example.MissingClass")
    static class MissingComponent { }

    @Configuration
    static class TestConfig {
        @Bean
        @ConditionalOnClass("com.example.StillMissing")
        ConditionalBean conditionalBean() {
            return new ConditionalBean();
        }
    }

    static class ConditionalBean { }
}
