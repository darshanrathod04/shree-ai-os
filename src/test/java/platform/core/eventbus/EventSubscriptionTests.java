package platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.engine.DispatchResult;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;
import com.shreeai.os.platform.core.eventbus.model.EventTopic;
import com.shreeai.os.platform.core.eventbus.service.DefaultEventBusService;
import com.shreeai.os.platform.core.eventbus.validator.EventValidator;
import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;

/**
 * <b>EventSubscriptionTests</b>
 *
 * <p>Verifies the subscription behavior of the {@link DefaultEventBusService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates subscribe() and unsubscribe() operations.</li>
 *   <li>Validates duplicate subscription handling.</li>
 *   <li>Validates registeredTopics() and hasSubscribers().</li>
 *   <li>Validates topic cleanup after last unsubscribe.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultEventBusService
 */
public class EventSubscriptionTests {

    private DefaultEventBusService createService() {
        EventValidator validator = new EventValidator();
        LifecycleService lifecycleService = null;
        LifecycleTransitionEngine lifecycleEngine = null;
        EventDispatchEngine dispatchEngine = new TestDispatchEngine();
        return new DefaultEventBusService(validator, lifecycleService, lifecycleEngine, dispatchEngine);
    }

    /**
     * Verifies subscribe() registers a subscriber.
     */
    public void testSubscribeRegistersSubscriber() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();

        // Act
        service.subscribe(topic, subscriber);

        // Assert
        assert service.hasSubscribers(topic) : "Topic should have subscribers";
    }

    /**
     * Verifies unsubscribe() removes a subscriber.
     */
    public void testUnsubscribeRemovesSubscriber() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();

        service.subscribe(topic, subscriber);
        assert service.hasSubscribers(topic) : "Should have subscribers after subscribe";

        // Act
        service.unsubscribe(topic, subscriber);

        // Assert
        assert !service.hasSubscribers(topic) : "Should not have subscribers after unsubscribe";
    }

    /**
     * Verifies duplicate subscription is handled correctly.
     */
    public void testDuplicateSubscriptionHandled() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();

        // Act - subscribe twice
        service.subscribe(topic, subscriber);
        service.subscribe(topic, subscriber);

        // Assert - should still have only one subscriber
        assert service.hasSubscribers(topic) : "Should have subscribers";
    }

    /**
     * Verifies registeredTopics() returns all registered topics.
     */
    public void testRegisteredTopicsReturnsAllTopics() {
        // Arrange
        var service = createService();
        EventTopic topic1 = new EventTopic("topic-1");
        EventTopic topic2 = new EventTopic("topic-2");
        EventSubscriber subscriber = new TestSubscriber();

        // Act
        service.subscribe(topic1, subscriber);
        service.subscribe(topic2, subscriber);

        // Assert
        var topics = service.registeredTopics();
        assert topics.contains(topic1) : "Should contain topic-1";
        assert topics.contains(topic2) : "Should contain topic-2";
    }

    /**
     * Verifies hasSubscribers() returns correct values.
     */
    public void testHasSubscribersReturnsCorrectValues() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();

        // Act & Assert - initially no subscribers
        assert !service.hasSubscribers(topic) : "Should not have subscribers initially";

        // Subscribe and check
        service.subscribe(topic, subscriber);
        assert service.hasSubscribers(topic) : "Should have subscribers after subscribe";

        // Unsubscribe and check
        service.unsubscribe(topic, subscriber);
        assert !service.hasSubscribers(topic) : "Should not have subscribers after unsubscribe";
    }

    /**
     * Verifies topic cleanup after last unsubscribe.
     */
    public void testTopicCleanupAfterLastUnsubscribe() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();

        service.subscribe(topic, subscriber1);
        service.subscribe(topic, subscriber2);

        // Act - unsubscribe both
        service.unsubscribe(topic, subscriber1);
        service.unsubscribe(topic, subscriber2);

        // Assert - topic should be removed from registry
        assert !service.hasSubscribers(topic) : "Should not have subscribers";
        assert !service.registeredTopics().contains(topic) : "Topic should be removed from registry";
    }

    /**
     * Verifies unsubscribe of non-subscribed subscriber is handled gracefully.
     */
    public void testUnsubscribeNonSubscribedSubscriber() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();

        // Act - unsubscribe without subscribing (should not throw)
        service.unsubscribe(topic, subscriber);

        // Assert
        assert !service.hasSubscribers(topic) : "Should not have subscribers";
    }

    /**
     * Verifies null topic throws IllegalArgumentException.
     */
    public void testSubscribeNullTopicThrowsException() {
        // Arrange
        var service = createService();
        EventSubscriber subscriber = new TestSubscriber();

        // Act & Assert
        try {
            service.subscribe(null, subscriber);
            throw new AssertionError("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * Verifies null subscriber throws IllegalArgumentException.
     */
    public void testSubscribeNullSubscriberThrowsException() {
        // Arrange
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");

        // Act & Assert
        try {
            service.subscribe(topic, null);
            throw new AssertionError("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    // Helper classes
    private static class TestSubscriber implements EventSubscriber {
        private Event receivedEvent;

        @Override
        public void onEvent(Event event) {
            this.receivedEvent = event;
        }
    }

    private static class TestDispatchEngine implements EventDispatchEngine {
        @Override
        public DispatchResult dispatch(Event event, java.util.Collection<EventSubscriber> subscribers) {
            return new DispatchResult(
                    true,
                    event,
                    subscribers.size(),
                    subscribers.size(),
                    0,
                    new java.util.ArrayList<>()
            );
        }
    }
}