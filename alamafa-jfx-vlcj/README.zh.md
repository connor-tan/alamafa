# Alamafa JavaFX + VLCJ Integration

## 概述
该模块将 VLCJ 视频播放能力接入 Alamafa 的 JavaFX 栈，支持两种模式：
- **External player**：启动独立 JVM（`PlayerHostApplication`），通过 VLCJ 渲染画面，并使用 stdio 命令/事件通道通信。
- **Embedded player**：直接在 JavaFX `StackPane` 中渲染，利用像素缓冲视频表面。
`VlcjAutoConfiguration` 会在类路径存在该模块时自动注册所有 Bean。

## 架构
```
App (JavaFX) ──┬─> ExternalPlayerLauncher (spawns player host)
               ├─> MediaCommandChannel (stdout)  ──> PlayerHostApplication
               └─> MediaEventChannel (stderr)    <── PlayerHostApplication
```

### 核心包
- **`com.alamafa.jfx.vlcj.core`**
  - `MediaEndpoint` / `MediaEndpointFactory`：定义可插拔的播放器终端。
  - `PlayerLaunchRequest`：描述窗口尺寸、标题、媒体 URL。
  - `PlayerProperties`：绑定 `player.*` 配置（默认媒体、窗口尺寸、心跳超时、模式）。
  - `MediaEventDispatcher`（`DefaultMediaEventDispatcher`）：将媒体事件广播给监听器（如 UI Registry）。
- **`...external`**
  - `ExternalPlayerLauncher` 通过当前 JVM classpath 启动 `PlayerHostLauncher`，实现 `MediaEndpointFactory`。
  - `ExternalProcessEndpoint` 包装 `Process`、`MediaCommandChannel`（`StdioCommandChannel`）、`MediaEventChannel`（`StdioEventChannel`）及心跳监控，并注册到 `ExternalProcessRegistry`，在 `ApplicationStoppingEvent` 时关闭进程。
- **`...embedded`**
  - `EmbeddedPlayerManager` / `EmbeddedPlayerSession` 将 VLCJ 像素缓冲绑定到 JavaFX `ImageView`，支持监控墙多路渲染。
  - `PixelBufferVideoSurfaceFactory` 创建基于 JavaFX `PixelBuffer` / `WritableImage` 的 `CallbackVideoSurface`。
- **`...host`**
  - `PlayerHostApplication` 是外部模式下的独立 JavaFX 应用，在 stdin 上监听 JSON `MediaCommand`，控制 VLCJ，并通过 stderr 输出 JSON `MediaEvent` 与定时心跳。
- **`...ipc`**
  - DTO（`MediaCommand`、`MediaEvent` 及枚举）与基于 stdio 的通道接口。

## 配置
常用 `player.*` 键：
- `player.mode`：`EXTERNAL`（默认）或 `EMBEDDED`。
- `player.defaultMediaUrl`：监控墙自动播放的 URL。
- `player.windowWidth/Height`：嵌入式 tile 与外部窗口的默认尺寸。
- `player.heartbeatTimeoutSeconds`：心跳超时阈值。

## 在 JavaFX 应用中使用
1. 引入 `alamafa-jfx-vlcj`（会传递依赖 `uk.co.caprica:vlcj`）。
2. 注入 `MediaEndpointFactory`（或 `ExternalPlayerLauncher`），调用 `launch(PlayerLaunchRequest.builder().mediaUrl(...).build())`。
3. 可选：注入 `EmbeddedPlayerManager`，将播放器附加到自定义 Pane。
4. 通过注册 `MediaEventDispatcher` 或更高层封装（如 `MediaChannelRegistry`，见 `tower-client`）接收 `MediaEvent`。

## 示例
`tower-client` 同时演示两种模式：
- `MonitoringWallViewController` 使用 `EmbeddedPlayerManager` 渲染 4×4 监控墙。
- `MediaChannelRegistry` 订阅 `DefaultMediaEventDispatcher`，将心跳/播放/暂停/错误事件呈现在 UI 中。

## 诊断
- 心跳监控会在播放器失联时记录日志，并在关闭进程前触发 `ERROR` 事件。
- 未来可通过 `player.autoRestartOnHeartbeatLoss=true` 钩住重启逻辑（预留配置）。
- 打开 `com.alamafa.jfx.vlcj` 的 SLF4J 调试日志，可查看命令/事件流量。
