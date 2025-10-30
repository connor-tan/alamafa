package com.alamafa.jfx.launcher;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.Lifecycle;
import com.alamafa.core.LifecycleErrorHandler;
import com.alamafa.core.LifecycleExecutionException;
import com.alamafa.core.LifecyclePhase;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Coordinates the Alamafa lifecycle with the JavaFX platform lifecycle.
 */
final class JavaFxLifecycleCoordinator {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(JavaFxLifecycleCoordinator.class);

    private final ApplicationContext context;
    private final Lifecycle lifecycle;
    private final LifecycleErrorHandler errorHandler;

    JavaFxLifecycleCoordinator(ApplicationContext context, Lifecycle lifecycle) {
        this.context = Objects.requireNonNull(context, "context");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.errorHandler = context.get(LifecycleErrorHandler.class);
    }

    void onInit() throws Exception {
        invoke(LifecyclePhase.INIT, () -> lifecycle.init(context));
    }

    void onStart(Stage primaryStage) throws Exception {
        if (primaryStage != null) {
            context.put(Stage.class, primaryStage);
        }
        try {
            invoke(LifecyclePhase.START, () -> lifecycle.start(context));
        } catch (Exception ex) {
            safeStop();
            throw ex;
        }
    }

    void onStop() throws Exception {
        invoke(LifecyclePhase.STOP, () -> lifecycle.stop(context));
    }

    private void invoke(LifecyclePhase phase, LifecycleInvocation invocation) throws Exception {
        try {
            invocation.invoke();
        } catch (Exception ex) {
            throw report(phase, ex);
        }
    }

    private void safeStop() {
        try {
            lifecycle.stop(context);
        } catch (Exception ex) {
            report(LifecyclePhase.STOP, ex);
        }
    }

    private Exception report(LifecyclePhase phase, Exception ex) {
        Exception toReport = ex instanceof LifecycleExecutionException execution && execution.phase() == phase
                ? ex
                : new LifecycleExecutionException(phase, lifecycle.getClass().getName(), ex);
        if (errorHandler != null) {
            try {
                errorHandler.onError(phase, toReport);
            } catch (Exception handlerError) {
                LOGGER.warn("Lifecycle error handler failed for phase {}", phase, handlerError);
            }
        } else {
            LOGGER.error("Lifecycle phase {} failed", phase, toReport);
        }
        return toReport;
    }

    private interface LifecycleInvocation {
        void invoke() throws Exception;
    }
}
