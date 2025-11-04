package com.alamafa.tower.client.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alamafa.tower.client.api.dto.ApiResponse;
import com.alamafa.tower.client.api.auth.TokenStore;
import com.alamafa.di.annotation.Service;
import com.alamafa.di.annotation.Inject;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Optional;

@Service
public class HttpExecutor {

    private final ApiProperties properties;
    private final TokenStore tokenStore;
    private final HttpTransport transport;
    private final ObjectMapper objectMapper;

    @Inject
    public HttpExecutor(ApiProperties properties, TokenStore tokenStore, HttpTransport transport) {
        this(properties, tokenStore, transport, defaultMapper());
    }

    HttpExecutor(ApiProperties properties,
                 TokenStore tokenStore,
                 HttpTransport transport,
                 ObjectMapper objectMapper) {
        this.properties = properties;
        this.tokenStore = tokenStore;
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    public <T> ApiResponse<T> post(String path, Object payload, Class<T> dataType) {
        HttpRequest request = buildRequest(path, HttpRequest.BodyPublishers.ofString(writeBody(payload)), "POST");
        HttpTransportResponse response = transport.execute(request);
        return parseResponse(response, dataType);
    }

    public <T> ApiResponse<T> get(String path, Class<T> dataType) {
        HttpRequest request = buildRequest(path, HttpRequest.BodyPublishers.noBody(), "GET");
        HttpTransportResponse response = transport.execute(request);
        return parseResponse(response, dataType);
    }

    private HttpRequest buildRequest(String path, HttpRequest.BodyPublisher bodyPublisher, String method) {
        URI uri = properties.resolve(path);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(properties.getRequestTimeout())
                .header("Accept", "application/json");
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            builder.header("Content-Type", "application/json");
        }
        tokenStore.accessToken().ifPresent(token -> builder.header("Authorization", "Bearer " + token));
        builder.method(method.toUpperCase(), bodyPublisher);
        return builder.build();
    }

    private <T> ApiResponse<T> parseResponse(HttpTransportResponse response, Class<T> dataType) {
        int status = response.statusCode();
        String body = Optional.ofNullable(response.body()).orElse("");
        try {
            JavaType javaType = objectMapper.getTypeFactory()
                    .constructParametricType(ApiResponse.class, dataType);
            ApiResponse<T> parsed = objectMapper.readValue(body, javaType);
            if (parsed == null) {
                throw new ApiClientException("Response body is empty");
            }
            if (status < 200 || status >= 300) {
                String message = parsed.msg();
                throw new ApiClientException(messageOrFallback(message, "请求失败，状态码 " + status));
            }
            if (!parsed.isSuccess()) {
                String message = parsed.msg();
                String fallback = "请求失败，错误码 " + parsed.code();
                throw new ApiClientException(messageOrFallback(message, fallback));
            }
            return parsed;
        } catch (JsonProcessingException ex) {
            if (status < 200 || status >= 300) {
                throw new ApiClientException("请求失败，状态码 " + status, ex);
            }
            throw new ApiClientException("Failed to parse API response", ex);
        }
    }

    private String writeBody(Object payload) {
        if (payload == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ApiClientException("Failed to serialize request payload", ex);
        }
    }

    private static ObjectMapper defaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        return mapper;
    }

    private String messageOrFallback(String message, String fallback) {
        return message != null && !message.isBlank() ? message : fallback;
    }
}
