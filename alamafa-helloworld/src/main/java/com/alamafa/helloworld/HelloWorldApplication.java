package com.alamafa.helloworld;

import com.alamafa.core.ApplicationBootstrap;
import com.alamafa.core.Lifecycle;
import com.alamafa.di.DiRuntimeBootstrap;

/**
 * 示例入口：通过 Alamafa 的引导流程启动一个最小化的 HelloWorld 应用。
 */
public final class HelloWorldApplication {
    private HelloWorldApplication() {
    }

    public static void main(String[] args) {
        ConsoleApplicationLauncher launcher = new ConsoleApplicationLauncher();
        ApplicationBootstrap bootstrap = new ApplicationBootstrap(launcher);
        DiRuntimeBootstrap diBootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(HelloWorldConfiguration.class)
                .scanPackages("com.alamafa.helloworld")
                .build();
        bootstrap.addLifecycleParticipant(diBootstrap);
        bootstrap.launch(Lifecycle.NO_OP);
    }
}
