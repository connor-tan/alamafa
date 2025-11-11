# Sample Greeting Starter

## 目的
该 Starter 演示如何封装 Alamafa 自动配置：导出 `GreetingService`、绑定配置属性、运行 `ApplicationRunner`，并注册 `ApplicationStartedEvent` 监听器。

## 自动配置组件
- **`GreetingProperties`**（`@ConfigurationProperties("greeting")`）：包含 `target` 与 `enabled` 字段。
- **`GreetingService`**（默认 `ConsoleGreetingService`）：通过 `AlamafaLogger` 输出问候信息。
- **`ApplicationRunner`**（`GreetingRunner`）：启动时问候，并可通过 CLI 参数 `--name=`/`--target=` 覆盖对象。
- **`ApplicationStartedEvent` 监听器**：应用启动完成后记录日志（仅当 greeting 启用）。

所有 Bean 由 `GreetingAutoConfiguration` 注册，并通过 `META-INF/alamafa.factories` 暴露。条件如下：
- `@ConditionalOnMissingBean` 允许使用者自定义 `GreetingService` / 属性。
- `@ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)` 控制 Runner 是否生效。

## 使用
添加依赖并通过类路径启用：
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>sample-greeting-starter</artifactId>
</dependency>
```
在 `application.properties` 中配置：
```
greeting.target=Alamafa Developer
greeting.enabled=true
```

## 开发说明
- `GreetingRunner#getOrder()` 返回 `Integer.MIN_VALUE`，确保其在其他 Runner 之前运行。
- 该示例展示了如何在单个模块中组合 DI、配置绑定与事件监听，可作为自定义 Starter 的模板。
