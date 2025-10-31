package com.alamafa.tower.client.ui.login;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import com.alamafa.jfx.viewmodel.window.FxWindowOptions;
import com.alamafa.tower.client.session.UserSession;
import com.alamafa.tower.client.ui.dashboard.DashboardViewController;
import com.alamafa.tower.client.ui.login.LoginViewModel.LoginResult;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@FxViewSpec(
        fxml = "views/login.fxml",
        styles = {"styles/app.css"},
        viewModel = LoginViewModel.class,
        primary = true,
        title = "Tower Client 登录",
        width = 1024,
        height = 768,
        resizable = false
)
public class LoginViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    private LoginViewModel viewModel;

    @Inject
    private FxWindowManager windowManager;

    @Inject
    private UserSession userSession;

    @FXML
    private void initialize() {
        errorLabel.setText("");
        forgotPasswordLink.setOnAction(event -> showForgotPasswordInfo());
    }

    public void setViewModel(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
            rememberMeCheckBox.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());
        }
    }

    @FXML
    private void handleLogin() {
        LoginResult result = viewModel.authenticate(
                usernameField.getText(),
                passwordField.getText(),
                rememberMeCheckBox.isSelected()
        );
        if (!result.success()) {
            errorLabel.setText(result.message());
            return;
        }

        errorLabel.setText("");
        userSession.updateUsername(result.displayName());

        FxWindowOptions options = FxWindowOptions.builder()
                .title("Tower Client 控制台")
                .width(960.0)
                .height(640.0)
                .resizable(true)
                .build();
        windowManager.openWindow(DashboardViewController.class, options);

        Stage stage = stage();
        if (stage != null) {
            stage.close();
        }
    }

    private void showForgotPasswordInfo() {
        errorLabel.setText("请联系系统管理员重置密码。");
    }

    private Stage stage() {
        return loginButton != null && loginButton.getScene() != null
                ? (Stage) loginButton.getScene().getWindow()
                : null;
    }
}
