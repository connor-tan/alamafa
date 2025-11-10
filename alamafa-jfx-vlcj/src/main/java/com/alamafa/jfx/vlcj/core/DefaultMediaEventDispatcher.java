package com.alamafa.jfx.vlcj.core;

import com.alamafa.di.annotation.Component;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DefaultMediaEventDispatcher implements MediaEventDispatcher {

    private final List<MediaEventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(MediaEventListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        listeners.add(listener);
    }

    @Override
    public void removeListener(MediaEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispatch(MediaEvent event) {
        for (MediaEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
