package com.alamafa.core.events;

import java.time.Instant;

/**
 * Base class for application events published through the event infrastructure.
 */
public abstract class ApplicationEvent {
    private final Instant timestamp;

    protected ApplicationEvent() {
        this.timestamp = Instant.now();
    }

    /**
     * Returns the instant the event was created.
     */
    public Instant timestamp() {
        return timestamp;
    }
}

