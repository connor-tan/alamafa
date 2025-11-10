package com.alamafa.jfx.vlcj.core;

/**
 * 创建 {@link MediaEndpoint} 的工厂接口，方便后续扩展不同实现。
 */
public interface MediaEndpointFactory {

    MediaEndpoint launch(PlayerLaunchRequest request);
}
