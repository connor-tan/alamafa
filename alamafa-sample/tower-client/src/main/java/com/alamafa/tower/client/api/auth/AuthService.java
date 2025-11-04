package com.alamafa.tower.client.api.auth;

import com.alamafa.di.annotation.Service;
import com.alamafa.tower.client.api.dto.ApiResponse;

import java.util.Objects;

@Service
public class AuthService {

    private final AuthApi authApi;
    private final TokenStore tokenStore;

    public AuthService(AuthApi authApi, TokenStore tokenStore) {
        this.authApi = authApi;
        this.tokenStore = tokenStore;
    }

    public TokenPayload authenticate(String username, String password) {
        String trimmedUsername = Objects.requireNonNull(username, "username must not be null").trim();
        String trimmedPassword = Objects.requireNonNull(password, "password must not be null").trim();
        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            throw new IllegalArgumentException("Username or password must not be blank");
        }
        ApiResponse<TokenPayload> response = authApi.login(trimmedUsername, trimmedPassword);
        TokenPayload payload = response.data();
        if (payload == null) {
            throw new AuthFailedException("Authentication response does not contain token data");
        }
        tokenStore.store(payload);
        return payload;
    }
}
