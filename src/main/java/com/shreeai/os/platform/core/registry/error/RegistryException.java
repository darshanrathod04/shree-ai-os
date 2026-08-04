package com.shreeai.os.platform.core.registry.error;

import java.time.Instant;

/**
 * <b>RegistryException</b>
 *
 * <p>Base exception for all Kernel Registry errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the single base exception type for all registry errors.</li>
 *   <li>Encapsulates a {@link RegistryError} containing code, message, timestamp, and details.</li>
 *   <li>All future registry exceptions SHALL extend this class.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see RegistryError
 * @see RegistryErrorCode
 */
public class RegistryException extends RuntimeException {

    private final RegistryError error;

    /**
     * Constructs a new {@code RegistryException} with the given error.
     *
     * @param error the registry error (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public RegistryException(RegistryError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Constructs a new {@code RegistryException} with the given error and cause.
     *
     * @param error the registry error (must not be null)
     * @param cause the underlying cause (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public RegistryException(RegistryError error, Throwable cause) {
        super(error.message(), cause);
        this.error = error;
    }

    /**
     * Returns the {@link RegistryError} associated with this exception.
     *
     * @return the registry error
     */
    public RegistryError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public RegistryErrorCode code() {
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
     * @return the error details map
     */
    public java.util.Map<String, Object> details() {
        return error.details();
    }
}