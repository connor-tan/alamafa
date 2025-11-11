# Sample JavaFX MVVM App

## Overview
This module showcases how to build a JavaFX application with Alamafa’s MVVM stack (`alamafa-jfx-starter`). It contains two annotated views (`MainViewController`, `AboutViewController`), two view-models, FXML layouts, and CSS.

## Structure
- **Entry point** – `SampleJfxApplication` is annotated with `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)` so the JavaFX launcher replaces the default.
- **Main view** – `@FxViewSpec(primary = true, fxml = "views/main.fxml", styles = "styles/main.css")`.
  - Controller binds `MainViewModel`, exposes a refresh command, and opens the about dialog via `FxWindowManager`.
- **About view** – `@FxViewSpec(fxml = "views/about.fxml", resizable = false)` with `@PostShow` hook to focus the close button.
- **View-models**
  - `MainViewModel` (`@FxViewModelSpec(scope = APPLICATION)`) owns an `AsyncFxCommand` backed by a single-thread executor and publishes a `StringProperty` message.
  - `AboutViewModel` is per-view scope and just exposes a static info string.
- **Resources** – FXML + CSS under `src/main/resources/views` and `src/main/resources/styles`.

## Runtime Features Demonstrated
- Auto-mounting the primary stage via `FxPrimaryStageLifecycle` (the descriptor marked `primary=true`).
- Using `FxWindowManager.openWindow` with `FxWindowOptions` to open modal dialogs.
- Binding command state (`runningProperty`) to disable UI controls while asynchronous work runs.
- Wiring additional dependencies (e.g., `FxWindowManager`) via field injection in controllers.

## Running
```
mvn -pl alamafa-sample/sample-jfx-mvvm -am -Djavafx.platform=win javafx:run
```
(adjust `javafx.platform` for your OS)

## What to Extend
- Add another view annotated with `@FxViewSpec(shared = true)` to explore shared view caching.
- Experiment with configuration overrides (e.g., `jfx.window.mainViewController.width=1200`) to see how `FxWindowManager` merges metadata.
