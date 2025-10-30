package com.alamafa.jfx.view;

/**
 * Runtime exception thrown when FXML or associated resources cannot be loaded.
 */
public class ViewLoadingException extends RuntimeException {
    public ViewLoadingException(String message) {
        super(message);
    }

    public ViewLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
