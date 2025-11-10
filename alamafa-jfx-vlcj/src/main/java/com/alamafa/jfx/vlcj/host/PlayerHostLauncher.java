package com.alamafa.jfx.vlcj.host;

import javafx.application.Application;

/**
 * 独立 JVM 入口，承载 vlcj JavaFX 播放窗口。
 */
public final class PlayerHostLauncher {

    private PlayerHostLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(PlayerHostApplication.class, args);
    }
}
