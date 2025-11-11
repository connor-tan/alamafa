# Sample HelloWorld Application

## Overview
This is the smallest possible Alamafa application. It pulls in `alamafa-bootstrap`, the greeting starter, and the logging starter, then launches via `@AlamafaBootApplication`.

## Entry Point
```java
@AlamafaBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        AlamafaApplication.run(HelloWorldApplication.class, args);
    }
}
```
No custom `Lifecycle` is required—the greeting starter’s `ApplicationRunner` prints output during startup.

## Configuration
`src/main/resources/application.properties` overrides the greeting target:
```
greeting.target=Alamafa Developer Connor
```
Override it via CLI (`--greeting.target=Alice`) or environment (`ALAMAFA_GREETING_TARGET`).

## Running
```
mvn -pl alamafa-sample/sample-helloworld -am exec:java \
    -Dexec.mainClass=com.alamafa.sample.helloworld.HelloWorldApplication
```

## Takeaways
- Shows how `AlamafaApplication` auto-discovers the annotated class without passing it explicitly to `run`.
- Demonstrates how starter-provided runners and configuration work out of the box.
