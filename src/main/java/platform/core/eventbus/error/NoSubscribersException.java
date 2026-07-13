package platform.core.eventbus.error;

/**
 * <b>NoSubscribersException</b>
 *
 * <p>Thrown when publishing to a topic without subscribers within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that no subscribers are registered for the event topic.</li>
 *   <li>Wraps an {@link EventError} with code {@link EventErrorCode#EVENT_NO_SUBSCRIBERS}.</li>
 *   <li>Implementation may choose whether this is exceptional or informational.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see EventErrorCode#EVENT_NO_SUBSCRIBERS
 * @see EventBusException
 */
public class NoSubscribersException extends EventBusException {

    /**
     * Constructs a new {@code NoSubscribersException} with the given error.
     *
     * @param error the event error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public NoSubscribersException(EventError error) {
        super(error);
    }
}