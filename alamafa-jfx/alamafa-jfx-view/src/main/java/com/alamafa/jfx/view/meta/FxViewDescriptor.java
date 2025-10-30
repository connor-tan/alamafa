package com.alamafa.jfx.view.meta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable metadata extracted from {@code @FxViewSpec}.
 */
public record FxViewDescriptor(Class<?> type,
                               String name,
                               String fxml,
                               List<String> styles,
                               String bundle,
                               Class<?> viewModelType,
                               boolean shared,
                               boolean primary,
                               String title,
                               double width,
                               double height,
                               boolean resizable) {

    public FxViewDescriptor {
        Objects.requireNonNull(type, "type");
        styles = styles == null ? List.of() : List.copyOf(styles);
        if (name != null) {
            name = name.trim();
        }
        title = title == null ? "" : title;
    }

    public Optional<String> fxmlOptional() {
        return fxml == null || fxml.isBlank() ? Optional.empty() : Optional.of(fxml);
    }

    public Optional<String> bundleOptional() {
        return bundle == null || bundle.isBlank() ? Optional.empty() : Optional.of(bundle);
    }

    public Optional<Class<?>> viewModelOptional() {
        return viewModelType == null || Object.class.equals(viewModelType)
                ? Optional.empty()
                : Optional.of(viewModelType);
    }

    public static FxViewDescriptor of(Class<?> type,
                                      String name,
                                      String fxml,
                                      String[] styles,
                                      String bundle,
                                      Class<?> viewModelType,
                                      boolean shared,
                                      boolean primary,
                                      String title,
                                      double width,
                                      double height,
                                      boolean resizable) {
        return new FxViewDescriptor(type,
                name,
                fxml,
                styles == null ? List.of() : Arrays.asList(styles),
                bundle,
                viewModelType,
                shared,
                primary,
                title,
                width,
                height,
                resizable);
    }
}
