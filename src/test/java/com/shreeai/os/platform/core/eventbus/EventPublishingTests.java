package com.shreeai.os.platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.engine.DispatchResult;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.error.EventErrorCode;
import com.shreeai.os.platform.core.eventbus.error.InvalidEventException;
import com.shreeai.os.platform.core.eventbus.error.NoSubscribersException;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventId;
import com.shreeai.os.platform.core.eventbus.model.EventMetadata;
import com.shreeai.os.platform.core.eventbus.model.EventPriority;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;
import com.shreeai.os.platform.core.eventbus.model.EventTopic;
import com.shreeai.os.platform.core.eventbus.service.DefaultEventBusService;
import com.shreeai.os.platform.core.eventbus.validator.EventValidator;
import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;

/**
 * <b>EventPublishingTests</b>
 *
 * <p>Verifies the publishing behavior of the {@link DefaultEventBusService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates successful publishing to subscribers.</li>
 *   <li>Validates publishing without subscribers throws NoSubscribersException.</li>
 *   <li>Validates multiple subscribers receive events.</li>
 *   <li>Validates empty subscriber list handling.</li>
 *   <li>Validates publish after unsubscribe.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultEventBusService
 */
public class EventPublishingTests {

    private DefaultEventBusService createService() {
        EventValidator validator = new EventValidator();
        LifecycleService lifecycleService = null; // Not needed for basic tests
        LifecycleTransitionEngine lifecycleEngine = null; // Not needed for basic tests
        EventDispatchEngine dispatchEngine = new TestDispatchEngine();
        return new DefaultEventBusService(validator, lifecycleService, lifecycleEngine, dispatchEngine);
    }

    private Event createValidEvent(EventTopic topic) {
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("test-publisher", EventPriority.NORMAL, "corr-123");
        return new Event(id, topic, metadata, "test-payload");
    }

    /**
     * Verifies successful publishing to a single subscriber.
     */
    public void testPublishToSingleSubscriber() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        service.subscribe(topic, subscriber);

        // Act
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber).receivedEvent != null : "Subscriber should receive event";
        assert ((TestSubscriber) subscriber).receivedEvent.equals(event) : "Received event should match";
    }

    /**
     * Verifies publishing without subscribers throws NoSubscribersException.
     */
    public void testPublishWithoutSubscribersThrowsException() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);

        // Act & Assert
        try {
            service.publish(event);
            throw new AssertionError("Should have thrown NoSubscribersException");
        } catch (NoSubscribersException e) {
            assert e.code() == EventErrorCode.EVENT_NO_SUBSCRIBERS : "Should be EVENT_NO_SUBSCRIBERS";
        }
    }

    /**
     * Verifies multiple subscribers receive the same event.
     */
    public void testPublishToMultipleSubscribers() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        EventSubscriber subscriber3 = new TestSubscriber();
        Event event = createValidEvent(topic);

        service.subscribe(topic, subscriber1);
        service.subscribe(topic, subscriber2);
        service.subscribe(topic, subscriber3);

        // Act
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber1).receivedEvent != null : "Subscriber 1 should receive event";
        assert ((TestSubscriber) subscriber2).receivedEvent != null : "Subscriber 2 should receive event";
        assert ((TestSubscriber) subscriber3).receivedEvent != null : "Subscriber 3 should receive event";
    }

    /**
     * Verifies publishing with empty subscriber list throws NoSubscribersException.
     */
    public void testPublishWithEmptySubscriberListThrowsException() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);

        // Subscribe and unsubscribe to create empty state
        EventSubscriber subscriber = new TestSubscriber();
        service.subscribe(topic, subscriber);
        service.unsubscribe(topic, subscriber);

        // Act & Assert
        try {
            service.publish(event);
            throw new AssertionError("Should have thrown NoSubscribersException");
        } catch (NoSubscribersException e) {
            assert e.code() == EventErrorCode.EVENT_NO_SUBSCRIBERS : "Should be EVENT_NO_SUBSCRIBERS";
        }
    }

    /**
     * Verifies publish after unsubscribe works correctly.
     */
    public void testPublishAfterUnsubscribe() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        Event event = createValidEvent(topic);

        service.subscribe(topic, subscriber1);
        service.subscribe(topic, subscriber2);

        // Unsubscribe subscriber1
        service.unsubscribe(topic, subscriber1);

        // Act - should only deliver to subscriber2
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber1).receivedEvent == null : "Unsubscribed subscriber should not receive event";
        assert ((TestSubscriber) subscriber2).receivedEvent != null : "Subscribed subscriber should receive event";
    }

    /**
     * Verifies that invalid events throw InvalidEventException during publish.
     */
    public void testPublishInvalidEventThrowsException() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        service.subscribe(topic, subscriber);

        // Create invalid event (null topic)
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("test-publisher", EventPriority.NORMAL, "corr-123");
        Event invalidEvent = new Event(id, null, metadata, "payload");

        // Act & Assert
        try {
            service.publish(invalidEvent);
            throw new AssertionError("Should have thrown InvalidEventException");
        } catch (InvalidEventException e) {
            assert e.code() == EventErrorCode.EVENT_INVALID : "Should be EVENT_INVALID";
        }
    }

    // Helper classes for testing
    private static class TestSubscriber implements EventSubscriber {
        private Event receivedEvent;

        @Override
        public void onEvent(Event event) {
            this.receivedEvent = event;
        }
    }

    private static class TestDispatchEngine implements EventDispatchEngine {
        private Event lastDispatchedEvent;
        private java.util.Collection<EventSubscriber> lastSubscribers;

        @Override
        public DispatchResult dispatch(Event event, java.util.Collection<EventSubscriber> subscribers) {
            this.lastDispatchedEvent = event;
            this.lastSubscribers = subscribers;

            // Simulate dispatch to all subscribers
            int succeeded = 0;
            int failed = 0;
            for (EventSubscriber subscriber : subscribers) {
                try {
                    subscriber.onEvent(event);
                    succeeded++;
                } catch (Exception e) {
                    failed++;
                }
            }

            return new DispatchResult(
                    failed == 0,
                    event,
                    subscribers.size(),
                    succeeded,
                    failed,
                    new java.util.ArrayList<>()
            );
        }
    }
}