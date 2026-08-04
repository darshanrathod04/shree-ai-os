package com.shreeai.os.platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ReasoningRequest</b>
 *
 * <p>Represents a reasoning request submitted to the Cognitive Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the intent for a reasoning operation.</li>
 *   <li>Provides immutable parameters for reasoning requests.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param id the unique identifier (must not be {@code null})
 * @param reasoningObjective the objective of the reasoning (must not be {@code null} or empty)
 * @param inputs the reasoning inputs (must not be {@code null}, values may be {@code null})
 * @param constraints the reasoning constraints (must not be {@code null}, values may be {@code null})
 * @param metadata additional request metadata (must not be {@code null}, values may be {@code null})
 * @param requestedAt the timestamp when the request was created (must not be {@code null})
 */
public record ReasoningRequest(
        CognitiveId id,
        String reasoningObjective,
        Map<String, Object> inputs,
        Map<String, Object> constraints,
        Map<String, Object> metadata,
        Instant requestedAt
) {

    /**
     * Creates a new ReasoningRequest with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param reasoningObjective the objective of the reasoning (must not be {@code null} or empty)
     * @param inputs the reasoning inputs (must not be {@code null}, values may be {@code null})
     * @param constraints the reasoning constraints (must not be {@code null}, values may be {@code null})
     * @param metadata additional request metadata (must not be {@code null}, values may be {@code null})
     * @param requestedAt the timestamp when the request was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public ReasoningRequest {
        Objects.requireNonNull(id, "ReasoningRequest id must not be null");
        Objects.requireNonNull(reasoningObjective, "ReasoningRequest reasoningObjective must not be null");
        if (reasoningObjective.isBlank()) {
            throw new IllegalArgumentException("ReasoningRequest reasoningObjective must not be empty");
        }
        Objects.requireNonNull(inputs, "ReasoningRequest inputs must not be null");
        Objects.requireNonNull(constraints, "ReasoningRequest constraints must not be null");
        Objects.requireNonNull(metadata, "ReasoningRequest metadata must not be null");
        Objects.requireNonNull(requestedAt, "ReasoningRequest requestedAt must not be null");
    }

    /**
     * Returns the unique identifier of this reasoning request.
     *
     * @return the reasoning request identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the objective of this reasoning request.
     *
     * @return the reasoning objective
     */
    public String reasoningObjective() {
        return reasoningObjective;
    }

    /**
     * Returns an unmodifiable view of the inputs for this reasoning request.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the inputs
     */
    public Map<String, Object> inputs() {
        return Map.copyOf(inputs);
    }

    /**
     * Returns an unmodifiable view of the constraints for this reasoning request.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the constraints
     */
    public Map<String, Object> constraints() {
        return Map.copyOf(constraints);
    }

    /**
     * Returns an unmodifiable view of the metadata for this reasoning request.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this reasoning request was created.
     *
     * @return the request timestamp
     */
    public Instant requestedAt() {
        return requestedAt;
    }

    /**
     * Returns a string representation of this reasoning request.
     *
     * <p>Includes the identifier, reasoning objective, and timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ReasoningRequest{" +
                "id=" + id +
                ", reasoningObjective='" + reasoningObjective + '\'' +
                ", requestedAt=" + requestedAt +
                '}';
    }
}