package com.alamafa.di;

import com.alamafa.config.Configuration;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLifecycle;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.PostConstruct;
import com.alamafa.di.annotation.PreDestroy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiRuntimeBootstrapTest {
    private static final List<String> EVENTS = new ArrayList<>();

    @Test
    void applicationLifecycleBeansRunInOrder() throws Exception {
        EVENTS.clear();
        ApplicationContext context = new ApplicationContext();

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(TestConfig.class)
                .build();

        bootstrap.init(context);
        bootstrap.start(context);
        bootstrap.stop(context);

        List<String> snapshot = new ArrayList<>(EVENTS);
        List<String> expectedCore = List.of(
                "beta-init",
                "alpha-init",
                "beta-start",
                "alpha-start",
                "alpha-stop",
                "beta-stop"
        );
        assertTrue(snapshot.size() >= expectedCore.size() + 2, "events=" + snapshot);
        List<String> postConstructs = snapshot.subList(0, 2);
        assertTrue(postConstructs.containsAll(List.of("alpha-postConstruct", "beta-postConstruct")),
                "postConstructs=" + postConstructs);
        assertEquals(expectedCore, snapshot.subList(2, 2 + expectedCore.size()));
        assertTrue(snapshot.contains("alpha-preDestroy"));
        assertTrue(snapshot.contains("beta-preDestroy"));

        assertNotNull(context.get(Configuration.class));
        assertNotNull(context.get(BeanRegistry.class));
    }

    @com.alamafa.di.annotation.Configuration
    static class TestConfig {
        @Bean
        AlphaLifecycle alphaLifecycle() {
            return new AlphaLifecycle();
        }

        @Bean
        BetaLifecycle betaLifecycle() {
            return new BetaLifecycle();
        }
    }

    static abstract class AbstractLifecycle implements ApplicationLifecycle {
        private final String name;
        private final int order;

        AbstractLifecycle(String name, int order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public void init(ApplicationContext ctx) {
            EVENTS.add(name + "-init");
        }

        @Override
        public void start(ApplicationContext ctx) {
            EVENTS.add(name + "-start");
        }

        @Override
        public void stop(ApplicationContext ctx) {
            EVENTS.add(name + "-stop");
        }

        @PostConstruct
        void postConstruct() {
            EVENTS.add(name + "-postConstruct");
        }

        @PreDestroy
        void preDestroy() {
            EVENTS.add(name + "-preDestroy");
        }
    }

    static final class AlphaLifecycle extends AbstractLifecycle {
        AlphaLifecycle() {
            super("alpha", 5);
        }
    }

    static final class BetaLifecycle extends AbstractLifecycle {
        BetaLifecycle() {
            super("beta", 1);
        }
    }
}
