# Sample Greeting Starter

## Purpose
This starter illustrates how to package Alamafa auto-configuration. It exports a `GreetingService`, binds configuration properties, runs an `ApplicationRunner`, and registers an `ApplicationStartedEvent` listener.

## Auto-Configured Components
- **`GreetingProperties`** (`@ConfigurationProperties("greeting")`): holds `target` and `enabled` flags.
- **`GreetingService`** (default `ConsoleGreetingService`): logs a greeting message via `AlamafaLogger`.
- **`ApplicationRunner`** (`GreetingRunner`): greets on startup, optionally overriding the target with CLI options `--name=` or `--target=`.
- **`ApplicationStartedEvent` listener**: emits a log once the app is fully started (only when greetings are enabled).

All beans are registered inside `GreetingAutoConfiguration`, which is exported through `META-INF/alamafa.factories`. Conditions:
- `@ConditionalOnMissingBean` ensures consumers can supply custom `GreetingService`/properties.
- `@ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)` gates the runner.

## Usage
Add the dependency and enable the starter via classpath:
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>sample-greeting-starter</artifactId>
</dependency>
```
Configure in `application.properties`:
```
greeting.target=Alamafa Developer
greeting.enabled=true
```

## Development Notes
- `GreetingRunner` implements `getOrder()` returning `Integer.MIN_VALUE` so it runs before other runners.
- The starter demonstrates how to combine DI, configuration binding, and event listeners within a single module—use it as a template for future starters.
