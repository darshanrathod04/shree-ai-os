package platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionSnapshot</b>
 *
 * <p>Represents an immutable snapshot of execution state.
 * This value object provides a historical representation of execution at a
 * point in time.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Captures execution state at a point in time.</li>
 *   <li>Provides historical execution representation.</li>
 *   <li>Enables execution state inspection.</li>
 *   <li>Contains no execution behavior.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param executionRequest  the execution request (must not be {@code null})
 * @param executionResult   the execution result (must not be {@code null})
 * @param workflowState     the workflow state (must not be {@code null})
 * @param actionStates      the action states map (must not be {@code null})
 * @param timestamp         the snapshot timestamp (must not be {@code null})
 * @param metadata          additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionSnapshot {

    private final ExecutionRequest executionRequest;
    private final ExecutionResult executionResult;
    private final WorkflowState workflowState;
    private final Map<String, ActionState> actionStates;
    private final java.time.Instant timestamp;
    private final Map<String, Object> metadata;

    /**
     * Constructs an {@code ExecutionSnapshot} with the specified parameters.
     *
     * @param executionRequest the execution request (must not be {@code null})
     * @param executionResult  the execution result (must not be {@code null})
     * @param workflowState    the workflow state (must not be {@code null})
     * @param actionStates     the action states map (must not be {@code null})
     * @param timestamp        the snapshot timestamp (must not be {@code null})
     * @param metadata         additional metadata (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ExecutionSnapshot(
            ExecutionRequest executionRequest,
            ExecutionResult executionResult,
            WorkflowState workflowState,
            Map<String, ActionState> actionStates,
            java.time.Instant timestamp,
            Map<String, Object> metadata) {
        if (executionRequest == null) {
            throw new IllegalArgumentException("ExecutionSnapshot executionRequest must not be null");
        }
        if (executionResult == null) {
            throw new IllegalArgumentException("ExecutionSnapshot executionResult must not be null");
        }
        if (workflowState == null) {
            throw new IllegalArgumentException("ExecutionSnapshot workflowState must not be null");
        }
        if (actionStates == null) {
            throw new IllegalArgumentException("ExecutionSnapshot actionStates must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("ExecutionSnapshot timestamp must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ExecutionSnapshot metadata must not be null");
        }

        this.executionRequest = executionRequest;
        this.executionResult = executionResult;
        this.workflowState = workflowState;
        this.actionStates = Collections.unmodifiableMap(new HashMap<>(actionStates));
        this.timestamp = timestamp;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns the execution request.
     *
     * @return the execution request
     */
    public ExecutionRequest executionRequest() {
        return executionRequest;
    }

    /**
     * Returns the execution result.
     *
     * @return the execution result
     */
    public ExecutionResult executionResult() {
        return executionResult;
    }

    /**
     * Returns the workflow state.
     *
     * @return the workflow state
     */
    public WorkflowState workflowState() {
        return workflowState;
    }

    /**
     * Returns an unmodifiable view of the action states.
     *
     * <p>The returned map is unmodifiable and reflects the action states at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of action states
     */
    public Map<String, ActionState> actionStates() {
        return actionStates;
    }

    /**
     * Returns the snapshot timestamp.
     *
     * @return the timestamp
     */
    public java.time.Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * <p>The returned map is unmodifiable and reflects the metadata at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionSnapshot} instances are equal if they have the same
     * execution request, result, workflow state, action states, timestamp, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionSnapshot that = (ExecutionSnapshot) obj;
        return Objects.equals(executionRequest, that.executionRequest) &&
                Objects.equals(executionResult, that.executionResult) &&
                Objects.equals(workflowState, that.workflowState) &&
                Objects.equals(actionStates, that.actionStates) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for this {@code ExecutionSnapshot}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(executionRequest, executionResult, workflowState, actionStates, timestamp, metadata);
    }

    /**
     * Returns a string representation of this {@code ExecutionSnapshot}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionSnapshot{" +
                "executionRequest=" + executionRequest +
                ", executionResult=" + executionResult +
                ", workflowState=" + workflowState +
                ", actionStates=" + actionStates +
                ", timestamp=" + timestamp +
                ", metadata=" + metadata +
                '}';
    }
}