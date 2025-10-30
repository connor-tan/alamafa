package com.alamafa.core.events;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default in-memory event publisher using synchronous dispatch.
 */
public final class DefaultApplicationEventPublisher implements ApplicationEventPublisher {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(DefaultApplicationEventPublisher.class);

    private final List<ApplicationEventListener<? extends ApplicationEvent>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publishEvent(ApplicationEvent event) {
        Objects.requireNonNull(event, "event");
        for (ApplicationEventListener<? extends ApplicationEvent> listener : listeners) {
            if (supports(listener, event)) {
                dispatch(listener, event);
            }
        }
    }

    @Override
    public void addListener(ApplicationEventListener<? extends ApplicationEvent> listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(ApplicationEventListener<? extends ApplicationEvent> listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    private static boolean supports(ApplicationEventListener<?> listener, ApplicationEvent event) {
        Class<?> eventType = listener.getEventType();
        if (eventType == null || eventType.equals(ApplicationEvent.class)) {
            return true;
        }
        return eventType.isAssignableFrom(event.getClass());
    }

    @SuppressWarnings("unchecked")
    private void dispatch(ApplicationEventListener<? extends ApplicationEvent> rawListener, ApplicationEvent event) {
        ApplicationEventListener<ApplicationEvent> listener = (ApplicationEventListener<ApplicationEvent>) rawListener;
        try {
            listener.onEvent(event);
        } catch (Exception ex) {
            LOGGER.warn("Application event listener {} failed while handling {}", listener, event.getClass().getName(), ex);
        }
    }
}

