package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.events.ApplicationEventListener;
import com.alamafa.core.events.ApplicationEventPublisher;
import com.alamafa.core.events.ApplicationStartedEvent;
import com.alamafa.core.events.DefaultApplicationEventPublisher;
import com.alamafa.di.annotation.Bean;
import com.alamafa.di.annotation.ConditionalOnMissingBean;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationEventListenerIntegrationTest {
    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @Test
    void registersAndRemovesEventListeners() throws Exception {
        ApplicationContext context = new ApplicationContext();
        DefaultApplicationEventPublisher publisher = new DefaultApplicationEventPublisher();
        context.put(ApplicationEventPublisher.class, publisher);

        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .withConfigurations(EventListenerConfig.class)
                .build();

        bootstrap.init(context);

        publisher.publishEvent(new ApplicationStartedEvent(context));
        assertEquals(List.of("started"), EVENTS);

        bootstrap.stop(context);
        EVENTS.clear();
        publisher.publishEvent(new ApplicationStartedEvent(context));
        assertEquals(List.of(), EVENTS);
    }

    @com.alamafa.di.annotation.Configuration
    static class EventListenerConfig {
        @Bean
        @ConditionalOnMissingBean(EventRecordingListener.class)
        ApplicationEventListener<ApplicationStartedEvent> listener() {
            return new EventRecordingListener();
        }
    }

    static final class EventRecordingListener implements ApplicationEventListener<ApplicationStartedEvent> {
        @Override
        public void onEvent(ApplicationStartedEvent event) {
            EVENTS.add("started");
        }

        @Override
        public Class<ApplicationStartedEvent> getEventType() {
            return ApplicationStartedEvent.class;
        }
    }
}

