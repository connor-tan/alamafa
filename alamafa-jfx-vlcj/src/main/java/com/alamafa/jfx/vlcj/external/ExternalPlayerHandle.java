package com.alamafa.jfx.vlcj.external;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 代表一个运行中的外部播放器进程。
 */
public final class ExternalPlayerHandle {

    private final UUID playerId;
    private final Process process;
    private final Instant launchedAt = Instant.now();

    ExternalPlayerHandle(UUID playerId, Process process) {
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.process = Objects.requireNonNull(process, "process must not be null");
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long pid() {
        return process.pid();
    }

    public Instant getLaunchedAt() {
        return launchedAt;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public CompletableFuture<Process> onExit(Runnable callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        return process.onExit().whenComplete((p, err) -> callback.run());
    }

    public void pipeOutputAsync(Consumer<String> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        Thread stdoutThread = new Thread(() -> readStream(process.getInputStream(), consumer), "vlcj-player-" + playerId);
        stdoutThread.setDaemon(true);
        stdoutThread.start();
    }

    public void close() {
        process.destroy();
    }

    private void readStream(java.io.InputStream stream, Consumer<String> consumer) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
        } catch (IOException ignored) {
            // ignore
        }
    }
}
