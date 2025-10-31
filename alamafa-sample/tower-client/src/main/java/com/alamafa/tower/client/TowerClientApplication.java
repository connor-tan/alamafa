package com.alamafa.tower.client;

import com.alamafa.bootstrap.AlamafaApplication;
import com.alamafa.bootstrap.AlamafaBootApplication;
import com.alamafa.jfx.launcher.JavaFxApplicationLauncher;

@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class TowerClientApplication {

    public static void main(String[] args) {
        AlamafaApplication.run(TowerClientApplication.class, args);
    }
}
