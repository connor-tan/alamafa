package com.alamafa.bootstrap;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.runner.ApplicationRunner;
import com.alamafa.di.annotation.Bean;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AlamafaApplicationTest {
    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();
    private static volatile List<String> receivedArgs = List.of();

    @Test
    void runBootstrapsApplicationRunnerAndArguments() {
        EVENTS.clear();
        receivedArgs = List.of();
        AlamafaApplication.run(TestApplication.class, "--mode=test", "--flag");

        assertEquals(List.of("runner"), EVENTS);
        assertEquals(List.of("--mode=test", "--flag"), receivedArgs);
    }

    @AlamafaBootApplication
    static class TestApplication {
    }

    @AutoConfiguration
    static class AutoConfig {
        @Bean
        ApplicationRunner runner() {
            return (context, arguments) -> {
                EVENTS.add("runner");
                assertNotNull(arguments, "application arguments should be registered");
                receivedArgs = arguments.asList();
                ApplicationShutdown shutdown = context.get(ApplicationShutdown.class);
                assertNotNull(shutdown, "shutdown access should be available");
            };
        }
    }
}
