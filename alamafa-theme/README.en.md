# Alamafa Theme

## Overview
`alamafa-theme` implements a simple scene-wide theming service so JavaFX apps can toggle between bundled light/dark styles. It exposes `ThemeManager` and `ThemeProperties` via auto-configuration, integrates with the Alamafa DI container, and persists the selected theme using `java.util.prefs.Preferences`.

## Components
- **`Theme` enum** – `LIGHT` or `DARK`.
- **`ThemeDefinition`** – pairs a theme with a stylesheet path (defaults: `/themes/light.css`, `/themes/dark.css`).
- **`ThemeProperties` (@ConfigurationProperties("theme"))** – currently only `defaultTheme`, but acts as an anchor for future knobs.
- **`ThemeManager`** – stores references to every `Scene` it has touched (via `WeakHashMap`) and re-applies CSS when the theme changes. It writes the last selection to user preferences so a restart keeps the same look.
- **`ThemeAutoConfiguration`** – registers `ThemeProperties` and `ThemeManager` when the module is present. Since it is annotated with `@AutoConfiguration`, `alamafa-bootstrap` discovers it automatically.

## Usage
```java
@Inject private ThemeManager themeManager;

public void onClickLight(Scene scene) {
    themeManager.apply(Theme.LIGHT, scene);
}
```
If a `Scene` is not readily available, call `themeManager.applyToContextScene(theme)` to use whatever `Scene` is stored inside the `ApplicationContext` (JavaFX launcher places the primary stage’s scene there once available).

## Configuration
```
theme.default-theme=LIGHT
```
The binder uppercases enum names automatically; invalid values fall back to `DARK`.

## Integration Tips
- Apply theme during controller initialisation (see `LoginViewController` and `DashboardViewController` in `tower-client`) by observing `sceneProperty()` and calling `applyCurrentTheme(scene)` when non-null.
- Add new CSS files by registering custom `ThemeDefinition`s inside a configuration class or by extending `ThemeManager`.
- Because stylesheets are removed/re-added each time, ensure selectors are deterministic and avoid large inline images for smooth switches.
