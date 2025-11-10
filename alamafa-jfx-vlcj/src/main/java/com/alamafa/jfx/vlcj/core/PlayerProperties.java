package com.alamafa.jfx.vlcj.core;

import com.alamafa.config.ConfigurationProperties;
import com.alamafa.di.annotation.Component;

/**
 * 配置默认播放窗口参数 & 媒体源。
 */
@Component
@ConfigurationProperties(prefix = "player")
public class PlayerProperties {

    private String defaultMediaUrl = "";
    private int windowWidth = 960;
    private int windowHeight = 540;
    private boolean autoStart = true;
    private int heartbeatTimeoutSeconds = 10;
    private boolean autoRestartOnHeartbeatLoss = true;
    private Mode mode = Mode.EXTERNAL;

    public String getDefaultMediaUrl() {
        return defaultMediaUrl;
    }

    public void setDefaultMediaUrl(String defaultMediaUrl) {
        this.defaultMediaUrl = defaultMediaUrl == null ? "" : defaultMediaUrl.trim();
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = validatePositive(windowWidth, "windowWidth");
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = validatePositive(windowHeight, "windowHeight");
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean hasDefaultMedia() {
        return !defaultMediaUrl.isBlank();
    }

    public int getHeartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    public void setHeartbeatTimeoutSeconds(int heartbeatTimeoutSeconds) {
        this.heartbeatTimeoutSeconds = validatePositive(heartbeatTimeoutSeconds, "heartbeatTimeoutSeconds");
    }

    public boolean isAutoRestartOnHeartbeatLoss() {
        return autoRestartOnHeartbeatLoss;
    }

    public void setAutoRestartOnHeartbeatLoss(boolean autoRestartOnHeartbeatLoss) {
        this.autoRestartOnHeartbeatLoss = autoRestartOnHeartbeatLoss;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(String mode) {
        if (mode == null || mode.isBlank()) {
            this.mode = Mode.EXTERNAL;
            return;
        }
        try {
            this.mode = Mode.valueOf(mode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            this.mode = Mode.EXTERNAL;
        }
    }

    public enum Mode {
        EXTERNAL,
        EMBEDDED
    }

    private int validatePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }
}
