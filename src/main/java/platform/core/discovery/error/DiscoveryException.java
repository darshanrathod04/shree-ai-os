package platform.core.discovery.error;

import java.time.Instant;

/**
 * <b>DiscoveryException</b>
 *
 * <p>Base exception for all Discovery Service errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the single base exception type for all discovery errors.</li>
 *   <li>Encapsulates a {@link DiscoveryError} containing code, message, timestamp, and details.</li>
 *   <li>All future discovery exceptions SHALL extend this class.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryError
 * @see DiscoveryErrorCode
 */
public class DiscoveryException extends RuntimeException {

    private final DiscoveryError error;

    /**
     * Constructs a new {@code DiscoveryException} with the given error.
     *
     * @param error the discovery error (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public DiscoveryException(DiscoveryError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Constructs a new {@code DiscoveryException} with the given error and cause.
     *
     * @param error the discovery error (must not be null)
     * @param cause the underlying cause (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public DiscoveryException(DiscoveryError error, Throwable cause) {
        super(error.message(), cause);
        this.error = error;
    }

    /**
     * Returns the {@link DiscoveryError} associated with this exception.
     *
     * @return the discovery error
     */
    public DiscoveryError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public DiscoveryErrorCode code() {
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