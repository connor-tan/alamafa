# Alamafa JavaFX View Layer

## Overview
This module introduces declarative view metadata and loading helpers around JavaFX FXML. It cooperates with the DI container so controllers, resources, and view lifecycle hooks can be injected/managed like any other bean.

## Core Pieces
- **`@FxViewSpec`** – annotation applied to controllers or view classes. It inherits `@Component(scope = PROTOTYPE)` so controllers can be DI-managed. Metadata captured: bean name, FXML path, stylesheets, resource bundle, associated view-model, whether the view is shared, and optional window hints (title, size, resizable, primary flag).
- **`FxViewMetadataProcessor` & `FxViewRegistry`** – a `BeanPostProcessor` records descriptor instances inside a concurrent registry keyed by type or name. Other modules (window manager, binder) query this registry for metadata.
- **`FxViewLoader`** – central loader that resolves resources (via `ResourceResolver`), instantiates controllers through `BeanRegistry` when available, binds resource bundles, applies stylesheets, and returns an `FxView<T>` record containing both root node and controller. Shared views are cached; non-shared views are reloaded each call.
- **Lifecycle annotations** – `@PostShow` and `@PreClose` mark controller methods to be invoked after a view is shown or before a window closes (wired by `FxWindowManager`).
- **Error handling** – `ViewLoadingException` wraps IO or instantiation failures with contextual information.

## Typical Usage
```java
@FxViewSpec(fxml = "views/login.fxml", styles = "styles/login.css", viewModel = LoginViewModel.class)
public class LoginViewController {
    @Inject private FxWindowManager windows;
    public void setViewModel(LoginViewModel vm) { ... }
}
```
Inject `FxViewLoader` wherever you need to inflate views manually:
```java
FxView<LoginViewController> view = viewLoader.load(LoginViewController.class);
Parent root = view.root();
LoginViewController controller = view.controller();
```

## Resource Resolution
`FxViewLoader` accepts a pluggable `ResourceResolver`. By default the starter wires `ResourceResolver.classpath()`; override it if views live outside the JAR (e.g., multi-tenant themes). Stylesheet paths are resolved relative to the view class when possible and converted to external URLs.

## Shared Views & Caching
Mark `@FxViewSpec(shared = true)` to reuse the same `FxView` instance until the root node detaches from a scene (the loader checks `root.getScene()` to avoid duplicate attaches). This is useful for persistent navigation panes.

## Integration Points
- `FxWindowManager` consumes descriptors + loader to mount views inside stages.
- `FxViewModelBinder` uses descriptors to figure out which view-model to bind.
- `alamafa-jfx-starter` registers all required beans and metadata processors automatically when JavaFX is available.
