package platform.core.eventbus.model;

/**
 * <b>EventPriority</b>
 *
 * <p>Represents the priority level of an event within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the priority levels available for event handling.</li>
 *   <li>Enables priority-based event processing.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum EventPriority {

    /**
     * Low priority — processed when resources are available.
     */
    LOW,

    /**
     * Normal priority — standard event processing.
     */
    NORMAL,

    /**
     * High priority — processed before normal events.
     */
    HIGH,

    /**
     * Critical priority — processed immediately.
     */
    CRITICAL
}