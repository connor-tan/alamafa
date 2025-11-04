package com.alamafa.tower.client.api.auth;

import com.alamafa.tower.client.api.client.ApiClientException;

public class AuthFailedException extends ApiClientException {

    public AuthFailedException(String message) {
        super(message);
    }
}
