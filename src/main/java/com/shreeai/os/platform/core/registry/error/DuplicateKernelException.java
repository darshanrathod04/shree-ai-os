package com.shreeai.os.platform.core.registry.error;

import java.time.Instant;

/**
 * <b>DuplicateKernelException</b>
 *
 * <p>Thrown when attempting to register a kernel with an identifier that is already registered.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals a duplicate kernel registration attempt (KR-001, KR-002).</li>
 *   <li>Extends {@link RegistryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see RegistryException
 * @see RegistryErrorCode#REGISTRY_DUPLICATE_KERNEL
 */
public class DuplicateKernelException extends RegistryException {

    /**
     * Constructs a new {@code DuplicateKernelException} with the given kernel identifier.
     *
     * @param kernelId the duplicate kernel identifier (must not be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public DuplicateKernelException(String kernelId) {
        this(kernelId, (String) null);
    }

    /**
     * Constructs a new {@code DuplicateKernelException} with the given kernel identifier and message.
     *
     * @param kernelId the duplicate kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public DuplicateKernelException(String kernelId, String message) {
        super(createError(kernelId, message));
    }

    /**
     * Constructs a new {@code DuplicateKernelException} with the given kernel identifier and cause.
     *
     * @param kernelId the duplicate kernel identifier (must not be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public DuplicateKernelException(String kernelId, Throwable cause) {
        super(createError(kernelId, null), cause);
    }

    /**
     * Constructs a new {@code DuplicateKernelException} with the given kernel identifier, message, and cause.
     *
     * @param kernelId the duplicate kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public DuplicateKernelException(String kernelId, String message, Throwable cause) {
        super(createError(kernelId, message), cause);
    }

    private static RegistryError createError(String kernelId, String message) {
        String errorMessage = message != null ? message : "Kernel with id '" + kernelId + "' is already registered";
        return new RegistryError(
                RegistryErrorCode.REGISTRY_DUPLICATE_KERNEL,
                errorMessage,
                Instant.now()
        );
    }
}