package com.alamafa.jfx.viewmodel.annotation;

/**
 * Defines lifecycle scopes for JavaFX view-model instances.
 */
public enum FxViewModelScope {
    /** Single instance shared across the application lifecycle. */
    APPLICATION,
    /** A fresh instance per view binding. */
    VIEW
}
