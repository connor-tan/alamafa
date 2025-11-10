package com.alamafa.tower.client.ui.dashboard.center;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.vlcj.core.MediaEndpoint;
import com.alamafa.jfx.vlcj.core.MediaEndpointFactory;
import com.alamafa.jfx.vlcj.core.MediaEventDispatcher;
import com.alamafa.jfx.vlcj.core.PlayerLaunchRequest;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import com.alamafa.jfx.vlcj.core.PlayerProperties.Mode;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventListener;
import com.alamafa.jfx.vlcj.ipc.MediaEventType;
import com.alamafa.jfx.vlcj.embedded.EmbeddedPlayerManager;
import com.alamafa.jfx.vlcj.embedded.EmbeddedPlayerSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.UUID;

@FxViewSpec(
        fxml = "views/dashboard/center-panel.fxml",
        styles = {"styles/dashboard.css"}
)
public class CenterPanelViewController {

    @FXML
    private StackPane playerContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button relaunchButton;

    @FXML
    private Button stopButton;

    @Inject
    private MediaEndpointFactory mediaEndpointFactory;

    @Inject
    private PlayerProperties playerProperties;

    @Inject
    private MediaEventDispatcher mediaEventDispatcher;

    @Inject
    private EmbeddedPlayerManager embeddedPlayerManager;

    private MediaEndpoint activeEndpoint;
    private MediaEventListener globalListener;
    private EmbeddedPlayerSession embeddedSession;
    private boolean embeddedMode;

    @FXML
    private void initialize() {
        updateStatus("未启动播放器");
        relaunchButton.setDisable(!playerProperties.hasDefaultMedia());
        stopButton.setDisable(true);
        registerGlobalListener();
        embeddedMode = playerProperties.getMode() == Mode.EMBEDDED;
        if (embeddedMode) {
            embeddedSession = embeddedPlayerManager.attach(playerContainer, playerProperties);
        }
        if (playerProperties.isAutoStart() && playerProperties.hasDefaultMedia()) {
            launchPlayer(playerProperties.getDefaultMediaUrl());
        }
    }

    @FXML
    private void handleLaunchDefault() {
        launchPlayer(playerProperties.getDefaultMediaUrl());
    }

    @FXML
    private void handleStopPlayer() {
        if (embeddedMode) {
            stopEmbeddedPlayer();
        } else {
            disposeActiveHandle();
        }
    }

    private void launchPlayer(String mediaUrl) {
        if (embeddedMode) {
            launchEmbeddedPlayer(mediaUrl);
        } else {
            launchExternalPlayer(mediaUrl);
        }
    }

    private void launchExternalPlayer(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            updateStatus("未配置默认媒体源，无法启动播放器");
            return;
        }
        disposeActiveHandle();
        PlayerLaunchRequest request = PlayerLaunchRequest.builder()
                .playerId(UUID.randomUUID())
                .mediaUrl(mediaUrl)
                .windowTitle("塔台监控主画面")
                .width(playerProperties.getWindowWidth())
                .height(playerProperties.getWindowHeight())
                .build();
        activeEndpoint = mediaEndpointFactory.launch(request);
        activeEndpoint.setEventListener(event -> Platform.runLater(() -> handleEvent(event)));
        updateStatus("外部播放器运行中 (PID " + activeEndpoint.pid() + ")");
        stopButton.setDisable(false);
        activeEndpoint.onExit(() -> Platform.runLater(() -> {
            updateStatus("外部播放器已退出");
            stopButton.setDisable(true);
        }));
    }

    private void launchEmbeddedPlayer(String mediaUrl) {
        if (embeddedSession == null) {
            embeddedSession = embeddedPlayerManager.attach(playerContainer, playerProperties);
        }
        if (mediaUrl == null || mediaUrl.isBlank()) {
            updateStatus("未配置默认媒体源，无法启动播放器");
            return;
        }
        embeddedSession.play(mediaUrl);
        updateStatus("嵌入播放器运行中");
        stopButton.setDisable(false);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void handleEvent(MediaEvent event) {
        if (embeddedMode || activeEndpoint == null) {
            return;
        }
        MediaEventType type = event.type();
        switch (type) {
            case PLAYING -> updateStatus("播放中 - PID " + activeEndpoint.pid());
            case PAUSED -> updateStatus("已暂停 - PID " + activeEndpoint.pid());
            case STOPPED -> updateStatus("已停止 - PID " + activeEndpoint.pid());
            case HEARTBEAT -> updateStatus("连接心跳 " + event.timestamp());
            case ERROR -> {
                String message = String.valueOf(event.payload().getOrDefault("message", "未知"));
                updateStatus("播放错误: " + message);
                if (message.contains("Heartbeat timeout") && playerProperties.isAutoRestartOnHeartbeatLoss()) {
                    launchExternalPlayer(playerProperties.getDefaultMediaUrl());
                }
            }
            default -> {
            }
        }
    }

    private void disposeActiveHandle() {
        if (activeEndpoint != null && activeEndpoint.isAlive()) {
            activeEndpoint.close();
        }
        activeEndpoint = null;
        updateStatus("播放器已停止");
        stopButton.setDisable(true);
        relaunchButton.setDisable(!playerProperties.hasDefaultMedia());
    }

    private void stopEmbeddedPlayer() {
        if (embeddedSession != null) {
            embeddedSession.stop();
        }
        updateStatus("嵌入播放器已停止");
        stopButton.setDisable(true);
        relaunchButton.setDisable(!playerProperties.hasDefaultMedia());
    }

    private void disposeEmbeddedPlayer() {
        if (embeddedSession != null) {
            embeddedSession.close();
            embeddedSession = null;
        }
    }

    private void registerGlobalListener() {
        if (mediaEventDispatcher == null) {
            return;
        }
        globalListener = event -> {
            if (activeEndpoint != null && event.playerId().equals(activeEndpoint.getPlayerId())) {
                return;
            }
            Platform.runLater(() -> statusLabel.setText("通道 " + event.playerId() + " 状态: " + event.type()));
        };
        mediaEventDispatcher.addListener(globalListener);
    }

}
