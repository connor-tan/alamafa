# Alamafa JavaFX ViewModel Layer

## Overview
`alamafa-jfx-viewmodel` provides the MVVM support system: a base `FxViewModel`, metadata annotations, binding helpers, window management utilities, and async command primitives. It builds on `alamafa-di`, `alamafa-core`, and the view module to create a cohesive UI stack.

## Building Blocks
- **`FxViewModel`** – abstract base class that stores `ApplicationContext` and lazily resolves beans (via `BeanRegistry` if available). Lifecycle hooks:
  - `attach(ApplicationContext)` / `onAttach()` – called when the binder wires the model.
  - `onActive()` / `onInactive()` – triggered when the associated view shows/hides.
  - `detach()` / `onDetach()` – cleanup before disposal.
- **`@FxViewModelSpec` + `FxViewModelDescriptor`** – declare scope (`APPLICATION` or `VIEW`), laziness, and bean name. Metadata is captured by `FxViewModelMetadataProcessor` and stored inside `FxViewModelRegistry`.
- **`FxViewModelRegistry`** – resolves instances respecting scope + caching rules. For application-scoped models it memoises instances; for per-view ones it either pulls from the DI container or reflects a new instance.
- **`FxViewModelBinder`** – glues a view (controller or `FxView`) to its view-model. It attaches, calls `onActive`, injects the model via controller setters, and knows how/when to detach (depending on scope).
- **`FxWindowManager`** – high-level API for opening windows backed by `@FxViewSpec`. It resolves views via `FxViewLoader`, binds view-models, applies descriptor/window option metadata, wires `@PostShow`/`@PreClose`, and tracks open `Stage`s via `FxWindowHandle`.
- **`FxWindowOptions`** – builder-style options (title, size, modality, owner, `showAndWait`) merged with descriptor defaults and configuration overrides (`jfx.window.<descriptor>.*`).
- **`FxPrimaryStageLifecycle`** – `ApplicationLifecycle` bean that mounts the primary stage automatically by loading the descriptor marked `primary=true`.
- **Commands** – `FxCommand` interface plus `AsyncFxCommand` implementation, exposing `BooleanProperty running/executable` for easy binding to controls.

## Usage Workflow
1. Annotate view-models:
```java
@FxViewModelSpec(scope = FxViewModelScope.APPLICATION)
public class MainViewModel extends FxViewModel { ... }
```
2. Bind controllers either manually or let `FxWindowManager` inject them when the window opens.
3. For modal dialogs: `windowManager.openWindow(DialogController.class, FxWindowOptions.modal(ownerWindow))`.
4. Use `FxWindowManager.mountPrimaryStage(stage)` indirectly via `FxPrimaryStageLifecycle` (auto-registered by the starter) to display the primary descriptor.

## Extension Points
- Register additional window event hooks by creating a `BeanPostProcessor` that inspects controllers and wires custom annotations before/after view display.
- Provide alternative `FxCommand` implementations (e.g., tasks integrated with your executor) and inject them into view-models.
- Override `FxViewModelRegistry` (bean replacement) if you need clustered caching or a hybrid DI strategy.
