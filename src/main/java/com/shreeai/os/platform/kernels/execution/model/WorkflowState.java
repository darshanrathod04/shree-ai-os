package com.shreeai.os.platform.kernels.execution.model;

import java.util.Objects;

/**
 * <b>WorkflowState</b>
 *
 * <p>Represents immutable workflow state.
 * This value object encapsulates the state of a workflow execution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents workflow lifecycle states.</li>
 *   <li>Provides clear workflow semantics.</li>
 *   <li>Enables state-based workflow management.</li>
 *   <li>Contains no orchestration logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param workflowId      the workflow identifier (must not be {@code null})
 * @param executionStage  the current execution stage (must not be {@code null})
 * @param taskReferences  references to tasks in the workflow (must not be {@code null})
 * @param metadata        additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class WorkflowState {

    private final String workflowId;
    private final String executionStage;
    private final java.util.List<String> taskReferences;
    private final java.util.Map<String, Object> metadata;

    /**
     * Constructs a {@code WorkflowState} with the specified parameters.
     *
     * @param workflowId      the workflow identifier (must not be {@code null} or empty)
     * @param executionStage  the current execution stage (must not be {@code null} or empty)
     * @param taskReferences  references to tasks in the workflow (must not be {@code null})
     * @param metadata        additional metadata (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    public WorkflowState(
            String workflowId,
            String executionStage,
            java.util.List<String> taskReferences,
            java.util.Map<String, Object> metadata) {
        if (workflowId == null || workflowId.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkflowState workflowId must not be null or empty");
        }
        if (executionStage == null || executionStage.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkflowState executionStage must not be null or empty");
        }
        if (taskReferences == null) {
            throw new IllegalArgumentException("WorkflowState taskReferences must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("WorkflowState metadata must not be null");
        }

        this.workflowId = workflowId;
        this.executionStage = executionStage;
        this.taskReferences = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(taskReferences));
        this.metadata = java.util.Collections.unmodifiableMap(new java.util.HashMap<>(metadata));
    }

    /**
     * Returns the workflow identifier.
     *
     * @return the workflow identifier
     */
    public String workflowId() {
        return workflowId;
    }

    /**
     * Returns the current execution stage.
     *
     * @return the execution stage
     */
    public String executionStage() {
        return executionStage;
    }

    /**
     * Returns an unmodifiable list of task references.
     *
     * <p>The returned list is unmodifiable and reflects the task references at the
     * time of this call.</p>
     *
     * @return an unmodifiable list of task references
     */
    public java.util.List<String> taskReferences() {
        return taskReferences;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * <p>The returned map is unmodifiable and reflects the metadata at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public java.util.Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code WorkflowState} instances are equal if they have the same
     * workflow identifier, execution stage, task references, and metadata.</p>
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
        WorkflowState that = (WorkflowState) obj;
        return Objects.equals(workflowId, that.workflowId) &&
                Objects.equals(executionStage, that.executionStage) &&
                Objects.equals(taskReferences, that.taskReferences) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for this {@code WorkflowState}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(workflowId, executionStage, taskReferences, metadata);
    }

    /**
     * Returns a string representation of this {@code WorkflowState}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "WorkflowState{" +
                "workflowId='" + workflowId + '\'' +
                ", executionStage='" + executionStage + '\'' +
                ", taskReferences=" + taskReferences +
                ", metadata=" + metadata +
                '}';
    }
}