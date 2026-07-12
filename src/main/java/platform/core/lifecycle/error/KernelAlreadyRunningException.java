package platform.core.lifecycle.error;

import platform.core.registry.model.KernelId;

/**
 * <b>KernelAlreadyRunningException</b>
 *
 * <p>Thrown when attempting to start an already running kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a kernel start was attempted on an already running kernel.</li>
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
 * @see LifecycleErrorCode#LIFECYCLE_KERNEL_ALREADY_RUNNING
 */
public class KernelAlreadyRunningException extends LifecycleException {

    /**
     * Constructs a new {@code KernelAlreadyRunningException} with the given kernel identifier.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelAlreadyRunningException(KernelId kernelId) {
        this(kernelId, (String) null);
    }

    /**
     * Constructs a new {@code KernelAlreadyRunningException} with the given kernel identifier and message.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelAlreadyRunningException(KernelId kernelId, String message) {
        super(createError(kernelId, message));
    }

    /**
     * Constructs a new {@code KernelAlreadyRunningException} with the given kernel identifier and cause.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelAlreadyRunningException(KernelId kernelId, Throwable cause) {
        super(createError(kernelId, null), cause);
    }

    /**
     * Constructs a new {@code KernelAlreadyRunningException} with the given kernel identifier, message, and cause.
     *
     * @param kernelId the kernel identifier (must not be null)
     * @param message  the detail message (may be null)
     * @param cause    the underlying cause (may be null)
     * @throws NullPointerException if {@code kernelId} is null
     */
    public KernelAlreadyRunningException(KernelId kernelId, String message, Throwable cause) {
        super(createError(kernelId, message), cause);
    }

    private static LifecycleError createError(KernelId kernelId, String message) {
        String errorMessage = message != null ? message
                : "Kernel '" + kernelId.value() + "' is already running";
        return new LifecycleError(
                LifecycleErrorCode.LIFECYCLE_KERNEL_ALREADY_RUNNING,
                errorMessage
        );
    }
}