package com.alamafa.starter;

import com.alamafa.bootstrap.AlamafaBootApplication;
import com.alamafa.bootstrap.BootApplicationParser;
import com.alamafa.di.BeanRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@AlamafaBootApplication
class BasicApp {}

public class LauncherMetadataTest {
    @Test
    void parsesAnnotationDefaults() {
        var descriptor = BootApplicationParser.parse(BasicApp.class);
        assertTrue(descriptor.basePackages().contains(BasicApp.class.getPackageName()));
    }

    @Test
    void runBootstrapsContext() {
        var ctx = AlamafaLauncher.run(BasicApp.class);
        assertNotNull(ctx.get(BeanRegistry.class));
    }
}
