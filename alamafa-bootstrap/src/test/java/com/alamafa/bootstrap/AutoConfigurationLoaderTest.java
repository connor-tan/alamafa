package com.alamafa.bootstrap;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.bootstrap.autoconfigure.AutoConfigurationLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoConfigurationLoaderTest {

    @Test
    void loadsAutoConfigurationsUsingCanonicalKey() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> configurations = AutoConfigurationLoader.loadAutoConfigurations(classLoader);

        assertTrue(configurations.contains(AlamafaApplicationTest.AutoConfig.class.getName()),
                "should include auto configuration discovered via canonical key");
        assertTrue(configurations.contains(AnnotatedConfig.class.getName()),
                "should include additional auto configuration entry");
    }

    @AutoConfiguration
    static class AnnotatedConfig {
    }
}
