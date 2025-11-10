package com.alamafa.jfx.vlcj.external;

import com.alamafa.core.events.ApplicationEventListener;
import com.alamafa.core.events.ApplicationStoppingEvent;
import com.alamafa.di.annotation.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ExternalProcessRegistry implements ApplicationEventListener<ApplicationStoppingEvent> {

    private final List<ExternalPlayerHandle> handles = new CopyOnWriteArrayList<>();

    public void register(ExternalPlayerHandle handle) {
        handles.add(handle);
    }

    public void unregister(ExternalPlayerHandle handle) {
        handles.remove(handle);
    }

    public void shutdownAll() {
        handles.forEach(ExternalPlayerHandle::close);
        handles.clear();
    }

    @Override
    public void onEvent(ApplicationStoppingEvent event) {
        shutdownAll();
    }

    @Override
    public Class<ApplicationStoppingEvent> getEventType() {
        return ApplicationStoppingEvent.class;
    }
}
