package platform.kernels.chief.api;

import platform.kernels.chief.model.DelegationResult;

/**
 * <b>TaskDelegationService</b>
 *
 * <p>Defines delegation contracts for the Chief Kernel.
 * This interface provides contracts for delegating validated tasks to the
 * appropriate kernels without implementing any execution logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines task delegation contracts.</li>
 *   <li>Provides delegation monitoring and cancellation contracts.</li>
 *   <li>Coordinates with the Execution Kernel through public contracts only.</li>
 *   <li>Contains no execution implementation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only delegation contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface TaskDelegationService {

    /**
     * Delegates a validated task to the appropriate kernel.
     *
     * <p>This operation delegates a validated task to the target kernel for
     * execution and returns a delegation result for tracking.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> Coordinates with the Execution Kernel for task
     * execution logic.</p>
     *
     * @param taskId       the task identifier (must not be {@code null} or empty)
     * @param targetKernel the target kernel name (must not be {@code null} or empty)
     * @param payload      the task payload (must not be {@code null})
     * @return the delegation result
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    DelegationResult delegateTask(String taskId, String targetKernel, java.util.Map<String, Object> payload);

    /**
     * Monitors the status of a delegated task.
     *
     * <p>This operation retrieves the current status of a previously delegated
     * task.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param delegationId the delegation identifier (must not be {@code null} or empty)
     * @return the delegation result with current status
     * @throws IllegalArgumentException if delegationId is {@code null} or empty
     */
    DelegationResult monitorDelegation(String delegationId);

    /**
     * Cancels a delegated task.
     *
     * <p>This operation requests cancellation of a previously delegated task.
     * The task may not cancel immediately if it is in a non-interruptible state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param delegationId the delegation identifier (must not be {@code null} or empty)
     * @return the delegation result with updated status
     * @throws IllegalArgumentException if delegationId is {@code null} or empty
     */
    DelegationResult cancelDelegation(String delegationId);

    /**
     * Queries the current status of a delegation.
     *
     * <p>This operation retrieves the current status and details of a
     * delegation operation.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param delegationId the delegation identifier (must not be {@code null} or empty)
     * @return the delegation result with current status
     * @throws IllegalArgumentException if delegationId is {@code null} or empty
     */
    DelegationResult queryDelegationStatus(String delegationId);
}