package com.shreeai.os.platform.kernels.execution.api;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionResult;
import com.shreeai.os.platform.kernels.execution.model.WorkflowState;

import java.util.List;

/**
 * <b>WorkflowExecutionService</b>
 *
 * <p>Defines contracts for workflow execution.
 * This interface provides the execution contract for workflows
 * without any orchestration algorithms or workflow engine implementation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines workflow execution contracts.</li>
 *   <li>Defines workflow lifecycle management contracts.</li>
 *   <li>Defines workflow control contracts (pause, resume, stop).</li>
 *   <li>Provides stable API boundaries for workflow execution.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only workflow execution contracts.</li>
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
 *   <li>Does not execute workflows (defines contract only).</li>
 *   <li>Does not implement orchestration algorithms.</li>
 *   <li>Does not implement workflow engine logic.</li>
 *   <li>Does not perform business logic.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface WorkflowExecutionService {

    /**
     * Executes a workflow based on the provided execution request.
     *
     * <p>This operation initiates workflow execution and returns a workflow
     * execution identifier for tracking the workflow's progress and outcome.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Execution Flow:</b></p>
     * <ol>
     *   <li>Validate the execution request.</li>
     *   <li>Initialize workflow execution context.</li>
     *   <li>Begin workflow execution.</li>
     *   <li>Return workflow execution identifier for tracking.</li>
     * </ol>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution requests.</p>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a workflow execution identifier for tracking
     * @throws IllegalArgumentException if executionRequest is {@code null}
     */
    String executeWorkflow(ExecutionRequest executionRequest);

    /**
     * Pauses an ongoing workflow execution.
     *
     * <p>This operation requests pausing of the specified workflow execution.
     * The workflow will pause at the next safe checkpoint. Currently executing
     * actions may complete before the workflow pauses.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Pause Behavior:</b></p>
     * <ul>
     *   <li>Requests pausing of the specified workflow.</li>
     *   <li>Does not guarantee immediate pausing.</li>
     *   <li>Returns status indicating whether pause was accepted.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical workflow identifiers.</p>
     *
     * @param workflowId the workflow execution identifier (must not be {@code null} or empty)
     * @return {@code true} if pause was requested successfully, {@code false} otherwise
     * @throws IllegalArgumentException if workflowId is {@code null} or empty
     */
    boolean pauseWorkflow(String workflowId);

    /**
     * Resumes a paused workflow execution.
     *
     * <p>This operation resumes a previously paused workflow execution from
     * the point where it was paused.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Resume Behavior:</b></p>
     * <ul>
     *   <li>Resumes the specified paused workflow.</li>
     *   <li>Continues from the last checkpoint.</li>
     *   <li>Returns status indicating whether resume was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical workflow identifiers.</p>
     *
     * @param workflowId the workflow execution identifier (must not be {@code null} or empty)
     * @return {@code true} if resume was successful, {@code false} otherwise
     * @throws IllegalArgumentException if workflowId is {@code null} or empty
     */
    boolean resumeWorkflow(String workflowId);

    /**
     * Stops a workflow execution.
     *
     * <p>This operation requests immediate stopping of the specified workflow
     * execution. Unlike pause, stop is permanent and cannot be resumed.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Stop Behavior:</b></p>
     * <ul>
     *   <li>Requests immediate stopping of the specified workflow.</li>
     *   <li>Terminates all executing actions in the workflow.</li>
     *   <li>Returns status indicating whether stop was accepted.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical workflow identifiers.</p>
     *
     * @param workflowId the workflow execution identifier (must not be {@code null} or empty)
     * @return {@code true} if stop was requested successfully, {@code false} otherwise
     * @throws IllegalArgumentException if workflowId is {@code null} or empty
     */
    boolean stopWorkflow(String workflowId);

    /**
     * Retrieves the current state of a workflow execution.
     *
     * <p>This operation retrieves the current state and status information
     * for the specified workflow execution.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>State Information:</b></p>
     * <ul>
     *   <li>Current workflow state (CREATED, RUNNING, PAUSED, COMPLETED, FAILED, STOPPED, WAITING).</li>
     *   <li>Execution progress information.</li>
     *   <li>Current action being executed.</li>
     *   <li>Error information if failed.</li>
     *   <li>Timing information.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical workflow identifiers at the same point in time.</p>
     *
     * @param workflowId the workflow execution identifier (must not be {@code null} or empty)
     * @return the current workflow state
     * @throws IllegalArgumentException if workflowId is {@code null} or empty
     */
    WorkflowState getWorkflowState(String workflowId);

    /**
     * Retrieves detailed execution result for a completed workflow.
     *
     * <p>This operation retrieves the complete execution result including
     * output data, metrics, and status information for a completed workflow.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Result Information:</b></p>
     * <ul>
     *   <li>Execution status and outcome.</li>
     *   <li>Result data produced by the workflow.</li>
     *   <li>Execution metrics (timing, retries, etc.).</li>
     *   <li>List of executed actions and their results.</li>
     *   <li>Error information if failed.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical workflow identifiers.</p>
     *
     * @param workflowId the workflow execution identifier (must not be {@code null} or empty)
     * @return the execution result
     * @throws IllegalArgumentException if workflowId is {@code null} or empty
     */
    ExecutionResult getWorkflowResult(String workflowId);

    /**
     * Lists all active workflow executions.
     *
     * <p>This operation retrieves all currently active (non-terminal) workflow
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
     * @return an unmodifiable list of active workflow execution identifiers
     */
    List<String> getActiveWorkflows();
}