package com.shreeai.os.platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionResult</b>
 *
 * <p>Represents the immutable outcome of execution.
 * This value object encapsulates the result of an execution operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution outcome.</li>
 *   <li>Provides execution metrics and status.</li>
 *   <li>Contains result data or error information.</li>
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
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param executionId  the execution identifier (must not be {@code null})
 * @param status       the execution status (must not be {@code null})
 * @param resultData   the result data (must not be {@code null})
 * @param metrics      the execution metrics (must not be {@code null})
 * @param completedAt  the completion timestamp (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionResult {

    private final ExecutionId executionId;
    private final ExecutionStatus status;
    private final Map<String, Object> resultData;
    private final ExecutionMetrics metrics;
    private final java.time.Instant completedAt;

    /**
     * Constructs an {@code ExecutionResult} with the specified parameters.
     *
     * @param executionId the execution identifier (must not be {@code null})
     * @param status      the execution status (must not be {@code null})
     * @param resultData  the result data (must not be {@code null})
     * @param metrics     the execution metrics (must not be {@code null})
     * @param completedAt the completion timestamp (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ExecutionResult(
            ExecutionId executionId,
            ExecutionStatus status,
            Map<String, Object> resultData,
            ExecutionMetrics metrics,
            java.time.Instant completedAt) {
        if (executionId == null) {
            throw new IllegalArgumentException("ExecutionResult executionId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ExecutionResult status must not be null");
        }
        if (resultData == null) {
            throw new IllegalArgumentException("ExecutionResult resultData must not be null");
        }
        if (metrics == null) {
            throw new IllegalArgumentException("ExecutionResult metrics must not be null");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("ExecutionResult completedAt must not be null");
        }

        this.executionId = executionId;
        this.status = status;
        this.resultData = Collections.unmodifiableMap(new HashMap<>(resultData));
        this.metrics = metrics;
        this.completedAt = completedAt;
    }

    /**
     * Returns the execution identifier.
     *
     * @return the execution identifier
     */
    public ExecutionId executionId() {
        return executionId;
    }

    /**
     * Returns the execution status.
     *
     * @return the execution status
     */
    public ExecutionStatus status() {
        return status;
    }

    /**
     * Returns an unmodifiable view of the result data.
     *
     * <p>The returned map is unmodifiable and reflects the result data at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of result data
     */
    public Map<String, Object> resultData() {
        return resultData;
    }

    /**
     * Returns the execution metrics.
     *
     * @return the execution metrics
     */
    public ExecutionMetrics metrics() {
        return metrics;
    }

    /**
     * Returns the completion timestamp.
     *
     * @return the completion timestamp
     */
    public java.time.Instant completedAt() {
        return completedAt;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionResult} instances are equal if they have the same
     * execution identifier, status, result data, metrics, and completion timestamp.</p>
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
        ExecutionResult that = (ExecutionResult) obj;
        return Objects.equals(executionId, that.executionId) &&
                status == that.status &&
                Objects.equals(resultData, that.resultData) &&
                Objects.equals(metrics, that.metrics) &&
                Objects.equals(completedAt, that.completedAt);
    }

    /**
     * Returns a hash code value for this {@code ExecutionResult}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(executionId, status, resultData, metrics, completedAt);
    }

    /**
     * Returns a string representation of this {@code ExecutionResult}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionResult{" +
                "executionId=" + executionId +
                ", status=" + status +
                ", resultData=" + resultData +
                ", metrics=" + metrics +
                ", completedAt=" + completedAt +
                '}';
    }
}