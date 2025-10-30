package com.alamafa.jfx.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Executes a potentially long-running task off the UI thread while updating command state.
 */
public final class AsyncFxCommand implements FxCommand {
    private final Executor executor;
    private final Callable<?> task;
    private final Consumer<Throwable> errorHandler;
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final BooleanProperty executable = new SimpleBooleanProperty(true);

    public AsyncFxCommand(Executor executor, Callable<?> task) {
        this(executor, task, throwable -> { throw new RuntimeException(throwable); });
    }

    public AsyncFxCommand(Executor executor, Callable<?> task, Consumer<Throwable> errorHandler) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.task = Objects.requireNonNull(task, "task");
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    }

    @Override
    public void execute() {
        if (!isExecutable() || isRunning()) {
            return;
        }
        setExecutable(false);
        setRunning(true);
        executor.execute(() -> {
            try {
                task.call();
            } catch (Throwable ex) {
                handleError(ex);
            } finally {
                Platform.runLater(() -> {
                    setRunning(false);
                    setExecutable(true);
                });
            }
        });
    }

    @Override
    public BooleanProperty runningProperty() {
        return running;
    }

    @Override
    public BooleanProperty executableProperty() {
        return executable;
    }

    private void setRunning(boolean value) {
        Platform.runLater(() -> running.set(value));
    }

    private void setExecutable(boolean value) {
        Platform.runLater(() -> executable.set(value));
    }

    private void handleError(Throwable throwable) {
        Platform.runLater(() -> errorHandler.accept(throwable));
    }
}
