package com.alamafa.jfx.vlcj.ipc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MediaEvent {

    private final MediaEventType type;
    private final UUID playerId;
    private final Instant timestamp;
    private final Map<String, Object> payload;

    @JsonCreator
    public MediaEvent(@JsonProperty("type") MediaEventType type,
                      @JsonProperty("playerId") UUID playerId,
                      @JsonProperty("timestamp") Instant timestamp,
                      @JsonProperty("payload") Map<String, Object> payload) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
    }

    public MediaEventType type() {
        return type;
    }

    public UUID playerId() {
        return playerId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public Map<String, Object> payload() {
        return payload;
    }

    @JsonProperty("type")
    public MediaEventType getType() {
        return type;
    }

    @JsonProperty("playerId")
    public UUID getPlayerId() {
        return playerId;
    }

    @JsonProperty("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }

    @JsonProperty("payload")
    public Map<String, Object> getPayload() {
        return payload;
    }

    public static MediaEvent heartbeat(UUID playerId) {
        return new MediaEvent(MediaEventType.HEARTBEAT, playerId, Instant.now(), Map.of());
    }
}
