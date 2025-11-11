# Alamafa 框架概览

Alamafa 是一个面向桌面/富客户端应用的轻量级引导与依赖注入框架。代码仓库按功能拆分为多个 Maven 模块，可独立发布或组合使用。

## 模块结构

| 模块 | 说明 |
| --- | --- |
| `alamafa-core` | 生命周期协议、`ApplicationContext`、事件发布器、日志门面、健康检查等核心基础设施。 |
| `alamafa-config` | 配置加载与绑定能力（Classpath/环境变量/系统属性叠加）、`@ConfigurationProperties` POJO 绑定。 |
| `alamafa-di` | 注解驱动的依赖注入容器，支持组件扫描、条件装配、Bean 后处理器、`ApplicationRunner` / `CommandLineRunner`。 |
| `alamafa-bootstrap` | 对标 SpringApplication 的启动器，实现 `@AlamafaBootApplication` 扫描、自动配置发现、DI 生命周期管理。 |
| `alamafa-jfx-*` | JavaFX 集成：Launcher（桥接 FX 生命周期）、视图装载/元数据、ViewModel & Window 管理、Starter 自动配置。 |
| `alamafa-logging-starter` | Logging 自动化配置（SLF4J + Logback、JUL Bridge）。 |
| `alamafa-sample` | 示例工程（Greeting Starter、CLI + JavaFX MVVM Demo）。 |
| `alamafa-sample/tower-client` | 使用 Alamafa JavaFX 栈构建的桌面监控客户端样例（登录 + 主界面）。 |

## JavaFX 平台配置

项目默认在 `pom.xml` 中声明 `javafx.platform=linux`，并根据操作系统自动激活对应 Profile（Windows → `win`、macOS Intel → `mac`、macOS ARM → `mac-aarch64`）。若需要在 CI 或其他平台上构建：

```bash
mvn -Djavafx.platform=win compile   # Windows
mvn -Djavafx.platform=mac compile   # macOS Intel
mvn -Djavafx.platform=mac-aarch64 compile  # macOS ARM
```

为确保 JavaFX 依赖可解压，建议使用 Maven 3.8+ 与 JDK 21。

## 构建与测试

全量构建（忽略测试）：

```bash
mvn -DskipTests install
```

单独构建示例 JavaFX 应用：

```bash
mvn -pl alamafa-sample/sample-jfx-mvvm -am package
```

### JavaFX 相关测试

`alamafa-jfx-view` 与 `alamafa-jfx-viewmodel` 模块提供对共享视图缓存与窗口管理的回归测试。测试依赖 JavaFX Toolkit，如果运行环境不支持会自动跳过：

```bash
mvn -pl alamafa-jfx/alamafa-jfx-view,alamafa-jfx/alamafa-jfx-viewmodel test
```

在无图形界面的 CI 场景，可显式跳过：

```bash
mvn -pl alamafa-jfx/alamafa-jfx-view,alamafa-jfx/alamafa-jfx-viewmodel -DskipTests test
```

## 开发者指南

1. **新增模块**：在根 POM 中登记 module，并确保子模块继承父 POM 以复用版本管理。
2. **自动配置**：通过 `META-INF/alamafa.factories` 声明并使用 `@AutoConfiguration` 注解。Bootstrap 会在启动时自动加载。
3. **依赖注入**：使用 `@Component`、`@Service`、`@Configuration` 等注解；`@Bean` 方法支持条件装配、配置属性绑定。
4. **JavaFX 集成**：视图使用 `@FxViewSpec`、ViewModel 使用 `@FxViewModelSpec` 描述元数据；通过 `FxWindowManager` 管理窗口生命周期。

更多细节请参考各模块源码与单元测试。欢迎在 issue 中反馈缺陷或提出改进建议。
