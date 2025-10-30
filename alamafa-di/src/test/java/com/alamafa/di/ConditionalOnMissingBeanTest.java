package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.ConditionalOnMissingBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalOnMissingBeanTest {

    @Test
    void skipsBeanWhenTypeAlreadyRegistered() throws Exception {
        ApplicationContext context = new ApplicationContext();
        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(ExistingTypeConfig.class, ConditionalTypeConfig.class)
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        assertEquals(1, registry.definitionsFor(SampleService.class).size());
        SampleService service = registry.get(SampleService.class);
        assertEquals("existing", service.name);

        bootstrap.stop(context);
    }

    @Test
    void registersBeanWhenTypeMissing() throws Exception {
        ApplicationContext context = new ApplicationContext();
        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(ConditionalTypeConfig.class)
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        assertEquals(1, registry.definitionsFor(SampleService.class).size());
        SampleService service = registry.get(SampleService.class);
        assertEquals("fallback", service.name);

        bootstrap.stop(context);
    }

    @Test
    void skipsBeanWhenNameExists() throws Exception {
        ApplicationContext context = new ApplicationContext();
        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(NamedExistingConfig.class, NamedConditionalConfig.class)
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        assertTrue(registry.hasBeanName("customService"));
        assertEquals(1, registry.definitionsFor(NamedService.class).size());
        NamedService service = registry.get(NamedService.class);
        assertEquals("named", service.name);

        bootstrap.stop(context);
    }

    @com.alamafa.di.annotation.Configuration
    static class ExistingTypeConfig {
        @Bean
        SampleService existing() {
            return new SampleService("existing");
        }
    }

    @com.alamafa.di.annotation.Configuration
    static class ConditionalTypeConfig {
        @Bean
        @ConditionalOnMissingBean(SampleService.class)
        SampleService fallback() {
            return new SampleService("fallback");
        }
    }

    @com.alamafa.di.annotation.Configuration
    static class NamedExistingConfig {
        @Bean(name = "customService")
        NamedService namedService() {
            return new NamedService("named");
        }
    }

    @com.alamafa.di.annotation.Configuration
    static class NamedConditionalConfig {
        @Bean
        @ConditionalOnMissingBean(name = "customService")
        NamedService fallback() {
            return new NamedService("fallback");
        }
    }

    static final class SampleService {
        final String name;

        SampleService(String name) {
            this.name = name;
        }
    }

    static final class NamedService {
        final String name;

        NamedService(String name) {
            this.name = name;
        }
    }
}
