package platform.core.registry.error;

import java.time.Instant;

/**
 * <b>InvalidKernelException</b>
 *
 * <p>Thrown when a kernel fails validation during registration.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a kernel does not satisfy the registration prerequisites.</li>
 *   <li>Extends {@link RegistryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see RegistryException
 * @see RegistryErrorCode#REGISTRY_VALIDATION_FAILED
 */
public class InvalidKernelException extends RegistryException {

    /**
     * Constructs a new {@code InvalidKernelException} with the given message.
     *
     * @param message the detail message (must not be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidKernelException(String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a new {@code InvalidKernelException} with the given message and details.
     *
     * @param message  the detail message (must not be null)
     * @param details  optional details string (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidKernelException(String message, String details) {
        super(createError(message, details));
    }

    /**
     * Constructs a new {@code InvalidKernelException} with the given message and cause.
     *
     * @param message the detail message (must not be null)
     * @param cause   the underlying cause (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidKernelException(String message, Throwable cause) {
        super(createError(message, null), cause);
    }

    /**
     * Constructs a new {@code InvalidKernelException} with the given message, details, and cause.
     *
     * @param message  the detail message (must not be null)
     * @param details  optional details string (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code message} is null
     */
    public InvalidKernelException(String message, String details, Throwable cause) {
        super(createError(message, details), cause);
    }

    private static RegistryError createError(String message, String details) {
        java.util.Map<String, Object> detailMap = details != null
                ? java.util.Map.of("details", details)
                : java.util.Collections.emptyMap();
        return new RegistryError(
                RegistryErrorCode.REGISTRY_VALIDATION_FAILED,
                message,
                Instant.now(),
                detailMap
        );
    }
}