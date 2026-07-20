package platform.kernels.execution.engine;

import java.time.Instant;

import platform.kernels.execution.model.ExecutionRequest;
import platform.kernels.execution.model.ExecutionResult;
import platform.kernels.execution.model.ExecutionStatus;
import platform.kernels.execution.model.RecoveryStrategy;

/**
 * <b>ExecutionProcessingEngine</b>
 *
 * <p>Processing contract for deterministic execution computation.
 * This interface defines the contract for execution processing operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines processing contracts for execution operations.</li>
 *   <li>Provides deterministic execution computation interface.</li>
 *   <li>Delegates to Engine implementation for computation.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only processing contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-106, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface ExecutionProcessingEngine {

    /**
     * Processes action execution deterministically.
     *
     * <p>This operation performs deterministic computation for action execution
     * and returns an immutable processing result.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by {@link DefaultExecutionProcessingEngine}.</p>
     *
     * <p><b>Delegation:</b> The Service Layer delegates to this method
     * for action execution processing.</p>
     *
     * @param executionRequest the execution request (must not be {@code null})
     * @return an immutable processing result
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    ExecutionProcessingResult processActionExecution(ExecutionRequest executionRequest);

    /**
     * Processes workflow execution deterministically.
     *
     * <p>This operation performs deterministic computation for workflow execution
     * and returns an immutable processing result.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by {@link DefaultExecutionProcessingEngine}.</p>
     *
     * <p><b>Delegation:</b> The Service Layer delegates to this method
     * for workflow execution processing.</p>
     *
     * @param executionRequest the execution request (must not be {@code null})
     * @return an immutable processing result
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    ExecutionProcessingResult processWorkflowExecution(ExecutionRequest executionRequest);

    /**
     * Processes task execution deterministically.
     *
     * <p>This operation performs deterministic computation for task execution
     * and returns an immutable processing result.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by {@link DefaultExecutionProcessingEngine}.</p>
     *
     * <p><b>Delegation:</b> The Service Layer delegates to this method
     * for task execution processing.</p>
     *
     * @param executionRequest the execution request (must not be {@code null})
     * @return an immutable processing result
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    ExecutionProcessingResult processTaskExecution(ExecutionRequest executionRequest);

    /**
     * Processes execution monitoring deterministically.
     *
     * <p>This operation performs deterministic computation for execution monitoring
     * and returns the current execution status.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by {@link DefaultExecutionProcessingEngine}.</p>
     *
     * <p><b>Delegation:</b> The Service Layer delegates to this method
     * for execution monitoring.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the current execution status
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    ExecutionStatus processExecutionMonitoring(String executionId);

    /**
     * Processes execution recovery deterministically.
     *
     * <p>This operation performs deterministic computation for execution recovery
     * and returns an immutable processing result.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementation is provided by {@link DefaultExecutionProcessingEngine}.</p>
     *
     * <p><b>Delegation:</b> The Service Layer delegates to this method
     * for execution recovery processing.</p>
     *
     * @param executionId     the execution identifier (must not be {@code null} or empty)
     * @param recoveryStrategy the recovery strategy to apply (must not be {@code null})
     * @return an immutable processing result
     * @throws IllegalArgumentException if executionId or recoveryStrategy is {@code null}
     */
    ExecutionProcessingResult processExecutionRecovery(String executionId, RecoveryStrategy recoveryStrategy);
}