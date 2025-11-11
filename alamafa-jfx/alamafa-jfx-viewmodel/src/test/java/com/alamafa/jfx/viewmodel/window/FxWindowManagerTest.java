package com.alamafa.jfx.viewmodel.window;

import com.alamafa.core.ApplicationContext;
import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.FxViewLoader;
import com.alamafa.jfx.view.ResourceResolver;
import com.alamafa.jfx.view.annotation.PreClose;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.FxViewModelBinder;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.meta.FxViewModelDescriptor;
import com.alamafa.jfx.viewmodel.meta.FxViewModelRegistry;
import com.alamafa.jfx.viewmodel.window.FxWindowManager;
import com.alamafa.jfx.viewmodel.window.FxWindowHandle;
import com.alamafa.jfx.viewmodel.window.FxWindowOptions;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxWindowManagerTest {

    @BeforeAll
    static void initToolkit() throws Exception {
        FxTestSupport.initToolkit();
    }

    @BeforeEach
    void resetCounters() {
        TestViewModel.reset();
    }

    @Test
    void closeRequestConsumedSkipsCleanup() throws Exception {
        ApplicationContext context = new ApplicationContext();
        FxViewRegistry viewRegistry = new FxViewRegistry();
        FxViewModelRegistry viewModelRegistry = new FxViewModelRegistry();
        viewModelRegistry.register(new FxViewModelDescriptor(TestViewModel.class, "testViewModel", false, FxViewModelScope.VIEW));

        FxViewLoader loader = new FxViewLoader(context, viewRegistry, ResourceResolver.classpath(), () -> null);
        FxViewModelBinder binder = new FxViewModelBinder(context, null, viewRegistry, viewModelRegistry);
        FxWindowManager manager = new FxWindowManager(context, loader, binder, viewRegistry);

        FxWindowHandle handle = FxTestSupport.call(() -> manager.openWindow(ConsumingView.class, FxWindowOptions.builder().build()));
        try {
            assertEquals(1, TestViewModel.activeCount.get(), "view model should be activated after opening window");

            FxTestSupport.run(() -> WindowEvent.fireEvent(handle.stage(), new WindowEvent(handle.stage(), WindowEvent.WINDOW_CLOSE_REQUEST)));
            FxTestSupport.flush();

            assertEquals(0, TestViewModel.inactiveCount.get(), "close request was consumed, so view model should remain active");
            assertEquals(0, TestViewModel.detachedCount.get(), "view model must not detach when close is cancelled");
            assertTrue(handle.stage().isShowing(), "stage should remain visible when close is cancelled");

            FxTestSupport.run(handle.stage()::close);
            FxTestSupport.flush();

            assertEquals(1, TestViewModel.inactiveCount.get(), "view model should receive onInactive after real close");
            assertEquals(1, TestViewModel.detachedCount.get(), "view model should detach after stage closes");
            assertTrue(!handle.stage().isShowing(), "stage should be closed");
        } finally {
            try {
                FxTestSupport.run(() -> {
                    if (handle.stage().isShowing()) {
                        handle.stage().close();
                    }
                });
                FxTestSupport.flush();
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    @FxViewSpec(viewModel = TestViewModel.class)
    public static class ConsumingView extends Pane {
        private TestViewModel viewModel;

        public void setViewModel(TestViewModel viewModel) {
            this.viewModel = viewModel;
        }

        public TestViewModel getViewModel() {
            return viewModel;
        }

        @PreClose
        private void beforeClose(WindowEvent event) {
            event.consume();
        }
    }

    static class TestViewModel extends FxViewModel {
        static final java.util.concurrent.atomic.AtomicInteger activeCount = new java.util.concurrent.atomic.AtomicInteger();
        static final java.util.concurrent.atomic.AtomicInteger inactiveCount = new java.util.concurrent.atomic.AtomicInteger();
        static final java.util.concurrent.atomic.AtomicInteger detachedCount = new java.util.concurrent.atomic.AtomicInteger();

        static void reset() {
            activeCount.set(0);
            inactiveCount.set(0);
            detachedCount.set(0);
        }

        @Override
        public void onActive() {
            activeCount.incrementAndGet();
        }

        @Override
        public void onInactive() {
            inactiveCount.incrementAndGet();
        }

        @Override
        protected void onDetach() {
            detachedCount.incrementAndGet();
        }
    }

    private static final class FxTestSupport {
        private static volatile boolean toolkitStarted = false;

        private FxTestSupport() {
        }

        static void initToolkit() throws Exception {
            boolean fxTestsEnabled = Boolean.parseBoolean(System.getProperty("enable.fx.tests", "false"));
            Assumptions.assumeTrue(fxTestsEnabled, "FX integration tests disabled (set -Denable.fx.tests=true to enable)");
            if (GraphicsEnvironment.isHeadless()) {
                Assumptions.assumeTrue(false, "JavaFX toolkit unavailable in headless environment");
            }
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
                try {
                    return callable.call();
                } catch (Exception ex) {
                    throw ex;
                }
            }
            CompletableFuture<T> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    future.complete(callable.call());
                } catch (Throwable ex) {
                    future.completeExceptionally(ex);
                }
            });
            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (Exception ex) {
                throw ex;
            }
        }

        static void flush() throws Exception {
            run(() -> { /* no-op */ });
        }
    }
}
