package com.alamafa.jfx.vlcj.external;

import com.alamafa.jfx.vlcj.core.MediaEndpoint;
import com.alamafa.jfx.vlcj.core.MediaEventDispatcher;
import com.alamafa.jfx.vlcj.core.MediaSource;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import com.alamafa.jfx.vlcj.ipc.MediaCommand;
import com.alamafa.jfx.vlcj.ipc.MediaCommandChannel;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventChannel;
import com.alamafa.jfx.vlcj.ipc.MediaEventListener;
import com.alamafa.jfx.vlcj.ipc.MediaEventType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 将外部进程句柄与命令信道组合为可控终端。
 */
public final class ExternalProcessEndpoint implements MediaEndpoint {

    private final ExternalPlayerHandle handle;
    private final MediaCommandChannel commandChannel;
    private final MediaEventChannel eventChannel;
    private final MediaEventDispatcher dispatcher;
    private final Duration heartbeatTimeout;
    private final ScheduledExecutorService scheduler;
    private volatile Instant lastHeartbeat;
    private final ExternalProcessRegistry processRegistry;
    private MediaEventListener listener;

    public ExternalProcessEndpoint(ExternalPlayerHandle handle,
                                  MediaCommandChannel commandChannel,
                                  MediaEventChannel eventChannel,
                                  MediaEventDispatcher dispatcher,
                                  PlayerProperties properties,
                                  ExternalProcessRegistry processRegistry) {
        this.handle = handle;
        this.commandChannel = commandChannel;
        this.eventChannel = eventChannel;
        this.dispatcher = dispatcher;
        this.processRegistry = processRegistry;
        this.heartbeatTimeout = Duration.ofSeconds(properties.getHeartbeatTimeoutSeconds());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "vlcj-heartbeat-monitor-" + handle.getPlayerId());
            t.setDaemon(true);
            return t;
        });
        this.lastHeartbeat = Instant.now();
        scheduler.scheduleAtFixedRate(this::checkHeartbeat, heartbeatTimeout.toSeconds(), heartbeatTimeout.toSeconds(), java.util.concurrent.TimeUnit.SECONDS);
    }

    @Override
    public UUID getPlayerId() {
        return handle.getPlayerId();
    }

    @Override
    public long pid() {
        return handle.pid();
    }

    @Override
    public Instant getLaunchedAt() {
        return handle.getLaunchedAt();
    }

    @Override
    public boolean isAlive() {
        return handle.isAlive();
    }

    @Override
    public CompletableFuture<Process> onExit(Runnable callback) {
        return handle.onExit(callback);
    }

    @Override
    public void setEventListener(MediaEventListener listener) {
        this.listener = listener;
        if (eventChannel != null && listener != null) {
            eventChannel.start(this::handleEvent);
        } else if (eventChannel != null) {
            eventChannel.start(this::handleEvent);
        }
    }

    private void handleEvent(MediaEvent event) {
        if (event.type() == MediaEventType.HEARTBEAT) {
            lastHeartbeat = Instant.now();
        }
        if (listener != null) {
            listener.onEvent(event);
        }
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        }
    }

    private void checkHeartbeat() {
        if (heartbeatTimeout.isZero() || heartbeatTimeout.isNegative()) {
            return;
        }
        Instant heartbeat = lastHeartbeat;
        if (heartbeat == null) {
            return;
        }
        if (Instant.now().isAfter(heartbeat.plus(heartbeatTimeout))) {
            MediaEvent timeoutEvent = new MediaEvent(MediaEventType.ERROR, getPlayerId(), Instant.now(), Map.of("message", "Heartbeat timeout"));
            handleEvent(timeoutEvent);
            close();
        }
    }

    @Override
    public void play(MediaSource source) {
        if (source == null || source.mediaUrl().isBlank()) {
            throw new IllegalArgumentException("Media source must not be blank");
        }
        commandChannel.send(MediaCommand.play(getPlayerId(), source.mediaUrl()));
    }

    @Override
    public void pausePlayback() {
        commandChannel.send(MediaCommand.pause(getPlayerId()));
    }

    @Override
    public void resumePlayback() {
        commandChannel.send(MediaCommand.resume(getPlayerId()));
    }

    @Override
    public void stopPlayback() {
        commandChannel.send(MediaCommand.stop(getPlayerId()));
    }

    @Override
    public void setVolume(int percent) {
        commandChannel.send(MediaCommand.setVolume(getPlayerId(), percent));
    }

    @Override
    public void seek(double positionSeconds) {
        commandChannel.send(MediaCommand.seek(getPlayerId(), positionSeconds));
    }

    @Override
    public void close() {
        try {
            commandChannel.close();
            if (eventChannel != null) {
                eventChannel.close();
            }
            scheduler.shutdownNow();
        } finally {
            processRegistry.unregister(handle);
            handle.close();
        }
    }
}
