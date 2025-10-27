package com.alamafa.helloworld;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.ApplicationShutdown;
import com.alamafa.core.ContextAwareApplicationLauncher;
import com.alamafa.core.Lifecycle;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 最简单的控制台启动器：按顺序执行 init/start，并在接收到关闭信号后调用 stop。
 * 通过 {@link ApplicationShutdown} Bean 让业务代码可以优雅通知退出。
 */
public final class ConsoleApplicationLauncher implements ContextAwareApplicationLauncher {
    private final ApplicationContext context = new ApplicationContext();
    private final AlamafaLogger logger = LoggerFactory.getLogger(ConsoleApplicationLauncher.class);

    @Override
    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void launch(Lifecycle lifecycle) {
        Objects.requireNonNull(lifecycle, "lifecycle");
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        AtomicBoolean stopRequested = new AtomicBoolean(false);
        context.put(ApplicationShutdown.class, () -> {
            if (stopRequested.compareAndSet(false, true)) {
                shutdownLatch.countDown();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (stopRequested.compareAndSet(false, true)) {
                try {
                    lifecycle.stop(context);
                } catch (Exception ex) {
                    logger.error("Failed to stop application during JVM shutdown", ex);
                }
            }
        }, "alamafa-shutdown-hook"));

        try {
            lifecycle.init(context);
            lifecycle.start(context);
            shutdownLatch.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to launch application", ex);
        } finally {
            if (stopRequested.compareAndSet(false, true)) {
                try {
                    lifecycle.stop(context);
                } catch (Exception ex) {
                    logger.error("Failed to stop application", ex);
                }
            }
        }
    }
}
