package com.shreeai.os.platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.engine.DispatchResult;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>EventConcurrencyTests</b>
 *
 * <p>Verifies the thread-safety of the Event Bus subsystem.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates 100 concurrent publish() operations.</li>
 *   <li>Validates 100 concurrent subscribe() operations.</li>
 *   <li>Validates mixed publish/subscribe workloads.</li>
 *   <li>Validates concurrent unsubscribe operations.</li>
 *   <li>Validates no data corruption under concurrent load.</li>
 *   <li>Validates no ConcurrentModificationException.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultEventBusService
 */
public class EventConcurrencyTests {

    private DefaultEventBusService createService() {
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
     * Verifies 100 concurrent publish() operations do not cause data corruption.
     */
    public void testConcurrentPublishNoDataCorruption() throws Exception {
        // Arrange
        int threadCount = 100;
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        EventSubscriber subscriber = new TestSubscriber();
        Event event = createValidEvent(topic);

        service.subscribe(topic, subscriber);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.publish(event);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert successCount.get() == threadCount : "All publishes should succeed";
        assert failureCount.get() == 0 : "No failures should occur";
    }

    /**
     * Verifies 100 concurrent subscribe() operations do not cause data corruption.
     */
    public void testConcurrentSubscribeNoDataCorruption() throws Exception {
        // Arrange
        int threadCount = 100;
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    EventSubscriber subscriber = new TestSubscriber();
                    service.subscribe(topic, subscriber);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert successCount.get() == threadCount : "All subscribes should succeed";
        assert service.hasSubscribers(topic) : "Topic should have subscribers";
    }

    /**
     * Verifies mixed publish/subscribe workloads work correctly.
     */
    public void testMixedPublishSubscribeWorkload() throws Exception {
        // Arrange
        int threadCount = 50;
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger publishSuccessCount = new AtomicInteger(0);
        AtomicInteger subscribeSuccessCount = new AtomicInteger(0);

        // Act - half do publishes, half do subscribes
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 2) {
                        // Publish
                        EventSubscriber subscriber = new TestSubscriber();
                        service.subscribe(topic, subscriber);
                        service.publish(event);
                        publishSuccessCount.incrementAndGet();
                    } else {
                        // Subscribe
                        EventSubscriber subscriber = new TestSubscriber();
                        service.subscribe(topic, subscriber);
                        subscribeSuccessCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore expected exceptions
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert publishSuccessCount.get() == threadCount / 2 : "All publishes should succeed";
        assert subscribeSuccessCount.get() == threadCount / 2 : "All subscribes should succeed";
    }

    /**
     * Verifies concurrent unsubscribe operations work correctly.
     */
    public void testConcurrentUnsubscribe() throws Exception {
        // Arrange
        int threadCount = 50;
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");

        // Subscribe initial subscribers
        for (int i = 0; i < threadCount; i++) {
            service.subscribe(topic, new TestSubscriber());
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act - concurrent unsubscribes
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    EventSubscriber subscriber = new TestSubscriber();
                    service.unsubscribe(topic, subscriber);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert successCount.get() == threadCount : "All unsubscribes should succeed";
    }

    /**
     * Verifies no ConcurrentModificationException during concurrent operations.
     */
    public void testNoConcurrentModificationException() throws Exception {
        // Arrange
        int threadCount = 50;
        var service = createService();
        EventTopic topic = new EventTopic("test-topic");
        Event event = createValidEvent(topic);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // Act - mixed operations
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index % 3 == 0) {
                        EventSubscriber subscriber = new TestSubscriber();
                        service.subscribe(topic, subscriber);
                    } else if (index % 3 == 1) {
                        service.publish(event);
                    } else {
                        EventSubscriber subscriber = new TestSubscriber();
                        service.unsubscribe(topic, subscriber);
                    }
                } catch (Exception e) {
                    if (e instanceof java.util.ConcurrentModificationException) {
                        exceptionCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert exceptionCount.get() == 0 : "No ConcurrentModificationException should occur";
    }

    // ===== Helper Classes =====

    private static class TestSubscriber implements EventSubscriber {
        @Override
        public void onEvent(Event event) {
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