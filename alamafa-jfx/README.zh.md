# Alamafa JavaFX Stack

## 概述
`alamafa-jfx` 家族让核心运行时与 JavaFX 无缝协作，涵盖：
- 让 `ApplicationBootstrap` 与 JavaFX `Application` 生命周期保持同步的 Launcher。
- 识别 DI 控制器的声明式视图元数据与加载器。
- 面向 MVVM 的 ViewModel 基础设施（命令、窗口管理器、元数据注册表）。
- 自动配置，用于为应用和 Starter 组装上述组件。

## 模块速览
| 模块 | 职责 |
| --- | --- |
| [`alamafa-jfx-launcher`](./alamafa-jfx-launcher) | 实现 `JavaFxApplicationLauncher`，把 JavaFX 生命周期回调映射到 Alamafa `Lifecycle`，并把主 `Stage` 注入 `ApplicationContext`。 |
| [`alamafa-jfx-view`](./alamafa-jfx-view) | 提供 `@FxViewSpec`、`FxViewLoader`、元数据注册表、控制器工厂、资源解析、共享视图缓存，以及 `@PostShow` / `@PreClose` 等生命周期注解。 |
| [`alamafa-jfx-viewmodel`](./alamafa-jfx-viewmodel) | 提供 `FxViewModel` 基类、`@FxViewModelSpec` 元数据收集、作用域感知注册表、`FxViewModelBinder`、`FxWindowManager`、命令辅助类与 `FxPrimaryStageLifecycle`。 |
| [`alamafa-jfx-starter`](./alamafa-jfx-starter) | 当类路径存在 JavaFX 时，自动注册视图与 ViewModel 相关 Bean，包括 `FxViewLoader`、元数据处理器、Binder、`FxWindowManager` 与生命周期参与者。 |

## 应用流程
1. 在入口类标注 `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)`。
2. 通过 `@FxViewSpec` 描述视图（FXML、样式、窗口属性），并用 `@FxViewModelSpec` 定义 ViewModel（作用域、lazy）。
3. 注入 `FxViewLoader` 来实例化视图，或调用 `FxWindowManager.openWindow(...)` 打开新 Stage，自动绑定 ViewModel 与窗口元数据。
4. 在控制器中使用 `FxViewModelBinder` 获取 ViewModel，让其自动执行 `attach/onActive` 生命周期钩子。

## 关键概念
- **上下文共享**：Launcher 会把主 `Stage`、`Scene`、`Window` 存入 `ApplicationContext`，以便主题、DI、窗口管理等模块复用。
- **元数据处理器**：视图与 ViewModel 注解由 `BeanPostProcessor` 解析，确保 JavaFX 加载场景前已有元数据，也让 `FxPrimaryStageLifecycle` 能按条件挂载主舞台。
- **窗口配置**：`FxWindowManager` 会合并描述符默认值、运行时 `FxWindowOptions`、以及配置覆盖项（`jfx.window.<view-name>.title/width/...`）。
- **命令工具**：`FxCommand` / `AsyncFxCommand` 暴露 `BooleanProperty`，便于和按钮的禁用/加载状态双向绑定。

## 开发者须知
- `javafx.platform` 属性来源于根 POM，可在构建其他 OS 目标时通过 `-Djavafx.platform=win` 等覆盖。
- JavaFX 相关测试仅在有 Toolkit 的环境运行，否则自动跳过。
- 扩展（如接入 VLCJ）时，优先注入 `ApplicationContext`，借助现有事件、DI、健康检查基础设施。
