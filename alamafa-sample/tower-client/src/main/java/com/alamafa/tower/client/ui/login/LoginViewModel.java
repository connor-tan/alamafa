package com.alamafa.tower.client.ui.login;

import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@FxViewModelSpec(lazy = false)
public class LoginViewModel extends FxViewModel {
    private final StringProperty username = new SimpleStringProperty();
    private final BooleanProperty rememberMe = new SimpleBooleanProperty();

    public StringProperty usernameProperty() {
        return username;
    }

    public BooleanProperty rememberMeProperty() {
        return rememberMe;
    }

    public LoginResult authenticate(String usernameValue, String passwordValue, boolean remember) {
        if (usernameValue == null || usernameValue.isBlank() || passwordValue == null || passwordValue.isBlank()) {
            return LoginResult.failure("请输入用户名和密码");
        }
        String normalized = usernameValue.trim();
        String displayName = Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
        username.set(normalized);
        rememberMe.set(remember);
        return LoginResult.success(displayName, remember);
    }

    public record LoginResult(boolean success, String message, String displayName, boolean rememberMe) {
        static LoginResult success(String displayName, boolean rememberMe) {
            return new LoginResult(true, null, displayName, rememberMe);
        }

        static LoginResult failure(String message) {
            return new LoginResult(false, message, null, false);
        }
    }
}
