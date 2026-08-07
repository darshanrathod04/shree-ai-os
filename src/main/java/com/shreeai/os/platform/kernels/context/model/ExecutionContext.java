package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionContext</b>
 *
 * <p>Represents the runtime context for current execution flow and running operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates current execution flow state.</li>
 *   <li>Manages running operations and runtime execution metadata.</li>
 *   <li>Provides specialized context for execution-related operations.</li>
 * </ul>
 *
 * <p><b>Context Type:</b> This is a specialized Context for execution scenarios.</p>
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
 * @param type the context type (must not be null, must be EXECUTION)
 * @param state the current state (must not be null)
 * @param data the context data (must not be null, defensively copied)
 * @param createdAt when the context was created (must not be null)
 * @param updatedAt when the context was last updated (must not be null)
 * @param executionId the execution identifier (must not be null)
 * @param operationName the current operation name (must not be null or blank)
 * @param stepNumber the current step number (must not be negative)
 */
public record ExecutionContext(
    ContextId id,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant createdAt,
    Instant updatedAt,
    String executionId,
    String operationName,
    int stepNumber
) {
    /**
     * Creates a new ExecutionContext with validation.
     *
     * <p>All parameters are validated for null and business rules. The data map
     * is defensively copied to ensure immutability.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be EXECUTION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, will be defensively copied)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param executionId the execution identifier (must not be null or blank)
     * @param operationName the current operation name (must not be null or blank)
     * @param stepNumber the current step number (must not be negative)
     * @return a new ExecutionContext instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not EXECUTION, or if executionId/operationName are blank, or if stepNumber is negative
     */
    public static ExecutionContext of(
        ContextId id,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant createdAt,
        Instant updatedAt,
        String executionId,
        String operationName,
        int stepNumber
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(operationName, "operationName must not be null");

        // Validate type is EXECUTION
        if (type != ContextType.EXECUTION) {
            throw new IllegalArgumentException("ExecutionContext type must be EXECUTION, got: " + type);
        }

        // Validate string fields are not blank
        if (executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be blank");
        }

        // Validate step number is not negative
        if (stepNumber < 0) {
            throw new IllegalArgumentException("stepNumber must not be negative, got: " + stepNumber);
        }

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new ExecutionContext(id, type, state, unmodifiableData, createdAt, updatedAt,
            executionId, operationName, stepNumber);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be EXECUTION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, must be unmodifiable)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param executionId the execution identifier (must not be null or blank)
     * @param operationName the current operation name (must not be null or blank)
     * @param stepNumber the current step number (must not be negative)
     */
    public ExecutionContext {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(operationName, "operationName must not be null");

        // Validate type is EXECUTION
        if (type != ContextType.EXECUTION) {
            throw new IllegalArgumentException("ExecutionContext type must be EXECUTION, got: " + type);
        }

        // Validate string fields are not blank
        if (executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be blank");
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
