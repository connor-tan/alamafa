package com.alamafa.tower.client.ui.monitoring;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.annotation.PreClose;
import com.alamafa.jfx.vlcj.embedded.EmbeddedPlayerManager;
import com.alamafa.jfx.vlcj.embedded.EmbeddedPlayerSession;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import com.alamafa.theme.ThemeManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

@FxViewSpec(
        fxml = "views/dashboard/monitoring-wall.fxml",
        styles = {"styles/monitoring-wall.css"},
        title = "监控墙",
        width = 1280,
        height = 800,
        resizable = true,
        viewModel = MonitoringWallViewModel.class
)
public class MonitoringWallViewController {
    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 4;

    @FXML
    private GridPane wallGrid;

    @Inject
    private EmbeddedPlayerManager embeddedPlayerManager;

    @Inject
    private PlayerProperties playerProperties;

    @Inject
    private ThemeManager themeManager;

    private final List<EmbeddedPlayerSession> sessions = new ArrayList<>();
    private MonitoringWallViewModel viewModel;

    @FXML
    private void initialize() {
        if (wallGrid == null) {
            return;
        }
        configureGrid();
        inflateTiles();
        autoplayDefaultMedia();
        wallGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && themeManager != null) {
                themeManager.applyCurrentTheme(newScene);
            }
        });
    }

    private void configureGrid() {
        wallGrid.getColumnConstraints().clear();
        wallGrid.getRowConstraints().clear();
        double percentWidth = 100.0 / GRID_COLS;
        double percentHeight = 100.0 / GRID_ROWS;
        for (int i = 0; i < GRID_COLS; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(percentWidth);
            wallGrid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < GRID_ROWS; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(percentHeight);
            wallGrid.getRowConstraints().add(row);
        }
    }

    private void inflateTiles() {
        wallGrid.getChildren().clear();
        sessions.forEach(EmbeddedPlayerSession::close);
        sessions.clear();
        int index = 0;
        int preferredWidth = Math.max(playerProperties.getWindowWidth() / GRID_COLS, 200);
        int preferredHeight = Math.max(playerProperties.getWindowHeight() / GRID_ROWS, 120);
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                StackPane tile = createTile();
                wallGrid.add(tile, col, row);
                EmbeddedPlayerSession session = embeddedPlayerManager.attach(tile, preferredWidth, preferredHeight);
                sessions.add(session);
                addOverlay(tile, ++index);
            }
        }
    }

    private StackPane createTile() {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("monitoring-tile");
        return pane;
    }

    private void addOverlay(StackPane pane, int index) {
        Label label = new Label("通道 " + index);
        label.getStyleClass().add("monitoring-label");
        pane.getChildren().add(label);
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        StackPane.setMargin(label, new Insets(8));
    }

    private void autoplayDefaultMedia() {
        String defaultMedia = playerProperties.getDefaultMediaUrl();
        if (defaultMedia == null || defaultMedia.isBlank()) {
            return;
        }
        for (EmbeddedPlayerSession session : sessions) {
            session.play(defaultMedia);
        }
    }

    @PreClose
    private void onClose() {
        sessions.forEach(EmbeddedPlayerSession::close);
        sessions.clear();
    }

    public void setViewModel(MonitoringWallViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
