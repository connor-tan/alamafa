package com.alamafa.jfx.vlcj.ipc;

public interface MediaEventChannel extends AutoCloseable {
    void start(MediaEventListener listener);

    @Override
    void close();
}
