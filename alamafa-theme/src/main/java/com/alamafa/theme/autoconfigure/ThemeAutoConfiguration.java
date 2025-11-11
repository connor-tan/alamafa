package com.alamafa.theme.autoconfigure;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Bean;
import com.alamafa.theme.ThemeManager;
import com.alamafa.theme.ThemeProperties;

@AutoConfiguration
public class ThemeAutoConfiguration {

    @Bean
    public ThemeProperties themeProperties() {
        return new ThemeProperties();
    }

    @Bean
    public ThemeManager themeManager(ApplicationContext context, ThemeProperties themeProperties) {
        return new ThemeManager(context, themeProperties);
    }
}
