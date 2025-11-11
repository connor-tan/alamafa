# Alamafa JavaFX Launcher

## 概述
`alamafa-jfx-launcher` 让 Alamafa 生命周期引擎适配 JavaFX，提供 `ContextAwareApplicationLauncher` 实现及一组辅助类，将 JavaFX `Application` 回调重新交给 `ApplicationBootstrap`，并在运行期暴露主 `Stage`。

## 关键类
- **`JavaFxApplicationLauncher`**：实现 `ContextAwareApplicationLauncher`，维护 `ApplicationContext`，注册 `ApplicationShutdown` 以调用 `Platform.exit()`，并在执行 `AlamafaFxApplication.launchApplication()` 前安装 `JavaFxLifecycleCoordinator`。
- **`JavaFxLifecycleCoordinator`**：接收 JavaFX 运行时的 `init`/`start`/`stop` 回调并转发到 Alamafa `Lifecycle`，同时把 `Stage` 放入上下文，供 `FxWindowManager`、`ThemeManager` 等 Bean 获取；所有异常会包装成 `LifecycleExecutionException` 并通过核心 `LifecycleErrorHandler` 上报。
- **`JavaFxRuntime`**：线程安全地保存当前协调器，供静态 JavaFX `Application` 子类查询。
- **`AlamafaFxApplication`**：极简的 JavaFX `Application`，仅将生命周期事件委托给协调器。

## 使用
```java
@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class MyFxApp {
    public static void main(String[] args) {
        AlamafaApplication.run(MyFxApp.class, args);
    }
}
```
只需在注解中指定 launcher，即可自动与 `AlamafaApplication` 协同工作。

## 扩展点
- **自定义生命周期逻辑**：通过 `ApplicationBootstrap#addLifecycleParticipant` 添加额外 `Lifecycle`（例如在 JavaFX Stage 出现前预加载字体）。
- **错误处理**：注册 `LifecycleErrorHandler` Bean，确保 UI 相关异常日志统一。
- **Stage 使用者**：`JavaFxLifecycleCoordinator` 完成 `start` 后，可从 `ApplicationContext` 获取 `Stage`/`Scene`。

## 诊断
为 `com.alamafa.jfx.launcher` 打开 DEBUG 日志，可查看平台初始化、生命周期切换、shutdown hook 执行情况。
