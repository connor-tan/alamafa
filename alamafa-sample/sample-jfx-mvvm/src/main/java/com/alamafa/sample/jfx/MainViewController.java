package com.alamafa.sample.jfx;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import com.alamafa.jfx.viewmodel.window.FxWindowOptions;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;

@FxViewSpec(
        fxml = "views/main.fxml",
        styles = {"styles/main.css"},
        viewModel = MainViewModel.class,
        primary = true,
        title = "Alamafa JavaFX MVVM Sample",
        width = 480,
        height = 320,
        resizable = true
)
public class MainViewController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button aboutButton;

    private MainViewModel viewModel;
    @Inject
    private FxWindowManager windowManager;

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        messageLabel.textProperty().bind(viewModel.messageProperty());
        refreshButton.disableProperty().bind(viewModel.refreshCommand().runningProperty());
    }

    @FXML
    private void handleRefresh() {
        viewModel.refreshCommand().execute();
    }

    @FXML
    private void handleShowAbout() {
        FxWindowOptions.Builder builder = FxWindowOptions.builder()
                .modality(Modality.WINDOW_MODAL)
                .showAndWait(true);
        if (aboutButton != null && aboutButton.getScene() != null) {
            builder.owner(aboutButton.getScene().getWindow());
        }
        windowManager.openWindow(AboutViewController.class, builder.build());
    }
}
