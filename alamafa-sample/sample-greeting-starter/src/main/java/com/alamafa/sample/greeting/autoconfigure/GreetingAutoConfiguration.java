package com.alamafa.sample.greeting.autoconfigure;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.events.ApplicationEventListener;
import com.alamafa.core.events.ApplicationStartedEvent;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.core.runner.ApplicationRunner;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.ConditionalOnMissingBean;
import com.alamafa.di.annotation.ConditionalOnProperty;
import com.alamafa.sample.greeting.ConsoleGreetingService;
import com.alamafa.sample.greeting.GreetingProperties;
import com.alamafa.sample.greeting.GreetingService;

/**
 * 自动配置问候服务与应用启动 runner。
 */
@AutoConfiguration
public class GreetingAutoConfiguration {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(GreetingAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(GreetingService.class)
    public GreetingService greetingService() {
        return new ConsoleGreetingService();
    }

    @Bean
    @ConditionalOnMissingBean(GreetingProperties.class)
    public GreetingProperties greetingProperties() {
        return new GreetingProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner greetingRunner(GreetingService greetingService, GreetingProperties properties) {
        return new GreetingRunner(greetingService, properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "greetingStartedListener")
    public ApplicationEventListener<ApplicationStartedEvent> greetingStartedListener(GreetingProperties properties) {
        return new ApplicationEventListener<>() {
            @Override
            public void onEvent(ApplicationStartedEvent event) {
                if (properties.isEnabled()) {
                    LOGGER.info("Greeting target '{}' ready after startup", properties.getTarget());
                }
            }

            @Override
            public Class<ApplicationStartedEvent> getEventType() {
                return ApplicationStartedEvent.class;
            }
        };
    }

    private static final class GreetingRunner implements ApplicationRunner {
        private final GreetingService greetingService;
        private final GreetingProperties properties;

        private GreetingRunner(GreetingService greetingService, GreetingProperties properties) {
            this.greetingService = greetingService;
            this.properties = properties;
        }

        @Override
        public void run(ApplicationContext context, ApplicationArguments arguments) {
            if (!properties.isEnabled()) {
                return;
            }
            String target = properties.getTarget();
            if (arguments != null) {
                String cliTarget = arguments.getOption("name");
                if (cliTarget == null || cliTarget.isBlank()) {
                    cliTarget = arguments.getOption("target");
                }
                if (cliTarget != null && !cliTarget.isBlank()) {
                    target = cliTarget;
                }
            }
            greetingService.greet(target);
        }

        @Override
        public int getOrder() {
            return Integer.MIN_VALUE; // ensure greeting runs early
        }
    }
}
