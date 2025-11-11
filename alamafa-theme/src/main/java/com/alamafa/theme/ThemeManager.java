package com.alamafa.theme;

import com.alamafa.core.ApplicationContext;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;

public final class ThemeManager {
    private final ApplicationContext context;
    private final Map<Theme, ThemeDefinition> definitions;
    private final WeakHashMap<Scene, Boolean> scenes = new WeakHashMap<>();
    private static final String KEY_SELECTED_THEME = "selectedTheme";
    private final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);
    private Theme currentTheme;

    public ThemeManager(ApplicationContext context, ThemeProperties properties) {
        this.context = Objects.requireNonNull(context, "context");
        this.definitions = new EnumMap<>(Theme.class);
        registerDefaults();
        Theme defaultTheme = properties.getDefaultTheme();
        Theme persisted = readPersistedTheme().orElse(null);
        Theme resolved = persisted != null ? persisted : defaultTheme;
        this.currentTheme = resolved == null ? Theme.DARK : resolved;
    }

    private Optional<Theme> readPersistedTheme() {
        String stored = preferences.get(KEY_SELECTED_THEME, null);
        if (stored == null || stored.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Theme.valueOf(stored.trim()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private void registerDefaults() {
        definitions.put(Theme.LIGHT, new ThemeDefinition(Theme.LIGHT, "/themes/light.css"));
        definitions.put(Theme.DARK, new ThemeDefinition(Theme.DARK, "/themes/dark.css"));
    }

    public void apply(Theme theme, Scene scene) {
        Objects.requireNonNull(scene, "scene");
        ThemeDefinition definition = definitions.get(theme);
        if (definition == null) {
            throw new IllegalArgumentException("Theme " + theme + " not registered");
        }
        Runnable task = () -> {
            scene.getStylesheets().removeIf(css -> css.contains("/themes/"));
            scene.getStylesheets().add(definition.stylesheet());
            currentTheme = theme;
            preferences.put(KEY_SELECTED_THEME, theme.name());
            scenes.put(scene, Boolean.TRUE);
        };
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    public void applyCurrentTheme(Scene scene) {
        apply(currentTheme, scene);
    }

    public void applyToKnownScenes(Theme theme) {
        List<Scene> targets = new ArrayList<>(scenes.keySet());
        for (Scene scene : targets) {
            if (scene != null) {
                apply(theme, scene);
            }
        }
    }

    public void applyToContextScene(Theme theme) {
        Scene scene = context.get(Scene.class);
        if (scene != null) {
            apply(theme, scene);
        }
        applyToKnownScenes(theme);
    }

    public Theme currentTheme() {
        return currentTheme;
    }

    public Optional<ThemeDefinition> definition(Theme theme) {
        return Optional.ofNullable(definitions.get(theme));
    }
}
