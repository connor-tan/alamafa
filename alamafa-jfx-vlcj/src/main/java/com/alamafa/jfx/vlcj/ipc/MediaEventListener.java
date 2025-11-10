package com.alamafa.jfx.vlcj.ipc;

@FunctionalInterface
public interface MediaEventListener {
    void onEvent(MediaEvent event);
}
