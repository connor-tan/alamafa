package com.alamafa.tower.client.api.client;

import com.alamafa.di.annotation.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class JavaHttpTransport implements HttpTransport {

    private final HttpClient httpClient;

    public JavaHttpTransport(ApiProperties properties) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Override
    public HttpTransportResponse execute(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new HttpTransportResponse(response.statusCode(), response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            throw new ApiNetworkException("Failed to execute HTTP request", ex);
        }
    }
}
