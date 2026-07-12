package platform.core.lifecycle.api;

import platform.core.lifecycle.model.KernelHealth;
import platform.core.lifecycle.model.KernelState;
import platform.core.registry.model.KernelId;

/**
 * <b>LifecycleService</b>
 *
 * <p>The official Platform contract for Kernel lifecycle management within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the Platform contract for kernel lifecycle operations.</li>
 *   <li>Specifies WHAT the Platform can do — implementations define HOW.</li>
 *   <li>Ensures lifecycle management is independent of implementation details.</li>
 *   <li>Provides a stable API for kernel state and health queries.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Lifecycle Principle:</b> The Lifecycle API defines WHAT the Platform can do.
 * Future services define HOW the Platform does it.</p>
 *
 * @see platform.core.lifecycle.api package-info
 */
public interface LifecycleService {

    /**
     * Initializes a kernel for operation.
     *
     * <p>Prepares the kernel for execution by performing one-time setup operations.
     * This method SHALL be called before {@link #start(KernelId)}.</p>
     *
     * <p>Initialization is idempotent — calling it multiple times on an already
     * initialized kernel SHALL return {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to initialize (must not be null)
     * @return {@code true} if initialization succeeded or kernel was already initialized,
     *         {@code false} if initialization failed
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean initialize(KernelId kernelId);

    /**
     * Starts a kernel.
     *
     * <p>Transitions the kernel from its current state to the RUNNING state.
     * The kernel MUST be initialized before it can be started.</p>
     *
     * <p>Starting an already running kernel SHALL return {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to start (must not be null)
     * @return {@code true} if the kernel was started successfully or was already running,
     *         {@code false} if the start operation failed
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean start(KernelId kernelId);

    /**
     * Stops a kernel.
     *
     * <p>Transitions the kernel from its current state to the STOPPED state.
     * The kernel MUST be running before it can be stopped.</p>
     *
     * <p>Stopping an already stopped kernel SHALL return {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to stop (must not be null)
     * @return {@code true} if the kernel was stopped successfully or was already stopped,
     *         {@code false} if the stop operation failed
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean stop(KernelId kernelId);

    /**
     * Suspends a kernel.
     *
     * <p>Transitions the kernel from its current state to the SUSPENDED state.
     * The kernel MUST be running before it can be suspended.</p>
     *
     * <p>Suspension preserves kernel state for later resumption.</p>
     *
     * <p>Suspending an already suspended kernel SHALL return {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to suspend (must not be null)
     * @return {@code true} if the kernel was suspended successfully or was already suspended,
     *         {@code false} if the suspend operation failed
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean suspend(KernelId kernelId);

    /**
     * Resumes a suspended kernel.
     *
     * <p>Transitions the kernel from the SUSPENDED state to the RUNNING state.
     * The kernel MUST be suspended before it can be resumed.</p>
     *
     * <p>Resuming preserves the kernel state from before suspension.</p>
     *
     * <p>Resuming a running kernel SHALL return {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to resume (must not be null)
     * @return {@code true} if the kernel was resumed successfully or was already running,
     *         {@code false} if the resume operation failed
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean resume(KernelId kernelId);

    /**
     * Returns the current state of a kernel.
     *
     * <p>Provides a snapshot of the kernel's lifecycle state at the time of the call.</p>
     *
     * <p>The returned {@link KernelState} is immutable and reflects the kernel's
     * current operational status.</p>
     *
     * @param kernelId the identifier of the kernel to query (must not be null)
     * @return the current {@link KernelState} of the kernel
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    KernelState state(KernelId kernelId);

    /**
     * Returns the current health status of a kernel.
     *
     * <p>Provides a snapshot of the kernel's health at the time of the call.</p>
     *
     * <p>The returned {@link KernelHealth} is immutable and reflects the kernel's
     * operational health.</p>
     *
     * @param kernelId the identifier of the kernel to query (must not be null)
     * @return the current {@link KernelHealth} of the kernel
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    KernelHealth health(KernelId kernelId);
}
