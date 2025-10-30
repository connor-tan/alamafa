package com.alamafa.core.events;

import com.alamafa.core.ApplicationContext;

/**
 * Published right before the application lifecycle STOP phase begins.
 */
public final class ApplicationStoppingEvent extends ApplicationEvent {
    private final ApplicationContext context;

    public ApplicationStoppingEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext context() {
        return context;
    }
}

