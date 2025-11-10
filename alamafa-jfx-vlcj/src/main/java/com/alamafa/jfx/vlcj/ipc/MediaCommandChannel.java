package com.alamafa.jfx.vlcj.ipc;

/**
 * 负责将命令发送到外部播放器进程。
 */
public interface MediaCommandChannel extends AutoCloseable {

    void send(MediaCommand command);

    @Override
    void close();
}
