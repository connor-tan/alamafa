package com.alamafa.sample.greeting;

/**
 * 简单的问候服务接口，可由业务自定义实现。
 */
public interface GreetingService {
    void greet(String target);
}
