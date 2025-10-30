package com.alamafa.core;

/**
 * Runtime exception thrown when the {@link ApplicationLauncher} fails to start the application.
 */
public final class ApplicationLaunchException extends RuntimeException {

    /**
     * Creates a new instance with the given message and cause.
     */
    public ApplicationLaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}

