# Alamafa BOM

## Purpose
`alamafa-bom` is a Maven Bill of Materials that pins the versions of third-party libraries shared across all modules (JUnit, SLF4J, Logback, JavaFX classifiers, etc.). Importing this BOM ensures every consumer builds against the same transitive stack, avoiding diamond conflicts when mixing starters, DI, and JavaFX artefacts.

## How to Consume
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.alamafa</groupId>
      <artifactId>alamafa-bom</artifactId>
      <version>${alamafa.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
After importing, declare Alamafa modules without explicit versions:
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-jfx-starter</artifactId>
</dependency>
```

## Managed Coordinates
| Artifact | Version Property | Notes |
| --- | --- | --- |
| `org.slf4j:slf4j-api` | `${slf4j.version}` | Core logging facade used by `alamafa-core`. |
| `ch.qos.logback:logback-classic` | `${logback.version}` | Default backend shipped by `alamafa-logging-starter`. |
| `org.junit.jupiter:*` | `${junit.jupiter.version}` | Shared by every module that runs Jupiter tests (sample apps, DI unit tests, etc.). |

Add new aligned dependencies here to keep every submodule consistent.

## Maintenance Tips
- **Single source of truth**: bump versions once in `pom.xml` → all downstream modules inherit them immediately.
- **Compatibility testing**: after upgrading a dependency, run `mvn -pl :alamafa-bom -am verify` to ensure no conflicting overrides remain in child modules.
- **CI**: treat BOM changes as release-impacting; update the root `CHANGELOG` (if present) so consumers know about upgrades.
