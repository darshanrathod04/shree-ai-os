package platform.core.lifecycle.error;

import java.time.Instant;

/**
 * <b>LifecycleException</b>
 *
 * <p>Base exception for all Lifecycle Service errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the single base exception for the Lifecycle subsystem.</li>
 *   <li>All future lifecycle exceptions SHALL extend this class.</li>
 *   <li>Contains a {@link LifecycleError} with structured error information.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see LifecycleError
 * @see LifecycleErrorCode
 */
public class LifecycleException extends RuntimeException {

    private final LifecycleError error;

    /**
     * Constructs a new {@code LifecycleException} with the given error.
     *
     * @param error the lifecycle error (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public LifecycleException(LifecycleError error) {
        super(createMessage(error));
        this.error = error;
    }

    /**
     * Constructs a new {@code LifecycleException} with the given error and cause.
     *
     * @param error the lifecycle error (must not be null)
     * @param cause the underlying cause (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public LifecycleException(LifecycleError error, Throwable cause) {
        super(createMessage(error), cause);
        this.error = error;
    }

    /**
     * Returns the associated lifecycle error.
     *
     * @return the lifecycle error
     */
    public LifecycleError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public LifecycleErrorCode code() {
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

    /**
     * Returns the error timestamp.
     *
     * @return the error timestamp
     */
    public Instant timestamp() {
        return error.timestamp();
    }

    /**
     * Returns the error details.
     *
     * @return the error details
     */
    public java.util.Map<String, Object> details() {
        return error.details();
    }

    private static String createMessage(LifecycleError error) {
        return error.code() + ": " + error.message();
    }
}