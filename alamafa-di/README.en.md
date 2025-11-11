# Alamafa DI

## Overview
`alamafa-di` is the annotation-driven dependency injection engine used throughout the project. It provides component scanning, configuration-class processing, bean scopes, conditional metadata, and lifecycle-aware post processing. The runtime is intentionally lightweight yet expressive enough to cover starters, auto-configuration, and sample applications.

## Runtime Pieces
- **`BeanRegistry`** – central registry that stores `BeanDefinition`s keyed by type and/or name, caches singleton instances, and tracks `@PreDestroy` callbacks. It exposes `get(Class)`, `get(String)`, `hasBeanDefinition`, and query helpers used by other modules.
- **`BeanDefinition` & scopes** – a record describing bean type, creation `BeanSupplier`, scope (`SINGLETON` or `PROTOTYPE`), `primary`, and `lazy` flags. The registry enforces singleton caching and circular-dependency detection per definition.
- **`DiRuntimeBootstrap`** – implements `Lifecycle`. During `init` it ensures a `BeanRegistry` and `Configuration` exist in the `ApplicationContext`, registers configuration classes supplied by `builder.withConfigurations(...)`, scans packages for components, runs singleton post-processors, and attaches `ApplicationLifecycle`, `ApplicationRunner`, `CommandLineRunner`, and `ApplicationEventListener` beans to the core runtime.
- **Configuration processing** – `ConfigurationProcessor` recognises `@Configuration`, `@Import`, `@Bean`, and implicit configuration stereotypes discovered via package scanning. It handles `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`, and custom annotations meta-annotated with `@Component`.
- **Component scanning** – `ComponentScanner` traverses the classpath and turns stereotype annotations (e.g. `@Component`, `@Service`, `@FxViewSpec`, `@FxViewModelSpec`) into `ComponentDefinition`s using `ComponentDefinitionFactory`. Constructor injection is resolved automatically, supports `@Inject`, `@Qualifier`, `@OptionalDependency`, collections, and `Optional<T>` parameters.
- **Bean post processing** – `BeanPostProcessorChain` executes registered processors after instantiation. Examples: `ConfigurationPropertiesBinderPostProcessor` binds `@ConfigurationProperties` beans; JavaFX metadata processors plug in via the chain.

## Annotations
- `@Component`, `@Service`, `@Configuration`, `@Bean`, `@Import`
- Conditional annotations: `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`
- Lifecycle & injection helpers: `@Inject`, `@Qualifier`, `@OptionalDependency`, `@PostConstruct`, `@PreDestroy`

## Extending the Container
1. Include the module and create a `DiRuntimeBootstrap` via `DiRuntimeBootstrap.builder()`.
2. Provide packages to scan (`scanPackages(String...)`) and configuration classes (`withConfigurations(Class<?>...)`).
3. Optionally add custom `BeanPostProcessor`s by registering singleton beans that implement the interface.
4. Use conditions to build starter-like behaviour (e.g. `@ConditionalOnProperty(prefix = "logging", name = "jul-bridge")`).

## Diagnostics & Testing
- Call `BeanRegistry#snapshot()` or `ApplicationContext.snapshot()` to inspect registered keys.
- The registry guards against recursive construction and will throw `BeanResolutionException` with the offending dependency chain when cycles happen.
- For units tests, instantiate `BeanRegistry` with a stubbed `ApplicationContext` and register definitions manually, or use the component scanner against a dedicated package.

## Interaction With Other Modules
- `alamafa-bootstrap` adds the DI lifecycle participant so that `AlamafaApplication` automatically prepares the container.
- JavaFX metadata processors (`FxViewMetadataProcessor`, `FxViewModelMetadataProcessor`) are ordinary `BeanPostProcessor`s registered via `alamafa-jfx-starter`.
