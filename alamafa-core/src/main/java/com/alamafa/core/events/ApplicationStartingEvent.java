package com.alamafa.core.events;

import com.alamafa.core.ApplicationContext;

/**
 * Published when the application bootstrap sequence begins.
 */
public final class ApplicationStartingEvent extends ApplicationEvent {
    private final ApplicationContext context;

    public ApplicationStartingEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext context() {
        return context;
    }
}

