package com.alamafa.tower.client.api.client;

/**
 * 表示底层HTTP网络请求失败，例如连接超时或IO异常。
 */
public class ApiNetworkException extends ApiClientException {

    public ApiNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
