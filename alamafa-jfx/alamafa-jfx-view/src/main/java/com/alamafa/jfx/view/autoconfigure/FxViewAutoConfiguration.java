package com.alamafa.jfx.view.autoconfigure;

import com.alamafa.core.ApplicationContext;
import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.Configuration;
import com.alamafa.jfx.view.FxViewLoader;
import com.alamafa.jfx.view.ResourceResolver;
import com.alamafa.jfx.view.meta.FxViewMetadataProcessor;
import com.alamafa.jfx.view.meta.FxViewRegistry;

/**
 * Registers core beans required for annotation-driven view handling.
 */
@AutoConfiguration
public class FxViewAutoConfiguration {

    @Bean
    public FxViewRegistry fxViewRegistry() {
        return new FxViewRegistry();
    }

    @Bean
    public FxViewLoader fxViewLoader(ApplicationContext context, FxViewRegistry registry) {
        return new FxViewLoader(context, registry, ResourceResolver.classpath(), () -> null);
    }

    @Bean
    public FxViewMetadataProcessor fxViewMetadataProcessor(FxViewRegistry registry) {
        return new FxViewMetadataProcessor(registry);
    }

}
