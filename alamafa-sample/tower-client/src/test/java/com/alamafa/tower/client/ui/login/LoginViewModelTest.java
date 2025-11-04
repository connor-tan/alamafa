package com.alamafa.tower.client.ui.login;

import com.alamafa.tower.client.api.auth.AuthApi;
import com.alamafa.tower.client.api.auth.AuthFailedException;
import com.alamafa.tower.client.api.auth.AuthService;
import com.alamafa.tower.client.api.auth.TokenPayload;
import com.alamafa.tower.client.api.auth.TokenStore;
import com.alamafa.tower.client.api.client.ApiClientException;
import com.alamafa.tower.client.api.dto.ApiResponse;
import com.alamafa.tower.client.session.CredentialsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class LoginViewModelTest {

    private TokenStore tokenStore;
    private Preferences preferences;
    private CredentialsStore credentialsStore;

    @BeforeEach
    void setUp() {
        tokenStore = new TokenStore();
        preferences = Preferences.userRoot().node("tower-client-test-" + System.nanoTime());
        credentialsStore = new CredentialsStore(preferences);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        preferences.removeNode();
    }

    @Test
    void authenticateShouldReturnSuccessWhenAuthServiceSucceeds() {
        TokenPayload payload = new TokenPayload("token", 720);
        AuthService authService = new AuthService(successApi(payload), tokenStore);
        LoginViewModel viewModel = new LoginViewModel(authService, credentialsStore);

        LoginViewModel.LoginResult result = viewModel.authenticate("admin", "password", true);

        assertTrue(result.success());
        assertEquals("Admin", result.displayName());
        assertTrue(result.rememberMe());
        assertEquals("admin", viewModel.usernameProperty().get());
        assertTrue(viewModel.rememberMeProperty().get());
        assertEquals("token", tokenStore.accessToken().orElse(null));
        assertTrue(credentialsStore.load().isPresent());
    }

    @Test
    void authenticateShouldReturnFailureWhenAuthServiceThrowsAuthFailed() {
        AuthService authService = new AuthService(failingApi(new AuthFailedException("认证失败")), tokenStore);
        LoginViewModel viewModel = new LoginViewModel(authService, credentialsStore);

        LoginViewModel.LoginResult result = viewModel.authenticate("admin", "password", false);

        assertFalse(result.success());
        assertEquals("认证失败", result.message());
        assertFalse(viewModel.rememberMeProperty().get());
        assertFalse(tokenStore.accessToken().isPresent());
    }

    @Test
    void authenticateShouldReturnFailureWhenAuthServiceThrowsClientException() {
        AuthService authService = new AuthService(failingApi(new ApiClientException("用户名或密码错误")), tokenStore);
        LoginViewModel viewModel = new LoginViewModel(authService, credentialsStore);

        LoginViewModel.LoginResult result = viewModel.authenticate("admin", "password", false);

        assertFalse(result.success());
        assertEquals("用户名或密码错误", result.message());
    }

    @Test
    void onAttachShouldPopulateStoredCredentials() {
        credentialsStore.save("savedUser", "savedPass");
        AuthService authService = new AuthService(successApi(new TokenPayload("token", 720)), tokenStore);
        LoginViewModel viewModel = new LoginViewModel(authService, credentialsStore);

        viewModel.onAttach();

        assertEquals("savedUser", viewModel.usernameProperty().get());
        assertEquals("savedPass", viewModel.passwordProperty().get());
        assertTrue(viewModel.rememberMeProperty().get());
    }

    @Test
    void authenticateShouldClearStoredCredentialsWhenRememberDisabled() {
        credentialsStore.save("user", "pass");
        AuthService authService = new AuthService(successApi(new TokenPayload("token", 720)), tokenStore);
        LoginViewModel viewModel = new LoginViewModel(authService, credentialsStore);

        viewModel.authenticate("admin", "password", false);

        assertFalse(credentialsStore.load().isPresent());
        assertEquals("", viewModel.passwordProperty().get());
        assertFalse(viewModel.rememberMeProperty().get());
    }

    private AuthApi successApi(TokenPayload payload) {
        return new AuthApi(null) {
            @Override
            public ApiResponse<TokenPayload> login(String username, String password) {
                return new ApiResponse<>(200, null, payload);
            }
        };
    }

    private AuthApi failingApi(RuntimeException exception) {
        return new AuthApi(null) {
            @Override
            public ApiResponse<TokenPayload> login(String username, String password) {
                throw exception;
            }
        };
    }
}
