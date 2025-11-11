# Alamafa JavaFX View Layer

## 概述
该模块围绕 JavaFX FXML 提供声明式视图元数据与加载工具，并与 DI 容器配合，使控制器、资源及视图生命周期钩子能够像普通 Bean 一样注入/管理。

## 核心组件
- **`@FxViewSpec`**：作用于控制器或视图类，继承 `@Component(scope = PROTOTYPE)`，支持 DI。可声明 Bean 名称、FXML 路径、样式表、资源包、关联 ViewModel、是否共享、以及窗口提示（标题、尺寸、是否可调、是否主窗口）。
- **`FxViewMetadataProcessor` 与 `FxViewRegistry`**：`BeanPostProcessor` 将描述符写入并发注册表（按类型/名称索引），供窗口管理器、Binder 等模块查询。
- **`FxViewLoader`**：核心加载器，可通过 `ResourceResolver` 解析资源，必要时从 `BeanRegistry` 获取控制器，绑定资源包、应用样式，并返回包含根节点与控制器的 `FxView<T>`。标记为共享的视图会缓存，否则每次重新加载。
- **生命周期注解**：`@PostShow` / `@PreClose` 标注在控制器方法上，分别在视图展示后、窗口关闭前由 `FxWindowManager` 调用。
- **错误处理**：`ViewLoadingException` 会携带上下文信息包装 IO/实例化异常。

## 典型用法
```java
@FxViewSpec(fxml = "views/login.fxml", styles = "styles/login.css", viewModel = LoginViewModel.class)
public class LoginViewController {
    @Inject private FxWindowManager windows;
    public void setViewModel(LoginViewModel vm) { ... }
}
```
手动加载视图：
```java
FxView<LoginViewController> view = viewLoader.load(LoginViewController.class);
Parent root = view.root();
LoginViewController controller = view.controller();
```

## 资源解析
`FxViewLoader` 支持可插拔的 `ResourceResolver`。Starter 默认使用 `ResourceResolver.classpath()`；若视图存放在 JAR 外（如多租户皮肤），可自定义。样式表路径会尽量相对视图类解析并转换为外部 URL。

## 共享视图与缓存
`@FxViewSpec(shared = true)` 可复用同一个 `FxView` 实例，直到根节点脱离 Scene（Loader 会检查 `root.getScene()`，防止重复附加），适合导航栏等持久视图。

## 集成点
- `FxWindowManager` 使用描述符与 Loader 将视图挂载到 Stage。
- `FxViewModelBinder` 借助描述符确定需要绑定的 ViewModel。
- `alamafa-jfx-starter` 会在检测到 JavaFX 时自动注册上述 Bean 与元数据处理器。
