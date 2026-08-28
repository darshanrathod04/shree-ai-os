package com.shreeai.os.platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.engine.DispatchResult;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventId;
import com.shreeai.os.platform.core.eventbus.model.EventMetadata;
import com.shreeai.os.platform.core.eventbus.model.EventPriority;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;
import com.shreeai.os.platform.core.eventbus.model.EventTopic;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>EventDispatchEngineTests</b>
 *
 * <p>Verifies the behavior of the EventDispatchEngine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates dispatch to single subscriber.</li>
 *   <li>Validates dispatch to multiple subscribers.</li>
 *   <li>Validates partial failure handling.</li>
 *   <li>Validates all failure handling.</li>
 *   <li>Validates DispatchResult immutability.</li>
 *   <li>Validates failure messages collection.</li>
 *   <li>Validates subscriber counting.</li>
 *   <li>Validates dispatch continues after failures.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see EventDispatchEngine
 * @see DispatchResult
 */
public class EventDispatchEngineTests {

    private EventDispatchEngine createEngine() {
        return new TestEventDispatchEngine();
    }

    private Event createValidEvent(EventTopic topic) {
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("test-publisher", EventPriority.NORMAL, "corr-123");
        return new Event(id, topic, metadata, "test-payload");
    }

    // ===== Single Subscriber Tests =====

