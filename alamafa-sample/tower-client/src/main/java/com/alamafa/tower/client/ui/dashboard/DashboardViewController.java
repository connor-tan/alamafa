package com.alamafa.tower.client.ui.dashboard;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.FxViewLoader;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.tower.client.ui.dashboard.center.CenterPanelViewController;
import com.alamafa.tower.client.ui.dashboard.footer.FooterViewController;
import com.alamafa.tower.client.ui.dashboard.header.HeaderViewController;
import com.alamafa.tower.client.ui.dashboard.left.LeftPanelViewController;
import com.alamafa.tower.client.ui.dashboard.right.RightPanelViewController;
import com.alamafa.tower.client.ui.monitoring.MonitoringWallViewController;
import com.alamafa.jfx.viewmodel.window.FxWindowHandle;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import com.alamafa.jfx.viewmodel.window.FxWindowOptions;
import com.alamafa.theme.Theme;
import com.alamafa.theme.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

@FxViewSpec(
        fxml = "views/dashboard/dashboard.fxml",
        styles = {"styles/dashboard.css"},
        viewModel = DashboardViewModel.class,
        title = "Tower Client 控制台",
        width = 960,
        height = 640,
        resizable = true
)
public class DashboardViewController {

    @FXML
    private StackPane headerContainer;

    @FXML
    private StackPane footerContainer;

    @FXML
    private StackPane leftContainer;

    @FXML
    private StackPane centerContainer;

    @FXML
    private StackPane rightContainer;

    @Inject
    private FxViewLoader viewLoader;

    @Inject
    private FxWindowManager windowManager;

    @Inject
    private ThemeManager themeManager;

    private DashboardViewModel viewModel;
    private boolean sectionsLoaded = false;
    private FxWindowHandle monitoringWallHandle;

    public void setViewModel(DashboardViewModel viewModel) {
        this.viewModel = viewModel;
        initializeSections();
    }

    @FXML
    private void initialize() {
        initializeSections();
        headerContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && themeManager != null) {
                themeManager.applyCurrentTheme(newScene);
            }
        });
    }

    private void initializeSections() {
        if (sectionsLoaded || viewLoader == null) {
            return;
        }
        loadInto(headerContainer, HeaderViewController.class);
        loadInto(footerContainer, FooterViewController.class);
        loadInto(leftContainer, LeftPanelViewController.class);
        loadInto(centerContainer, CenterPanelViewController.class);
        loadInto(rightContainer, RightPanelViewController.class);
        sectionsLoaded = true;
    }

    private <T> void loadInto(Pane container, Class<T> viewType) {
        if (container == null) {
            return;
        }
        try {
            FxView<T> view = viewLoader.load(viewType);
            container.getChildren().setAll(view.root());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load section " + viewType.getSimpleName(), ex);
        }
    }

    @FXML
    private void handleOpenMonitoringWall() {
        if (windowManager == null) {
            return;
        }
        if (monitoringWallHandle != null && monitoringWallHandle.stage() != null
                && monitoringWallHandle.stage().isShowing()) {
            monitoringWallHandle.stage().toFront();
            monitoringWallHandle.stage().requestFocus();
            return;
        }
        FxWindowOptions options = FxWindowOptions.builder()
                .title("监控墙")
                .width(1280.0)
                .height(800.0)
                .resizable(true)
                .build();
        monitoringWallHandle = windowManager.openWindow(MonitoringWallViewController.class, options);
        if (monitoringWallHandle != null && monitoringWallHandle.stage() != null) {
            monitoringWallHandle.stage().setOnHidden(event -> monitoringWallHandle = null);
        }
    }

    @FXML
    private void handleApplyLightTheme() {
        applyTheme(Theme.LIGHT);
    }

    @FXML
    private void handleApplyDarkTheme() {
        applyTheme(Theme.DARK);
    }

    private void applyTheme(Theme theme) {
        if (themeManager == null) {
            return;
        }
        Scene scene = headerContainer.getScene();
        if (scene != null) {
            themeManager.apply(theme, scene);
        } else {
            themeManager.applyToContextScene(theme);
        }
        themeManager.applyToKnownScenes(theme);
    }
}
