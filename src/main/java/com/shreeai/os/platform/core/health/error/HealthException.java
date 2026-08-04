package com.shreeai.os.platform.core.health.error;

/**
 * <b>HealthException</b>
 *
 * <p>The base runtime exception for all Health errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the base exception type for all Health errors.</li>
 *   <li>Wraps a {@link HealthError} to provide structured error information.</li>
 *   <li>Enables consistent exception handling across the Health subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Note:</b> This SHALL become the ONLY base exception for the Health subsystem.
 * All concrete exceptions extend this class.</p>
 *
 * @see HealthError
 * @see HealthComponentNotFoundException
 * @see HealthCheckFailedException
 * @see InvalidHealthComponentException
 */
public class HealthException extends RuntimeException {

    private final HealthError error;

    /**
     * Constructs a new {@code HealthException} with the given error.
     *
     * @param error the health error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public HealthException(HealthError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Returns the health error associated with this exception.
     *
     * @return the health error
     */
    public HealthError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public HealthErrorCode code() {
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