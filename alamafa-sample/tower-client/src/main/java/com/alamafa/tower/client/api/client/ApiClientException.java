package com.alamafa.tower.client.api.client;

/**
 * 统一封装API调用过程中产生的异常。
 */
public class ApiClientException extends RuntimeException {

    public ApiClientException(String message) {
        super(message);
    }

    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
