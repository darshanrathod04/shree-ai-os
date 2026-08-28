package com.shreeai.os.platform.core.discovery.error;

import java.time.Instant;

/**
 * <b>CapabilityNotFoundException</b>
 *
 * <p>Thrown when a requested capability is not found in the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a capability lookup returned no result.</li>
 *   <li>Extends {@link DiscoveryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryException
 * @see DiscoveryErrorCode#DISCOVERY_CAPABILITY_NOT_FOUND
 */
public class CapabilityNotFoundException extends DiscoveryException {

    /**
     * Constructs a new {@code CapabilityNotFoundException} with the given capability identifier.
     *
     * @param capabilityId the capability identifier that was not found (must not be null)
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public CapabilityNotFoundException(String capabilityId) {
        this(capabilityId, (String) null);
    }

    /**
     * Constructs a new {@code CapabilityNotFoundException} with the given capability identifier and message.
     *
     * @param capabilityId the capability identifier that was not found (must not be null)
     * @param message      the detail message (may be null)
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public CapabilityNotFoundException(String capabilityId, String message) {
        super(createError(capabilityId, message));
    }

    /**
     * Constructs a new {@code CapabilityNotFoundException} with the given capability identifier and cause.
     *
     * @param capabilityId the capability identifier that was not found (must not be null)
     * @param cause        the underlying cause (may be null)
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public CapabilityNotFoundException(String capabilityId, Throwable cause) {
        super(createError(capabilityId, null), cause);
    }

    /**
     * Constructs a new {@code CapabilityNotFoundException} with the given capability identifier, message, and cause.
     *
     * @param capabilityId the capability identifier that was not found (must not be null)
     * @param message      the detail message (may be null)
     * @param cause        the underlying cause (may be null)
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public CapabilityNotFoundException(String capabilityId, String message, Throwable cause) {
        super(createError(capabilityId, message), cause);
    }

    private static DiscoveryError createError(String capabilityId, String message) {
        String errorMessage = message != null ? message : "Capability '" + capabilityId + "' was not found";
        return new DiscoveryError(
                DiscoveryErrorCode.DISCOVERY_CAPABILITY_NOT_FOUND,
                errorMessage,
                Instant.now()
        );
    }
}