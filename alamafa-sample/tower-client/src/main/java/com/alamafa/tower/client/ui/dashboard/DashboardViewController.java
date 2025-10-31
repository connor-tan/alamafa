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
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@FxViewSpec(
        fxml = "views/dashboard/dashboard.fxml",
        styles = {"styles/app.css"},
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

    private DashboardViewModel viewModel;
    private boolean sectionsLoaded = false;

    public void setViewModel(DashboardViewModel viewModel) {
        this.viewModel = viewModel;
        initializeSections();
    }

    @FXML
    private void initialize() {
        initializeSections();
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
}
