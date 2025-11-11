package com.alamafa.theme;

import java.util.Objects;

public final class ThemeDefinition {
    private final Theme theme;
    private final String stylesheet;

    public ThemeDefinition(Theme theme, String stylesheet) {
        this.theme = Objects.requireNonNull(theme, "theme");
        this.stylesheet = Objects.requireNonNull(stylesheet, "stylesheet");
    }

    public Theme theme() {
        return theme;
    }

    public String stylesheet() {
        return stylesheet;
    }
}
