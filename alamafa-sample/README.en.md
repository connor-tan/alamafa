# Alamafa Samples

This directory groups runnable samples that demonstrate how to combine the core runtime, DI, JavaFX stack, and VLCJ integration. All sample modules inherit from the parent POM and can be built individually via `mvn -pl <module> -am package`.

## Modules
| Module | Highlights |
| --- | --- |
| [`sample-greeting-starter`](./sample-greeting-starter) | A tiny auto-configuration that registers `GreetingService`, binds `GreetingProperties`, emits startup events, and ships an `ApplicationRunner`. Shows how to create Alamafa starters with `META-INF/alamafa.factories`. |
| [`sample-helloworld`](./sample-helloworld) | Minimal CLI app using `@AlamafaBootApplication`. Depends on the greeting starter to print a welcome message and demonstrates property overrides via `application.properties`. |
| [`sample-jfx-mvvm`](./sample-jfx-mvvm) | JavaFX MVVM example using `alamafa-jfx-starter`. Includes FXML views, controllers annotated with `@FxViewSpec`, application-scoped vs per-view view-models, and use of `FxWindowManager` to open modal windows. |
| [`tower-client`](./tower-client) | A richer desktop client that combines authentication APIs, encrypted credential storage, theming, monitoring dashboards, and VLCJ-powered video walls. It also features unit tests for API, session, and view-model layers. |

## Running Samples
1. **Greeting CLI**
   ```bash
   mvn -pl alamafa-sample/sample-helloworld -am exec:java \
       -Dexec.mainClass=com.alamafa.sample.helloworld.HelloWorldApplication
   ```
2. **JavaFX Samples** – set the JavaFX classifier if you are not on Linux:
   ```bash
   mvn -pl alamafa-sample/sample-jfx-mvvm -am -Djavafx.platform=win javafx:run
   ```
3. **Tower Client** – same steps as above but target `tower-client`. The app launches a login window and spawns additional stages via `FxWindowManager`.

## Structure Notes
- Every module reuses the shared BOM and inherits `maven-surefire-plugin` configuration from the root.
- JavaFX modules read `application.properties` for theme/window overrides and rely on the starter’s auto-configuration.
- Tests live next to the code (`tower-client/src/test/java/...`) and can be run with `mvn -pl alamafa-sample/tower-client test`.
