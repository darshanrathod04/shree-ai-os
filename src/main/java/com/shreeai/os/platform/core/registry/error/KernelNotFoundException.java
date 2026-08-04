package com.shreeai.os.platform.core.registry.error;

import java.time.Instant;

/**
 * <b>KernelNotFoundException</b>
 *
 * <p>Thrown when a requested kernel is not found in the registry.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a lookup by kernel identifier returned no result.</li>
 *   <li>Extends {@link RegistryException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see RegistryException
 * @see RegistryErrorCode#REGISTRY_KERNEL_NOT_FOUND
 */
public class KernelNotFoundException extends RegistryException {

    /**
     * Constructs a new {@code KernelNotFoundException} with the given kernel identifier.
     *
     * @param kernelId the kernel identifier that was not found (must not be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotFoundException(String kernelId) {
        this(kernelId, (String) null);
    }

    /**
     * Constructs a new {@code KernelNotFoundException} with the given kernel identifier and message.
     *
     * @param kernelId the kernel identifier that was not found (must not be null)
     * @param message  the detail message (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotFoundException(String kernelId, String message) {
        super(createError(kernelId, message));
    }

    /**
     * Constructs a new {@code KernelNotFoundException} with the given kernel identifier and cause.
     *
     * @param kernelId the kernel identifier that was not found (must not be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotFoundException(String kernelId, Throwable cause) {
        super(createError(kernelId, null), cause);
    }

    /**
     * Constructs a new {@code KernelNotFoundException} with the given kernel identifier, message, and cause.
     *
     * @param kernelId the kernel identifier that was not found (must not be null)
     * @param message  the detail message (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotFoundException(String kernelId, String message, Throwable cause) {
        super(createError(kernelId, message), cause);
    }

    private static RegistryError createError(String kernelId, String message) {
        String errorMessage = message != null ? message : "Kernel with id '" + kernelId + "' was not found";
        return new RegistryError(
                RegistryErrorCode.REGISTRY_KERNEL_NOT_FOUND,
                errorMessage,
                Instant.now()
        );
    }
}