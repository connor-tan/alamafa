package com.alamafa.core.events;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultApplicationEventPublisherTest {

    @Test
    void dispatchesEventsToMatchingListeners() {
        DefaultApplicationEventPublisher publisher = new DefaultApplicationEventPublisher();
        List<String> received = new ArrayList<>();

        publisher.addListener(new ApplicationEventListener<TestEvent>() {
            @Override
            public void onEvent(TestEvent event) {
                received.add(event.message());
            }

            @Override
            public Class<TestEvent> getEventType() {
                return TestEvent.class;
            }
        });

        publisher.publishEvent(new TestEvent("hello"));

        assertEquals(List.of("hello"), received);
    }

    @Test
    void supportsRemovingListeners() {
        DefaultApplicationEventPublisher publisher = new DefaultApplicationEventPublisher();
        List<String> received = new ArrayList<>();
        ApplicationEventListener<TestEvent> listener = new ApplicationEventListener<>() {
            @Override
            public void onEvent(TestEvent event) {
                received.add(event.message());
            }

            @Override
            public Class<TestEvent> getEventType() {
                return TestEvent.class;
            }
        };

        publisher.addListener(listener);
        publisher.removeListener(listener);
        publisher.publishEvent(new TestEvent("ignored"));

        assertEquals(List.of(), received);
    }

    private static final class TestEvent extends ApplicationEvent {
        private final String message;

        private TestEvent(String message) {
            this.message = message;
        }

        String message() {
            return message;
        }
    }
}

