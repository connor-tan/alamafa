package com.alamafa.jfx.vlcj.ipc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 序列化传输的媒体控制指令。
 */
public final class MediaCommand {

    private final MediaCommandType type;
    private final UUID playerId;
    private final Map<String, Object> payload;

    @JsonCreator
    public MediaCommand(@JsonProperty("type") MediaCommandType type,
                        @JsonProperty("playerId") UUID playerId,
                        @JsonProperty("payload") Map<String, Object> payload) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
    }

    public MediaCommandType type() {
        return type;
    }

    public UUID playerId() {
        return playerId;
    }

    public Map<String, Object> payload() {
        return Collections.unmodifiableMap(payload);
    }

    @JsonProperty("type")
    public MediaCommandType getType() {
        return type;
    }

    @JsonProperty("playerId")
    public UUID getPlayerId() {
        return playerId;
    }

    @JsonProperty("payload")
    public Map<String, Object> getPayload() {
        return payload;
    }

    public static MediaCommand play(UUID playerId, String mediaUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("media", mediaUrl);
        return new MediaCommand(MediaCommandType.PLAY, playerId, map);
    }

    public static MediaCommand pause(UUID playerId) {
        return new MediaCommand(MediaCommandType.PAUSE, playerId, Map.of());
    }

    public static MediaCommand resume(UUID playerId) {
        return new MediaCommand(MediaCommandType.RESUME, playerId, Map.of());
    }

    public static MediaCommand stop(UUID playerId) {
        return new MediaCommand(MediaCommandType.STOP, playerId, Map.of());
    }

    public static MediaCommand setVolume(UUID playerId, int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", value);
        return new MediaCommand(MediaCommandType.SET_VOLUME, playerId, map);
    }

    public static MediaCommand seek(UUID playerId, double positionSeconds) {
        Map<String, Object> map = new HashMap<>();
        map.put("position", positionSeconds);
        return new MediaCommand(MediaCommandType.SEEK, playerId, map);
    }
}
