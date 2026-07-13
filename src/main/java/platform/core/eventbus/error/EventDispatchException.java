package platform.core.eventbus.error;

/**
 * <b>EventDispatchException</b>
 *
 * <p>Thrown only for unexpected dispatch failures within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates an unexpected failure during event dispatch.</li>
 *   <li>Wraps an {@link EventError} with code {@link EventErrorCode#EVENT_DISPATCH_FAILED}.</li>
 *   <li>Should only be thrown for truly exceptional circumstances.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see EventErrorCode#EVENT_DISPATCH_FAILED
 * @see EventBusException
 */
public class EventDispatchException extends EventBusException {

    /**
     * Constructs a new {@code EventDispatchException} with the given error.
     *
     * @param error the event error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public EventDispatchException(EventError error) {
        super(error);
    }
}