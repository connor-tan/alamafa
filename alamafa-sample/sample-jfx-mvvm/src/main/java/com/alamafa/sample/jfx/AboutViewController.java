package com.alamafa.sample.jfx;

import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.annotation.PostShow;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@FxViewSpec(fxml = "views/about.fxml", viewModel = AboutViewModel.class, title = "About", resizable = false)
public class AboutViewController {

    @FXML
    private Label infoLabel;

    @FXML
    private Button closeButton;

    private AboutViewModel viewModel;

    public void setViewModel(AboutViewModel viewModel) {
        this.viewModel = viewModel;
        infoLabel.setText(viewModel.getInfo());
    }

    @PostShow
    private void focusCloseButton(Stage stage) {
        stage.sizeToScene();
        closeButton.requestFocus();
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}
