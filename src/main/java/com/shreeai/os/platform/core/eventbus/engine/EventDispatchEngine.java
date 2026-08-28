package com.shreeai.os.platform.core.eventbus.engine;

import com.shreeai.os.platform.core.eventbus.service.DefaultEventBusService;
import com.shreeai.os.platform.core.eventbus.error.EventDispatchException;
import com.shreeai.os.platform.core.eventbus.model.Event;
import com.shreeai.os.platform.core.eventbus.model.EventSubscriber;

import java.util.Collection;

/**
 * <b>EventDispatchEngine</b>
 *
 * <p>Executes event delivery within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes event delivery to subscribers.</li>
 *   <li>Records dispatch outcomes and collects failures.</li>
 *   <li>Remains independent from validation, subscriber registry, and service coordination.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> Service coordinates. Engine executes. Validator protects.
 * Models describe. Errors report. Responsibilities SHALL remain independent.</p>
 *
 * @see DispatchResult
 * @see DefaultEventBusService
 */
public interface EventDispatchEngine {

    /**
     * Dispatches an event to the given subscribers.
     *
     * <p>Dispatch flow:</p>
     * <ol>
     *   <li>Receive Event</li>
     *   <li>Receive Subscribers</li>
     *   <li>Invoke subscriber.onEvent(event) for each subscriber</li>
     *   <li>Collect results</li>
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
     * @param event      the event to dispatch (must not be null)
     * @param subscribers the collection of subscribers to dispatch to (must not be null)
     * @return the dispatch result
     * @throws EventDispatchException if an unexpected infrastructure failure occurs
     */
    DispatchResult dispatch(Event event, Collection<EventSubscriber> subscribers) throws EventDispatchException;
}