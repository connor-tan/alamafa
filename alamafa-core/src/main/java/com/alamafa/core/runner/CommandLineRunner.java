package com.alamafa.core.runner;

import com.alamafa.core.ApplicationArguments;

/**
 * 简化版的 runner，只接受命令行参数数组。
 */
@FunctionalInterface
public interface CommandLineRunner {

    /**
     * 当容器完成启动后执行。
     */
    void run(String... args) throws Exception;

    /**
     * 控制执行顺序，数值越小越先执行。
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 辅助方法，帮助实现类使用 {@link ApplicationArguments} 数据。
     */
    default void run(ApplicationArguments arguments) throws Exception {
        run(arguments == null ? new String[0] : arguments.toArray());
    }
}

