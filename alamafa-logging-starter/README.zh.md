# Alamafa Logging Starter

## 目标
该 Starter 为 `alamafa-bom` 中包含的 SLF4J/Logback 栈提供即开即用的配置，主要解决两件事：
1. 按需安装 SLF4J 的 JUL Bridge，让仍使用 `java.util.logging` 的三方库输出到 Logback。
2. 在启动完成时输出一条包含当前 `ApplicationContext` 键集合的诊断日志，便于确认运行时注入内容。

## 自动配置的 Bean（`META-INF/alamafa.factories`）
- `loggingContextInitializer`：`ApplicationEventListener<ApplicationStartedEvent>`，在应用进入 `START` 阶段后记录上下文快照。
- `slf4jJulBridgeInitializer`：受 `logging.jul-bridge=true` 条件控制的监听器，启动时通过反射调用 `org.slf4j.bridge.SLF4JBridgeHandler`，移除默认 JUL 处理器并安装桥接。

两者都标注了 `@ConditionalOnMissingBean`，可通过自定义 Bean 覆盖。

## 使用方式
1. 添加依赖：
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-logging-starter</artifactId>
</dependency>
```
2. （可选）在 `application.properties` 中启用 JUL Bridge：
```
logging.jul-bridge=true
```
3. 启动应用。当桥接安装或上下文键可用时，Starter 会输出日志。

## 自定义建议
- 提供名为 `slf4jJulBridgeInitializer` 的 Bean，可替换桥接策略（例如先安装日志轮转）。
- 可在模块内通过 Logback XML 配置附加日志级别、Appender；Starter 仅提供最小默认配置。
- 由于 Bean 是普通事件监听器，可借助注入的 `ApplicationContext` 与 `HealthRegistry`、指标系统等交互。
