package com.shreeai.os.platform.core.eventbus.service;

import com.shreeai.os.platform.core.eventbus.error.EventError;
import com.shreeai.os.platform.core.eventbus.error.EventErrorCode;
import com.shreeai.os.platform.core.eventbus.api.EventBus;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.error.InvalidEventException;
import com.shreeai.os.platform.core.eventbus.error.NoSubscribersException;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;
import com.shreeai.os.platform.core.eventbus.model.EventTopic;
import com.shreeai.os.platform.core.eventbus.validator.EventValidator;
import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <b>DefaultEventBusService</b>
 *
 * <p>The default in-memory implementation of the {@link EventBus} contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates publishing and subscriber management.</li>
 *   <li>Owns the subscriber registry.</li>
 *   <li>Delegates actual event delivery to the Event Dispatch Engine.</li>
 *   <li>Ensures all events are validated before processing.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> Service coordinates. Engine executes. Validator protects.
 * Responsibilities SHALL remain separated.</p>
 *
 * @see EventBus
 * @see EventValidator
 * @see EventDispatchEngine
 */
public final class DefaultEventBusService implements EventBus {

    private final EventValidator validator;
    private final LifecycleService lifecycleService;
    private final LifecycleTransitionEngine lifecycleTransitionEngine;
    private final EventDispatchEngine dispatchEngine;

    private final ConcurrentMap<EventTopic, java.util.Set<EventSubscriber>> subscribers;

    /**
     * Constructs a new {@code DefaultEventBusService} with the given dependencies.
     *
     * @param validator                the event validator (must not be null)
     * @param lifecycleService         the lifecycle service (must not be null)
     * @param lifecycleTransitionEngine the lifecycle transition engine (must not be null)
     * @param dispatchEngine           the event dispatch engine (must not be null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public DefaultEventBusService(EventValidator validator,
                                  LifecycleService lifecycleService,
                                  LifecycleTransitionEngine lifecycleTransitionEngine,
                                  EventDispatchEngine dispatchEngine) {
        if (validator == null) {
            throw new IllegalArgumentException("EventValidator must not be null");
        }
        if (lifecycleService == null) {
            throw new IllegalArgumentException("LifecycleService must not be null");
        }
        if (lifecycleTransitionEngine == null) {
            throw new IllegalArgumentException("LifecycleTransitionEngine must not be null");
        }
        if (dispatchEngine == null) {
            throw new IllegalArgumentException("EventDispatchEngine must not be null");
        }
        this.validator = validator;
        this.lifecycleService = lifecycleService;
        this.lifecycleTransitionEngine = lifecycleTransitionEngine;
        this.dispatchEngine = dispatchEngine;
        this.subscribers = new ConcurrentHashMap<>();
    }

    /**
     * Publishes an event to the Event Bus.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate Event</li>
     *   <li>Check subscribers exist</li>
     *   <li>Delegate to EventDispatchEngine</li>
     *   <li>Return</li>
     * </ol>
     *
     * @param event the event to publish (must not be null)
     * @throws IllegalArgumentException if {@code event} is {@code null}
     * @throws InvalidEventException if the event fails validation
     * @throws NoSubscribersException if no subscribers are registered for the event's topic
     */
    @Override
    public void publish(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        // Step 1: Validate Event
        var validationResult = validator.validateEvent(event);
        if (!validationResult.isValid()) {
            throw new InvalidEventException(
                    new EventError(
                            EventErrorCode.EVENT_INVALID,
                            "Event validation failed: " + validationResult.errors()
                    )
            );
        }

        // Step 2: Check subscribers exist
        EventTopic topic = event.topic();
        java.util.Set<EventSubscriber> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers == null || topicSubscribers.isEmpty()) {
            throw new NoSubscribersException(
                    new EventError(
                            EventErrorCode.EVENT_NO_SUBSCRIBERS,
                            "No subscribers registered for topic: " + topic.value()
                    )
            );
        }

        // Step 3: Delegate to EventDispatchEngine
        dispatchEngine.dispatch(event, topicSubscribers);
    }

    /**
     * Subscribes a subscriber to a topic.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate Topic</li>
     *   <li>Validate Subscriber</li>
     *   <li>Register subscriber</li>
     *   <li>Return</li>
     * </ol>
     *
     * @param topic      the topic to subscribe to (must not be null)
     * @param subscriber the subscriber to register (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    @Override
    public void subscribe(EventTopic topic, EventSubscriber subscriber) {
        if (topic == null) {
            throw new IllegalArgumentException("EventTopic must not be null");
        }
        if (subscriber == null) {
            throw new IllegalArgumentException("EventSubscriber must not be null");
        }

        // Step 1: Validate Topic
        var topicValidation = validator.validateTopic(topic);
        if (!topicValidation.isValid()) {
            throw new InvalidEventException(
                    new EventError(
                            EventErrorCode.EVENT_INVALID,
                            "Invalid topic: " + topicValidation.errors()
                    )
            );
        }

        // Step 2: Validate Subscriber
        var subscriberValidation = validator.validateSubscriber(subscriber);
        if (!subscriberValidation.isValid()) {
            throw new InvalidEventException(
                    new EventError(
                            EventErrorCode.EVENT_INVALID,
                            "Invalid subscriber: " + subscriberValidation.errors()
                    )
            );
        }

        // Step 3: Register subscriber (thread-safe)
        subscribers.computeIfAbsent(topic, t -> ConcurrentHashMap.newKeySet()).add(subscriber);
    }

    /**
     * Unsubscribes a subscriber from a topic.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate</li>
     *   <li>Remove subscriber</li>
     *   <li>Return</li>
     * </ol>
     *
     * @param topic      the topic to unsubscribe from (must not be null)
     * @param subscriber the subscriber to remove (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    @Override
    public void unsubscribe(EventTopic topic, EventSubscriber subscriber) {
        if (topic == null) {
            throw new IllegalArgumentException("EventTopic must not be null");
        }
        if (subscriber == null) {
            throw new IllegalArgumentException("EventSubscriber must not be null");
        }

        // Step 1: Validate
        var topicValidation = validator.validateTopic(topic);
        if (!topicValidation.isValid()) {
            throw new InvalidEventException(
                    new EventError(
                            EventErrorCode.EVENT_INVALID,
                            "Invalid topic: " + topicValidation.errors()
                    )
            );
        }

        // Step 2: Remove subscriber (thread-safe)
        java.util.Set<EventSubscriber> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.remove(subscriber);
            // Clean up empty topic sets
            if (topicSubscribers.isEmpty()) {
                subscribers.remove(topic);
            }
        }
    }

    /**
     * Returns whether there are any subscribers for the given topic.
     *
     * <p>This is a read-only lookup — no validation is performed.</p>
     *
     * @param topic the topic to check (must not be null)
     * @return {@code true} if there is at least one subscriber for the topic,
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code topic} is {@code null}
     */
    @Override
    public boolean hasSubscribers(EventTopic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("EventTopic must not be null");
        }
        java.util.Set<EventSubscriber> topicSubscribers = subscribers.get(topic);
        return topicSubscribers != null && !topicSubscribers.isEmpty();
    }

    /**
     * Returns a collection of all currently registered topics.
     *
     * <p>The returned collection is a snapshot of the topics at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * @return an unmodifiable collection of all registered topics;
     *         returns an empty collection if no topics are registered
     */
    @Override
    public Collection<EventTopic> registeredTopics() {
        return Collections.unmodifiableCollection(subscribers.keySet());
    }
}