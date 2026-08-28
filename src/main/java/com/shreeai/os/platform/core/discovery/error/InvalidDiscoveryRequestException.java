package com.shreeai.os.platform.core.discovery.error;

import java.time.Instant;

/**
 * <b>InvalidDiscoveryRequestException</b>
 *
 * <p>Thrown when a discovery request is invalid or malformed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a discovery request does not satisfy the required format or prerequisites.</li>
 *   <li>Extends {@link DiscoveryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryException
 * @see DiscoveryErrorCode#DISCOVERY_INVALID_REQUEST
 */
public class InvalidDiscoveryRequestException extends DiscoveryException {

    /**
     * Constructs a new {@code InvalidDiscoveryRequestException} with the given message.
     *
     * @param message the detail message (must not be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidDiscoveryRequestException(String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a new {@code InvalidDiscoveryRequestException} with the given message and details.
     *
     * @param message  the detail message (must not be null)
     * @param details  optional details string (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidDiscoveryRequestException(String message, String details) {
        super(createError(message, details));
    }

    /**
     * Constructs a new {@code InvalidDiscoveryRequestException} with the given message and cause.
     *
     * @param message the detail message (must not be null)
     * @param cause   the underlying cause (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidDiscoveryRequestException(String message, Throwable cause) {
        super(createError(message, null), cause);
    }

    /**
     * Constructs a new {@code InvalidDiscoveryRequestException} with the given message, details, and cause.
     *
     * @param message  the detail message (must not be null)
     * @param details  optional details string (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidDiscoveryRequestException(String message, String details, Throwable cause) {
        super(createError(message, details), cause);
    }

    private static DiscoveryError createError(String message, String details) {
        java.util.Map<String, Object> detailMap = details != null
                ? java.util.Map.of("details", details)
                : java.util.Collections.emptyMap();
        return new DiscoveryError(
                DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST,
                message,
                Instant.now(),
                detailMap
        );
    }
}