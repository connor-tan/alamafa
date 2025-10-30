package com.alamafa.sample.greeting;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

/**
 * 默认的控制台问候实现，通过日志输出欢迎信息。
 */
public class ConsoleGreetingService implements GreetingService {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ConsoleGreetingService.class);

    @Override
    public void greet(String target) {
        LOGGER.info("Hello, {}! Welcome to Alamafa.", target);
    }
}