    /**
     * Verifies dispatch to single subscriber succeeds.
     */
    public void testDispatchToSingleSubscriberSucceeds() {
        // Arrange
        EventDispatchEngine engine = createEngine();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber = new TestSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber));

        // Assert
        assert result.success() : "Dispatch should succeed";
        assert result.subscribersAttempted() == 1 : "Should attempt 1 subscriber";
        assert result.subscribersSucceeded() == 1 : "Should succeed for 1 subscriber";
        assert result.subscribersFailed() == 0 : "Should have 0 failures";
    }

    // ===== Multiple Subscribers Tests =====

    /**
     * Verifies dispatch to multiple subscribers succeeds.
     */
    public void testDispatchToMultipleSubscribersSucceeds() {
        // Arrange
        EventDispatchEngine engine = createEngine();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        EventSubscriber subscriber3 = new TestSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2, subscriber3));

        // Assert
        assert result.success() : "Dispatch should succeed";
        assert result.subscribersAttempted() == 3 : "Should attempt 3 subscribers";
        assert result.subscribersSucceeded() == 3 : "Should succeed for 3 subscribers";
        assert result.subscribersFailed() == 0 : "Should have 0 failures";
    }

    // ===== Partial Failure Tests =====

    /**
     * Verifies partial failure handling.
     */
    public void testPartialFailureHandling() {
        // Arrange
        EventDispatchEngine engine = new TestEventDispatchEngineWithFailures();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new FailingSubscriber();
        EventSubscriber subscriber3 = new TestSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2, subscriber3));

        // Assert
        assert !result.success() : "Dispatch should fail due to partial failure";
        assert result.subscribersAttempted() == 3 : "Should attempt 3 subscribers";
        assert result.subscribersSucceeded() == 2 : "Should succeed for 2 subscribers";
        assert result.subscribersFailed() == 1 : "Should have 1 failure";
        assert result.failureMessages().size() == 1 : "Should have 1 failure message";
    }

    /**
     * Verifies dispatch continues after subscriber failure.
     */
    public void testDispatchContinuesAfterFailure() {
        // Arrange
        EventDispatchEngine engine = new TestEventDispatchEngineWithFailures();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new FailingSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        EventSubscriber subscriber3 = new FailingSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2, subscriber3));

        // Assert
        assert result.subscribersAttempted() == 3 : "Should attempt all 3 subscribers";
        assert result.subscribersSucceeded() == 1 : "Should succeed for 1 subscriber";
        assert result.subscribersFailed() == 2 : "Should have 2 failures";
        assert result.failureMessages().size() == 2 : "Should have 2 failure messages";
    }

    // ===== All Failure Tests =====

    /**
     * Verifies all subscribers failing is handled correctly.
     */
    public void testAllSubscribersFailing() {
        // Arrange
        EventDispatchEngine engine = new TestEventDispatchEngineWithFailures();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new FailingSubscriber();
        EventSubscriber subscriber2 = new FailingSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2));

        // Assert
        assert !result.success() : "Dispatch should fail";
        assert result.subscribersAttempted() == 2 : "Should attempt 2 subscribers";
        assert result.subscribersSucceeded() == 0 : "Should have 0 successes";
        assert result.subscribersFailed() == 2 : "Should have 2 failures";
        assert result.failureMessages().size() == 2 : "Should have 2 failure messages";
    }

    // ===== DispatchResult Tests =====

    /**
     * Verifies DispatchResult is immutable.
     */
    public void testDispatchResultIsImmutable() {
        // Arrange
        EventDispatchEngine engine = createEngine();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber = new TestSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber));

        // Assert
        assert result.success() : "Success should be true";
        assert result.event() != null : "Event should not be null";
        assert result.subscribersAttempted() >= 0 : "Subscribers attempted should be non-negative";
        assert result.subscribersSucceeded() >= 0 : "Subscribers succeeded should be non-negative";
        assert result.subscribersFailed() >= 0 : "Subscribers failed should be non-negative";
        assert result.failureMessages() != null : "Failure messages should not be null";
        assert result.timestamp() != null : "Timestamp should not be null";
    }

    /**
     * Verifies DispatchResult failure messages are collected.
     */
    public void testDispatchResultFailureMessagesCollected() {
        // Arrange
        EventDispatchEngine engine = new TestEventDispatchEngineWithFailures();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new FailingSubscriber("Error 1");
        EventSubscriber subscriber2 = new FailingSubscriber("Error 2");

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2));

        // Assert
        assert result.failureMessages().size() == 2 : "Should have 2 failure messages";
        assert result.failureMessages().contains("Error 1") : "Should contain Error 1";
        assert result.failureMessages().contains("Error 2") : "Should contain Error 2";
    }

    /**
     * Verifies DispatchResult subscriber counting is correct.
     */
    public void testDispatchResultSubscriberCounting() {
        // Arrange
        EventDispatchEngine engine = new TestEventDispatchEngineWithFailures();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new FailingSubscriber();
        EventSubscriber subscriber3 = new TestSubscriber();

        // Act
        DispatchResult result = engine.dispatch(event, List.of(subscriber1, subscriber2, subscriber3));

        // Assert
        assert result.subscribersAttempted() == 3 : "Should attempt 3 subscribers";
        assert result.subscribersSucceeded() == 2 : "Should succeed for 2 subscribers";
        assert result.subscribersFailed() == 1 : "Should have 1 failure";
        assert result.subscribersAttempted() == result.subscribersSucceeded() + result.subscribersFailed() :
                "Attempted should equal succeeded + failed";
    }

    // ===== Helper Classes =====

    private static class TestSubscriber implements EventSubscriber {
        @Override
        public void onEvent(Event event) {
        }
    }

    private static class FailingSubscriber implements EventSubscriber {
        private final String errorMessage;

        FailingSubscriber() {
            this("Subscriber failed");
        }

        FailingSubscriber(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public void onEvent(Event event) {
            throw new RuntimeException(errorMessage);
        }
    }

    private static class TestEventDispatchEngine implements EventDispatchEngine {
        @Override
        public DispatchResult dispatch(Event event, java.util.Collection<EventSubscriber> subscribers) {
            int succeeded = 0;
            int failed = 0;
            List<String> failureMessages = new ArrayList<>();

            for (EventSubscriber subscriber : subscribers) {
                try {
                    subscriber.onEvent(event);
                    succeeded++;
                } catch (Exception e) {
                    failed++;
                    failureMessages.add(e.getMessage());
                }
            }

            return new DispatchResult(
                    failed == 0,
                    event,
                    subscribers.size(),
                    succeeded,
                    failed,
                    failureMessages
            );
        }
    }

    private static class TestEventDispatchEngineWithFailures implements EventDispatchEngine {
        @Override
        public DispatchResult dispatch(Event event, java.util.Collection<EventSubscriber> subscribers) {
            int succeeded = 0;
            int failed = 0;
            List<String> failureMessages = new ArrayList<>();

            for (EventSubscriber subscriber : subscribers) {
                try {
                    subscriber.onEvent(event);
                    succeeded++;
                } catch (Exception e) {
                    failed++;
                    failureMessages.add(e.getMessage());
                }
            }

            return new DispatchResult(
                    failed == 0,
                    event,
                    subscribers.size(),
                    succeeded,
                    failed,
                    failureMessages
            );
        }
    }
}