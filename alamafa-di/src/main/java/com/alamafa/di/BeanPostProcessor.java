package com.alamafa.di;
import com.alamafa.core.ApplicationContext;

/**
 * Bean 创建完成后执行扩展逻辑的回调接口。
 */
@FunctionalInterface
public interface BeanPostProcessor {
    /**
     * 对新创建的 Bean 进行加工，可访问应用上下文。
     */
    void postProcess(Object bean, ApplicationContext context) throws Exception;
}
