package com.alamafa.di.internal;

import com.alamafa.config.Configuration;
import com.alamafa.config.ConfigurationBinder;
import com.alamafa.config.ConfigurationProperties;
import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanPostProcessor;

/**
 * 在 Bean 创建后，根据 @ConfigurationProperties 将配置绑定到实例。
 */
public final class ConfigurationPropertiesBinderPostProcessor implements BeanPostProcessor {

    @Override
    public void postProcess(Object bean, ApplicationContext context) {
        if (bean == null) {
            return;
        }
        ConfigurationProperties properties = bean.getClass().getAnnotation(ConfigurationProperties.class);
        if (properties == null) {
            return;
        }
        Configuration configuration = context.get(Configuration.class);
        if (configuration == null) {
            return;
        }
        ConfigurationBinder.bind(configuration, properties.prefix(), bean);
    }
}
