package com.alamafa.di;


import com.alamafa.core.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 将多个 {@link BeanPostProcessor} 串联执行的帮助类。
 */
final class BeanPostProcessorChain {
    private final List<BeanPostProcessor> processors = new ArrayList<>();

    /** 追加一个后处理器。 */
    void add(BeanPostProcessor processor) {
        if (!processors.contains(processor)) {
            processors.add(processor);
        }
    }

    /** 在链首插入一个后处理器。 */
    void addFirst(BeanPostProcessor processor) {
        if (!processors.contains(processor)) {
            processors.add(0, processor);
        }
    }

    /**
     * 依次执行所有后处理器。
     */
    void apply(Object bean, ApplicationContext context) throws Exception {
        for (BeanPostProcessor processor : processors) {
            processor.postProcess(bean, context);
        }
    }
}
