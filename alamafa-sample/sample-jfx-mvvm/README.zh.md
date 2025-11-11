# Sample JavaFX MVVM App

## 概述
该模块展示如何使用 Alamafa 的 MVVM 栈（`alamafa-jfx-starter`）构建 JavaFX 应用，包含两个带注解的视图（`MainViewController`、`AboutViewController`）、两套 ViewModel、FXML 布局与 CSS。

## 结构
- **入口**：`SampleJfxApplication` 标注 `@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)`，以 JavaFX Launcher 取代默认 Launcher。
- **主视图**：`@FxViewSpec(primary = true, fxml = "views/main.fxml", styles = "styles/main.css")`
  - 控制器绑定 `MainViewModel`，暴露刷新命令，并通过 `FxWindowManager` 打开 About 对话框。
- **About 视图**：`@FxViewSpec(fxml = "views/about.fxml", resizable = false)`，包含 `@PostShow` 钩子以聚焦关闭按钮。
- **ViewModel**
  - `MainViewModel`（`@FxViewModelSpec(scope = APPLICATION)`）拥有基于单线程执行器的 `AsyncFxCommand`，向 UI 发布 `StringProperty`。
  - `AboutViewModel` 为视图作用域，仅暴露静态信息。
- **资源**：FXML/CSS 存放于 `src/main/resources/views` 与 `src/main/resources/styles`。

## 运行时特性
- 通过 `FxPrimaryStageLifecycle` 自动挂载 `primary=true` 的主舞台。
- 使用 `FxWindowManager.openWindow` + `FxWindowOptions` 打开模态对话框。
- 将命令状态（`runningProperty`）绑定到控件禁用/启用逻辑，处理异步任务。
- 通过字段注入方式获取额外依赖（如 `FxWindowManager`）。

## 运行
```
mvn -pl alamafa-sample/sample-jfx-mvvm -am -Djavafx.platform=win javafx:run
```
（根据实际系统调整 `javafx.platform`）

## 可扩展点
- 新增一个带 `@FxViewSpec(shared = true)` 的视图，体验共享视图缓存。
- 尝试配置覆盖（如 `jfx.window.mainViewController.width=1200`），观察 `FxWindowManager` 如何合并元数据。
