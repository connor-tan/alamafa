package com.alamafa.core;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default JVM-based launcher that executes lifecycle phases on the calling thread
 * and installs a shutdown hook to trigger graceful termination.
 */
public final class DefaultApplicationLauncher implements ContextAwareApplicationLauncher {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(DefaultApplicationLauncher.class);

    private final ApplicationContext context = new ApplicationContext();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);

    private volatile Lifecycle lifecycle;
    private volatile Thread shutdownHook;

    public DefaultApplicationLauncher() {
        context.put(ApplicationShutdown.class, this::requestShutdown);
    }

    @Override
    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public synchronized void launch(Lifecycle lifecycle) {
        Objects.requireNonNull(lifecycle, "lifecycle");
        if (running.get()) {
            throw new IllegalStateException("Application lifecycle already running");
        }
        this.lifecycle = lifecycle;
        boolean initialized = false;
        try {
            lifecycle.init(context);
            initialized = true;
            lifecycle.start(context);
            installShutdownHook();
            running.set(true);
        } catch (Exception ex) {
            if (initialized) {
                safeStop(lifecycle);
            }
            throw new ApplicationLaunchException("Failed to launch application", ex);
        }
    }

    /**
     * Initiates an orderly shutdown, invoked either by the hook or programmatically.
     */
    private void stopLifecycle(String trigger) {
        Lifecycle active = lifecycle;
        if (active == null || !stopRequested.compareAndSet(false, true)) {
            return;
        }
        removeShutdownHook();
        try {
            active.stop(context);
        } catch (Exception ex) {
            reportStopFailure(active, ex);
        } finally {
            running.set(false);
            lifecycle = null;
            LOGGER.info("Application stopped via {}", trigger);
        }
    }

    private void installShutdownHook() {
        Thread hook = new Thread(() -> stopLifecycle("JVM shutdown"), "alamafa-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(hook);
        shutdownHook = hook;
    }

    private void removeShutdownHook() {
        Thread hook = shutdownHook;
        if (hook == null) {
            return;
        }
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            // Hook is already executing or not registered; nothing else to do.
        } finally {
            shutdownHook = null;
        }
    }

    private void safeStop(Lifecycle lifecycle) {
        try {
            lifecycle.stop(context);
        } catch (Exception ex) {
            reportStopFailure(lifecycle, ex);
        }
    }

    private void reportStopFailure(Lifecycle lifecycle, Exception ex) {
        LifecycleErrorHandler handler = context.get(LifecycleErrorHandler.class);
        Exception toReport = ex instanceof LifecycleExecutionException
                ? ex
                : new LifecycleExecutionException(LifecyclePhase.STOP,
                lifecycle == null ? "unknown lifecycle" : lifecycle.getClass().getName(),
                ex);
        if (handler != null) {
            handler.onError(LifecyclePhase.STOP, toReport);
        } else {
            LOGGER.error("Lifecycle stop phase failed", toReport);
        }
    }

    private void requestShutdown() {
        stopLifecycle("ApplicationShutdown");
    }
}

