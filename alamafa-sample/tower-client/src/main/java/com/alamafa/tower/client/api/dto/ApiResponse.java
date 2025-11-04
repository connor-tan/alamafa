package com.alamafa.tower.client.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(
        @JsonProperty("code") int code,
        @JsonProperty("msg") String msg,
        @JsonProperty("data") T data
) {

    public boolean isSuccess() {
        return code == 200;
    }
}
