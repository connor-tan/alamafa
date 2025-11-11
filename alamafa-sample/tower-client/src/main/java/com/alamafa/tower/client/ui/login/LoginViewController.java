package com.alamafa.tower.client.ui.login;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.annotation.PreClose;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import com.alamafa.jfx.viewmodel.window.FxWindowOptions;
import com.alamafa.theme.ThemeManager;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;

@FxViewSpec(
        fxml = "views/login.fxml",
        styles = {"styles/login.css"},
        viewModel = LoginViewModel.class,
        primary = true,
        title = "Tower Client 登录",
        width = 1024,
        height = 768,
        resizable = false
)
public class LoginViewController {

    private static final int MAX_CREDENTIAL_LENGTH = 20;

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

    @Inject
    private ThemeManager themeManager;

    @FXML
    private void initialize() {
        errorLabel.setText("");
        forgotPasswordLink.setOnAction(event -> showForgotPasswordInfo());
        applyLengthLimit(usernameField, MAX_CREDENTIAL_LENGTH);
        applyLengthLimit(passwordField, MAX_CREDENTIAL_LENGTH);
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
        if (usernameField != null) {
            usernameField.setOnAction(event -> handleLogin());
        }
        if (loginButton != null) {
            loginButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && themeManager != null) {
                    themeManager.applyCurrentTheme(newScene);
                }
            });
        }
    }

    public void setViewModel(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
            passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
            rememberMeCheckBox.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());
            loginButton.disableProperty().bind(viewModel.authenticatingProperty());
            forgotPasswordLink.disableProperty().bind(viewModel.authenticatingProperty());
        }
    }

    @FXML
    private void handleLogin() {
        if (viewModel == null) {
            return;
        }
        errorLabel.setText("正在登录...");
        viewModel.authenticateAsync(
                usernameField.getText(),
                passwordField.getText(),
                rememberMeCheckBox.isSelected(),
                this::handleLoginResult
        );
    }

    private void handleLoginResult(LoginResult result) {
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

    private void applyLengthLimit(TextInputControl control, int maxLength) {
        if (control == null || maxLength <= 0) {
            return;
        }
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return newText.length() <= maxLength ? change : null;
        });
        control.setTextFormatter(formatter);
    }

    @PreClose
    private void cleanupBindings() {
        if (viewModel == null) {
            return;
        }
        if (usernameField != null) {
            usernameField.textProperty().unbindBidirectional(viewModel.usernameProperty());
        }
        if (passwordField != null) {
            passwordField.textProperty().unbindBidirectional(viewModel.passwordProperty());
        }
        if (rememberMeCheckBox != null) {
            rememberMeCheckBox.selectedProperty().unbindBidirectional(viewModel.rememberMeProperty());
        }
    }
}
