package com.alamafa.jfx.vlcj.core;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 描述播放源及其附加参数。
 */
public final class MediaSource {

    private final String mediaUrl;
    private final List<String> options;

    private MediaSource(String mediaUrl, List<String> options) {
        this.mediaUrl = Objects.requireNonNull(mediaUrl, "mediaUrl must not be null").trim();
        if (this.mediaUrl.isEmpty()) {
            throw new IllegalArgumentException("mediaUrl must not be blank");
        }
        this.options = options == null ? List.of() : List.copyOf(options);
    }

    public static MediaSource of(String mediaUrl) {
        return new MediaSource(mediaUrl, List.of());
    }

    public static MediaSource of(String mediaUrl, List<String> options) {
        return new MediaSource(mediaUrl, options);
    }

    public String mediaUrl() {
        return mediaUrl;
    }

    public List<String> options() {
        return Collections.unmodifiableList(options);
    }
}
