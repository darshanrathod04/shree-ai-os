package platform.core.eventbus.model;

/**
 * <b>EventSubscriber</b>
 *
 * <p>A functional interface that defines the contract for receiving events
 * from the Event Bus within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines how components receive published events.</li>
 *   <li>Enables decoupled communication between components.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
@FunctionalInterface
public interface EventSubscriber {

    /**
     * Called when an event is published to a topic to which this subscriber
     * has subscribed.
     *
     * @param event the published event (must not be null)
     */
    void onEvent(Event event);
}