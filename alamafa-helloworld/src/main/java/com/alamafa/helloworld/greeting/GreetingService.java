package com.alamafa.helloworld.greeting;

import com.alamafa.config.Configuration;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.annotation.Component;

/**
 * 使用配置与模板生成最终问候语并输出。
 */
@Component
public final class GreetingService {
    private final Configuration configuration;
    private final GreetingTemplate template;
    private final AlamafaLogger logger = LoggerFactory.getLogger(GreetingService.class);

    public GreetingService(Configuration configuration, GreetingTemplate template) {
        this.configuration = configuration;
        this.template = template;
    }

    public String greetingMessage() {
        String target = configuration.get("hello.target").orElse("Alamafa");
        String punctuation = configuration.get("hello.punctuation").orElse("!");
        return template.render(target, punctuation);
    }

    public void greet() {
        logger.info(greetingMessage());
    }
}
