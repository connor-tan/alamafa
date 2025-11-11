# Alamafa Theme

## 概述
`alamafa-theme` 为 JavaFX 应用提供简易的全局主题服务，可在自带的浅色/深色主题间切换。它通过自动配置暴露 `ThemeManager` 与 `ThemeProperties`，与 Alamafa DI 集成，并使用 `java.util.prefs.Preferences` 记住最后一次选择。

## 组件
- **`Theme` 枚举**：`LIGHT` 或 `DARK`。
- **`ThemeDefinition`**：将主题与样式表路径关联（默认 `/themes/light.css`、`/themes/dark.css`）。
- **`ThemeProperties`（`@ConfigurationProperties("theme")`）**：目前仅包含 `defaultTheme`，后续可扩展更多配置项。
- **`ThemeManager`**：通过 `WeakHashMap` 记录已应用过的 `Scene`，在主题切换时重新附加 CSS，并把最后一次主题写入用户偏好，确保重启后保持一致。
- **`ThemeAutoConfiguration`**：注册上述 Bean，并因带有 `@AutoConfiguration`，会被 `alamafa-bootstrap` 自动发现。

## 使用示例
```java
@Inject private ThemeManager themeManager;

public void onClickLight(Scene scene) {
    themeManager.apply(Theme.LIGHT, scene);
}
```
若无法直接获取 `Scene`，可调用 `themeManager.applyToContextScene(theme)`，它会读取 `ApplicationContext` 中由 JavaFX Launcher 注册的 Primary Scene。

## 配置
```
theme.default-theme=LIGHT
```
绑定器会自动将枚举名转为大写；无效值会回退到 `DARK`。

## 集成建议
- 在控制器初始化阶段应用主题（参考 `tower-client` 中的 `LoginViewController`、`DashboardViewController`），监听 `sceneProperty()`，scene 可用时调用 `applyCurrentTheme(scene)`。
- 可在配置类中注册自定义 `ThemeDefinition` 或扩展 `ThemeManager` 以增加 CSS 资源。
- 由于切换主题会移除再添加样式表，建议保持选择器稳定、避免超大内联图片以获得流畅体验。
