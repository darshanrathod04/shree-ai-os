package com.shreeai.os.platform.kernels.execution.api;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionStatus;
import com.shreeai.os.platform.kernels.execution.model.RecoveryStrategy;

/**
 * <b>ExecutionService</b>
 *
 * <p>Primary façade for the Execution Kernel, providing high-level execution
 * operations and coordinating execution-related contracts.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Exposes high-level execution operations for the platform.</li>
 *   <li>Coordinates execution-related service contracts.</li>
 *   <li>Provides stable API boundaries for execution capabilities.</li>
 *   <li>Delegates specialized execution tasks to subordinate services.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only execution contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-101, EIO-ARCH-001</p>
 *
 * @see ActionExecutionService
 * @see WorkflowExecutionService
 * @see TaskExecutionService
 * @see ExecutionMonitoringService
 * @see ExecutionRecoveryService
 */
public interface ExecutionService {

    /**
     * Executes an action based on the provided execution request.
     *
     * <p>This operation initiates action execution and returns an execution
     * identifier for tracking.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link ActionExecutionService} for actual execution logic.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return an execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeAction(ExecutionRequest executionRequest);

    /**
     * Executes a workflow based on the provided execution request.
     *
     * <p>This operation initiates workflow execution and returns a workflow
     * identifier for tracking.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link WorkflowExecutionService} for actual execution logic.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a workflow execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeWorkflow(ExecutionRequest executionRequest);

    /**
     * Executes a planned task based on the provided execution request.
     *
     * <p>This operation initiates task execution and returns a task execution
     * identifier for tracking.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link TaskExecutionService} for actual execution logic.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a task execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeTask(ExecutionRequest executionRequest);

    /**
     * Retrieves the current execution status for a given execution identifier.
     *
     * <p>This operation provides real-time execution status information.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link ExecutionMonitoringService} for status retrieval.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the current execution status
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    ExecutionStatus getExecutionStatus(String executionId);

    /**
     * Cancels an ongoing execution.
     *
     * <p>This operation requests cancellation of the specified execution.
     * The execution may not cancel immediately if it is in a non-interruptible
     * state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link ActionExecutionService} or {@link WorkflowExecutionService}
     * for cancellation logic.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return {@code true} if cancellation was requested successfully
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    boolean cancelExecution(String executionId);

    /**
     * Initiates recovery for a failed execution.
     *
     * <p>This operation requests recovery of a failed execution using the
     * specified recovery strategy.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link ExecutionRecoveryService} for recovery logic.</p>
     *
     * @param executionId     the execution identifier (must not be {@code null} or empty)
     * @param recoveryStrategy the recovery strategy to apply (must not be {@code null})
     * @return a recovery execution identifier
     * @throws IllegalArgumentException if executionId or recoveryStrategy is {@code null}
     */
    String recoverExecution(String executionId, RecoveryStrategy recoveryStrategy);
}