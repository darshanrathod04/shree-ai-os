package platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionContext</b>
 *
 * <p>Represents the immutable execution context.
 * This value object encapsulates the environment and parameters in which
 * execution occurs.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution environment information.</li>
 *   <li>Provides execution boundaries and constraints.</li>
 *   <li>Links execution to planning and cognitive context.</li>
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
 * @param executionId   the execution identifier (must not be {@code null})
 * @param planId        the associated plan identifier (must not be {@code null})
 * @param objectiveId   the associated objective identifier (must not be {@code null})
 * @param contextData   additional context data (must not be {@code null})
 * @param priority      the execution priority
 *
 * @since 1.0
 */
public final class ExecutionContext {

    private final ExecutionId executionId;
    private final String planId;
    private final String objectiveId;
    private final Map<String, Object> contextData;
    private final int priority;

    /**
     * Constructs an {@code ExecutionContext} with the specified parameters.
     *
     * @param executionId the execution identifier (must not be {@code null})
     * @param planId      the associated plan identifier (must not be {@code null} or empty)
     * @param objectiveId the associated objective identifier (must not be {@code null} or empty)
     * @param contextData additional context data (must not be {@code null})
     * @param priority    the execution priority
     * @throws IllegalArgumentException if executionId, planId, objectiveId, or contextData is {@code null}
     * @throws IllegalArgumentException if planId or objectiveId is empty
     */
    public ExecutionContext(
            ExecutionId executionId,
            String planId,
            String objectiveId,
            Map<String, Object> contextData,
            int priority) {
        if (executionId == null) {
            throw new IllegalArgumentException("ExecutionContext executionId must not be null");
        }
        if (planId == null || planId.trim().isEmpty()) {
            throw new IllegalArgumentException("ExecutionContext planId must not be null or empty");
        }
        if (objectiveId == null || objectiveId.trim().isEmpty()) {
            throw new IllegalArgumentException("ExecutionContext objectiveId must not be null or empty");
        }
        if (contextData == null) {
            throw new IllegalArgumentException("ExecutionContext contextData must not be null");
        }

        this.executionId = executionId;
        this.planId = planId;
        this.objectiveId = objectiveId;
        this.contextData = Collections.unmodifiableMap(new HashMap<>(contextData));
        this.priority = priority;
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
     * Returns the associated plan identifier.
     *
     * @return the plan identifier
     */
    public String planId() {
        return planId;
    }

    /**
     * Returns the associated objective identifier.
     *
     * @return the objective identifier
     */
    public String objectiveId() {
        return objectiveId;
    }

    /**
     * Returns an unmodifiable view of the context data.
     *
     * <p>The returned map is unmodifiable and reflects the context data at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of context data
     */
    public Map<String, Object> contextData() {
        return contextData;
    }

    /**
     * Returns the execution priority.
     *
     * @return the execution priority
     */
    public int priority() {
        return priority;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionContext} instances are equal if they have the same
     * execution identifier, plan identifier, objective identifier, context data,
     * and priority.</p>
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
        ExecutionContext that = (ExecutionContext) obj;
        return priority == that.priority &&
                Objects.equals(executionId, that.executionId) &&
                Objects.equals(planId, that.planId) &&
                Objects.equals(objectiveId, that.objectiveId) &&
                Objects.equals(contextData, that.contextData);
    }

    /**
     * Returns a hash code value for this {@code ExecutionContext}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(executionId, planId, objectiveId, contextData, priority);
    }

    /**
     * Returns a string representation of this {@code ExecutionContext}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionContext{" +
                "executionId=" + executionId +
                ", planId='" + planId + '\'' +
                ", objectiveId='" + objectiveId + '\'' +
                ", contextData=" + contextData +
                ", priority=" + priority +
                '}';
    }
}