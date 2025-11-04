package com.alamafa.tower.client.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenPayload(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") long expiresIn
) {
}
