package platform.kernels.execution.api;

import java.util.List;
import java.util.Map;

/**
 * <b>ExecutionMonitoringService</b>
 *
 * <p>Defines runtime monitoring contracts for execution.
 * This interface provides monitoring contracts without any monitoring
 * implementation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines execution state monitoring contracts.</li>
 *   <li>Defines execution progress monitoring contracts.</li>
 *   <li>Defines execution metrics contracts.</li>
 *   <li>Defines active executions monitoring contracts.</li>
 *   <li>Defines execution health monitoring contracts.</li>
 *   <li>Provides stable API boundaries for execution monitoring.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only monitoring contracts.</li>
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
 *   <li>Does not monitor executions (defines contract only).</li>
 *   <li>Does not implement metrics collection.</li>
 *   <li>Does not perform health checks.</li>
 *   <li>Does not perform business logic.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface ExecutionMonitoringService {

    /**
     * Retrieves the current execution state for a given execution identifier.
     *
     * <p>This operation provides real-time execution state information including
     * current status, progress, and timing information.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>State Information:</b></p>
     * <ul>
     *   <li>Current execution status (PENDING, RUNNING, COMPLETED, FAILED, etc.).</li>
     *   <li>Execution progress percentage or metrics.</li>
     *   <li>Start time and elapsed time.</li>
     *   <li>Current execution phase or step.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers at the same point in time.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the current execution state
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    platform.kernels.execution.model.ExecutionStatus getExecutionState(String executionId);

    /**
     * Retrieves execution progress information.
     *
     * <p>This operation provides detailed progress information for the specified
     * execution, including percentage complete, current step, and estimated
     * time to completion.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Progress Information:</b></p>
     * <ul>
     *   <li>Progress percentage (0-100).</li>
     *   <li>Current step or phase.</li>
     *   <li>Total steps or phases.</li>
     *   <li>Estimated time to completion.</li>
     *   <li>Progress message or description.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers at the same point in time.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return a map containing progress information
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    Map<String, Object> getExecutionProgress(String executionId);

    /**
     * Retrieves execution metrics for a completed or ongoing execution.
     *
     * <p>This operation provides performance metrics for the specified execution
     * including timing, resource usage, and retry information.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Metrics Information:</b></p>
     * <ul>
     *   <li>Start time and end time.</li>
     *   <li>Total duration.</li>
     *   <li>Retry count.</li>
     *   <li>Resource usage (CPU, memory, etc.).</li>
     *   <li>Custom metrics defined by execution.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the execution metrics
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    platform.kernels.execution.model.ExecutionMetrics getExecutionMetrics(String executionId);

    /**
     * Lists all currently active executions.
     *
     * <p>This operation retrieves all currently active (non-terminal) executions
     * across all execution types (actions, tasks, workflows) for monitoring
     * and management purposes.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Active Execution Information:</b></p>
     * <ul>
     *   <li>Execution identifiers.</li>
     *   <li>Execution types (action, task, workflow).</li>
     *   <li>Current status.</li>
     *   <li>Start time.</li>
     *   <li>Elapsed time.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * at the same point in time.</p>
     *
     * @return an unmodifiable list of active execution identifiers
     */
    List<String> getActiveExecutions();

    /**
     * Checks the health of the execution system.
     *
     * <p>This operation performs a health check of the execution system and
     * returns the current health status. This is useful for monitoring and
     * operational awareness.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Health Information:</b></p>
     * <ul>
     *   <li>Overall health status (HEALTHY, DEGRADED, UNHEALTHY).</li>
     *   <li>Active execution count.</li>
     *   <li>Failed execution count.</li>
     *   <li>System resource availability.</li>
     *   <li>Error messages if unhealthy.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * at the same point in time.</p>
     *
     * @return a map containing health information
     */
    Map<String, Object> getExecutionHealth();

    /**
     * Retrieves execution history for a given execution identifier.
     *
     * <p>This operation retrieves the execution history including state changes,
     * retries, and significant events for the specified execution.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>History Information:</b></p>
     * <ul>
     *   <li>Timeline of state changes.</li>
     *   <li>Retry attempts and outcomes.</li>
     *   <li>Error events and resolutions.</li>
     *   <li>Significant execution milestones.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return an unmodifiable list of execution history events
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    List<Map<String, Object>> getExecutionHistory(String executionId);
}