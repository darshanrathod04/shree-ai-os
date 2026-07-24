package platform.kernels.execution.api;

import java.util.List;
import java.util.Map;

/**
 * <b>ExecutionRecoveryService</b>
 *
 * <p>Defines execution recovery contracts.
 * This interface provides recovery contracts without any recovery
 * implementation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines retry request contracts.</li>
 *   <li>Defines rollback request contracts.</li>
 *   <li>Defines compensation request contracts.</li>
 *   <li>Defines recovery strategy selection contracts.</li>
 *   <li>Provides stable API boundaries for execution recovery.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only recovery contracts.</li>
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
 *   <li>Does not execute recovery (defines contract only).</li>
 *   <li>Does not implement retry logic.</li>
 *   <li>Does not implement rollback logic.</li>
 *   <li>Does not implement compensation logic.</li>
 *   <li>Does not perform business logic.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface ExecutionRecoveryService {

    /**
     * Retries a failed execution.
     *
     * <p>This operation retries a previously failed execution using the
     * specified recovery strategy. The retry uses the original execution
     * request parameters.</p>
 *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Retry Behavior:</b></p>
     * <ul>
     *   <li>Retries the specified failed execution.</li>
     *   <li>Uses the specified recovery strategy.</li>
     *   <li>Returns a new execution identifier for the retry.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers and recovery strategies.</p>
     *
     * @param executionId     the execution identifier to retry (must not be {@code null} or empty)
     * @param recoveryStrategy the recovery strategy to apply (must not be {@code null})
     * @return a new execution identifier for the retry
     * @throws IllegalArgumentException if executionId or recoveryStrategy is {@code null}
     */
    String retryExecution(String executionId, platform.kernels.execution.model.RecoveryStrategy recoveryStrategy);

    /**
     * Rolls back a failed or partial execution.
     *
     * <p>This operation rolls back the specified execution to a previous
     * state. Rollback is used to undo partial or failed executions and
     * restore the system to a consistent state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Rollback Behavior:</b></p>
     * <ul>
     *   <li>Rolls back the specified execution.</li>
     *   <li>Restores system to a previous consistent state.</li>
     *   <li>Returns status indicating whether rollback was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier to rollback (must not be {@code null} or empty)
     * @return {@code true} if rollback was successful, {@code false} otherwise
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    boolean rollbackExecution(String executionId);

    /**
     * Executes compensation logic for a failed execution.
     *
     * <p>This operation executes compensation logic to undo partial execution
     * effects. Compensation is used when rollback is not possible or when
     * specific compensation actions are required.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Compensation Behavior:</b></p>
     * <ul>
     *   <li>Executes compensation logic for the specified execution.</li>
     *   <li>Undoes partial execution effects.</li>
     *   <li>Returns status indicating whether compensation was successful.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier to compensate (must not be {@code null} or empty)
     * @return {@code true} if compensation was successful, {@code false} otherwise
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    boolean compensateExecution(String executionId);

    /**
     * Selects the appropriate recovery strategy for a failed execution.
     *
     * <p>This operation analyzes the failed execution and recommends or selects
     * the most appropriate recovery strategy based on execution context,
     * failure type, and system state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Strategy Selection:</b></p>
     * <ul>
     *   <li>Analyzes execution failure context.</li>
     *   <li>Evaluates available recovery strategies.</li>
     *   <li>Recommends optimal recovery strategy.</li>
     *   <li>Returns selected strategy with rationale.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution contexts and failure states.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the recommended {@link ExecutionTypes.RecoveryStrategy}
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    platform.kernels.execution.model.RecoveryStrategy selectRecoveryStrategy(String executionId);

    /**
     * Retrieves recovery options for a failed execution.
     *
     * <p>This operation retrieves all available recovery options for the
     * specified failed execution, including possible strategies and their
     * associated risks and benefits.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Recovery Options:</b></p>
     * <ul>
     *   <li>Available recovery strategies.</li>
     *   <li>Risk assessment for each strategy.</li>
     *   <li>Estimated recovery time.</li>
     *   <li>Prerequisites for each strategy.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return a map of recovery options
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    Map<String, Object> getRecoveryOptions(String executionId);

    /**
     * Checks if an execution is recoverable.
     *
     * <p>This operation determines whether the specified execution can be
     * recovered based on its current state, failure type, and system
     * capabilities.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Recoverability Criteria:</b></p>
     * <ul>
     *   <li>Execution state allows recovery.</li>
     *   <li>Failure type is recoverable.</li>
     *   <li>System resources are available.</li>
     *   <li>Recovery prerequisites are met.</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe and can be
     * called concurrently from multiple threads.</p>
     *
     * <p><b>Determinism:</b> This operation produces deterministic results
     * for identical execution identifiers.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return {@code true} if the execution is recoverable, {@code false} otherwise
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    boolean isRecoverable(String executionId);

    /**
     * Lists all executions that require recovery.
     *
     * <p>This operation retrieves all failed executions that are eligible
     * for recovery. This is useful for operational monitoring and manual
     * recovery management.</p>
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
     * @return an unmodifiable list of execution identifiers requiring recovery
     */
    List<String> getRecoverableExecutions();
}