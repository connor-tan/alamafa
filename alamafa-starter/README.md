# Alamafa Starter

## Purpose
Aggregate the core framework modules (core, config, di) into a single dependency to simplify adoption.

## Usage (Maven)
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
Optionally also import the BOM for version pinning:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.alamafa</groupId>
      <artifactId>alamafa-bom</artifactId>
      <version>1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Quick Start
```java
public class DemoApp {
    public static void main(String[] args) {
        var ctx = new com.alamafa.core.ApplicationContext();
        var registry = new com.alamafa.di.BeanRegistry(ctx);
        // Scan your application packages
        registry.scanPackages("com.example.app");
        // Acquire a bean
        MyService svc = registry.get(MyService.class);
        svc.run();
    }
}
```

## Provided Modules
- alamafa-core
- alamafa-config
- alamafa-di

Logback is pulled in at runtime scope for convenience. Replace or exclude if you use another backend.

