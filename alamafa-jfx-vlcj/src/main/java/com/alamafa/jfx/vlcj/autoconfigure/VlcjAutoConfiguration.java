package com.alamafa.jfx.vlcj.autoconfigure;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.di.annotation.Configuration;

/**
 * 基础 vlcj 自动配置，导出默认外部进程播放器。
 */
@AutoConfiguration
@Configuration(scanBasePackages = {
        "com.alamafa.jfx.vlcj.core",
        "com.alamafa.jfx.vlcj.external",
        "com.alamafa.jfx.vlcj.embedded"
})
public class VlcjAutoConfiguration {
}
