# Tower Client Desktop Sample

## 概述
`tower-client` 是一个桌面端参考实现，串联 Alamafa 的全部能力：JavaFX Launcher + MVVM、主题、VLCJ 集成、DI 与配置绑定。它模拟带登录、仪表盘与 4×4 监控墙的视频监控客户端。

## 功能亮点
- **认证流程**：`LoginViewModel` 调用 `AuthService`（封装 `AuthApi`/`HttpExecutor`），令牌存入 `TokenStore`，凭据通过 `CredentialsStore`（AES/GCM + `Preferences`）安全缓存。
- **会话数据**：`UserSession` 暴露可观察的用户名/首字母，供仪表盘组件绑定。
- **API 客户端**：`HttpExecutor` 构造 `HttpRequest`，自动处理 JSON（Jackson）、Bearer Token 与来自 `ApiProperties` 的超时配置；`JavaHttpTransport` 委托 `java.net.http.HttpClient`；错误被包装成 `ApiClientException` 或 `ApiNetworkException`。
- **模块化 UI**：Dashboard 由多个子视图组成（头/尾/左右/中心），通过 `FxViewLoader` 加载，每个部分都是 `@FxViewSpec` 控制器，便于独立样式。
- **主题切换**：顶层控制器注入 `ThemeManager`，可在运行期切换明暗主题。
- **监控墙**：`MonitoringWallViewController` 构建 4×4 `StackPane` 网格，使用 `EmbeddedPlayerManager` 附加 VLCJ 像素缓冲、自动播放 `player.defaultMediaUrl`，并在 `@PreClose` 中清理。
- **媒体遥测**：`MediaChannelRegistry` 订阅 `MediaEventDispatcher`，维护 `MediaChannelStatus` 可观察列表（UUID、状态、更新时间），驱动监控界面。

## 包结构
- `api.client`：传输层（`HttpExecutor`、`HttpTransport`、`JavaHttpTransport`、DTO 与异常）。
- `api.auth`：登录 API 包装、令牌持久化、业务异常、`TokenPayload`。
- `media`：VLCJ 事件注册（`MediaChannelRegistry`、`MediaChannelStatus`）。
- `session`：`CredentialsStore`（加密持久化）与 `UserSession`（JavaFX 属性）。
- `ui.login`：登录视图/ViewModel、输入校验、记住我逻辑、`@PreClose` 解绑。
- `ui.dashboard.*`：头/尾/左右/中心布局，加上 `MonitoringWallViewController` 与其 ViewModel。

## 运行流程
1. **启动**：`TowerClientApplication` 标注 `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)`，`FxPrimaryStageLifecycle` 自动挂载标记 `primary=true` 的 `LoginViewController`。
2. **登录**：点击“登录”触发 `LoginViewModel.authenticateAsync`，成功后 `FxWindowManager` 打开 `DashboardViewController` 新 Stage，并关闭登录窗口。
3. **仪表盘**：`DashboardViewController` 加载五个子视图，注入 `ThemeManager`，并可通过 `FxWindowManager.openWindow` 打开监控墙。
4. **监控墙**：每个 Tile 附加一个 `EmbeddedPlayerSession`，VLCJ 事件经 `MediaEventDispatcher` → `MediaChannelRegistry` → UI 绑定。

## 配置
`src/main/resources/application.properties` 提供示例配置（`api.base-url`、`player.default-media-url`、主题覆盖等），可在运行期或各 OS Profile 中调整。

## 测试
`src/test/java` 覆盖关键逻辑：
- `HttpExecutorTest`：验证 JSON 反序列化与错误处理。
- `CredentialsStoreTest`：确认 AES/GCM 加解密与 Preferences 写入。
- `AuthServiceTest`：模拟 `AuthApi` 响应并校验令牌存储。
- `LoginViewModelTest`：测试认证状态机与错误信息。
运行：`mvn -pl alamafa-sample/tower-client test`。

## 运行
```
mvn -pl alamafa-sample/tower-client -am -Djavafx.platform=win javafx:run
```
首先出现登录窗口，点击“监控墙”可打开 VLCJ 视图。将 `player.defaultMediaUrl` 指向可访问的流即可看到实时视频。

## 扩展建议
1. 实现基于 Retrofit 或 Reactor 的 `HttpTransport`，接入真实 REST 后端。
2. 新增 `MediaEventListener`，记录心跳或在 `MediaEventType.ERROR` 时触发告警。
3. 将 `EmbeddedPlayerManager` 替换为 `ExternalPlayerLauncher`（参见 `alamafa-jfx-vlcj`），为每个通道提供隔离播放进程。
