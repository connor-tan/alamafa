package com.alamafa.sample.helloworld;

import com.alamafa.bootstrap.AlamafaApplication;
import com.alamafa.bootstrap.AlamafaBootApplication;

/**
 * 演示入口，展示如何通过 {@link AlamafaApplication#run} 启动应用。
 */
@AlamafaBootApplication
public class HelloWorldApplication {

    public static void main(String[] args) {
        AlamafaApplication.run(HelloWorldApplication.class, args);
    }
}

