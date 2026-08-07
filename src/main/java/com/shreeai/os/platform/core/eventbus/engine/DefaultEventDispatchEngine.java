package com.shreeai.os.platform.core.eventbus.engine;

import com.shreeai.os.platform.core.eventbus.error.EventDispatchException;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <b>DefaultEventDispatchEngine</b>
 *
 * <p>Default implementation of the {@link EventDispatchEngine} contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes synchronous event delivery to subscribers.</li>
 *   <li>Provides exception isolation between subscribers.</li>
 *   <li>Collects dispatch outcomes and failures.</li>
 *   <li>Ensures one subscriber failure does not affect others.</li>
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
 * <p><b>V1 Scope:</b></p>
 * <ul>
 *   <li>Synchronous event dispatch only</li>
 *   <li>Thread-safe subscriber iteration</li>
 *   <li>Exception isolation between subscribers</li>
 *   <li>No distributed messaging</li>
 *   <li>No persistence</li>
 *   <li>No replay</li>
 *   <li>No event history</li>
 *   <li>No clustering</li>
 *   <li>No advanced routing</li>
 * </ul>
 *
 * @see EventDispatchEngine
 * @see DispatchResult
 */
public final class DefaultEventDispatchEngine implements EventDispatchEngine {

    /**
     * Constructs a new {@code DefaultEventDispatchEngine}.
     *
     * <p>This constructor creates a new instance with no initial configuration.</p>
     */
    public DefaultEventDispatchEngine() {
        // No dependencies required for V1
    }

    /**
     * Dispatches an event to the given subscribers.
     *
     * <p>Dispatch flow:</p>
     * <ol>
     *   <li>Receive Event</li>
     *   <li>Receive Subscribers</li>
     *   <li>Invoke subscriber.onEvent(event) for each subscriber (synchronously)</li>
     *   <li>Collect results and failures</li>
     *   <li>Create DispatchResult</li>
     *   <li>Return</li>
     * </ol>
     *
     * <p>Dispatch rules:</p>
     * <ul>
     *   <li>Continue dispatching even if one subscriber fails.</li>
     *   <li>One subscriber failure SHALL NOT stop other subscribers.</li>
     *   <li>Collect all failures.</li>
     *   <li>Never throw for expected subscriber failures.</li>
     *   <li>Unexpected infrastructure failures may throw EventDispatchException.</li>
     * </ul>
     *
     * <p>Thread safety:</p>
     * <ul>
     *   <li>This method is thread-safe.</li>
     *   <li>Subscriber collection is iterated in a snapshot to avoid ConcurrentModificationException.</li>
     *   <li>Each subscriber receives the event independently.</li>
     * </ul>
     *
     * @param event      the event to dispatch (must not be null)
     * @param subscribers the collection of subscribers to dispatch to (must not be null)
     * @return the dispatch result
     * @throws EventDispatchException if an unexpected infrastructure failure occurs
     */
    @Override
    public DispatchResult dispatch(Event event, Collection<EventSubscriber> subscribers) throws EventDispatchException {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (subscribers == null) {
            throw new IllegalArgumentException("Subscribers must not be null");
        }

        // Create snapshot of subscribers to avoid ConcurrentModificationException
        List<EventSubscriber> subscribersSnapshot;
        synchronized (subscribers) {
            subscribersSnapshot = new ArrayList<>(subscribers);
        }

        int subscribersAttempted = subscribersSnapshot.size();
        int subscribersSucceeded = 0;
        int subscribersFailed = 0;
        List<String> failureMessages = new ArrayList<>();

        // Dispatch to each subscriber synchronously
        for (EventSubscriber subscriber : subscribersSnapshot) {
            try {
                // Invoke subscriber's event handler
                subscriber.onEvent(event);
                subscribersSucceeded++;
            } catch (Exception e) {
                // Exception isolation: catch and record, but continue dispatching
                subscribersFailed++;
                String failureMessage = String.format(
                    "Subscriber %s failed to process event %s: %s",
                    subscriber.getClass().getName(),
                    event,
                    e.getMessage()
                );
                failureMessages.add(failureMessage);
                // Continue to next subscriber - do not throw
            }
        }

        // Determine overall success
        boolean success = subscribersFailed == 0;

        // Create and return dispatch result
        return new DispatchResult(
            success,
            event,
            subscribersAttempted,
            subscribersSucceeded,
            subscribersFailed,
            failureMessages
        );
    }
}