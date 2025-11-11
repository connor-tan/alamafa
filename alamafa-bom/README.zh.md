# Alamafa BOM

## 目标
`alamafa-bom` 是一个 Maven Bill of Materials，用于锁定全仓库共用的三方依赖版本（如 JUnit、SLF4J、Logback、JavaFX Classifier 等）。引入该 BOM 后，所有消费者都会在相同的传递依赖树上构建，避免 Starter、DI、JavaFX 组件混用时出现菱形依赖冲突。

## 使用方式
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
导入后即可省略 Alamafa 模块的 `<version>`：
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-jfx-starter</artifactId>
</dependency>
```

## 受管坐标
| 依赖 | 版本属性 | 说明 |
| --- | --- | --- |
| `org.slf4j:slf4j-api` | `${slf4j.version}` | `alamafa-core` 使用的核心日志门面。 |
| `ch.qos.logback:logback-classic` | `${logback.version}` | `alamafa-logging-starter` 默认提供的后端。 |
| `org.junit.jupiter:*` | `${junit.jupiter.version}` | 所有使用 JUnit Jupiter 的模块（示例、DI 单测等）共享。 |

如需新增对齐的依赖，请在此维护，确保子模块一致。

## 维护建议
- **单一事实来源**：只需在此 `pom.xml` 中升级版本，所有子模块立即继承。
- **兼容性验证**：升级后执行 `mvn -pl :alamafa-bom -am verify`，确保下游没有覆盖冲突。
- **CI 提醒**：BOM 变更可能影响发布，请在根 `CHANGELOG`（若存在）中记录，方便使用者知晓升级信息。
