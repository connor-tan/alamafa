package com.alamafa.di;

import com.alamafa.config.Configuration;
import com.alamafa.config.ConfigurationLoader;
import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.ConditionalOnProperty;
import com.alamafa.di.BeanRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionalOnPropertyTest {

    @Test
    void registersBeanWhenPropertyMatches() throws Exception {
        ApplicationContext context = contextWithProperties(Map.of("feature.enabled", "true"));

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(FeatureConfiguration.class)
                .build();

        bootstrap.init(context);

        FeatureService service = context.get(BeanRegistry.class).get(FeatureService.class);
        assertEquals("enabled", service.flag);

        bootstrap.stop(context);
    }

    @Test
    void skipsBeanWhenPropertyMissing() throws Exception {
        ApplicationContext context = contextWithProperties(Map.of());

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(FeatureConfiguration.class)
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        assertThrows(IllegalArgumentException.class, () -> registry.get(FeatureService.class));

        bootstrap.stop(context);
    }

    private ApplicationContext contextWithProperties(Map<String, String> properties) {
        ApplicationContext context = new ApplicationContext();
        Configuration configuration = ConfigurationLoader.create()
                .addProperties(properties)
                .load();
        context.put(Configuration.class, configuration);
        return context;
    }

    @com.alamafa.di.annotation.Configuration
    static class FeatureConfiguration {
        @Bean
        @ConditionalOnProperty(prefix = "feature", name = {"enabled"}, havingValue = "true")
        FeatureService service() {
            return new FeatureService("enabled");
        }
    }

    static final class FeatureService {
        final String flag;

        FeatureService(String flag) {
            this.flag = flag;
        }
    }
}
