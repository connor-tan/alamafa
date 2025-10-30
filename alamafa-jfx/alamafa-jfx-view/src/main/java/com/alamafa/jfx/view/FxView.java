package com.alamafa.jfx.view;

import javafx.scene.Parent;

import java.util.Objects;

/**
 * Immutable representation of a loaded JavaFX view, exposing both the root node and
 * the controller instance created by the loader.
 *
 * @param root       primary node to be attached to the scene graph
 * @param controller controller instance (may be {@code null} for pure view cases)
 * @param <T>        controller type
 */
public record FxView<T>(Parent root, T controller) {
    public FxView {
        Objects.requireNonNull(root, "root");
    }

    /**
     * Convenience accessor returning the controller cast to the desired type.
     */
    public <C extends T> C controllerAs(Class<C> type) {
        Objects.requireNonNull(type, "type");
        return type.cast(controller);
    }
}
