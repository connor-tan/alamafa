package com.alamafa.tower.client.ui.dashboard.header;

import com.alamafa.jfx.view.annotation.FxViewSpec;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

@FxViewSpec(
        fxml = "views/dashboard/header.fxml",
        styles = {"styles/dashboard.css"},
        viewModel = HeaderViewModel.class
)
public class HeaderViewController {

    @FXML
    private StackPane avatarHolder;

    @FXML
    private Label avatarLabel;

    @FXML
    private Label usernameLabel;

    private HeaderViewModel viewModel;

    public void setViewModel(HeaderViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            avatarLabel.textProperty().bind(viewModel.avatarInitialsProperty());
            usernameLabel.textProperty().bind(viewModel.displayNameProperty());
        }
    }
}
