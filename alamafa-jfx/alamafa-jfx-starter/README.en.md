# Alamafa JavaFX Starter

## Overview
The starter bundles auto-configuration (`META-INF/alamafa.factories`) that wires all JavaFX beans when `javafx.*` classes are present. It removes boilerplate so consumers simply depend on `alamafa-jfx-starter` and point `@AlamafaBootApplication` at `JavaFxApplicationLauncher`.

## Auto-Configured Beans
| Bean | Description |
| --- | --- |
| `FxViewRegistry` | Thread-safe storage for `FxViewDescriptor`s. |
| `FxViewLoader` | Preconfigured loader that resolves resources from the classpath and integrates with DI. |
| `FxViewMetadataProcessor` | Captures `@FxViewSpec` metadata after bean instantiation. |
| `FxViewModelRegistry` | Maintains descriptor metadata + application-scoped cache. |
| `FxViewModelMetadataProcessor` | Registers `@FxViewModelSpec` metadata. |
| `FxViewModelBinder` | Resolves and attaches view-models to controllers/views using the registry and bean factory. |
| `FxWindowManager` | Centralised window API (depends on context, loader, binder, registry). |
| `FxPrimaryStageLifecycle` | Hooks into application lifecycle and mounts the descriptor marked `primary=true` on the JavaFX primary stage. |

All beans honour `@ConditionalOnClass` to avoid loading when JavaFX is absent.

## Usage Steps
1. Add dependency:
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-jfx-starter</artifactId>
</dependency>
```
2. Annotate the entrypoint:
```java
@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class MyApp { ... }
```
3. Annotate views/view-models as needed. The starter ensures metadata processors run and that `FxWindowManager` is ready.

## Configuration Overrides
- Core JavaFX platform classifier inherits from the root property `javafx.platform`. Override via command line when cross-compiling: `mvn -Djavafx.platform=win package`.
- Window hints can be overridden at runtime via `application.properties` keys: `jfx.window.<descriptor-name>.title=Custom Title`, `.width`, `.height`, `.resizable`.

## Testing Notes
- JavaFX tests require a toolkit. When not available, mark tests with conditional assumptions (see `FxAutoConfigurationTest` for reference) or skip via `-Dtestfx.skip=true` style flags.
