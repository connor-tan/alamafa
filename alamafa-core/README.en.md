# Alamafa Core

## Overview
`alamafa-core` is the runtime nucleus that everything else builds upon. It defines the minimal application lifecycle contract, an in-memory `ApplicationContext`, synchronous eventing, logging facades, health probes, and runner abstractions. Higher-level modules (DI, JavaFX, logging starter, etc.) plug into these types to participate in bootstrap/start/stop operations without re-implementing boilerplate.

## Key Building Blocks
- **Lifecycle orchestration** – `Lifecycle`, `LifecyclePhase`, and `LifecycleExecutionException` standardise init/start/stop semantics and error propagation. `ApplicationLifecycle` extends `Lifecycle` with ordering so DI-managed beans can hook into the runtime.
- **Application bootstrapper** – `ApplicationBootstrap` wires a `ContextAwareApplicationLauncher` (e.g. `DefaultApplicationLauncher` or JavaFX launcher) with context initialisers, lifecycle participants, and an `ApplicationEventPublisher`. It publishes `ApplicationStarting/Started/Stopp*` events around lifecycle execution and funnels failures through `LifecycleErrorHandler`.
- **Context & arguments** – `ApplicationContext` is a lightweight typed/kv registry shared across modules. `ApplicationArguments` parses CLI options and is stored in the context for reuse by runners or components.
- **Lifecycle-aware launcher** – `DefaultApplicationLauncher` owns a JVM lifecycle, installs a shutdown hook, and exposes `ApplicationShutdown` to request a graceful stop. Any custom launcher only needs to implement `ContextAwareApplicationLauncher`.
- **Event bus** – `DefaultApplicationEventPublisher` keeps a thread-safe listener list and dispatches synchronously while filtering by listener-declared event type. Listener registration is exposed to DI (see `ApplicationEventListener`).
- **Health checks** – `HealthRegistry`, `HealthCheck`, `HealthIndicator`, and `HealthStatus` describe a pull-based health snapshot. Modules register checks in the context under `HealthRegistry.CONTEXT_KEY`.
- **Runners** – `ApplicationRunner` and `CommandLineRunner` provide ordered hooks that run after lifecycle `start`, mirroring the Spring Boot pattern.

## Execution Flow
1. Construct a launcher (most apps use `DefaultApplicationLauncher`).
2. Wrap it with `ApplicationBootstrap`, register any context initialisers or extra `Lifecycle` participants (DI bootstrap, schedulers, etc.).
3. Call `launch` with your main `Lifecycle`. Bootstrap publishes `ApplicationStarting`, runs initialisers, and then delegates to the launcher.
4. The launcher invokes init/start/stop, reporting failures to `LifecycleErrorHandler` stored in the context. Stop order is reversed to guarantee LIFO cleanup.

## Usage Notes & Extensibility
- **Adding lifecycle participants**: `ApplicationBootstrap#addLifecycleParticipant` is the canonical hook used by `DiRuntimeBootstrap`, JavaFX window lifecycles, or custom services.
- **Context seeding**: Register singletons (config, metrics, schedulers) via `ApplicationContext#put` before DI initialises so downstream modules can reuse them.
- **Events**: Publish domain events via `ApplicationEventPublisher` fetched from the context; register listeners by implementing `ApplicationEventListener<T>` and adding them either manually or through DI (`BeanRegistry` automatically wires them when the publisher exists).
- **Health checks**: Register via `HealthRegistry#register(name, check)`; `HealthRegistry#snapshot()` is typically exposed via diagnostics endpoints.
- **Error handling**: Override `LifecycleErrorHandler` through `ApplicationBootstrap#onError` to integrate with custom logging or alerting.

## Quick Start
```java
ContextAwareApplicationLauncher launcher = new DefaultApplicationLauncher();
ApplicationBootstrap bootstrap = new ApplicationBootstrap(launcher);
bootstrap.addLifecycleParticipant(new DiRuntimeBootstrap.Builder()
        .scanPackages("com.example.app")
        .build());
bootstrap.launch(Lifecycle.NO_OP);
```
This snippet starts Alamafa with DI support only; real apps pass a concrete `Lifecycle` (e.g. HTTP server) to `launch`.

## Related Modules
- `alamafa-config` stores the resulting `Configuration` object inside the core `ApplicationContext`.
- `alamafa-di` registers its `BeanRegistry` and uses core runners/events/health infrastructure.
- JavaFX launchers (`alamafa-jfx-launcher`) implement `ContextAwareApplicationLauncher` defined here.
