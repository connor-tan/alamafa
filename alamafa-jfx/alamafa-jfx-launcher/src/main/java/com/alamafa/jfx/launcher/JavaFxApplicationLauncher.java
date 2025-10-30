package com.alamafa.jfx.launcher;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLaunchException;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.ContextAwareApplicationLauncher;
import com.alamafa.core.Lifecycle;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link ContextAwareApplicationLauncher} implementation that bridges the Alamafa lifecycle
 * with the JavaFX platform lifecycle.
 */
public final class JavaFxApplicationLauncher implements ContextAwareApplicationLauncher {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(JavaFxApplicationLauncher.class);

    private final ApplicationContext context = new ApplicationContext();
    private final AtomicBoolean launched = new AtomicBoolean(false);
    private volatile Lifecycle lifecycle;

    public JavaFxApplicationLauncher() {
        context.put(ApplicationShutdown.class, this::requestShutdown);
    }

    @Override
    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public synchronized void launch(Lifecycle lifecycle) {
        Objects.requireNonNull(lifecycle, "lifecycle");
        if (!launched.compareAndSet(false, true)) {
            throw new IllegalStateException("JavaFX application lifecycle already running");
        }
        this.lifecycle = lifecycle;
        JavaFxLifecycleCoordinator coordinator = new JavaFxLifecycleCoordinator(context, lifecycle);
        JavaFxRuntime.install(coordinator);
        try {
            AlamafaFxApplication.launchApplication();
        } catch (Exception ex) {
            JavaFxRuntime.clear();
            launched.set(false);
            throw new ApplicationLaunchException("Failed to launch JavaFX application", ex);
        }
    }

    private void requestShutdown() {
        try {
            if (Platform.isFxApplicationThread()) {
                Platform.exit();
            } else {
                Platform.runLater(Platform::exit);
            }
        } catch (IllegalStateException ex) {
            // Platform not initialized; ignore to allow graceful fallback.
            LOGGER.debug("JavaFX platform not initialized during shutdown request", ex);
        }
    }
}
