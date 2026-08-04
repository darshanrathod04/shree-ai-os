package com.shreeai.os.platform.core.eventbus.api;

import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;
import com.shreeai.os.platform.core.eventbus.model.EventTopic;

import java.util.Collection;

/**
 * <b>EventBus</b>
 *
 * <p>The public contract for Platform event communication within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines how Platform components publish and subscribe to events.</li>
 *   <li>Specifies WHAT the Event Bus can do — implementations define HOW.</li>
 *   <li>Enables decoupled communication between Platform components.</li>
 *   <li>Ensures components communicate through contracts, not direct dependencies.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Event Bus Principle:</b> Platform components communicate through contracts.
 * Implementations remain hidden.</p>
 *
 * @see platform.core.eventbus.api package-info
 */
public interface EventBus {

    /**
     * Publishes an event to the Event Bus.
     *
     * <p>The event SHALL be delivered to all subscribers of the event's topic.
     * The delivery semantics (synchronous, asynchronous, ordered) are defined
     * by the implementation.</p>
     *
     * <p>If there are no subscribers for the event's topic, the event SHALL
     * be silently discarded.</p>
     *
     * @param event the event to publish (must not be null)
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    void publish(Event event);

    /**
     * Subscribes a subscriber to a topic.
     *
     * <p>The subscriber SHALL receive all events published to the specified topic
     * from the point of subscription onward.</p>
     *
     * <p>If the subscriber is already subscribed to the topic, this method
     * SHALL have no effect.</p>
     *
     * @param topic      the topic to subscribe to (must not be null)
     * @param subscriber the subscriber to register (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    void subscribe(EventTopic topic, EventSubscriber subscriber);

    /**
     * Unsubscribes a subscriber from a topic.
     *
     * <p>The subscriber SHALL stop receiving events published to the specified topic
     * after this method returns.</p>
     *
     * <p>If the subscriber is not subscribed to the topic, this method
     * SHALL have no effect.</p>
     *
     * @param topic      the topic to unsubscribe from (must not be null)
     * @param subscriber the subscriber to remove (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    void unsubscribe(EventTopic topic, EventSubscriber subscriber);

    /**
     * Returns whether there are any subscribers for the given topic.
     *
     * @param topic the topic to check (must not be null)
     * @return {@code true} if there is at least one subscriber for the topic,
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code topic} is {@code null}
     */
    boolean hasSubscribers(EventTopic topic);

    /**
     * Returns a collection of all currently registered topics.
     *
     * <p>The returned collection is a snapshot of the topics at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * @return an unmodifiable collection of all registered topics;
     *         returns an empty collection if no topics are registered
     */
    Collection<EventTopic> registeredTopics();
}
