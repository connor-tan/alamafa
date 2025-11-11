# Alamafa Bootstrap

## Overview
`alamafa-bootstrap` is the opinionated entry point that mirrors Spring Boot’s `SpringApplication`. It locates the primary class annotated with `@AlamafaBootApplication`, wires the core runtime, registers auto-configurations, and hands control to DI + lifecycle modules. Every executable sample in this repo ultimately calls `AlamafaApplication.run(...)`.

## `@AlamafaBootApplication`
The annotation exposes four knobs:
- `scanBasePackages` / `scanBasePackageClasses` – override component-scan roots for `alamafa-di`.
- `modules` – extra classes that either implement `AlamafaBootstrapModule`, `Lifecycle`, or are themselves `@Configuration` classes. They can register additional scanners, lifecycle participants, or configuration imports.
- `launcher` – selects a `ContextAwareApplicationLauncher` (defaults to `DefaultApplicationLauncher`; JavaFX apps pass `JavaFxApplicationLauncher`).

## Bootstrapping Pipeline
1. `AlamafaApplication.run` resolves the primary source (StackWalker) and validates the annotation.
2. It instantiates the requested launcher and wraps it in `ApplicationBootstrap`.
3. `AlamafaBootstrapContext` collects base packages + explicit configuration classes, exposing helper methods for modules to add lifecycle participants or context initialisers.
4. `AutoConfigurationLoader` scans `META-INF/alamafa.factories` resources for classes annotated with `@AutoConfiguration`; each is added to the configuration set (mirrors the Spring `spring.factories` contract but namespaced).
5. Modules from the annotation are instantiated. If they implement:
   - `AlamafaBootstrapModule`: `configure(AlamafaBootstrapContext)` is invoked;
   - `Lifecycle`: registered directly with `ApplicationBootstrap#addLifecycleParticipant`.
   Configuration-annotated modules are queued for DI even if they do not implement the above interfaces.
6. Finally, `DiRuntimeBootstrap` is built with all discovered packages/configurations and added as a lifecycle participant before `bootstrap.launch()` executes.

## Extending Bootstrap
- **Custom auto-config**: add `META-INF/alamafa.factories` to your module and list fully qualified classes annotated with `@AutoConfiguration`. They will be discovered automatically at runtime.
- **Bootstrap module**: implement `AlamafaBootstrapModule` to register extra context initialisers (e.g., metrics registries) or to add bespoke lifecycle participants (schedulers, IPC bridges).
- **Alternative launchers**: implement `ContextAwareApplicationLauncher` for non-JavaFX environments (e.g., SWT, Android) and reference it from `@AlamafaBootApplication(launcher = ...)`.

## Files of Interest
- `AlamafaApplication` – main entrypoint.
- `AlamafaBootApplication` – annotation definition.
- `AlamafaBootstrapContext` – mutable state passed to bootstrap modules.
- `AlamafaBootstrapModule` – SPI for modules to contribute to the pipeline.
- `AutoConfigurationLoader` – reads `META-INF/alamafa.factories` and filters entries for the `AutoConfiguration` key.

## Usage Example
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
The example adds JavaFX bootstrapping and includes theme auto-configuration before the DI container starts.
