package com.alamafa.tower.client.api.client;

import com.alamafa.tower.client.api.auth.TokenPayload;
import com.alamafa.tower.client.api.auth.TokenStore;
import com.alamafa.tower.client.api.dto.ApiResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpExecutorTest {

    private ApiProperties properties;
    private TokenStore tokenStore;
    private StubTransport transport;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        properties = new ApiProperties();
        properties.setBaseUrl("http://localhost:8080/api");
        tokenStore = new TokenStore();
        transport = new StubTransport();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void postShouldReturnParsedResponse() {
        transport.configure(200, """
                {"code":200,"msg":null,"data":{"access_token":"abc","expires_in":720}}
                """);
        HttpExecutor executor = new HttpExecutor(properties, tokenStore, transport, mapper);

        ApiResponse<TokenPayload> response = executor.post("/auth/login",
                new Credentials("admin", "123456"),
                TokenPayload.class);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.data());
        assertEquals("abc", response.data().accessToken());
        assertEquals("http://localhost:8080/api/auth/login", transport.lastRequest.uri().toString());
        assertEquals("application/json", transport.lastRequest.headers().firstValue("Content-Type").orElse(null));
    }

    @Test
    void postShouldAttachAuthorizationHeaderWhenTokenPresent() {
        tokenStore.store(new TokenPayload("token-xyz", 3600));
        transport.configure(200, """
                {"code":200,"msg":null,"data":{"access_token":"abc","expires_in":720}}
                """);
        HttpExecutor executor = new HttpExecutor(properties, tokenStore, transport, mapper);

        executor.post("/auth/login", new Credentials("admin", "123456"), TokenPayload.class);

        assertEquals("Bearer token-xyz",
                transport.lastRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    void postShouldSkipExpiredTokenAndClearStore() {
        ExpiredTokenStore expiredStore = new ExpiredTokenStore();
        transport.configure(200, """
                {"code":200,"msg":null,"data":{"access_token":"abc","expires_in":720}}
                """);
        HttpExecutor executor = new HttpExecutor(properties, expiredStore, transport, mapper);

        executor.post("/auth/login", new Credentials("admin", "123456"), TokenPayload.class);

        assertTrue(transport.lastRequest.headers().firstValue("Authorization").isEmpty());
        assertTrue(expiredStore.isCleared());
    }

    @Test
    void postShouldThrowWhenApiReturnsErrorCode() {
        transport.configure(200, """
                {"code":401,"msg":"Unauthorized","data":null}
                """);
        HttpExecutor executor = new HttpExecutor(properties, tokenStore, transport, mapper);

        ApiClientException ex = assertThrows(ApiClientException.class, () ->
                executor.post("/auth/login", new Credentials("admin", "bad"), TokenPayload.class));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void postShouldThrowWhenHttpStatusIsNotSuccessful() {
        transport.configure(500, """
                {"code":500,"msg":"Internal","data":null}
                """);
        HttpExecutor executor = new HttpExecutor(properties, tokenStore, transport, mapper);

        ApiClientException ex = assertThrows(ApiClientException.class, () ->
                executor.post("/auth/login", new Credentials("admin", "bad"), TokenPayload.class));
        assertEquals("Internal", ex.getMessage());
    }

    private record Credentials(String username, String password) {
    }

    private static class StubTransport implements HttpTransport {

        private HttpTransportResponse nextResponse;
        private HttpRequest lastRequest;

        void configure(int statusCode, String body) {
            this.nextResponse = new HttpTransportResponse(statusCode, body);
        }

        @Override
        public HttpTransportResponse execute(HttpRequest request) {
            this.lastRequest = request;
            return nextResponse;
        }
    }

    private static final class ExpiredTokenStore extends TokenStore {
        private final java.util.concurrent.atomic.AtomicBoolean cleared = new java.util.concurrent.atomic.AtomicBoolean();

        @Override
        public boolean hasValidToken() {
            return false;
        }

        @Override
        public boolean isExpired() {
            return true;
        }

        @Override
        public Optional<String> accessToken() {
            return Optional.of("expired-token");
        }

        @Override
        public void clear() {
            super.clear();
            cleared.set(true);
        }

        boolean isCleared() {
            return cleared.get();
        }
    }
}
