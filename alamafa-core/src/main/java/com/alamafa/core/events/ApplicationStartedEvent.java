package com.alamafa.core.events;

import com.alamafa.core.ApplicationContext;

/**
 * Published after the application lifecycle START phase completes successfully.
 */
public final class ApplicationStartedEvent extends ApplicationEvent {
    private final ApplicationContext context;

    public ApplicationStartedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext context() {
        return context;
    }
}

