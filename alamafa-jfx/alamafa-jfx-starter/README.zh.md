# Alamafa JavaFX Starter

## 概述
该 Starter 在 `META-INF/alamafa.factories` 中声明自动配置，当类路径存在 `javafx.*` 时会自动装配所有 JavaFX 相关 Bean。这样使用者只需依赖 `alamafa-jfx-starter`，并在 `@AlamafaBootApplication` 中指定 `JavaFxApplicationLauncher` 即可。

## 自动配置的 Bean
| Bean | 说明 |
| --- | --- |
| `FxViewRegistry` | 线程安全的 `FxViewDescriptor` 存储。 |
| `FxViewLoader` | 预配置的视图加载器，从 classpath 解析资源并集成 DI。 |
| `FxViewMetadataProcessor` | 在 Bean 创建后捕获 `@FxViewSpec` 元数据。 |
| `FxViewModelRegistry` | 维护 ViewModel 描述及应用作用域缓存。 |
| `FxViewModelMetadataProcessor` | 注册 `@FxViewModelSpec` 元数据。 |
| `FxViewModelBinder` | 基于注册表与 BeanFactory 解析并绑定 ViewModel。 |
| `FxWindowManager` | 中央窗口 API，依赖上下文、Loader、Binder、Registry。 |
| `FxPrimaryStageLifecycle` | 接入应用生命周期，自动挂载 `primary=true` 的视图到 JavaFX 主舞台。 |

所有 Bean 均带 `@ConditionalOnClass`，在缺少 JavaFX 时不会加载。

## 使用步骤
1. 添加依赖：
```xml
<dependency>
  <groupId>com.alamafa</groupId>
  <artifactId>alamafa-jfx-starter</artifactId>
</dependency>
```
2. 标注入口类：
```java
@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class MyApp { ... }
```
3. 根据需要为视图 / ViewModel 加注解。Starter 会启动元数据处理器并准备好 `FxWindowManager`。

## 配置覆盖
- JavaFX 平台 classifier 继承自根属性 `javafx.platform`，交叉编译时可通过命令行覆盖：`mvn -Djavafx.platform=win package`。
- 窗口提示可在运行期通过 `application.properties` 覆盖：`jfx.window.<descriptor-name>.title=Custom Title` 及 `.width/.height/.resizable`。

## 测试注意
- JavaFX 测试需要 Toolkit。如不可用，可在测试中加条件假设（参考 `FxAutoConfigurationTest`），或通过 `-Dtestfx.skip=true` 跳过。
