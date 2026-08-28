package com.shreeai.os.platform.kernels.execution.api;

import com.shreeai.os.platform.kernels.execution.model.ActionState;
import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionResult;

import java.util.List;
import java.util.Map;

/**
 * <b>TaskExecutionService</b>
 *
 * <p>Defines contracts for execution of Planning tasks.
 * This interface provides the execution contract for planned tasks
 * without any execution implementation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines task execution contracts.</li>
 *   <li>Defines task lifecycle management contracts.</li>
 *   <li>Defines task control contracts (execute, skip, complete, fail).</li>
 *   <li>Provides stable API boundaries for task execution.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only task execution contracts.</li>
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
 *   <li>Does not execute tasks (defines contract only).</li>
 *   <li>Does not implement task scheduling.</li>
 *   <li>Does not manage task state.</li>
 *   <li>Does not perform business logic.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface TaskExecutionService {

    /**
     * Executes a planned task based on the provided execution request.
     *
     * <p>This operation initiates execution of a planned task and returns
     * a task execution identifier for tracking the task's progress and outcome.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Execution Flow:</b></p>
     * <ol>
     *   <li>Validate the execution request.</li>
     *   <li>Initialize task execution context.</li>
     *   <li>Begin task execution.</li>
     *   <li>Return task execution identifier for tracking.</li>
     * </ol>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution requests.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a task execution identifier for tracking
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeTask(ExecutionRequest executionRequest);

    /**
     * Skips a planned task execution.
     *
     * <p>This operation skips the specified task execution. The task will be
     * marked as skipped and will not execute. This is useful for conditional
     * task execution where certain tasks may be skipped based on runtime
     * conditions.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Skip Behavior:</b></p>
     * <ul>
     *   <li>Marks the specified task as skipped.</li>
     *   <li>Does not execute the task.</li>
     *   <li>Returns status indicating whether skip was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical task identifiers.</p>
     *
     * @param taskId the task execution identifier (must not be {@code null} or empty)
     * @return {@code true} if skip was successful, {@code false} otherwise
     * @throws IllegalArgumentException if taskId is {@code null} or empty
     */
    boolean skipTask(String taskId);

    /**
     * Marks a task execution as completed.
     *
     * <p>This operation marks the specified task execution as completed with
     * the provided result data. This is typically called by the task execution
     * implementation when the task finishes successfully.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Completion Behavior:</b></p>
     * <ul>
     *   <li>Marks the specified task as completed.</li>
     *   <li>Associates result data with the task execution.</li>
     *   <li>Returns status indicating whether completion was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical task identifiers and result data.</p>
     *
     * @param taskId     the task execution identifier (must not be {@code null} or empty)
     * @param resultData the result data from task execution (must not be {@code null})
     * @return {@code true} if completion was successful, {@code false} otherwise
     * @throws IllegalArgumentException if taskId or resultData is {@code null}
     */
    boolean completeTask(String taskId, Map<String, Object> resultData);

    /**
     * Marks a task execution as failed.
     *
     * <p>This operation marks the specified task execution as failed with
     * the provided error information. This is typically called by the task
     * execution implementation when the task encounters an error.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Failure Behavior:</b></p>
     * <ul>
     *   <li>Marks the specified task as failed.</li>
     *   <li>Associates error information with the task execution.</li>
     *   <li>Returns status indicating whether failure recording was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical task identifiers and error information.</p>
     *
     * @param taskId        the task execution identifier (must not be {@code null} or empty)
     * @param errorMessage  the error message describing the failure (must not be {@code null})
     * @param errorDetails  additional error details (must not be {@code null})
     * @return {@code true} if failure recording was successful, {@code false} otherwise
     * @throws IllegalArgumentException if taskId, errorMessage, or errorDetails is {@code null}
     */
    boolean failTask(String taskId, String errorMessage, Map<String, Object> errorDetails);

    /**
     * Retrieves the current status of a task execution.
     *
     * <p>This operation retrieves the current state and status information
     * for the specified task execution.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Status Information:</b></p>
     * <ul>
     *   <li>Current task state (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, RETRYING, SKIPPED).</li>
     *   <li>Execution progress information.</li>
     *   <li>Error information if failed.</li>
     *   <li>Timing information.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical task identifiers at the same point in time.</p>
     *
     * @param taskId the task execution identifier (must not be {@code null} or empty)
     * @return the current task state
     * @throws IllegalArgumentException if taskId is {@code null} or empty
     */
    ActionState getTaskStatus(String taskId);

    /**
     * Retrieves detailed execution result for a completed task.
     *
     * <p>This operation retrieves the complete execution result including
     * output data, metrics, and status information for a completed task.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Result Information:</b></p>
     * <ul>
     *   <li>Execution status and outcome.</li>
     *   <li>Result data produced by the task.</li>
     *   <li>Execution metrics (timing, retries, etc.).</li>
     *   <li>Error information if failed.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical task identifiers.</p>
     *
     * @param taskId the task execution identifier (must not be {@code null} or empty)
     * @return the execution result
     * @throws IllegalArgumentException if taskId is {@code null} or empty
     */
    ExecutionResult getTaskResult(String taskId);

    /**
     * Lists all active task executions.
     *
     * <p>This operation retrieves all currently active (non-terminal) task
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
     * @return an unmodifiable list of active task execution identifiers
     */
    List<String> getActiveTasks();
}