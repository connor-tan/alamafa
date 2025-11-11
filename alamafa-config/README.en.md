# Alamafa Config

## Overview
`alamafa-config` supplies configuration primitives layered on top of the core `ApplicationContext`. It aggregates multiple property sources, exposes a typed `Configuration` view, and binds strongly-typed POJOs annotated with `@ConfigurationProperties` into DI-managed beans.

## Components
- **`ConfigurationLoader`** – fluent builder that merges ordered `ConfigurationSource`s. `withDefaults()` loads:
  1. `application.properties`
  2. Optional profile file resolved via `alamafa.profile` JVM prop or `ALAMAFA_PROFILE` env
  3. Environment variables (filtered by prefix, e.g. `ALAMAFA_`)
  4. System properties (highest priority)
  Each source is tagged with a `Priority`; later sources override keys from lower priorities. `requireKeys()` enforces presence of critical properties before returning the final `Configuration`.
- **`ConfigurationSource` implementations** – `ClasspathPropertiesSource`, `EnvironmentVariablesSource`, `SystemPropertiesSource`, and `MapConfigurationSource`. Custom sources can implement the interface and be appended via `addSource`.
- **`Configuration`** – immutable value-object offering `Optional` getter, fallback getters for `String`, `int`, `boolean`, etc., and a `snapshot()` for diagnostics.
- **`ConfigurationBinder`** – reflection-based binder that maps hierarchical keys onto fields or setters. It recognises both dot-separated (`logging.level`) and kebab-case (`logging-level`) notations and honours nested prefixes supplied via annotation or direct parameters.
- **`@ConfigurationProperties`** – annotation to mark POJOs that should be bound automatically. When combined with `alamafa-di`, `ConfigurationPropertiesBinderPostProcessor` will call `ConfigurationBinder` after bean creation.

## Typical Flow
1. Create or obtain a `ConfigurationLoader` (DI uses `withDefaults()` automatically).
2. Call `load()` to get a `Configuration` and store it in the `ApplicationContext` (handled by `DiRuntimeBootstrap`).
3. Annotate property classes with `@ConfigurationProperties(prefix = "theme")` to have fields bound lazily by the DI post-processor.

## Development Tips
- **Profiles**: pass `-Dalamafa.profile=dev` or export `ALAMAFA_PROFILE=dev` to cascade profile-specific properties.
- **Custom sources**: e.g. to merge a remote secrets map, implement `ConfigurationSource` and register it with `addSource(secretSource, HIGH)`. The loader preserves declaration order before sorting by priority.
- **Validation**: throw inside setters if values are out of range (see `PlayerProperties#setWindowWidth`). The binder surfaces those exceptions early.

## Related Modules
- `alamafa-di` auto-loads configuration and feeds it into the bean registry.
- Feature modules such as `alamafa-theme` and `alamafa-jfx-vlcj` annotate their property holders to benefit from this binder.
