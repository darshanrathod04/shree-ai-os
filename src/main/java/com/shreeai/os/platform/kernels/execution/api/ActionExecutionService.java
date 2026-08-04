package com.shreeai.os.platform.kernels.execution.api;

import com.shreeai.os.platform.kernels.execution.model.ActionState;
import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionResult;

import java.util.List;

/**
 * <b>ActionExecutionService</b>
 *
 * <p>Defines contracts for executing individual actions.
 * This interface provides the execution contract for atomic actions
 * without any execution behavior or implementation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines action execution contracts.</li>
 *   <li>Defines action cancellation contracts.</li>
 *   <li>Defines action retry contracts.</li>
 *   <li>Defines action status query contracts.</li>
 *   <li>Provides stable API boundaries for action execution.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only action execution contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-101, EIO-ARCH-001</p>
 *
 * <p><b>What This Interface Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not execute actions (defines contract only).</li>
 *   <li>Does not implement retry logic.</li>
 *   <li>Does not manage action state.</li>
 *   <li>Does not perform business logic.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface ActionExecutionService {

    /**
     * Executes an action based on the provided execution request.
     *
     * <p>This operation initiates the execution of a single action and returns
     * an execution identifier for tracking the action's progress and outcome.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Execution Flow:</b></p>
     * <ol>
     *   <li>Validate the execution request.</li>
     *   <li>Initialize action execution context.</li>
     *   <li>Begin action execution.</li>
     *   <li>Return execution identifier for tracking.</li>
     * </ol>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution requests.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return an execution identifier for tracking
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeAction(ExecutionRequest executionRequest);

    /**
     * Cancels an ongoing action execution.
     *
     * <p>This operation requests cancellation of the specified action execution.
     * The action may not cancel immediately if it is in a non-interruptible
     * state. The implementation should ensure graceful cancellation when possible.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Cancellation Behavior:</b></p>
     * <ul>
     *   <li>Requests cancellation of the specified execution.</li>
     *   <li>Does not guarantee immediate cancellation.</li>
     *   <li>Returns status indicating whether cancellation was accepted.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return {@code true} if cancellation was requested successfully, {@code false} otherwise
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    boolean cancelAction(String executionId);

    /**
     * Retries a failed action execution.
     *
     * <p>This operation retries a previously failed action execution. The retry
     * uses the same execution request parameters as the original execution.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Retry Behavior:</b></p>
     * <ul>
     *   <li>Retries the specified failed execution.</li>
     *   <li>Uses original execution parameters.</li>
     *   <li>Returns new execution identifier for the retry.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier to retry (must not be {@code null} or empty)
     * @return a new execution identifier for the retry
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    String retryAction(String executionId);

    /**
     * Queries the current status of an action execution.
     *
     * <p>This operation retrieves the current state and status information
     * for the specified action execution.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Status Information:</b></p>
     * <ul>
     *   <li>Current action state (PENDING, RUNNING, COMPLETED, FAILED, etc.).</li>
     *   <li>Execution progress information.</li>
     *   <li>Error information if failed.</li>
     *   <li>Timing information.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers at the same point in time.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the current action state
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    ActionState getActionStatus(String executionId);

    /**
     * Retrieves detailed execution result for a completed action.
     *
     * <p>This operation retrieves the complete execution result including
     * output data, metrics, and status information for a completed action.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Result Information:</b></p>
     * <ul>
     *   <li>Execution status and outcome.</li>
     *   <li>Result data produced by the action.</li>
     *   <li>Execution metrics (timing, retries, etc.).</li>
     *   <li>Error information if failed.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the execution result
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    ExecutionResult getActionResult(String executionId);

    /**
     * Lists all active action executions.
     *
     * <p>This operation retrieves all currently active (non-terminal) action
     * executions for monitoring and management purposes.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * at the same point in time.</p>
     *
     * @return an unmodifiable list of active execution identifiers
     */
    List<String> getActiveActions();
}