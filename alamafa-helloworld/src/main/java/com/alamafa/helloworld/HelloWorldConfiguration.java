package com.alamafa.helloworld;

import com.alamafa.di.BeanDefinition;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.helloworld.greeting.GreetingTemplate;

/**
 * 声明 HelloWorld 示例需要的 Bean。
 */
@Configuration
public class HelloWorldConfiguration {

    /**
     * 提供 greeting 模板 Bean，展示 @Bean 的注册方式。
     */
    @Bean(scope = BeanDefinition.Scope.SINGLETON)
    public GreetingTemplate greetingTemplate() {
        return new GreetingTemplate("Hello, %s%s");
    }
}
