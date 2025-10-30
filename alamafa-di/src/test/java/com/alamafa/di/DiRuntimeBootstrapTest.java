package com.alamafa.di;

import com.alamafa.config.Configuration;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLifecycle;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.runner.ApplicationRunner;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.PostConstruct;
import com.alamafa.di.annotation.PreDestroy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiRuntimeBootstrapTest {
    private static final List<String> EVENTS = new ArrayList<>();
    private static final List<String> RUNNER_EVENTS = new ArrayList<>();

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

    @Test
    void applicationRunnerExecutesAndTriggersAutoShutdown() throws Exception {
        RUNNER_EVENTS.clear();
        AtomicBoolean shutdownRequested = new AtomicBoolean(false);
        ApplicationContext context = new ApplicationContext();
        context.put(ApplicationArguments.class, new ApplicationArguments("--name=test"));
        context.put(ApplicationShutdown.class, () -> shutdownRequested.set(true));

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(RunnerConfig.class)
                .build();

        bootstrap.init(context);
        bootstrap.start(context);
        bootstrap.stop(context);

        assertEquals(List.of("runner:test"), RUNNER_EVENTS);
        assertTrue(shutdownRequested.get());
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

    @com.alamafa.di.annotation.Configuration
    static class RunnerConfig {
        @Bean
        ApplicationRunner applicationRunner() {
            return (ctx, args) -> {
                String value = args == null ? "" : String.join("", args.asList());
                RUNNER_EVENTS.add("runner:" + value.replace("--name=", ""));
            };
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
