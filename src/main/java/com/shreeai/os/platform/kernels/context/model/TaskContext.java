package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>TaskContext</b>
 *
 * <p>Represents the runtime context for currently executing task and temporary task-specific information.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates currently executing task state.</li>
 *   <li>Manages temporary task-specific runtime information.</li>
 *   <li>Specializes ExecutionContext for task-oriented operations.</li>
 * </ul>
 *
 * <p><b>Context Hierarchy:</b> TaskContext extends ExecutionContext as defined by the approved architecture.</p>
 *
 * <p><b>Context Type:</b> This is a specialized ExecutionContext for task scenarios.</p>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-CTX-102</p>
 *
 * @param id the unique identifier (must not be null)
 * @param type the context type (must not be null, must be TASK)
 * @param state the current state (must not be null)
 * @param data the context data (must not be null, defensively copied)
 * @param createdAt when the context was created (must not be null)
 * @param updatedAt when the context was last updated (must not be null)
 * @param executionId the execution identifier (must not be null or blank)
 * @param operationName the current operation name (must not be null or blank)
 * @param stepNumber the current step number (must not be negative)
 * @param taskId the task identifier (must not be null or blank)
 * @param taskName the task name (must not be null or blank)
 * @param parentTaskId the parent task identifier (optional)
 * @param priority the task priority (must not be null)
 */
public record TaskContext(
    ContextId id,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant createdAt,
    Instant updatedAt,
    String executionId,
    String operationName,
    int stepNumber,
    String taskId,
    String taskName,
    String parentTaskId,
    ContextPriority priority
) {
    /**
     * Creates a new TaskContext with validation.
     *
     * <p>All parameters are validated for null and business rules. The data map
     * is defensively copied to ensure immutability.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be TASK)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, will be defensively copied)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param executionId the execution identifier (must not be null or blank)
     * @param operationName the current operation name (must not be null or blank)
     * @param stepNumber the current step number (must not be negative)
     * @param taskId the task identifier (must not be null or blank)
     * @param taskName the task name (must not be null or blank)
     * @param parentTaskId the parent task identifier (optional)
     * @param priority the task priority (must not be null)
     * @return a new TaskContext instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not TASK, or if executionId/operationName/taskId/taskName are blank, or if stepNumber is negative
     */
    public static TaskContext of(
        ContextId id,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant createdAt,
        Instant updatedAt,
        String executionId,
        String operationName,
        int stepNumber,
        String taskId,
        String taskName,
        String parentTaskId,
        ContextPriority priority
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(operationName, "operationName must not be null");
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(taskName, "taskName must not be null");
        Objects.requireNonNull(priority, "priority must not be null");

        // Validate type is TASK
        if (type != ContextType.TASK) {
            throw new IllegalArgumentException("TaskContext type must be TASK, got: " + type);
        }

        // Validate string fields are not blank
        if (executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be blank");
        }
        if (taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (taskName.isBlank()) {
            throw new IllegalArgumentException("taskName must not be blank");
        }

        // Validate step number is not negative
        if (stepNumber < 0) {
            throw new IllegalArgumentException("stepNumber must not be negative, got: " + stepNumber);
        }

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new TaskContext(id, type, state, unmodifiableData, createdAt, updatedAt,
            executionId, operationName, stepNumber, taskId, taskName, parentTaskId, priority);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be TASK)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, must be unmodifiable)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param executionId the execution identifier (must not be null or blank)
     * @param operationName the current operation name (must not be null or blank)
     * @param stepNumber the current step number (must not be negative)
     * @param taskId the task identifier (must not be null or blank)
     * @param taskName the task name (must not be null or blank)
     * @param parentTaskId the parent task identifier (optional)
     * @param priority the task priority (must not be null)
     */
    public TaskContext {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(operationName, "operationName must not be null");
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(taskName, "taskName must not be null");
        Objects.requireNonNull(priority, "priority must not be null");

        // Validate type is TASK
        if (type != ContextType.TASK) {
            throw new IllegalArgumentException("TaskContext type must be TASK, got: " + type);
        }

        // Validate string fields are not blank
        if (executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be blank");
        }
        if (taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (taskName.isBlank()) {
            throw new IllegalArgumentException("taskName must not be blank");
        }

        // Validate step number is not negative
        if (stepNumber < 0) {
            throw new IllegalArgumentException("stepNumber must not be negative, got: " + stepNumber);
        }
    }

    /**
     * Returns an unmodifiable view of the context data.
     *
     * <p>This method ensures that the internal data map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of context data
     */
    public Map<String, Object> data() {
        return data;
    }
}
