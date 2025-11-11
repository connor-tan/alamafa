# Alamafa JavaFX Launcher

## Overview
`alamafa-jfx-launcher` adapts the Alamafa lifecycle engine to JavaFX. It supplies a `ContextAwareApplicationLauncher` implementation plus helper classes that relay JavaFX `Application` callbacks back into `ApplicationBootstrap` and provide runtime access to the primary `Stage`.

## Key Classes
- **`JavaFxApplicationLauncher`** – implements `ContextAwareApplicationLauncher`. Stores an `ApplicationContext`, registers `ApplicationShutdown` to call `Platform.exit()`, and installs a `JavaFxLifecycleCoordinator` before invoking `AlamafaFxApplication.launchApplication()`.
- **`JavaFxLifecycleCoordinator`** – receives lifecycle phases (`init`, `start`, `stop`) from the JavaFX runtime and forwards them to the Alamafa `Lifecycle`. It also places the `Stage` into the context so downstream beans (`FxWindowManager`, `ThemeManager`) can retrieve it. Errors are wrapped as `LifecycleExecutionException` and reported through the core `LifecycleErrorHandler`.
- **`JavaFxRuntime`** – thread-safe holder for the active coordinator so the static JavaFX `Application` subclass can find it.
- **`AlamafaFxApplication`** – minimal JavaFX `Application` that delegates `init`, `start`, and `stop` events to the coordinator.

## Usage
```java
@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class MyFxApp {
    public static void main(String[] args) {
        AlamafaApplication.run(MyFxApp.class, args);
    }
}
```
The launcher cooperates with `AlamafaApplication` automatically—you only need to select it via the annotation.

## Extension Points
- **Custom lifecycle logic**: add `Lifecycle` participants via `ApplicationBootstrap#addLifecycleParticipant` (e.g., to preload fonts before the JavaFX stage appears).
- **Error handling**: register a `LifecycleErrorHandler` bean so UI-related exceptions are logged or surfaced consistently.
- **Stage consumers**: fetch `Stage` or `Scene` from `ApplicationContext` once the `JavaFxLifecycleCoordinator` has run `start`.

## Diagnostics
Enable debug logging for `com.alamafa.jfx.launcher` to see when the platform initialises, when lifecycle transitions occur, and when shutdown hooks fire.
