package com.alamafa.jfx.viewmodel.lifecycle;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationLifecycle;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import javafx.stage.Stage;

import java.util.Objects;

public final class FxPrimaryStageLifecycle implements ApplicationLifecycle {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(FxPrimaryStageLifecycle.class);

    private final FxWindowManager windowManager;

    public FxPrimaryStageLifecycle(FxWindowManager windowManager) {
        this.windowManager = Objects.requireNonNull(windowManager, "windowManager");
    }

    @Override
    public void start(ApplicationContext context) {
        Stage stage = context.get(Stage.class);
        if (stage == null) {
            LOGGER.warn("Primary Stage not available in ApplicationContext; skipping auto mount");
            return;
        }
        windowManager.mountPrimaryStage(stage);
    }

    @Override
    public void stop(ApplicationContext context) {
        windowManager.unmountPrimaryStage();
    }
}
