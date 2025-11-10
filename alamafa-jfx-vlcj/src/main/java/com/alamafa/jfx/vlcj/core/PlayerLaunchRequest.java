package com.alamafa.jfx.vlcj.core;

import java.util.Objects;
import java.util.UUID;

/**
 * 描述一次外部播放器的启动参数。
 */
public final class PlayerLaunchRequest {

    private final UUID playerId;
    private final String mediaUrl;
    private final String windowTitle;
    private final int width;
    private final int height;

    private PlayerLaunchRequest(Builder builder) {
        this.playerId = Objects.requireNonNull(builder.playerId, "playerId must not be null");
        this.mediaUrl = Objects.requireNonNull(builder.mediaUrl, "mediaUrl must not be null");
        this.windowTitle = builder.windowTitle == null ? "Tower Player" : builder.windowTitle;
        this.width = builder.width;
        this.height = builder.height;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be greater than 0");
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID playerId = UUID.randomUUID();
        private String mediaUrl = "";
        private String windowTitle;
        private int width = 960;
        private int height = 540;

        private Builder() {
        }

        public Builder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl == null ? "" : mediaUrl.trim();
            return this;
        }

        public Builder windowTitle(String windowTitle) {
            this.windowTitle = windowTitle;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public PlayerLaunchRequest build() {
            return new PlayerLaunchRequest(this);
        }
    }
}
