package com.alamafa.jfx.vlcj.embedded;

import com.alamafa.di.annotation.Component;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import javafx.scene.layout.StackPane;

@Component
public class EmbeddedPlayerManager {

    public EmbeddedPlayerSession attach(StackPane container, PlayerProperties properties) {
        EmbeddedPlayerSession session = new EmbeddedPlayerSession(
                container,
                properties.getWindowWidth(),
                properties.getWindowHeight()
        );
        session.initialize();
        return session;
    }

    public EmbeddedPlayerSession attach(StackPane container, int preferredWidth, int preferredHeight) {
        EmbeddedPlayerSession session = new EmbeddedPlayerSession(container, preferredWidth, preferredHeight);
        session.initialize();
        return session;
    }
}
