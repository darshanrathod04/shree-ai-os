package platform.core.lifecycle.error;

/**
 * <b>LifecycleErrorCode</b>
 *
 * <p>Standardized error codes for lifecycle operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible lifecycle error conditions.</li>
 *   <li>Enables consistent error reporting across the Lifecycle Service.</li>
 *   <li>Supports the LifecycleException hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see LifecycleError
 * @see LifecycleException
 */
public enum LifecycleErrorCode {

    /**
     * A state transition violates the Lifecycle State Model.
     */
    LIFECYCLE_INVALID_TRANSITION,

    /**
     * Execution was attempted before kernel initialization.
     */
    LIFECYCLE_KERNEL_NOT_INITIALIZED,

    /**
     * Attempted to start an already running kernel.
     */
    LIFECYCLE_KERNEL_ALREADY_RUNNING,

    /**
     * Attempted to stop an already stopped kernel.
     */
    LIFECYCLE_KERNEL_ALREADY_STOPPED,

    /**
     * Attempted to suspend an already suspended kernel.
     */
    LIFECYCLE_KERNEL_ALREADY_SUSPENDED,

    /**
     * Attempted to operate on a terminated kernel.
     */
    LIFECYCLE_KERNEL_TERMINATED,

    /**
     * Lifecycle validation failed.
     */
    LIFECYCLE_VALIDATION_FAILED
}