package com.alamafa.theme;

import com.alamafa.config.ConfigurationProperties;

@ConfigurationProperties(prefix = "theme")
public class ThemeProperties {
    private Theme defaultTheme = Theme.DARK;

    public Theme getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(Theme defaultTheme) {
        this.defaultTheme = defaultTheme;
    }
}
