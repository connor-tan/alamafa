# Alamafa Bootstrap

## 概述
`alamafa-bootstrap` 是与 Spring Boot `SpringApplication` 类似的启动入口。它定位标注 `@AlamafaBootApplication` 的主类，接好核心运行时、注册自动配置，并把控制权交给 DI 与生命周期模块。仓库中所有可执行示例最终都会调用 `AlamafaApplication.run(...)`。

## `@AlamafaBootApplication`
该注解暴露三个主要配置：
- `scanBasePackages` / `scanBasePackageClasses`：覆盖 `alamafa-di` 的组件扫描根路径。
- `modules`：附加的类，既可以实现 `AlamafaBootstrapModule`、`Lifecycle`，也可以是 `@Configuration`。它们可注册额外扫描器、生命周期参与者或导入配置。
- `launcher`：选择 `ContextAwareApplicationLauncher`（默认 `DefaultApplicationLauncher`；JavaFX 应用通常传入 `JavaFxApplicationLauncher`）。

## 启动流水线
1. `AlamafaApplication.run` 通过 StackWalker 定位主类并校验注解。
2. 按注解实例化指定 launcher，并用 `ApplicationBootstrap` 包裹。
3. `AlamafaBootstrapContext` 收集基础包与显式配置类，暴露 helper 供模块注册生命周期参与者或上下文初始化器。
4. `AutoConfigurationLoader` 扫描类路径 `META-INF/alamafa.factories`，将带 `@AutoConfiguration` 的类加入配置集合（类似 Spring 的 `spring.factories`）。
5. 注解声明的模块被实例化：
   - 若实现 `AlamafaBootstrapModule`，调用其 `configure`；
   - 若实现 `Lifecycle`，直接注册到 `ApplicationBootstrap#addLifecycleParticipant`；
   - 标注 `@Configuration` 的模块无须额外接口也会交给 DI。
6. 汇总得到的包与配置类后构建 `DiRuntimeBootstrap`，将其加入生命周期后再执行 `bootstrap.launch()`。

## 扩展方式
- **自定义自动配置**：在模块中新增 `META-INF/alamafa.factories`，列出带 `@AutoConfiguration` 的全类名，运行时会自动发现。
- **Bootstrap 模块**：实现 `AlamafaBootstrapModule`，可注册额外上下文初始化器（如指标注册表）、或添加自定义 `Lifecycle`（调度器、IPC 等）。
- **自定义 Launcher**：针对 Swing/SWT/Android 等环境，实现 `ContextAwareApplicationLauncher` 并在注解中指定 `launcher`。

## 重点文件
- `AlamafaApplication`：入口类。
- `AlamafaBootApplication`：注解定义。
- `AlamafaBootstrapContext`：传给模块的可变状态。
- `AlamafaBootstrapModule`：模块扩展 SPI。
- `AutoConfigurationLoader`：读取 `META-INF/alamafa.factories` 并过滤 `AutoConfiguration` 项。

## 使用示例
```java
@AlamafaBootApplication(
        launcher = JavaFxApplicationLauncher.class,
        modules = { ThemeAutoConfiguration.class }
)
public class MyFxApp {
    public static void main(String[] args) {
        AlamafaApplication.run(MyFxApp.class, args);
    }
}
```
上述示例在 DI 启动前，先接入 JavaFX Launcher 并加载主题自动配置。
