package com.alamafa.sample.helloworld.runner;

import com.alamafa.config.Configuration;
import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.core.runner.ApplicationRunner;
import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.Inject;
import com.alamafa.sample.helloworld.greeting.GreetingService;

/**
 * 使用 ApplicationRunner 在容器就绪后执行问候逻辑。
 */
@Component
public final class GreetingRunner implements ApplicationRunner {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(GreetingRunner.class);

    private final GreetingService greetingService;

    @Inject
    public GreetingRunner(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Override
    public void run(ApplicationContext context, ApplicationArguments arguments) {
        String target = arguments != null ? arguments.getOption("name") : null;
        if (target == null || target.isBlank()) {
            Configuration configuration = context.get(Configuration.class);
            target = configuration == null ? "World" : configuration.get("greeting.target", "World");
        }
        greetingService.greet(target);
        LOGGER.info("ApplicationRunner completed; application will exit automatically.");
    }
}
