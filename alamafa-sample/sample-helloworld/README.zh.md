# Sample HelloWorld Application

## 概述
这是最精简的 Alamafa 应用，依赖 `alamafa-bootstrap`、Greeting Starter 与 Logging Starter，并通过 `@AlamafaBootApplication` 启动。

## 入口
```java
@AlamafaBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        AlamafaApplication.run(HelloWorldApplication.class, args);
    }
}
```
无需自定义 `Lifecycle`，Greeting Starter 的 `ApplicationRunner` 会在启动阶段输出问候。

## 配置
`src/main/resources/application.properties` 中覆盖了 greeting target：
```
greeting.target=Alamafa Developer Connor
```
可通过 CLI（`--greeting.target=Alice`）或环境变量（`ALAMAFA_GREETING_TARGET`）再次覆盖。

## 运行
```
mvn -pl alamafa-sample/sample-helloworld -am exec:java \
    -Dexec.mainClass=com.alamafa.sample.helloworld.HelloWorldApplication
```

## 要点
- 演示 `AlamafaApplication` 如何在未显式传参的情况下自动定位带注解的主类。
- 展示 Starter 提供的 Runner 与配置如何即插即用。
