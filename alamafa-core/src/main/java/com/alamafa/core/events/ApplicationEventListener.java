package com.alamafa.core.events;

/**
 * Listener for application events. Implementations typically declare the event type they support.
 */
@FunctionalInterface
public interface ApplicationEventListener<E extends ApplicationEvent> {

    /**
     * Handles the incoming event.
     */
    void onEvent(E event) throws Exception;

    /**
     * Returns the supported event type. Default implementation matches the generic parameter when possible.
     */
    @SuppressWarnings("unchecked")
    default Class<E> getEventType() {
        return (Class<E>) ApplicationEvent.class;
    }
}

