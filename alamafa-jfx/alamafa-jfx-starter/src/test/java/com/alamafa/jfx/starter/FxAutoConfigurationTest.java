package com.alamafa.jfx.starter;

import com.alamafa.bootstrap.autoconfigure.AutoConfigurationLoader;
import com.alamafa.jfx.view.autoconfigure.FxViewAutoConfiguration;
import com.alamafa.jfx.viewmodel.autoconfigure.FxViewModelAutoConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FxAutoConfigurationTest {

    @Test
    void starterExposesAllJavaFxAutoConfigurations() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> autoConfigurations = AutoConfigurationLoader.loadAutoConfigurations(classLoader);

        assertTrue(autoConfigurations.contains(FxViewAutoConfiguration.class.getName()),
                "FxViewAutoConfiguration should be discoverable via starter");
        assertTrue(autoConfigurations.contains(FxViewModelAutoConfiguration.class.getName()),
                "FxViewModelAutoConfiguration should be discoverable via starter");
    }
}
