# Tower Client Desktop Sample

## Overview
`tower-client` is a reference desktop application that stitches together the entire Alamafa ecosystem: JavaFX launcher + MVVM stack, theming, VLCJ integration, DI, and configuration binding. It simulates a video monitoring console with login, dashboard, and a 4×4 monitoring wall.

## Feature Highlights
- **Authentication flow** – `LoginViewModel` talks to `AuthService`, which wraps `AuthApi`/`HttpExecutor`. Tokens are stored in `TokenStore`, and credentials can be cached securely with `CredentialsStore` (AES/GCM + `Preferences`).
- **Session data** – `UserSession` exposes observable username/initials for dashboard widgets.
- **API client** – `HttpExecutor` builds `HttpRequest`s with automatic JSON (Jackson) marshalling, bearer tokens, and timeout settings from `ApiProperties` (`api.base-url`, `api.connect-timeout-seconds`, etc.). `JavaHttpTransport` delegates to `java.net.http.HttpClient`. Errors bubble up as `ApiClientException` or `ApiNetworkException`.
- **Modular UI** – The dashboard is composed of subviews (header, footer, left/right panels, center panel) loaded via `FxViewLoader`. Each section is an `@FxViewSpec` annotated controller, enabling independent styling.
- **Theme switching** – `ThemeManager` is injected into top-level controllers so users can toggle light/dark styles at runtime.
- **Monitoring wall** – `MonitoringWallViewController` creates a 4×4 grid of `StackPane`s, uses `EmbeddedPlayerManager` to attach VLCJ pixel buffers, autoplays `player.defaultMediaUrl`, and registers `@PreClose` cleanup.
- **Media telemetry** – `MediaChannelRegistry` subscribes to `MediaEventDispatcher` and maintains an observable list of `MediaChannelStatus` objects (UUID, state, last updated). This drives monitoring dashboards.

## Package Guide
- `api.client` – transport layer (`HttpExecutor`, `HttpTransport`, `JavaHttpTransport`, DTOs, error types).
- `api.auth` – login API wrapper, token persistence, domain exceptions, `TokenPayload` DTO.
- `media` – VLCJ event registry (`MediaChannelRegistry`, `MediaChannelStatus`).
- `session` – `CredentialsStore` (encrypted persistence) and `UserSession` (JavaFX properties).
- `ui.login` – login view/view-model, input validation, remember-me logic, and `@PreClose` unbinding.
- `ui.dashboard.*` – header, footer, left/right panels, center content, plus `MonitoringWallViewController` and view-model.

## Runtime Flow
1. **Startup** – `TowerClientApplication` uses `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)`. `FxPrimaryStageLifecycle` mounts `LoginViewController` (marked `primary=true`).
2. **Login** – pressing “登录” executes `LoginViewModel.authenticateAsync`. On success, `FxWindowManager` opens `DashboardViewController` in a new stage and closes the login window.
3. **Dashboard** – `DashboardViewController` loads five subviews, injects `ThemeManager`, and can launch the monitoring wall via `FxWindowManager.openWindow`.
4. **Monitoring wall** – each tile attaches an `EmbeddedPlayerSession`. Media events from VLCJ propagate through `MediaEventDispatcher` into `MediaChannelRegistry` and onto UI bindings.

## Configuration
`src/main/resources/application.properties` contains sample settings (`api.base-url`, `player.default-media-url`, theme overrides). Override at runtime or via OS-specific profiles.

## Tests
`src/test/java` covers critical logic:
- `HttpExecutorTest` – verifies JSON unmarshalling + error handling.
- `CredentialsStoreTest` – ensures AES/GCM encryption round-trips and preferences flush behaviour.
- `AuthServiceTest` – fakes `AuthApi` responses and validates token storage.
- `LoginViewModelTest` – unit tests the authentication state machine and error messaging.
Run them via `mvn -pl alamafa-sample/tower-client test`.

## Running
```
mvn -pl alamafa-sample/tower-client -am -Djavafx.platform=win javafx:run
```
The login window appears first; clicking “监控墙” opens the VLCJ-backed view. Set `player.defaultMediaUrl` to a reachable stream to see live video.

## Extension Ideas
1. Plug in a real REST backend by implementing `HttpTransport` with Retrofit or Reactor.
2. Add additional `MediaEventListener`s to log heartbeats or trigger alerts when `MediaEventType.ERROR` occurs.
3. Replace `EmbeddedPlayerManager` with `ExternalPlayerLauncher` (see `alamafa-jfx-vlcj`) for sandboxed playback per channel.
