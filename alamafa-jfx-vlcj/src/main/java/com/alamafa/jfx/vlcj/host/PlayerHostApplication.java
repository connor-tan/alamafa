package com.alamafa.jfx.vlcj.host;

import com.alamafa.jfx.vlcj.ipc.MediaCommand;
import com.alamafa.jfx.vlcj.ipc.MediaCommandType;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import com.alamafa.jfx.vlcj.embedded.PixelBufferVideoSurfaceFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 在独立进程中展示 vlcj JavaFX 播放器窗口。
 */
public class PlayerHostApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(PlayerHostApplication.class);

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;
    private ImageView imageView;

    private UUID playerUuid;
    private ObjectMapper mapper;
    private BufferedWriter eventWriter;

    @Override
    public void start(Stage stage) {
        Map<String, String> params = getParameters().getNamed();
        String playerId = params.getOrDefault("playerId", "unknown");
        try {
            playerUuid = UUID.fromString(playerId);
        } catch (IllegalArgumentException ex) {
            playerUuid = null;
        }
        String media = params.getOrDefault("media", "");
        String title = params.getOrDefault("title", "Tower Player");
        int width = parseInt(params.get("width"), 960);
        int height = parseInt(params.get("height"), 540);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(PixelBufferVideoSurfaceFactory.videoSurfaceFor(imageView));

        StackPane root = new StackPane(imageView);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, width, height);
        imageView.fitWidthProperty().bind(scene.widthProperty());
        imageView.fitHeightProperty().bind(scene.heightProperty());
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setOnCloseRequest(event -> {
            stopPlayer();
            Platform.exit();
        });
        stage.show();

        if (!media.isBlank()) {
            log.info("Player {} start media {}", playerId, media);
            mediaPlayer.media().play(media);
        } else {
            log.warn("Player {} launched without media url", playerId);
        }

        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        eventWriter = new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8));
        startCommandListener();
        startHeartbeat();
    }

    @Override
    public void stop() {
        stopPlayer();
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
            mediaPlayerFactory = null;
        }
    }

    private void startCommandListener() {
        Thread listener = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    MediaCommand command = mapper.readValue(trimmed, MediaCommand.class);
                    if (playerUuid != null && !playerUuid.equals(command.playerId())) {
                        continue;
                    }
                    handleCommand(command);
                }
            } catch (IOException ex) {
                log.warn("Command listener stopped", ex);
            }
        }, "vlcj-host-command-listener");
        listener.setDaemon(true);
        listener.start();
    }

    private void handleCommand(MediaCommand command) {
        if (mediaPlayer == null) {
            return;
        }
        MediaCommandType type = command.type();
        switch (type) {
            case PLAY -> {
                Object mediaValue = command.payload().get("media");
                if (mediaValue instanceof String media && !media.isBlank()) {
                    Platform.runLater(() -> mediaPlayer.media().play(media));
                    publishEvent(MediaEventType.PLAYING, Map.of("media", media));
                }
            }
            case PAUSE -> Platform.runLater(() -> {
                mediaPlayer.controls().pause();
                publishEvent(MediaEventType.PAUSED, Map.of());
            });
            case RESUME -> Platform.runLater(() -> {
                mediaPlayer.controls().play();
                publishEvent(MediaEventType.PLAYING, Map.of());
            });
            case STOP -> Platform.runLater(() -> {
                mediaPlayer.controls().stop();
                publishEvent(MediaEventType.STOPPED, Map.of());
            });
            case SET_VOLUME -> {
                Object value = command.payload().get("value");
                if (value instanceof Number number) {
                    Platform.runLater(() -> mediaPlayer.audio().setVolume(number.intValue()));
                }
            }
            case SEEK -> {
                Object value = command.payload().get("position");
                if (value instanceof Number number) {
                    Platform.runLater(() -> mediaPlayer.controls().setTime((long) (number.doubleValue() * 1000)));
                }
            }
            default -> log.debug("Unsupported command {}", type);
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void publishEvent(MediaEventType type, Map<String, Object> payload) {
        if (playerUuid == null || eventWriter == null) {
            return;
        }
        MediaEvent event = new MediaEvent(type, playerUuid, Instant.now(), payload);
        try {
            eventWriter.write(mapper.writeValueAsString(event));
            eventWriter.write('\n');
            eventWriter.flush();
        } catch (IOException ex) {
            log.warn("Failed to publish media event", ex);
        }
    }

    private void startHeartbeat() {
        Thread heartbeat = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(2_000);
                    publishEvent(MediaEventType.HEARTBEAT, Map.of());
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "vlcj-heartbeat");
        heartbeat.setDaemon(true);
        heartbeat.start();
    }
}
