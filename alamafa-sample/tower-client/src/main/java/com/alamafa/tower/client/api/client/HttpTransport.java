package com.alamafa.tower.client.api.client;

import java.net.http.HttpRequest;

public interface HttpTransport {

    HttpTransportResponse execute(HttpRequest request);
}
