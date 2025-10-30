package com.alamafa.sample.jfx;

import com.alamafa.bootstrap.AlamafaApplication;
import com.alamafa.bootstrap.AlamafaBootApplication;
import com.alamafa.jfx.launcher.JavaFxApplicationLauncher;

@AlamafaBootApplication(launcher = JavaFxApplicationLauncher.class)
public class SampleJfxApplication {

    public static void main(String[] args) {
        AlamafaApplication.run(SampleJfxApplication.class, args);
        System.out.println(System.getProperty("java.class.path"));
    }
}
