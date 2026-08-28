package com.shreeai.os.platform.kernels.execution.engine;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionResult;
import com.shreeai.os.platform.kernels.execution.model.ExecutionStatus;
import com.shreeai.os.platform.kernels.execution.model.ExecutionMetrics;
import com.shreeai.os.platform.kernels.execution.model.WorkflowState;
import com.shreeai.os.platform.kernels.execution.model.ActionState;
import com.shreeai.os.platform.kernels.execution.model.ExecutionSnapshot;

/**
 * <b>ExecutionProcessingResult</b>
 *
 * <p>Represents the immutable outcome of execution processing.
 * This value object encapsulates the deterministic computation results from the Engine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution processing outcome.</li>
 *   <li>Provides immutable processing results.</li>
 *   <li>Contains processing metadata.</li>
 *   <li>Contains no processing logic.</li>
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
 * <p><b>Ownership:</b> Execution Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-106, EIO-ARCH-001</p>
 *
 * @param successful     whether the processing was successful
 * @param processedAt    the timestamp when processing was performed (must not be {@code null})
 * @param metadata       additional processing metadata (must not be {@code null})
 * @param executionRequest the original execution request (must not be {@code null})
 * @param executionResult  the execution result (may be {@code null})
 * @param executionStatus  the execution status (may be {@code null})
 * @param executionMetrics the execution metrics (may be {@code null})
 * @param workflowState    the workflow state (may be {@code null})
 * @param actionState      the action state (may be {@code null})
 * @param executionSnapshot the execution snapshot (may be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionProcessingResult {

    private final boolean successful;
    private final Instant processedAt;
    private final Map<String, Object> metadata;
    private final ExecutionRequest executionRequest;
    private final ExecutionResult executionResult;
    private final ExecutionStatus executionStatus;
    private final ExecutionMetrics executionMetrics;
    private final WorkflowState workflowState;
    private final ActionState actionState;
    private final ExecutionSnapshot executionSnapshot;

    /**
     * Constructs an {@code ExecutionProcessingResult} with the specified parameters.
     *
     * @param successful        whether the processing was successful
     * @param processedAt       the timestamp when processing was performed (must not be {@code null})
     * @param metadata          additional processing metadata (must not be {@code null})
     * @param executionRequest  the original execution request (must not be {@code null})
     * @param executionResult   the execution result (may be {@code null})
     * @param executionStatus   the execution status (may be {@code null})
     * @param executionMetrics  the execution metrics (may be {@code null})
     * @param workflowState     the workflow state (may be {@code null})
     * @param actionState       the action state (may be {@code null})
     * @param executionSnapshot the execution snapshot (may be {@code null})
     * @throws IllegalArgumentException if processedAt, metadata, or executionRequest is {@code null}
     */
    public ExecutionProcessingResult(
            boolean successful,
            Instant processedAt,
            Map<String, Object> metadata,
            ExecutionRequest executionRequest,
            ExecutionResult executionResult,
            ExecutionStatus executionStatus,
            ExecutionMetrics executionMetrics,
            WorkflowState workflowState,
            ActionState actionState,
            ExecutionSnapshot executionSnapshot) {
        if (processedAt == null) {
            throw new IllegalArgumentException("ExecutionProcessingResult processedAt must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ExecutionProcessingResult metadata must not be null");
        }
        if (executionRequest == null) {
            throw new IllegalArgumentException("ExecutionProcessingResult executionRequest must not be null");
        }

        this.successful = successful;
        this.processedAt = processedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.executionRequest = executionRequest;
        this.executionResult = executionResult;
        this.executionStatus = executionStatus;
        this.executionMetrics = executionMetrics;
        this.workflowState = workflowState;
        this.actionState = actionState;
        this.executionSnapshot = executionSnapshot;
    }

    /**
     * Returns whether the processing was successful.
     *
     * @return {@code true} if processing was successful, {@code false} otherwise
     */
    public boolean successful() {
        return successful;
    }

    /**
     * Returns the timestamp when processing was performed.
     *
     * @return the processing timestamp
     */
    public Instant processedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable view of the processing metadata.
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
     * Returns the original execution request.
     *
     * @return the execution request
     */
    public ExecutionRequest executionRequest() {
        return executionRequest;
    }

    /**
     * Returns the execution result, if available.
     *
     * @return the execution result, or {@code null} if not available
     */
    public ExecutionResult executionResult() {
        return executionResult;
    }

    /**
     * Returns the execution status, if available.
     *
     * @return the execution status, or {@code null} if not available
     */
    public ExecutionStatus executionStatus() {
        return executionStatus;
    }

    /**
     * Returns the execution metrics, if available.
     *
     * @return the execution metrics, or {@code null} if not available
     */
    public ExecutionMetrics executionMetrics() {
        return executionMetrics;
    }

    /**
     * Returns the workflow state, if available.
     *
     * @return the workflow state, or {@code null} if not available
     */
    public WorkflowState workflowState() {
        return workflowState;
    }

    /**
     * Returns the action state, if available.
     *
     * @return the action state, or {@code null} if not available
     */
    public ActionState actionState() {
        return actionState;
    }

    /**
     * Returns the execution snapshot, if available.
     *
     * @return the execution snapshot, or {@code null} if not available
     */
    public ExecutionSnapshot executionSnapshot() {
        return executionSnapshot;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionProcessingResult} instances are equal if they have the same
     * success status, timestamp, metadata, and all optional fields.</p>
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
        ExecutionProcessingResult that = (ExecutionProcessingResult) obj;
        return successful == that.successful &&
                Objects.equals(processedAt, that.processedAt) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(executionRequest, that.executionRequest) &&
                Objects.equals(executionResult, that.executionResult) &&
                executionStatus == that.executionStatus &&
                Objects.equals(executionMetrics, that.executionMetrics) &&
                Objects.equals(workflowState, that.workflowState) &&
                Objects.equals(actionState, that.actionState) &&
                Objects.equals(executionSnapshot, that.executionSnapshot);
    }

    /**
     * Returns a hash code value for this {@code ExecutionProcessingResult}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, processedAt, metadata, executionRequest, executionResult,
                executionStatus, executionMetrics, workflowState, actionState, executionSnapshot);
    }

    /**
     * Returns a string representation of this {@code ExecutionProcessingResult}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionProcessingResult{" +
                "successful=" + successful +
                ", processedAt=" + processedAt +
                ", metadata=" + metadata +
                ", executionRequest=" + executionRequest +
                ", executionResult=" + executionResult +
                ", executionStatus=" + executionStatus +
                ", executionMetrics=" + executionMetrics +
                ", workflowState=" + workflowState +
                ", actionState=" + actionState +
                ", executionSnapshot=" + executionSnapshot +
                '}';
    }
}