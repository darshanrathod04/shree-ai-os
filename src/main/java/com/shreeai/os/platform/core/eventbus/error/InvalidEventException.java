package com.shreeai.os.platform.core.eventbus.error;

/**
 * <b>InvalidEventException</b>
 *
 * <p>Thrown when an Event fails structural validation within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that an event does not meet the structural requirements.</li>
 *   <li>Wraps an {@link EventError} with code {@link EventErrorCode#EVENT_INVALID}
 *       or {@link EventErrorCode#EVENT_VALIDATION_FAILED}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see EventErrorCode#EVENT_INVALID
 * @see EventErrorCode#EVENT_VALIDATION_FAILED
 * @see EventBusException
 */
public class InvalidEventException extends EventBusException {

    /**
     * Constructs a new {@code InvalidEventException} with the given error.
     *
     * @param error the event error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public InvalidEventException(EventError error) {
        super(error);
    }
}