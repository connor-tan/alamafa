package com.alamafa.jfx.launcher;

import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX {@link Application} that delegates lifecycle events back into the Alamafa runtime.
 */
public final class AlamafaFxApplication extends Application {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(AlamafaFxApplication.class);

    static void launchApplication() {
        Application.launch(AlamafaFxApplication.class);
    }

    @Override
    public void init() throws Exception {
        try {
            JavaFxRuntime.require().onInit();
        } catch (Exception ex) {
            JavaFxRuntime.clear();
            throw ex;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            JavaFxRuntime.require().onStart(primaryStage);
        } catch (Exception ex) {
            LOGGER.error("JavaFX start phase failed, requesting platform exit", ex);
            Platform.exit();
            JavaFxRuntime.clear();
            throw ex;
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            JavaFxRuntime.require().onStop();
        } finally {
            JavaFxRuntime.clear();
        }
    }
}
