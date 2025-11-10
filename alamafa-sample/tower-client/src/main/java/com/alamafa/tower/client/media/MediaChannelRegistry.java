package com.alamafa.tower.client.media;

import com.alamafa.di.annotation.Component;
import com.alamafa.jfx.vlcj.core.MediaEventDispatcher;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MediaChannelRegistry {

    private final ObservableList<MediaChannelStatus> channelStatuses = FXCollections.observableArrayList();
    private final Map<UUID, MediaChannelStatus> statusMap = new ConcurrentHashMap<>();

    public MediaChannelRegistry(MediaEventDispatcher dispatcher) {
        dispatcher.addListener(this::handleEvent);
    }

    public ObservableList<MediaChannelStatus> getChannelStatuses() {
        return channelStatuses;
    }

    private void handleEvent(MediaEvent event) {
        UUID playerId = event.playerId();
        MediaChannelStatus status = statusMap.compute(playerId, (id, existing) -> {
            if (existing == null) {
                return new MediaChannelStatus(id, event.type().name(), event.timestamp());
            }
            existing.update(event.type().name(), event.timestamp());
            return existing;
        });
        Platform.runLater(() -> refreshList(status));
    }

    private void refreshList(MediaChannelStatus status) {
        channelStatuses.removeIf(entry -> entry.getPlayerId().equals(status.getPlayerId()));
        channelStatuses.add(status.copy());
        channelStatuses.sort(Comparator.comparing(MediaChannelStatus::getLastUpdated).reversed());
    }
}
