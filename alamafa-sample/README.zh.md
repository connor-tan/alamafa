# Alamafa Samples

该目录汇总可运行示例，展示如何组合核心运行时、DI、JavaFX 栈与 VLCJ 集成。所有示例模块继承父 POM，可通过 `mvn -pl <module> -am package` 单独构建。

## 模块
| 模块 | 亮点 |
| --- | --- |
| [`sample-greeting-starter`](./sample-greeting-starter) | 微型自动配置：注册 `GreetingService`、绑定 `GreetingProperties`、发布启动事件并附带 `ApplicationRunner`，演示如何通过 `META-INF/alamafa.factories` 构建 Alamafa Starter。 |
| [`sample-helloworld`](./sample-helloworld) | 最小 CLI 应用，使用 `@AlamafaBootApplication`，依赖 greeting starter 输出欢迎语，并展示 `application.properties` 覆盖配置的方式。 |
| [`sample-jfx-mvvm`](./sample-jfx-mvvm) | 基于 `alamafa-jfx-starter` 的 JavaFX MVVM 示例，包含 FXML 视图、`@FxViewSpec` 控制器、应用级与视图级 ViewModel，以及使用 `FxWindowManager` 打开模态窗口。 |
| [`tower-client`](./tower-client) | 更完整的桌面客户端，结合认证 API、加密凭据存储、主题系统、监控面板与 VLCJ 视频墙，并附带 API/Session/ViewModel 单元测试。 |

## 运行示例
1. **Greeting CLI**
   ```bash
   mvn -pl alamafa-sample/sample-helloworld -am exec:java \
       -Dexec.mainClass=com.alamafa.sample.helloworld.HelloWorldApplication
   ```
2. **JavaFX 示例**（非 Linux 平台记得设置 classifier）：
   ```bash
   mvn -pl alamafa-sample/sample-jfx-mvvm -am -Djavafx.platform=win javafx:run
   ```
3. **Tower Client**：与上类似，将模块切换为 `tower-client`，应用会先弹出登录窗口，再通过 `FxWindowManager` 打开额外 Stage。

## 结构说明
- 所有模块复用共享 BOM，并继承根项目的 `maven-surefire-plugin` 配置。
- JavaFX 模块读取 `application.properties` 以覆盖主题/窗口配置，依赖 Starter 自动装配。
- 测试文件与源码同级（如 `tower-client/src/test/java/...`），可通过 `mvn -pl alamafa-sample/tower-client test` 运行。
