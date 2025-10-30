package com.alamafa.jfx.viewmodel;

import javafx.beans.property.BooleanProperty;

/**
 * Represents an executable UI command, exposing reactive flags that can be bound to controls.
 */
public interface FxCommand {
    void execute();

    BooleanProperty runningProperty();

    BooleanProperty executableProperty();

    default boolean isRunning() {
        return runningProperty().get();
    }

    default boolean isExecutable() {
        return executableProperty().get();
    }
}
