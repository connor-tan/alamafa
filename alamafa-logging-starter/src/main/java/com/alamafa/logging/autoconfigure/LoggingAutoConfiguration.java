package com.alamafa.logging.autoconfigure;

import com.alamafa.bootstrap.autoconfigure.AutoConfiguration;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.events.ApplicationEventListener;
import com.alamafa.core.events.ApplicationStartedEvent;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.ConditionalOnMissingBean;
import com.alamafa.di.annotation.ConditionalOnProperty;

import java.lang.reflect.Method;

/**
 * Provides sensible logging defaults and optional JUL bridging.
 */
@AutoConfiguration
public class LoggingAutoConfiguration {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(LoggingAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "slf4jJulBridgeInitializer")
    @ConditionalOnProperty(prefix = "logging", name = "jul-bridge", havingValue = "true", matchIfMissing = false)
    public ApplicationEventListener<ApplicationStartedEvent> slf4jJulBridgeInitializer() {
        return new ApplicationEventListener<>() {
            @Override
            public void onEvent(ApplicationStartedEvent event) {
                if (!isJulBridgeInstalled()) {
                    installJulBridge();
                }
            }

            @Override
            public Class<ApplicationStartedEvent> getEventType() {
                return ApplicationStartedEvent.class;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "loggingContextInitializer")
    public ApplicationEventListener<ApplicationStartedEvent> loggingContextInitializer(ApplicationContext context) {
        return new ApplicationEventListener<>() {
            @Override
            public void onEvent(ApplicationStartedEvent event) {
                LOGGER.debug("Logging system initialized with context keys {}", context.snapshot().keySet());
            }

            @Override
            public Class<ApplicationStartedEvent> getEventType() {
                return ApplicationStartedEvent.class;
            }
        };
    }

    private static boolean isJulBridgeInstalled() {
        try {
            Class<?> handlerClass = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            Method isInstalled = handlerClass.getMethod("isInstalled");
            return (boolean) isInstalled.invoke(null);
        } catch (ClassNotFoundException ignored) {
            LOGGER.warn("SLF4JBridgeHandler not found on classpath; skipping JUL bridge auto-configuration");
            return true;
        } catch (Exception ex) {
            LOGGER.warn("Failed to inspect SLF4JBridgeHandler", ex);
            return true;
        }
    }

    private static void installJulBridge() {
        try {
            Class<?> handlerClass = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            handlerClass.getMethod("removeHandlersForRootLogger").invoke(null);
            handlerClass.getMethod("install").invoke(null);
            LOGGER.info("Installed SLF4JBridgeHandler for java.util.logging");
        } catch (ClassNotFoundException ignored) {
            LOGGER.warn("SLF4JBridgeHandler not found on classpath; skipping JUL bridge installation");
        } catch (Exception ex) {
            LOGGER.warn("Failed to install SLF4JBridgeHandler", ex);
        }
    }
}
