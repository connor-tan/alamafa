package com.alamafa.bootstrap;

import com.alamafa.core.ApplicationBootstrap;
import com.alamafa.core.DefaultApplicationLauncher;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlamafaBootstrapContextTest {

    @Test
    void errorMessageMentionsAutoConfiguration() {
        ApplicationBootstrap bootstrap = new ApplicationBootstrap(new DefaultApplicationLauncher());
        AlamafaBootstrapContext context = new AlamafaBootstrapContext(
                bootstrap,
                bootstrap.getContext(),
                getClass(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> context.addConfiguration(String.class));
        assertTrue(ex.getMessage().contains("@AutoConfiguration"));
    }
}
