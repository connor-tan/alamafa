package com.alamafa.tower.client.api.auth;

import com.alamafa.di.annotation.Service;
import com.alamafa.tower.client.api.client.HttpExecutor;
import com.alamafa.tower.client.api.dto.ApiResponse;

@Service
public class AuthApi {

    private static final String LOGIN_PATH = "/auth/login";

    private final HttpExecutor httpExecutor;

    public AuthApi(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public ApiResponse<TokenPayload> login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        return httpExecutor.post(LOGIN_PATH, request, TokenPayload.class);
    }

    private record LoginRequest(String username, String password) {
    }
}
