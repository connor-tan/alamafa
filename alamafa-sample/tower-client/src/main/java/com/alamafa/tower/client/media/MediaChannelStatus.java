package com.alamafa.tower.client.media;

import java.time.Instant;
import java.util.UUID;

public class MediaChannelStatus {

    private final UUID playerId;
    private String status;
    private Instant lastUpdated;

    public MediaChannelStatus(UUID playerId, String status, Instant lastUpdated) {
        this.playerId = playerId;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    void update(String status, Instant timestamp) {
        this.status = status;
        this.lastUpdated = timestamp != null ? timestamp : Instant.now();
    }

    MediaChannelStatus copy() {
        return new MediaChannelStatus(playerId, status, lastUpdated);
    }
}
