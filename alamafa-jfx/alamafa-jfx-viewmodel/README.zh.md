# Alamafa JavaFX ViewModel Layer

## 概述
`alamafa-jfx-viewmodel` 是 MVVM 支撑层，提供 `FxViewModel` 基类、元数据注解、绑定助手、窗口管理工具以及异步命令原语。它建立在 `alamafa-di`、`alamafa-core` 与视图模块之上，形成完整 UI 栈。

## 构成
- **`FxViewModel`**：抽象基类，保存 `ApplicationContext` 并按需解析 Bean（优先使用 `BeanRegistry`）。生命周期钩子包括：
  - `attach(ApplicationContext)` / `onAttach()`：在 Binder 关联模型时调用。
  - `onActive()` / `onInactive()`：视图显示/隐藏时触发。
  - `detach()` / `onDetach()`：释放资源后再解绑。
- **`@FxViewModelSpec` 与 `FxViewModelDescriptor`**：声明作用域（`APPLICATION` 或 `VIEW`）、懒加载与 Bean 名称。`FxViewModelMetadataProcessor` 记录元数据并存入 `FxViewModelRegistry`。
- **`FxViewModelRegistry`**：按作用域与缓存规则解析实例。应用级模型会被缓存；视图级模型则从 DI 容器获取或通过反射创建。
- **`FxViewModelBinder`**：把视图（控制器或 `FxView`）与 ViewModel 连接，负责调用 `attach/onActive` 并根据作用域决定何时 `detach`。
- **`FxWindowManager`**：高级窗口 API，加载 `@FxViewSpec` 描述的视图，绑定 ViewModel，合并窗口选项，触发 `@PostShow`/`@PreClose`，并通过 `FxWindowHandle` 跟踪打开的 Stage。
- **`FxWindowOptions`**：Builder 风格的窗口配置（标题、尺寸、模态、父窗口、`showAndWait`），会与描述符默认值及配置覆盖项（`jfx.window.<descriptor>.*`）合并。
- **`FxPrimaryStageLifecycle`**：`ApplicationLifecycle` Bean，会加载 `primary=true` 的描述符并自动挂载主舞台。
- **命令**：`FxCommand` 接口及 `AsyncFxCommand` 实现，暴露 `running/executable` 的 `BooleanProperty`，便于和控件绑定。

## 使用流程
1. 为 ViewModel 添加注解：
```java
@FxViewModelSpec(scope = FxViewModelScope.APPLICATION)
public class MainViewModel extends FxViewModel { ... }
```
2. 通过 `FxWindowManager` 或手动方式将控制器绑定到 ViewModel。
3. 模态对话框示例：`windowManager.openWindow(DialogController.class, FxWindowOptions.modal(owner))`。
4. 主舞台由 Starter 自动注册的 `FxPrimaryStageLifecycle` 挂载，内部会调用 `FxWindowManager.mountPrimaryStage(stage)`。

## 扩展点
- 实现自定义 `BeanPostProcessor`，在视图展示前后插入额外窗口事件钩子。
- 替换/扩展 `FxCommand`（如整合自有线程池），并注入到 ViewModel 中。
- 如需跨节点缓存或混合 DI 策略，可自定义并替换 `FxViewModelRegistry` Bean。
