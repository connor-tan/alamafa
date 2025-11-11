# Alamafa Config

## 概述
`alamafa-config` 在核心 `ApplicationContext` 之上提供配置基础能力。它聚合多个配置源，暴露类型安全的 `Configuration` 视图，并自动将带 `@ConfigurationProperties` 的 POJO 绑定成 DI Bean。

## 组件
- **`ConfigurationLoader`**：链式构建器，用于合并按优先级排序的 `ConfigurationSource`。`withDefaults()` 默认加载：
  1. `application.properties`
  2. 通过 JVM 属性 `alamafa.profile` 或环境变量 `ALAMAFA_PROFILE` 解析的 profile 文件
  3. 环境变量（可按前缀过滤，例如 `ALAMAFA_`）
  4. 系统属性（最高优先级）
  每个数据源都带 `Priority`，后加载的高优先级条目会覆盖低优先级键。`requireKeys()` 可在返回最终 `Configuration` 前校验必填项。
- **`ConfigurationSource` 实现**：`ClasspathPropertiesSource`、`EnvironmentVariablesSource`、`SystemPropertiesSource`、`MapConfigurationSource`。可自定义实现并通过 `addSource` 注入。
- **`Configuration`**：不可变值对象，提供 `Optional` 读取、`String`/`int`/`boolean` 等带默认值的读取方法，以及用于诊断的 `snapshot()`。
- **`ConfigurationBinder`**：基于反射的绑定器，可把层级键映射到字段或 setter，既识别点分写法（`logging.level`），也识别 kebab-case（`logging-level`），并支持注解或方法参数提供的嵌套前缀。
- **`@ConfigurationProperties`**：标记需要自动绑定的 POJO。结合 `alamafa-di` 后，`ConfigurationPropertiesBinderPostProcessor` 会在 Bean 创建后调用 `ConfigurationBinder`。

## 典型流程
1. 创建或获取 `ConfigurationLoader`（DI 默认调用 `withDefaults()`）。
2. 执行 `load()` 得到 `Configuration` 并放入 `ApplicationContext`（`DiRuntimeBootstrap` 会自动完成）。
3. 在属性类上标注 `@ConfigurationProperties(prefix = "theme")`，DI 后处理器会按需延迟绑定字段。

## 开发提示
- **Profile**：通过 `-Dalamafa.profile=dev` 或设置 `ALAMAFA_PROFILE=dev` 来加载对应的 profile 配置。
- **自定义配置源**：例如合并远程机密数据，可自行实现 `ConfigurationSource` 并以 `addSource(secretSource, HIGH)` 追加；Loader 会先按声明顺序收集，再按优先级排序。
- **校验**：在 setter 内抛出异常即可触发绑定期校验（如 `PlayerProperties#setWindowWidth`）；异常会在早期暴露。

## 关联模块
- `alamafa-di` 会自动加载配置并注入 Bean Registry。
- `alamafa-theme`、`alamafa-jfx-vlcj` 等特性模块通过在属性类上标注 `@ConfigurationProperties` 复用该绑定机制。
