package com.alamafa.jfx.viewmodel.meta;

import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;

import java.util.Objects;

/**
 * Metadata captured from {@code @FxViewModelSpec}.
 */
public record FxViewModelDescriptor(Class<?> type, String name, boolean lazy, FxViewModelScope scope) {
    public FxViewModelDescriptor {
        Objects.requireNonNull(type, "type");
        if (name != null) {
            name = name.trim();
        }
        scope = scope == null ? FxViewModelScope.VIEW : scope;
    }
}
