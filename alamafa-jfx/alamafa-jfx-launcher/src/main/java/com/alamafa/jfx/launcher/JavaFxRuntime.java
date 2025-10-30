package com.alamafa.jfx.launcher;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the active JavaFX lifecycle coordinator so that the {@link javafx.application.Application}
 * instance bootstrapped by the platform can access framework state prepared by the launcher.
 */
final class JavaFxRuntime {
    private static final AtomicReference<JavaFxLifecycleCoordinator> COORDINATOR = new AtomicReference<>();

    private JavaFxRuntime() {
    }

    static void install(JavaFxLifecycleCoordinator coordinator) {
        if (!COORDINATOR.compareAndSet(null, coordinator)) {
            throw new IllegalStateException("JavaFX runtime already initialized");
        }
    }

    static JavaFxLifecycleCoordinator require() {
        JavaFxLifecycleCoordinator coordinator = COORDINATOR.get();
        if (coordinator == null) {
            throw new IllegalStateException("JavaFX runtime not initialized");
        }
        return coordinator;
    }

    static void clear() {
        COORDINATOR.set(null);
    }
}
