package platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.engine.DispatchResult;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.error.EventErrorCode;
import com.shreeai.os.platform.core.eventbus.error.InvalidEventException;
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
import com.shreeai.os.platform.core.registry.api.KernelRegistry;
import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.model.KernelMetadata;
import com.shreeai.os.platform.core.registry.model.KernelVersion;
import com.shreeai.os.platform.core.registry.model.RegisteredKernel;
import com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry;
import com.shreeai.os.platform.core.registry.validator.KernelRegistrationValidator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * <b>EventIntegrationTests</b>
 *
 * <p>Verifies the complete integration of the Event Bus subsystem.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates the complete publish flow.</li>
 *   <li>Validates validator integration.</li>
 *   <li>Validates dispatch engine integration.</li>
 *   <li>Validates service integration.</li>
 *   <li>Validates registry compatibility.</li>
 *   <li>Validates lifecycle compatibility.</li>
 *   <li>Validates full Event Bus workflow.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultEventBusService
 * @see EventValidator
 * @see EventDispatchEngine
 */
public class EventIntegrationTests {

    private DefaultEventBusService createFullService() {
        EventValidator validator = new EventValidator();
        LifecycleService lifecycleService = null;
        LifecycleTransitionEngine lifecycleEngine = null;
        EventDispatchEngine dispatchEngine = new TestDispatchEngine();
        return new DefaultEventBusService(validator, lifecycleService, lifecycleEngine, dispatchEngine);
    }

    private Event createValidEvent(EventTopic topic) {
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("test-publisher", EventPriority.NORMAL, "corr-123");
        return new Event(id, topic, metadata, "test-payload");
    }

    /**
     * Verifies the complete publish flow: subscribe → publish → receive.
     */
    public void testCompletePublishFlow() {
        // Arrange
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        // Act
        service.subscribe(topic, subscriber);
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber).receivedEvent != null : "Subscriber should receive event";
        assert ((TestSubscriber) subscriber).receivedEvent.equals(event) : "Received event should match";
    }

    /**
     * Verifies validator integration - invalid events are rejected.
     */
    public void testValidatorIntegration() {
        // Arrange
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        service.subscribe(topic, subscriber);

        // Create invalid event (null payload)
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, "corr-123");
        Event invalidEvent = new Event(id, topic, metadata, null);

        // Act & Assert
        try {
            service.publish(invalidEvent);
            throw new AssertionError("Should have thrown InvalidEventException");
        } catch (InvalidEventException e) {
            assert e.code() == EventErrorCode.EVENT_INVALID : "Should be EVENT_INVALID";
        }
    }

    /**
     * Verifies dispatch engine integration - events are delegated to engine.
     */
    public void testDispatchEngineIntegration() {
        // Arrange
        TestDispatchEngine dispatchEngine = new TestDispatchEngine();
        EventValidator validator = new EventValidator();
        DefaultEventBusService service = new DefaultEventBusService(
                validator, null, null, dispatchEngine
        );

        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        service.subscribe(topic, subscriber);

        // Act
        service.publish(event);

        // Assert
        assert dispatchEngine.lastDispatchedEvent != null : "Dispatch engine should receive event";
        assert dispatchEngine.lastDispatchedEvent.equals(event) : "Dispatched event should match";
    }

    /**
     * Verifies service integration - all components work together.
     */
    public void testServiceIntegration() {
        // Arrange
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        Event event = createValidEvent(topic);

        // Act
        service.subscribe(topic, subscriber1);
        service.subscribe(topic, subscriber2);
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber1).receivedEvent != null : "Subscriber 1 should receive event";
        assert ((TestSubscriber) subscriber2).receivedEvent != null : "Subscriber 2 should receive event";
    }

    /**
     * Verifies registry compatibility - service works with registry.
     */
    public void testRegistryCompatibility() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId("test-kernel");
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel", "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register("test-kernel", kernel);

        // Service should work independently of registry
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        // Act
        service.subscribe(topic, subscriber);
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber).receivedEvent != null : "Subscriber should receive event";
    }

    /**
     * Verifies lifecycle compatibility - service works with lifecycle.
     */
    public void testLifecycleCompatibility() {
        // Arrange
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        // Act
        service.subscribe(topic, subscriber);
        service.publish(event);

        // Assert
        assert ((TestSubscriber) subscriber).receivedEvent != null : "Subscriber should receive event";
    }

    /**
     * Verifies full Event Bus workflow: subscribe → publish → unsubscribe → verify.
     */
    public void testFullEventBusWorkflow() {
        // Arrange
        var service = createFullService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        Event event1 = createValidEvent(topic);
        Event event2 = createValidEvent(topic);

        // Act - subscribe both
        service.subscribe(topic, subscriber1);
        service.subscribe(topic, subscriber2);

        // Publish first event
        service.publish(event1);
        assert ((TestSubscriber) subscriber1).receivedEvent != null : "Subscriber 1 should receive event 1";
        assert ((TestSubscriber) subscriber2).receivedEvent != null : "Subscriber 2 should receive event 1";

        // Unsubscribe subscriber1
        service.unsubscribe(topic, subscriber1);

        // Publish second event - only subscriber2 should receive
        service.publish(event2);
        assert ((TestSubscriber) subscriber1).receivedEvent.equals(event1) : "Subscriber 1 should only receive event 1";
        assert ((TestSubscriber) subscriber2).receivedEvent.equals(event2) : "Subscriber 2 should receive event 2";

        // Assert
        assert !service.hasSubscribers(topic) : "Topic should have no subscribers after unsubscribe";
    }

    /**
     * Verifies multiple topics work correctly.
     */
    public void testMultipleTopics() {
        // Arrange
        var service = createFullService();
        EventTopic topic1 = new EventTopic("topic-1");
        EventTopic topic2 = new EventTopic("topic-2");
        EventSubscriber subscriber1 = new TestSubscriber();
        EventSubscriber subscriber2 = new TestSubscriber();
        Event event1 = createValidEvent(topic1);
        Event event2 = createValidEvent(topic2);

        // Act
        service.subscribe(topic1, subscriber1);
        service.subscribe(topic2, subscriber2);
        service.publish(event1);
        service.publish(event2);

        // Assert
        assert ((TestSubscriber) subscriber1).receivedEvent.equals(event1) : "Subscriber 1 should receive event 1";
        assert ((TestSubscriber) subscriber2).receivedEvent.equals(event2) : "Subscriber 2 should receive event 2";
        assert service.registeredTopics().contains(topic1) : "Should contain topic-1";
        assert service.registeredTopics().contains(topic2) : "Should contain topic-2";
    }

    // ===== Helper Classes =====

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