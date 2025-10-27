package com.alamafa.helloworld.lifecycle;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLifecycle;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.annotation.Component;
import com.alamafa.helloworld.greeting.GreetingService;

/**
 * 通过 DI 获取问候服务，在 start 阶段输出内容后请求关闭。
 */
@Component
public final class HelloWorldLifecycle implements ApplicationLifecycle {
    private final GreetingService greetingService;
    private final AlamafaLogger logger = LoggerFactory.getLogger(HelloWorldLifecycle.class);

    public HelloWorldLifecycle(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Override
    public void init(ApplicationContext ctx) {
        logger.info("HelloWorldLifecycle init");
    }

    @Override
    public void start(ApplicationContext ctx) {
        greetingService.greet();
        ApplicationShutdown shutdown = ctx.get(ApplicationShutdown.class);
        if (shutdown != null) {
            shutdown.requestShutdown();
        } else {
            logger.warn("ApplicationShutdown bean not available; application will continue running");
        }
    }

    @Override
    public void stop(ApplicationContext ctx) {
        logger.info("HelloWorldLifecycle stop");
    }
}
