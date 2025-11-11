# Alamafa DI

## 概述
`alamafa-di` 是仓库内的注解驱动依赖注入引擎，提供组件扫描、配置类处理、Bean 作用域、条件元数据以及生命周期感知的后处理能力。它保持轻量但足够表达力，既能支撑 Starter / Auto Configuration，又能覆盖示例应用。

## 运行时代码
- **`BeanRegistry`**：中心注册表，按类型/名称存储 `BeanDefinition`，缓存单例实例，并记录 `@PreDestroy` 回调，对外暴露 `get(Class)`、`get(String)`、`hasBeanDefinition` 等查询。
- **`BeanDefinition` 与作用域**：记录 Bean 类型、创建 `BeanSupplier`、作用域（`SINGLETON` 或 `PROTOTYPE`）、`primary`、`lazy` 标记。注册表会针对每个定义处理单例缓存与循环依赖检测。
- **`DiRuntimeBootstrap`**：实现 `Lifecycle`。`init` 阶段保证 `ApplicationContext` 中存在 `BeanRegistry` 与 `Configuration`，注册 `builder.withConfigurations(...)` 传入的配置类，扫描组件包，初始化单例后处理器，并将 `ApplicationLifecycle`、`ApplicationRunner`、`CommandLineRunner`、`ApplicationEventListener` Bean 接入核心运行时。
- **配置处理**：`ConfigurationProcessor` 识别 `@Configuration`、`@Import`、`@Bean` 及包扫描发现的隐式配置原型，支持 `@ConditionalOnProperty`、`@ConditionalOnClass`、`@ConditionalOnMissingBean` 以及带 `@Component` 元注解的自定义注解。
- **组件扫描**：`ComponentScanner` 遍历类路径，将 `@Component`、`@Service`、`@FxViewSpec`、`@FxViewModelSpec` 等 stereotype 注解转成 `ComponentDefinition`。构造器注入自动解析，支持 `@Inject`、`@Qualifier`、`@OptionalDependency`、集合与 `Optional<T>` 参数。
- **Bean 后处理**：`BeanPostProcessorChain` 在实例化后执行注册的处理器，例如 `ConfigurationPropertiesBinderPostProcessor` 绑定 `@ConfigurationProperties` Bean，JavaFX 元数据处理器也通过该链插入。

## 相关注解
- 组件：`@Component`、`@Service`、`@Configuration`、`@Bean`、`@Import`
- 条件：`@ConditionalOnProperty`、`@ConditionalOnClass`、`@ConditionalOnMissingBean`
- 生命周期/注入：`@Inject`、`@Qualifier`、`@OptionalDependency`、`@PostConstruct`、`@PreDestroy`

## 扩展容器
1. 引入模块并通过 `DiRuntimeBootstrap.builder()` 创建 bootstrap。
2. 调用 `scanPackages`、`withConfigurations` 指定扫描包与配置类。
3. 可注册实现 `BeanPostProcessor` 的单例自定义后处理器。
4. 利用条件注解构建 Starter 式行为（如 `@ConditionalOnProperty(prefix = "logging", name = "jul-bridge")`）。

## 诊断与测试
- 调用 `BeanRegistry#snapshot()` 或 `ApplicationContext.snapshot()` 查看当前注册键。
- 注册表内建循环依赖防护，检测到时会抛出包含依赖链的 `BeanResolutionException`。
- 单元测试可用 Stub `ApplicationContext` + 手工注册定义，或直接调用组件扫描定位到特定包。

## 与其他模块的交互
- `alamafa-bootstrap` 会把 DI 生命周期参与者加入启动流程，使 `AlamafaApplication` 自动准备容器。
- JavaFX 元数据处理器（`FxViewMetadataProcessor`、`FxViewModelMetadataProcessor`）是普通 `BeanPostProcessor`，由 `alamafa-jfx-starter` 自动注册。
