package com.alamafa.sample.helloworld.greeting;

/**
 * 简单的问候服务接口，用于演示依赖注入功能。
 */
public interface GreetingService {

    /**
     * 对目标对象输出问候语。
     */
    void greet(String target);
}

