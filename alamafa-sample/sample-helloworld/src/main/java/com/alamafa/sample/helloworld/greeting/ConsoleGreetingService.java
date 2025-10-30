package com.alamafa.sample.helloworld.greeting;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.annotation.Component;

/**
 * 基于日志输出的问候服务，实现所有依赖均通过 Alamafa 容器管理。
 */
@Component
public class ConsoleGreetingService implements GreetingService {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ConsoleGreetingService.class);

    @Override
    public void greet(String target) {
        LOGGER.info("Hello, {}! Welcome to Alamafa.", target);
    }
}

