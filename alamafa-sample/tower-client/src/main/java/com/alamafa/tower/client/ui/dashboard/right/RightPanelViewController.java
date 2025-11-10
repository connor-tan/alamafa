package com.alamafa.tower.client.ui.dashboard.right;

import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.tower.client.media.MediaChannelRegistry;
import com.alamafa.tower.client.media.MediaChannelStatus;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

@FxViewSpec(
        fxml = "views/dashboard/right-panel.fxml",
        styles = {"styles/dashboard.css"}
)
public class RightPanelViewController {

    @FXML
    private ListView<MediaChannelStatus> channelList;

    @Inject
    private MediaChannelRegistry registry;

    @FXML
    private void initialize() {
        if (channelList != null && registry != null) {
            channelList.setItems(registry.getChannelStatuses());
            channelList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(MediaChannelStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        getStyleClass().removeAll("status-error", "status-warn", "status-ok");
                    } else {
                        setText("通道 " + shortId(item.getPlayerId()) + " - " + item.getStatus() + " @ " + item.getLastUpdated());
                        styleFor(item.getStatus());
                    }
                }

                private String shortId(java.util.UUID id) {
                    String raw = id.toString();
                    return raw.substring(0, 8);
                }

                private void styleFor(String status) {
                    getStyleClass().removeAll("status-error", "status-warn", "status-ok");
                    if (status == null) {
                        return;
                    }
                    String upper = status.toUpperCase();
                    if (upper.contains("ERROR")) {
                        getStyleClass().add("status-error");
                    } else if (upper.contains("HEARTBEAT")) {
                        getStyleClass().add("status-warn");
                    } else {
                        getStyleClass().add("status-ok");
                    }
                }
            });
        }
    }
}
