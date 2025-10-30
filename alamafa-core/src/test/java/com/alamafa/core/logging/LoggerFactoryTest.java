package com.alamafa.core.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LoggerFactoryTest {

    @AfterEach
    void resetProvider() {
        LoggerFactory.resetProvider();
    }

    @Test
    void defaultProviderUsesLogbackLogger() {
        AlamafaLogger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        assertTrue(logger instanceof Slf4jLoggerAdapter, "expected default SLF4J adapter");
        Slf4jLoggerAdapter adapter = (Slf4jLoggerAdapter) logger;
        assertEquals("ch.qos.logback.classic.Logger",
                adapter.delegate().getClass().getName(),
                "logback classic should be bound as default backend");
    }

    @Test
    void customProviderCanBeInstalledAndRestored() {
        AtomicBoolean invoked = new AtomicBoolean();
        TestLogger customLogger = new TestLogger(invoked);
        LoggerFactory.setProvider(name -> customLogger);

        AlamafaLogger logger = LoggerFactory.getLogger("custom");
        assertSame(customLogger, logger, "custom provider should be used");

        logger.info("hello");
        assertTrue(invoked.get(), "custom logger should receive calls");

        LoggerFactory.resetProvider();
        AlamafaLogger restored = LoggerFactory.getLogger("restored");
        assertTrue(restored instanceof Slf4jLoggerAdapter, "default provider should be restored");
    }

    private static final class TestLogger implements AlamafaLogger {
        private final AtomicBoolean invoked;

        private TestLogger(AtomicBoolean invoked) {
            this.invoked = invoked;
        }

        private void touch() {
            invoked.set(true);
        }

        @Override
        public void trace(String message, Object... args) { touch(); }

        @Override
        public void debug(String message, Object... args) { touch(); }

        @Override
        public void info(String message, Object... args) { touch(); }

        @Override
        public void warn(String message, Object... args) { touch(); }

        @Override
        public void warn(String message, Throwable throwable) { touch(); }

        @Override
        public void error(String message, Object... args) { touch(); }

        @Override
        public void error(String message, Throwable throwable) { touch(); }

        @Override
        public boolean isTraceEnabled() { return true; }

        @Override
        public boolean isDebugEnabled() { return true; }

        @Override
        public boolean isInfoEnabled() { return true; }

        @Override
        public boolean isWarnEnabled() { return true; }

        @Override
        public boolean isErrorEnabled() { return true; }
    }
}

