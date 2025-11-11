# Alamafa Framework Overview

Alamafa is a lightweight bootstrap + dependency injection framework for desktop / rich-client applications. The repository is split into Maven modules that can be published individually or combined as needed.

## Module Map

| Module | Description |
| --- | --- |
| `alamafa-core` | Core lifecycle contracts, `ApplicationContext`, event publisher, logging facade, health checks. |
| `alamafa-config` | Configuration loading & binding (classpath / env / system overrides) plus `@ConfigurationProperties` POJOs. |
| `alamafa-di` | Annotation-driven DI container with component scanning, conditional wiring, bean post-processors, `ApplicationRunner` / `CommandLineRunner`. |
| `alamafa-bootstrap` | SpringApplication-style launcher that scans `@AlamafaBootApplication`, discovers auto-configurations, and coordinates the DI lifecycle. |
| `alamafa-jfx-*` | JavaFX integration: launcher (bridging FX lifecycle), view loading/metadata, view-model & window management, starter auto-config. |
| `alamafa-logging-starter` | Logging auto-config (SLF4J + Logback, JUL bridge). |
| `alamafa-sample` | Sample projects (Greeting starter, CLI + JavaFX MVVM demos). |
| `alamafa-sample/tower-client` | Desktop monitoring client built with the JavaFX stack (login + dashboard). |

## JavaFX Platform Profiles

The root `pom.xml` sets `javafx.platform=linux` and activates OS-specific profiles (Windows → `win`, macOS Intel → `mac`, macOS ARM → `mac-aarch64`). Override the classifier when building on different environments:

```bash
mvn -Djavafx.platform=win compile        # Windows
mvn -Djavafx.platform=mac compile        # macOS Intel
mvn -Djavafx.platform=mac-aarch64 compile # macOS ARM
```
Use Maven 3.8+ and JDK 21 to ensure JavaFX artifacts unpack correctly.

## Build & Test

Full build (skip tests):

```bash
mvn -DskipTests install
```

Build the JavaFX sample only:

```bash
mvn -pl alamafa-sample/sample-jfx-mvvm -am package
```

### JavaFX Tests

`alamafa-jfx-view` and `alamafa-jfx-viewmodel` host regression tests for shared view caching and window management. They rely on a JavaFX Toolkit and auto-skip when unavailable:

```bash
mvn -pl alamafa-jfx/alamafa-jfx-view,alamafa-jfx/alamafa-jfx-viewmodel test
```

On headless CI, skip explicitly:

```bash
mvn -pl alamafa-jfx/alamafa-jfx-view,alamafa-jfx/alamafa-jfx-viewmodel -DskipTests test
```

## Developer Tips

1. **New modules**: register them in the root POM and inherit the parent to reuse dependency management.
2. **Auto-configuration**: declare classes in `META-INF/alamafa.factories` and annotate with `@AutoConfiguration`; bootstrap loads them automatically.
3. **Dependency injection**: use `@Component`, `@Service`, `@Configuration`; `@Bean` methods support conditional wiring and property binding.
4. **JavaFX integration**: describe views with `@FxViewSpec`, view-models with `@FxViewModelSpec`, and manage windows via `FxWindowManager`.

Refer to module sources and tests for deeper details, and feel free to open issues for bugs or improvements.
