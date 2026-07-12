package platform.core.lifecycle.error;

import platform.core.lifecycle.model.KernelState;
import platform.core.registry.model.KernelId;

/**
 * <b>KernelNotInitializedException</b>
 *
 * <p>Thrown when execution is attempted before kernel initialization.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a kernel operation was attempted before initialization.</li>
 *   <li>Extends {@link LifecycleException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see LifecycleException
 * @see LifecycleErrorCode#LIFECYCLE_KERNEL_NOT_INITIALIZED
 */
public class KernelNotInitializedException extends LifecycleException {

    /**
     * Constructs a new {@code KernelNotInitializedException} with the given kernel identifier.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotInitializedException(KernelId kernelId) {
        this(kernelId, (String) null);
    }

    /**
     * Constructs a new {@code KernelNotInitializedException} with the given kernel identifier and message.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotInitializedException(KernelId kernelId, String message) {
        super(createError(kernelId, message));
    }

    /**
     * Constructs a new {@code KernelNotInitializedException} with the given kernel identifier and cause.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotInitializedException(KernelId kernelId, Throwable cause) {
        super(createError(kernelId, null), cause);
    }

    /**
     * Constructs a new {@code KernelNotInitializedException} with the given kernel identifier, message, and cause.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelNotInitializedException(KernelId kernelId, String message, Throwable cause) {
        super(createError(kernelId, message), cause);
    }

    private static LifecycleError createError(KernelId kernelId, String message) {
        String errorMessage = message != null ? message
                : "Kernel '" + kernelId.value() + "' has not been initialized. Current state: " + KernelState.CREATED;
        return new LifecycleError(
                LifecycleErrorCode.LIFECYCLE_KERNEL_NOT_INITIALIZED,
                errorMessage
        );
    }
}