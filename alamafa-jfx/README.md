# Alamafa JavaFX Extensions

This directory hosts the JavaFX integration layer for the Alamafa framework. The
foundation consists of a dedicated launcher plus MVVM-oriented view/view-model
building blocks.

## Modules

- `alamafa-jfx-launcher`: bridges `ApplicationBootstrap` with `javafx.application.Application`,
  publishing the `Stage` into the `ApplicationContext` and wiring `ApplicationShutdown`
  to `Platform.exit()`.
- `alamafa-jfx-view`: shared view infrastructure – centralised FXML loader (`FxViewLoader`),
  resource resolver abstraction、`FxView` 返回值以及注解 `@FxViewSpec` 元数据注册（含 `primary`、`title` 等窗口元信息），帮助统一视图装载流程。
- `alamafa-jfx-viewmodel`: MVVM 核心基类，包括 `FxViewModel` 生命周期、注解 `@FxViewModelSpec`、可绑定命令
  (`FxCommand`/`AsyncFxCommand`) 以及 `FxViewModelBinder`、`FxWindowManager` 辅助类。
  注解可声明 `scope`（`APPLICATION`/`VIEW`，默认 `VIEW`）与 `lazy`，由 `FxViewModelRegistry` 管理实例缓存与懒加载。
- `alamafa-jfx-starter`: 聚合上述组件与自动配置，应用只需引入此依赖并在 `@AlamafaBootApplication` 上指定
  `launcher = JavaFxApplicationLauncher.class` 即可获得开箱即用的 JavaFX MVVM 支持。

后续计划继续补充导航管理、对话框、测试工具等子模块。

## Usage Overview

1. Replace `DefaultApplicationLauncher` with `JavaFxApplicationLauncher` when
   constructing `ApplicationBootstrap`.
2. 使用 `@FxViewSpec` / `@FxViewModelSpec` 标记组件后，框架会在启动时注册元数据。
3. 使用 `FxViewLoader` 装载 FXML，自动应用 `@FxViewSpec` 中的 bundle、styles，并支持共享缓存；若标记 `primary=true`，框架默认会把该视图挂载到 JavaFX PrimaryStage。
4. 利用 `FxWindowManager` 打开额外窗口/对话框（支持 `FxWindowOptions` 和配置覆盖 `jfx.window.<name>.title/width/height/resizable`），并在 `@PostShow`、`@PreClose` 钩子中处理视图逻辑；`FxViewModelBinder` 负责注入/解绑 ViewModel。
5. 覆盖 `javafx.platform`（默认 `linux`）以匹配当前平台的 JavaFX classifier。

随着 MVVM 组件完善，建议提供脚手架或 starter 以自动注入上述 Bean 与配置。
