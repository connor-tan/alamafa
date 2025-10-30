package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.di.annotation.ConditionalOnProperty;
import com.alamafa.di.annotation.Qualifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanMethodInjectionTest {

    @Test
    void resolvesCollectionsQualifiersAndOptionalParameters() {
        ApplicationContext context = new ApplicationContext();
        BeanRegistry registry = new BeanRegistry(context);
        context.put("test.bean.method", "true");

        BeanDefinition<SampleService> alphaDefinition = new BeanDefinition<>(SampleService.class, AlphaService::new,
                BeanDefinition.Scope.SINGLETON, true, false);
        BeanDefinition<SampleService> betaDefinition = new BeanDefinition<>(SampleService.class, BetaService::new,
                BeanDefinition.Scope.SINGLETON, false, false);
        registry.register(SampleService.class, alphaDefinition);
        registry.register("alphaService", alphaDefinition);
        registry.register(SampleService.class, betaDefinition);
        registry.register("betaService", betaDefinition);

        registry.registerConfigurations(BeanMethodConfiguration.class);

        SampleAggregator aggregator = registry.get(SampleAggregator.class);
        assertEquals(2, aggregator.services().size());
        assertEquals("alpha", aggregator.alpha().name());
        assertTrue(aggregator.missing().isEmpty());
    }

    interface SampleService {
        String name();
    }

    static class AlphaService implements SampleService {
        @Override
        public String name() {
            return "alpha";
        }
    }

    static class BetaService implements SampleService {
        @Override
        public String name() {
            return "beta";
        }
    }

    static class SampleAggregator {
        private final List<SampleService> services;
        private final SampleService alpha;
        private final Optional<MissingService> missing;

        SampleAggregator(List<SampleService> services,
                         SampleService alpha,
                         Optional<MissingService> missing) {
            this.services = services;
            this.alpha = alpha;
            this.missing = missing;
        }

        List<SampleService> services() {
            return services;
        }

        SampleService alpha() {
            return alpha;
        }

        Optional<MissingService> missing() {
            return missing;
        }
    }

    interface MissingService { }

    @Configuration
    @ConditionalOnProperty(prefix = "test", name = "bean.method", havingValue = "true")
    static class BeanMethodConfiguration {
        @Bean
        SampleAggregator sampleAggregator(List<SampleService> services,
                                          @Qualifier("alphaService") SampleService alpha,
                                          Optional<MissingService> missing) {
            return new SampleAggregator(services, alpha, missing);
        }
    }
}
