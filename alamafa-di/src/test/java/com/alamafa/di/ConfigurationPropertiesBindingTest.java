package com.alamafa.di;

import com.alamafa.config.Configuration;
import com.alamafa.config.ConfigurationLoader;
import com.alamafa.config.ConfigurationProperties;
import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Component;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationPropertiesBindingTest {

    @Test
    void bindsConfigurationPropertiesComponent() throws Exception {
        ApplicationContext context = new ApplicationContext();
        Configuration configuration = ConfigurationLoader.create()
                .addProperties(Map.of(
                        "demo.title", "Alamafa",
                        "demo.enabled", "true"
                ))
                .load();
        context.put(Configuration.class, configuration);

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .scanPackages(getClass().getPackageName())
                .build();

        bootstrap.init(context);

        DemoProperties properties = context.get(BeanRegistry.class).get(DemoProperties.class);
        assertEquals("Alamafa", properties.getTitle());
        assertEquals(true, properties.isEnabled());

        bootstrap.stop(context);
    }

    @Component
    @ConfigurationProperties(prefix = "demo")
    static class DemoProperties {
        private String title;
        private boolean enabled;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

