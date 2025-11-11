package com.alamafa.jfx.vlcj.external;

import com.alamafa.jfx.vlcj.core.MediaEventDispatcher;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import com.alamafa.jfx.vlcj.ipc.MediaCommand;
import com.alamafa.jfx.vlcj.ipc.MediaCommandChannel;
import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventChannel;
import com.alamafa.jfx.vlcj.ipc.MediaEventListener;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProcessEndpointTest {

    @Test
    void shouldCloseResourcesWhenProcessExits() {
        TestProcess process = new TestProcess();
        ExternalPlayerHandle handle = new ExternalPlayerHandle(UUID.randomUUID(), process);
        TestCommandChannel commandChannel = new TestCommandChannel();
        TestEventChannel eventChannel = new TestEventChannel();
        TestProcessRegistry registry = new TestProcessRegistry();
        registry.register(handle);
        PlayerProperties properties = new PlayerProperties();
        properties.setHeartbeatTimeoutSeconds(1);
        TestEventDispatcher dispatcher = new TestEventDispatcher();

        ExternalProcessEndpoint endpoint = new ExternalProcessEndpoint(
                handle,
                commandChannel,
                eventChannel,
                dispatcher,
                properties,
                registry);

        process.completeExit();

        assertTrue(commandChannel.closed.get());
        assertTrue(eventChannel.closed.get());
        assertEquals(1, registry.unregisterCount.get());
        assertFalse(process.isAlive());
        assertTrue(endpointSchedulerShutdown(endpoint));
    }

    @Test
    void closeShouldBeIdempotent() {
        TestProcess process = new TestProcess();
        ExternalPlayerHandle handle = new ExternalPlayerHandle(UUID.randomUUID(), process);
        TestCommandChannel commandChannel = new TestCommandChannel();
        TestEventChannel eventChannel = new TestEventChannel();
        TestProcessRegistry registry = new TestProcessRegistry();
        registry.register(handle);
        PlayerProperties properties = new PlayerProperties();
        properties.setHeartbeatTimeoutSeconds(1);

        ExternalProcessEndpoint endpoint = new ExternalProcessEndpoint(
                handle,
                commandChannel,
                eventChannel,
                new TestEventDispatcher(),
                properties,
                registry);

        endpoint.close();
        endpoint.close();

        assertEquals(1, commandChannel.closeCount.get());
        assertEquals(1, eventChannel.closeCount.get());
        assertEquals(1, registry.unregisterCount.get());
    }

    private boolean endpointSchedulerShutdown(ExternalProcessEndpoint endpoint) {
        try {
            var field = ExternalProcessEndpoint.class.getDeclaredField("scheduler");
            field.setAccessible(true);
            java.util.concurrent.ScheduledExecutorService executor = (java.util.concurrent.ScheduledExecutorService) field.get(endpoint);
            return executor.isShutdown();
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to inspect scheduler", ex);
        }
    }

    private static final class TestCommandChannel implements MediaCommandChannel {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final AtomicInteger closeCount = new AtomicInteger();

        @Override
        public void send(MediaCommand command) { }

        @Override
        public void close() {
            closed.set(true);
            closeCount.incrementAndGet();
        }
    }

    private static final class TestEventChannel implements MediaEventChannel {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final AtomicInteger closeCount = new AtomicInteger();

        @Override
        public void start(MediaEventListener listener) { }

        @Override
        public void close() {
            closed.set(true);
            closeCount.incrementAndGet();
        }
    }

    private static final class TestProcessRegistry extends ExternalProcessRegistry {
        private final AtomicInteger unregisterCount = new AtomicInteger();

        @Override
        public void unregister(ExternalPlayerHandle handle) {
            super.unregister(handle);
            unregisterCount.incrementAndGet();
        }
    }

    private static final class TestEventDispatcher implements MediaEventDispatcher {
        @Override
        public void addListener(MediaEventListener listener) { }

        @Override
        public void removeListener(MediaEventListener listener) { }

        @Override
        public void dispatch(MediaEvent event) { }
    }

    private static final class TestProcess extends Process {
        private final CompletableFuture<Process> exitFuture = new CompletableFuture<>();
        private volatile boolean alive = true;

        void completeExit() {
            alive = false;
            exitFuture.complete(this);
        }

        @Override
        public OutputStream getOutputStream() { return OutputStream.nullOutputStream(); }

        @Override
        public InputStream getInputStream() { return InputStream.nullInputStream(); }

        @Override
        public InputStream getErrorStream() { return InputStream.nullInputStream(); }

        @Override
        public int waitFor() { return 0; }

        @Override
        public int exitValue() { return 0; }

        @Override
        public void destroy() { alive = false; }

        @Override
        public boolean isAlive() { return alive; }

        @Override
        public Process destroyForcibly() { alive = false; return this; }

        @Override
        public long pid() { return 42L; }

        @Override
        public CompletableFuture<Process> onExit() { return exitFuture; }
    }
}
