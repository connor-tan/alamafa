package com.alamafa.jfx.vlcj.embedded;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.Objects;

public final class EmbeddedPlayerSession implements AutoCloseable {

    private final StackPane container;
    private final int preferredWidth;
    private final int preferredHeight;

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private ImageView imageView;
    private Region sizeAnchor;

    public EmbeddedPlayerSession(StackPane container, int preferredWidth, int preferredHeight) {
        this.container = Objects.requireNonNull(container, "container");
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
    }

    public void initialize() {
        if (imageView != null) {
            return;
        }
        sizeAnchor = new Region();
        sizeAnchor.setMinSize(preferredWidth, preferredHeight);
        sizeAnchor.setPrefSize(preferredWidth, preferredHeight);
        sizeAnchor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(sizeAnchor, Pos.CENTER);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setManaged(false);
        imageView.fitWidthProperty().bind(container.widthProperty());
        imageView.fitHeightProperty().bind(container.heightProperty());

        container.getChildren().setAll(sizeAnchor, imageView);

        factory = new MediaPlayerFactory();
        mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(PixelBufferVideoSurfaceFactory.videoSurfaceFor(imageView));
    }

    public void play(String mediaUrl) {
        if (mediaPlayer == null) {
            initialize();
        }
        if (mediaUrl == null || mediaUrl.isBlank()) {
            return;
        }
        Platform.runLater(() -> mediaPlayer.media().play(mediaUrl));
    }

    public void stop() {
        if (mediaPlayer != null) {
            Platform.runLater(() -> mediaPlayer.controls().stop());
        }
    }

    @Override
    public void close() {
        stop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (factory != null) {
            factory.release();
            factory = null;
        }
        if (imageView != null) {
            ImageView view = imageView;
            Platform.runLater(() -> container.getChildren().remove(view));
            imageView = null;
        }
        if (sizeAnchor != null) {
            Region anchor = sizeAnchor;
            Platform.runLater(() -> container.getChildren().remove(anchor));
            sizeAnchor = null;
        }
    }
}
