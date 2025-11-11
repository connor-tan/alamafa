# Alamafa Logging Starter

## Purpose
The logging starter auto-configures sane defaults around the SLF4J/Logback stack bundled in `alamafa-bom`. It focuses on two runtime conveniences:
1. Install SLF4J’s JUL bridge when requested so legacy libraries that log via `java.util.logging` flow into Logback.
2. Emit a simple startup diagnostic event with the current `ApplicationContext` keys to verify what the runtime injected.

## Auto-Configured Beans (`META-INF/alamafa.factories`)
- `loggingContextInitializer` – `ApplicationEventListener<ApplicationStartedEvent>` that logs the context snapshot once the application reaches `START`.
- `slf4jJulBridgeInitializer` – Optional listener gated by `logging.jul-bridge=true`. On startup it reflects on `org.slf4j.bridge.SLF4JBridgeHandler`, removes default JUL handlers, and installs the bridge.

Both beans are annotated with `@ConditionalOnMissingBean` so that custom implementations can override them.

## Usage
1. Add the dependency:
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-logging-starter</artifactId>
</dependency>
```
2. (Optional) Enable JUL bridging in `application.properties`:
```
logging.jul-bridge=true
```
3. Start the app. The starter will log when the bridge is installed and when the context keys are available.

## Customisation Tips
- Provide your own bean named `slf4jJulBridgeInitializer` to change the bridging strategy (e.g., install log rotation first).
- Bind extra configuration (log level, appenders) using Logback XML in your module; the starter ships with minimal defaults and leaves layout control to consuming apps.
- Since the beans are plain event listeners, they can interact with `HealthRegistry`, metrics, etc., via the injected `ApplicationContext` that `@Bean` methods receive.
