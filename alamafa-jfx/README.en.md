# Alamafa JavaFX Stack

## Overview
The `alamafa-jfx` family bridges the core Alamafa runtime with JavaFX. It covers:
- A launcher that keeps `ApplicationBootstrap` and the JavaFX `Application` lifecycle in sync.
- Declarative view metadata + loaders that understand DI-managed controllers.
- View-model infrastructure with MVVM-friendly utilities (commands, window manager, metadata registry).
- Auto-configuration that wires everything together for applications and starters.

## Module Map
| Module | Responsibilities |
| --- | --- |
| [`alamafa-jfx-launcher`](./alamafa-jfx-launcher) | Implements `JavaFxApplicationLauncher`, translates JavaFX lifecycle callbacks into Alamafa `Lifecycle` phases, and injects the primary `Stage` into the `ApplicationContext`. |
| [`alamafa-jfx-view`](./alamafa-jfx-view) | Provides `@FxViewSpec`, `FxViewLoader`, metadata registry, controller factories, resource resolution, shared-view cache, and lifecycle annotations (`@PostShow`, `@PreClose`). |
| [`alamafa-jfx-viewmodel`](./alamafa-jfx-viewmodel) | Supplies `FxViewModel` base class, metadata collection via `@FxViewModelSpec`, scope-aware registry, `FxViewModelBinder`, window manager (`FxWindowManager`), command helpers, and `FxPrimaryStageLifecycle`. |
| [`alamafa-jfx-starter`](./alamafa-jfx-starter) | Auto-registers the view + view-model beans when JavaFX is on the classpath, exposing `FxViewLoader`, metadata processors, binder, `FxWindowManager`, and lifecycle participants. |

## Typical Application Flow
1. Annotate your entry class with `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)`.
2. Describe views using `@FxViewSpec` (FXML path, styles, window hints) and view-models using `@FxViewModelSpec` (scope, lazy flags).
3. Inject `FxViewLoader` to instantiate views, or call `FxWindowManager.openWindow(...)` to show new stages with automatic `FxViewModel` binding and window metadata.
4. Use `FxViewModelBinder` (injected into controllers) to obtain view-model instances and automatically call `attach/onActive` lifecycle hooks.

## Key Concepts
- **Context sharing**: the launcher stores the JavaFX primary `Stage` plus any `Scene`/`Window` references inside the `ApplicationContext`, so other modules (themeing, DI, window manager) can pull them out.
- **Metadata processors**: view and view-model annotations are parsed by `BeanPostProcessor`s so metadata exists before JavaFX scenes load. This also enables conditionally mounting the primary stage (`FxPrimaryStageLifecycle`).
- **Window configuration**: `FxWindowManager` merges descriptor defaults, runtime `FxWindowOptions`, and configuration overrides (`jfx.window.<view-name>.(title|width|...)` via `Configuration`).
- **Command utilities**: `FxCommand`/`AsyncFxCommand` expose `BooleanProperty` handles for binding to buttons’ disabled/loading states.

## Developer Notes
- The property `javafx.platform` is resolved from the root POM profiles; override it (`-Djavafx.platform=win`) during builds targeting other OSes.
- Tests for JavaFX-heavy modules run only when a toolkit is available; otherwise they auto-skip.
- When writing new integrations (e.g., VLCJ), prefer injecting `ApplicationContext` to interact with existing infrastructure (events, DI registry, health checks).
