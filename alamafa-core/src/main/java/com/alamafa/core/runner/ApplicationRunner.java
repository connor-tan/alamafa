package com.alamafa.core.runner;

import com.alamafa.core.ApplicationArguments;
import com.alamafa.core.ApplicationContext;

/**
 * 在应用上下文完全就绪后触发的回调接口，类似 Spring Boot 的 ApplicationRunner。
 */
@FunctionalInterface
public interface ApplicationRunner {

    /**
     * 当容器完成启动后执行。
     */
    void run(ApplicationContext context, ApplicationArguments arguments) throws Exception;

    /**
     * 控制执行顺序，数值越小越先执行。
     */
    default int getOrder() {
        return 0;
    }
}

