# Alamafa Core

## 概述
`alamafa-core` 是整个框架的运行时内核，提供最小化的应用生命周期协定、内存型 `ApplicationContext`、同步事件系统、日志门面、健康探针以及 Runner 抽象。上层模块（DI、JavaFX、Logging Starter 等）都依赖这些能力接入启动、运行与停止阶段，而无需重复造轮子。

## 核心构件
- **生命周期编排**：`Lifecycle`、`LifecyclePhase` 与 `LifecycleExecutionException` 统一 init/start/stop 语义与异常传播。`ApplicationLifecycle` 进一步提供排序，确保 DI Bean 能以受控顺序接入。
- **应用引导器**：`ApplicationBootstrap` 将 `ContextAwareApplicationLauncher`（如 `DefaultApplicationLauncher` 或 JavaFX Launcher）与上下文初始化器、生命周期参与者、`ApplicationEventPublisher` 绑定，并在生命周期前后发布 `ApplicationStarting/Started/Stopping/Stopped` 事件，同时通过 `LifecycleErrorHandler` 统一处理异常。
- **上下文与参数**：`ApplicationContext` 是轻量键值/类型注册表，模块间共享。`ApplicationArguments` 将 CLI 选项解析后放入上下文，供 Runner 或组件复用。
- **生命周期感知 Launcher**：`DefaultApplicationLauncher` 管理 JVM 生命周期，安装 shutdown hook，并通过 `ApplicationShutdown` 提供优雅停机入口。自定义 launcher 只需实现 `ContextAwareApplicationLauncher`。
- **事件总线**：`DefaultApplicationEventPublisher` 维护线程安全的监听器列表，按监听器声明的事件类型同步派发。可通过 DI 自动注册实现 `ApplicationEventListener` 的 Bean。
- **健康检查**：`HealthRegistry`、`HealthCheck`、`HealthIndicator`、`HealthStatus` 描述拉取式健康快照。模块可在上下文键 `HealthRegistry.CONTEXT_KEY` 下注册检查项。
- **Runner**：`ApplicationRunner` 与 `CommandLineRunner` 提供有序的启动后钩子，模式类似 Spring Boot。

## 执行流程
1. 构造 Launcher（多数应用使用 `DefaultApplicationLauncher`）。
2. 通过 `ApplicationBootstrap` 包裹 launcher，注册上下文初始化器或额外的 `Lifecycle` 参与者（如 DI、调度器等）。
3. 调用 `launch` 并传入主 `Lifecycle`。Bootstrap 会发布 `ApplicationStarting`，执行初始化器，然后交给 launcher。
4. Launcher 依次执行 init/start/stop，并将异常交给上下文中的 `LifecycleErrorHandler`。停止阶段按注册的逆序执行，确保 LIFO 清理。

## 使用与扩展建议
- **新增生命周期参与者**：调用 `ApplicationBootstrap#addLifecycleParticipant`，这是 DI、JavaFX 窗口生命周期或自定义服务的标准接入点。
- **上下文预热**：在 DI 初始化前通过 `ApplicationContext#put` 注册配置、指标、调度器等单例，便于下游复用。
- **事件**：通过上下文中的 `ApplicationEventPublisher` 发布领域事件；实现 `ApplicationEventListener<T>` 并交由 DI/代码注册即可监听。
- **健康检查**：调用 `HealthRegistry#register(name, check)` 注册；`HealthRegistry#snapshot()` 可暴露给诊断接口。
- **错误处理**：通过 `ApplicationBootstrap#onError` 替换 `LifecycleErrorHandler`，注入自定义日志或告警策略。

## 快速开始
```java
ContextAwareApplicationLauncher launcher = new DefaultApplicationLauncher();
ApplicationBootstrap bootstrap = new ApplicationBootstrap(launcher);
bootstrap.addLifecycleParticipant(new DiRuntimeBootstrap.Builder()
        .scanPackages("com.example.app")
        .build());
bootstrap.launch(Lifecycle.NO_OP);
```
上述示例仅启动 DI，真实项目会将具体 `Lifecycle`（如 HTTP 服务器）传入 `launch`。

## 相关模块
- `alamafa-config` 将最终 `Configuration` 对象放入核心 `ApplicationContext`。
- `alamafa-di` 注册 `BeanRegistry` 并复用核心 Runner / 事件 / 健康基础设施。
- JavaFX launcher（`alamafa-jfx-launcher`）实现了这里定义的 `ContextAwareApplicationLauncher`。
