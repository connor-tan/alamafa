package com.alamafa.tower.client.api.auth;

import com.alamafa.di.annotation.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenStore {

    private final AtomicReference<TokenState> state = new AtomicReference<>(TokenState.empty());

    public void store(TokenPayload payload) {
        if (payload == null || payload.accessToken() == null || payload.accessToken().isBlank()) {
            throw new IllegalArgumentException("Token payload is invalid");
        }
        Instant expiresAt = payload.expiresIn() > 0
                ? Instant.now().plusSeconds(payload.expiresIn())
                : null;
        state.set(new TokenState(payload.accessToken(), expiresAt));
    }

    public Optional<String> accessToken() {
        return Optional.ofNullable(state.get().accessToken);
    }

    public Optional<Instant> expiresAt() {
        return Optional.ofNullable(state.get().expiresAt);
    }

    public boolean isExpired() {
        TokenState current = state.get();
        return current.expiresAt != null && Instant.now().isAfter(current.expiresAt);
    }

    public void clear() {
        state.set(TokenState.empty());
    }

    private record TokenState(String accessToken, Instant expiresAt) {
        static TokenState empty() {
            return new TokenState(null, null);
        }
    }
}
