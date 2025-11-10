package com.alamafa.jfx.vlcj.core;

import com.alamafa.jfx.vlcj.ipc.MediaEventListener;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 描述一个可被控制的媒体终端（播放器实例）。
 */
public interface MediaEndpoint extends AutoCloseable {

    UUID getPlayerId();

    long pid();

    Instant getLaunchedAt();

    boolean isAlive();

    CompletableFuture<Process> onExit(Runnable callback);

    default void play(MediaSource source) {
        // optional: implemented by controllable endpoints
    }

    default void pausePlayback() {
        // optional
    }

    default void resumePlayback() {
        // optional
    }

    default void stopPlayback() {
        // optional
    }

    default void seek(double positionSeconds) {
        // optional
    }

    default void setVolume(int percent) {
        // optional
    }

    default void setEventListener(MediaEventListener listener) {
        // optional
    }

    @Override
    void close();
}
