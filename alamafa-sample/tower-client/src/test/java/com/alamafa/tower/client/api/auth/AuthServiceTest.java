package com.alamafa.tower.client.api.auth;

import com.alamafa.tower.client.api.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void authenticateShouldStoreTokenAndReturnPayload() {
        TokenStore tokenStore = new TokenStore();
        TokenPayload payload = new TokenPayload("jwt-token", 720);
        AuthApi authApi = new AuthApi(null) {
            @Override
            public ApiResponse<TokenPayload> login(String username, String password) {
                return new ApiResponse<>(200, null, payload);
            }
        };
        AuthService authService = new AuthService(authApi, tokenStore);

        TokenPayload result = authService.authenticate("admin", "password");

        assertEquals(payload, result);
        assertTrue(tokenStore.accessToken().isPresent());
        assertEquals("jwt-token", tokenStore.accessToken().orElse(null));
    }

    @Test
    void authenticateShouldRejectBlankCredentials() {
        AuthService authService = new AuthService(new AuthApi(null) {
            @Override
            public ApiResponse<TokenPayload> login(String username, String password) {
                return null;
            }
        }, new TokenStore());

        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate(" ", "password"));
        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate("admin", " "));
    }

    @Test
    void authenticateShouldThrowWhenPayloadMissing() {
        AuthService authService = new AuthService(new AuthApi(null) {
            @Override
            public ApiResponse<TokenPayload> login(String username, String password) {
                return new ApiResponse<>(200, null, null);
            }
        }, new TokenStore());

        assertThrows(AuthFailedException.class, () ->
                authService.authenticate("admin", "password"));
    }
}
