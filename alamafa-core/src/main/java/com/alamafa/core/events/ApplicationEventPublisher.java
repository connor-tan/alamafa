package com.alamafa.core.events;

/**
 * Publishes application events to registered listeners.
 */
public interface ApplicationEventPublisher {

    /**
     * Publishes the given event to all matching listeners.
     */
    void publishEvent(ApplicationEvent event);

    /**
     * Registers a new listener.
     */
    void addListener(ApplicationEventListener<? extends ApplicationEvent> listener);

    /**
     * Removes a previously registered listener.
     */
    void removeListener(ApplicationEventListener<? extends ApplicationEvent> listener);
}

