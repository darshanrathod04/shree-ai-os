package platform.core.eventbus.error;

/**
 * <b>EventBusException</b>
 *
 * <p>The base runtime exception for all Event Bus errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the base exception type for all Event Bus errors.</li>
 *   <li>Wraps an {@link EventError} to provide structured error information.</li>
 *   <li>Enables consistent exception handling across the Event Bus subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Note:</b> This SHALL become the ONLY base exception for the Event Bus subsystem.
 * All concrete exceptions extend this class.</p>
 *
 * @see EventError
 * @see InvalidEventException
 * @see NoSubscribersException
 * @see EventDispatchException
 */
public class EventBusException extends RuntimeException {

    private final EventError error;

    /**
     * Constructs a new {@code EventBusException} with the given error.
     *
     * @param error the event error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public EventBusException(EventError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Returns the event error associated with this exception.
     *
     * @return the event error
     */
    public EventError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public EventErrorCode code() {
        return error.code();
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return error.message();
    }
}