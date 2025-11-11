# Alamafa JavaFX + VLCJ Integration

## Overview
This module integrates VLCJ-based video playback with the Alamafa JavaFX stack. It supports two execution modes:
- **External player** – launches a dedicated JVM (`PlayerHostApplication`) that renders video using VLCJ and communicates over stdio command/event channels.
- **Embedded player** – renders directly inside a JavaFX `StackPane` using pixel-buffer video surfaces.
Auto-configuration (`VlcjAutoConfiguration`) registers all beans when the module is on the classpath.

## Architecture
```
App (JavaFX) ──┬─> ExternalPlayerLauncher (spawns player host)
               ├─> MediaCommandChannel (stdout)  ──> PlayerHostApplication
               └─> MediaEventChannel (stderr)    <── PlayerHostApplication
```

### Core Packages
- **`com.alamafa.jfx.vlcj.core`**
  - `MediaEndpoint` / `MediaEndpointFactory` define pluggable endpoints.
  - `PlayerLaunchRequest` captures dimensions, titles, media URLs.
  - `PlayerProperties` binds `player.*` configuration (default media, window size, heartbeat timeout, mode).
  - `MediaEventDispatcher` (`DefaultMediaEventDispatcher`) fan-outs media events to listeners such as UI registries.
- **`...external`**
  - `ExternalPlayerLauncher` implements `MediaEndpointFactory` by spawning `PlayerHostLauncher` using the current JVM’s classpath.
  - `ExternalProcessEndpoint` wraps the `Process`, `MediaCommandChannel` (`StdioCommandChannel`), `MediaEventChannel` (`StdioEventChannel`), and heartbeat monitoring logic. It registers itself with `ExternalProcessRegistry` so open players are closed on `ApplicationStoppingEvent`.
- **`...embedded`**
  - `EmbeddedPlayerManager` / `EmbeddedPlayerSession` attach VLCJ’s pixel buffer to JavaFX `ImageView`s, allowing monitoring walls to host multiple streams.
  - `PixelBufferVideoSurfaceFactory` creates `CallbackVideoSurface`s backed by JavaFX `PixelBuffer` / `WritableImage`.
- **`...host`**
  - `PlayerHostApplication` is the standalone JavaFX app launched in external mode. It listens for JSON `MediaCommand`s on stdin, controls VLCJ, and publishes JSON `MediaEvent`s + periodic heartbeats on stderr.
- **`...ipc`**
  - DTOs (`MediaCommand`, `MediaEvent`, their enums) and channel interfaces for stdio transport.

## Configuration
Default keys (`player.*`):
- `player.mode` – `EXTERNAL` (default) or `EMBEDDED`.
- `player.defaultMediaUrl` – autoplay URL used by monitoring wall tiles.
- `player.windowWidth/Height` – default size hints for both embedded tiles & external windows.
- `player.heartbeatTimeoutSeconds` – heartbeat watchdog threshold.

## Consuming From JavaFX Apps
1. Depend on `alamafa-jfx-vlcj` (and `uk.co.caprica:vlcj` transitively).
2. Inject `MediaEndpointFactory` (or specifically `ExternalPlayerLauncher`) and call `launch(PlayerLaunchRequest.builder().mediaUrl(...).build())`.
3. Optionally inject `EmbeddedPlayerManager` to attach players to panes.
4. Listen for `MediaEvent`s by registering with `MediaEventDispatcher` or by using higher-level helpers like `MediaChannelRegistry` (see `tower-client`).

## Sample Integration
The `tower-client` module demonstrates both modes:
- `MonitoringWallViewController` uses `EmbeddedPlayerManager` to render a 4×4 wall.
- `MediaChannelRegistry` subscribes to `DefaultMediaEventDispatcher` to populate UI lists with heartbeat, playing, paused, and error events.

## Diagnostics
- Heartbeat watchdog logs when a player stops responding and automatically fires an `ERROR` event before closing the process.
- Set `player.autoRestartOnHeartbeatLoss=true` (future enhancement placeholder) to hook restart logic.
- Enable SLF4J debug for `com.alamafa.jfx.vlcj` to inspect command/event traffic.
