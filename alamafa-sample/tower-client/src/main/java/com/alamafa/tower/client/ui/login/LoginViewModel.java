package com.alamafa.tower.client.ui.login;

import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;
import com.alamafa.tower.client.api.auth.AuthFailedException;
import com.alamafa.tower.client.api.auth.AuthService;
import com.alamafa.tower.client.api.auth.TokenPayload;
import com.alamafa.tower.client.api.client.ApiClientException;
import com.alamafa.tower.client.api.client.ApiNetworkException;
import com.alamafa.tower.client.session.CredentialsStore;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

@FxViewModelSpec(lazy = false, scope = FxViewModelScope.VIEW)
public class LoginViewModel extends FxViewModel {
    private static final String GENERIC_FAILURE_MESSAGE = "登录失败，请稍后再试";
    private static final String NETWORK_FAILURE_MESSAGE = "无法连接服务器，请稍后再试";
    private static final String CREDENTIAL_REQUIRED_MESSAGE = "请输入用户名和密码";

    private final StringProperty username = new SimpleStringProperty();
    private final BooleanProperty rememberMe = new SimpleBooleanProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final AuthService authService;
    private final CredentialsStore credentialsStore;
    private final ExecutorService authExecutor;
    private final BooleanProperty authenticating = new SimpleBooleanProperty(false);

    public LoginViewModel(AuthService authService, CredentialsStore credentialsStore) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
        this.credentialsStore = Objects.requireNonNull(credentialsStore, "credentialsStore must not be null");
        this.authExecutor = Executors.newSingleThreadExecutor(new LoginThreadFactory());
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public BooleanProperty rememberMeProperty() {
        return rememberMe;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public BooleanProperty authenticatingProperty() {
        return authenticating;
    }

    @Override
    protected void onAttach() {
        credentialsStore.load().ifPresent(credentials -> {
            username.set(credentials.username());
            password.set(credentials.password());
            rememberMe.set(true);
        });
    }

    @Override
    protected void onDetach() {
        authExecutor.shutdownNow();
    }

    public void authenticateAsync(String usernameValue,
                                  String passwordValue,
                                  boolean remember,
                                  Consumer<LoginResult> callback) {
        Objects.requireNonNull(callback, "callback");
        if (authenticating.get()) {
            return;
        }
        setAuthenticating(true);
        authExecutor.submit(() -> {
            LoginResult result = authenticate(usernameValue, passwordValue, remember);
            Platform.runLater(() -> {
                setAuthenticating(false);
                callback.accept(result);
            });
        });
    }

    public LoginResult authenticate(String usernameValue, String passwordValue, boolean remember) {
        if (usernameValue == null || usernameValue.isBlank() || passwordValue == null || passwordValue.isBlank()) {
            return LoginResult.failure(CREDENTIAL_REQUIRED_MESSAGE);
        }

        String normalizedUsername = usernameValue.trim();
        String trimmedPassword = passwordValue.trim();
        try {
            TokenPayload payload = authService.authenticate(normalizedUsername, trimmedPassword);
            if (payload == null) {
                return LoginResult.failure(GENERIC_FAILURE_MESSAGE);
            }
            username.set(normalizedUsername);
            rememberMe.set(remember);
             handleRememberMe(remember, normalizedUsername, trimmedPassword);
            return LoginResult.success(toDisplayName(normalizedUsername), remember);
        } catch (IllegalArgumentException ex) {
            return LoginResult.failure(messageOrDefault(ex.getMessage(), CREDENTIAL_REQUIRED_MESSAGE));
        } catch (AuthFailedException ex) {
            return LoginResult.failure(messageOrDefault(ex.getMessage(), "认证失败，请检查用户名或密码"));
        } catch (ApiNetworkException ex) {
            return LoginResult.failure(NETWORK_FAILURE_MESSAGE);
        } catch (ApiClientException ex) {
            return LoginResult.failure(messageOrDefault(ex.getMessage(), GENERIC_FAILURE_MESSAGE));
        } catch (RuntimeException ex) {
            return LoginResult.failure(GENERIC_FAILURE_MESSAGE);
        }
    }

    private String toDisplayName(String normalizedUsername) {
        if (normalizedUsername.isEmpty()) {
            return "";
        }
        char first = Character.toUpperCase(normalizedUsername.charAt(0));
        String rest = normalizedUsername.length() > 1 ? normalizedUsername.substring(1) : "";
        return first + rest;
    }

    private String messageOrDefault(String candidate, String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }

    private void handleRememberMe(boolean remember, String normalizedUsername, String trimmedPassword) {
        if (remember) {
            credentialsStore.save(normalizedUsername, trimmedPassword);
            password.set(trimmedPassword);
        } else {
            credentialsStore.clear();
            password.set("");
        }
    }

    private void setAuthenticating(boolean value) {
        if (Platform.isFxApplicationThread()) {
            authenticating.set(value);
        } else {
            Platform.runLater(() -> authenticating.set(value));
        }
    }

    private static final class LoginThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "tower-login-worker");
            thread.setDaemon(true);
            return thread;
        }
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
