package com.alamafa.core.events;

import com.alamafa.core.ApplicationContext;

/**
 * Published after the application lifecycle STOP phase completes.
 */
public final class ApplicationStoppedEvent extends ApplicationEvent {
    private final ApplicationContext context;

    public ApplicationStoppedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext context() {
        return context;
    }
}

