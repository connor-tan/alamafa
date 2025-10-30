package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.di.annotation.ConditionalOnProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigurationConstructorInjectionTest {

    @Test
    void instantiatesConfigurationWithInjectedDependencies() {
        ApplicationContext context = new ApplicationContext();
        BeanRegistry registry = new BeanRegistry(context);
        context.put("test.constructor.config", "true");
        registry.register(SampleService.class,
                new BeanDefinition<>(SampleService.class, SampleService::new,
                        BeanDefinition.Scope.SINGLETON, true, false));

        registry.registerConfigurations(ConstructorInjectedConfiguration.class);

        SampleConsumer consumer = registry.get(SampleConsumer.class);
        assertNotNull(consumer);
        assertSame(context, consumer.context());
        assertSame(registry, consumer.registry());
        assertSame(registry.get(SampleService.class), consumer.service());
        assertEquals("service", consumer.service().name());
    }

    static class SampleService {
        String name() {
            return "service";
        }
    }

    static class SampleConsumer {
        private final SampleService service;
        private final ApplicationContext context;
        private final BeanRegistry registry;

        SampleConsumer(SampleService service, ApplicationContext context, BeanRegistry registry) {
            this.service = service;
            this.context = context;
            this.registry = registry;
        }

        SampleService service() {
            return service;
        }

        ApplicationContext context() {
            return context;
        }

        BeanRegistry registry() {
            return registry;
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "test", name = "constructor.config", havingValue = "true")
    static class ConstructorInjectedConfiguration {
        private final SampleService service;
        private final ApplicationContext context;
        private final BeanRegistry registry;

        ConstructorInjectedConfiguration(SampleService service,
                                         ApplicationContext context,
                                         BeanRegistry registry) {
            this.service = service;
            this.context = context;
            this.registry = registry;
        }

        @Bean
        SampleConsumer sampleConsumer() {
            return new SampleConsumer(service, context, registry);
        }
    }
}
