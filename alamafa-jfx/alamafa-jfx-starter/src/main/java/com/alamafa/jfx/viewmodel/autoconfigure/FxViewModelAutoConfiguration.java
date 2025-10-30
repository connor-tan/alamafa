package com.alamafa.jfx.viewmodel.autoconfigure;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.di.annotation.Bean;
import com.alamafa.jfx.view.FxViewLoader;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import com.alamafa.jfx.viewmodel.FxViewModelBinder;
import com.alamafa.jfx.viewmodel.lifecycle.FxPrimaryStageLifecycle;
import com.alamafa.jfx.viewmodel.meta.FxViewModelMetadataProcessor;
import com.alamafa.jfx.viewmodel.meta.FxViewModelRegistry;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;

@AutoConfiguration
public class FxViewModelAutoConfiguration {

    @Bean
    public FxViewModelRegistry fxViewModelRegistry() {
        return new FxViewModelRegistry();
    }

    @Bean
    public FxViewModelMetadataProcessor fxViewModelMetadataProcessor(FxViewModelRegistry registry) {
        return new FxViewModelMetadataProcessor(registry);
    }

    @Bean
    public FxViewModelBinder fxViewModelBinder(ApplicationContext context,
                                               BeanRegistry registry,
                                               FxViewRegistry viewRegistry,
                                               FxViewModelRegistry viewModelRegistry) {
        return new FxViewModelBinder(context, registry, viewRegistry, viewModelRegistry);
    }

    @Bean
    public FxWindowManager fxWindowManager(ApplicationContext context,
                                           FxViewLoader viewLoader,
                                           FxViewModelBinder binder,
                                           FxViewRegistry viewRegistry) {
        return new FxWindowManager(context, viewLoader, binder, viewRegistry);
    }

    @Bean
    public FxPrimaryStageLifecycle fxPrimaryStageLifecycle(FxWindowManager windowManager) {
        return new FxPrimaryStageLifecycle(windowManager);
    }
}
