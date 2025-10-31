package com.alamafa.jfx.view;

import com.alamafa.core.ApplicationContext;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class FxViewLoaderTest {

    @BeforeAll
    static void initFxToolkit() throws Exception {
        FxTestSupport.initToolkit();
    }

    @Test
    void sharedViewInstanceIsReusedUntilAttached() throws Exception {
        ApplicationContext context = new ApplicationContext();
        FxViewRegistry viewRegistry = new FxViewRegistry();
        FxViewLoader loader = new FxViewLoader(context, viewRegistry, ResourceResolver.classpath(), () -> null);

        FxView<SharedPane> first = FxTestSupport.call(() -> loader.load(SharedPane.class));
        FxView<SharedPane> second = FxTestSupport.call(() -> loader.load(SharedPane.class));

        assertSame(first.root(), second.root(), "shareable view should reuse root while detached");

        CountDownLatch shown = new CountDownLatch(1);
        FxTestSupport.run(() -> {
            Stage stage = new Stage();
            stage.addEventHandler(javafx.stage.WindowEvent.WINDOW_SHOWN, event -> shown.countDown());
            stage.setScene(new Scene(first.root(), 200, 120));
            stage.show();
        });
        shown.await(5, TimeUnit.SECONDS);

        FxView<SharedPane> third = FxTestSupport.call(() -> loader.load(SharedPane.class));
        assertNotSame(first.root(), third.root(), "view should be recreated when cached instance is in use");

        FxTestSupport.run(() -> {
            Stage stage = (Stage) first.root().getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
        FxTestSupport.flush();
    }

    @FxViewSpec(shared = true)
    static class SharedPane extends Pane {
    }

    private static final class FxTestSupport {
        private static volatile boolean toolkitStarted = false;

        private FxTestSupport() {
        }

        static void initToolkit() throws Exception {
            if (toolkitStarted) {
                return;
            }
            synchronized (FxTestSupport.class) {
                if (toolkitStarted) {
                    return;
                }
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    Platform.startup(latch::countDown);
                    latch.await(10, TimeUnit.SECONDS);
                    toolkitStarted = true;
                } catch (IllegalStateException alreadyStarted) {
                    toolkitStarted = true;
                } catch (Throwable ex) {
                    Assumptions.assumeTrue(false, "JavaFX toolkit unavailable: " + ex.getMessage());
                }
            }
        }

        static void run(Runnable runnable) throws Exception {
            if (Platform.isFxApplicationThread()) {
                runnable.run();
                return;
            }
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for FX task");
            }
        }

        static <T> T call(Callable<T> callable) throws Exception {
            if (Platform.isFxApplicationThread()) {
                return callable.call();
            }
            CompletableFuture<T> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    future.complete(callable.call());
                } catch (Throwable ex) {
                    future.completeExceptionally(ex);
                }
            });
            return future.get(10, TimeUnit.SECONDS);
        }

        static void flush() throws Exception {
            run(() -> { /* no-op */ });
        }
    }
}
