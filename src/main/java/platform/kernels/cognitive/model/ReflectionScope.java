package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ReflectionScope</b>
 *
 * <p>Represents the scope of reflective analysis.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the boundaries of reflective analysis.</li>
 *   <li>Provides immutable reflection scope parameters.</li>
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
 * @param reflectionTarget the target of reflection (must not be {@code null} or empty)
 * @param analysisBoundaries the boundaries of analysis (must not be {@code null}, values may be {@code null})
 * @param includedArtifacts the artifacts included in reflection (must not be {@code null}, elements may be {@code null})
 * @param metadata additional scope metadata (must not be {@code null}, values may be {@code null})
 * @param createdAt the timestamp when the scope was created (must not be {@code null})
 */
public record ReflectionScope(
        CognitiveId id,
        String reflectionTarget,
        Map<String, Object> analysisBoundaries,
        List<String> includedArtifacts,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new ReflectionScope with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param reflectionTarget the target of reflection (must not be {@code null} or empty)
     * @param analysisBoundaries the boundaries of analysis (must not be {@code null}, values may be {@code null})
     * @param includedArtifacts the artifacts included in reflection (must not be {@code null}, elements may be {@code null})
     * @param metadata additional scope metadata (must not be {@code null}, values may be {@code null})
     * @param createdAt the timestamp when the scope was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public ReflectionScope {
        Objects.requireNonNull(id, "ReflectionScope id must not be null");
        Objects.requireNonNull(reflectionTarget, "ReflectionScope reflectionTarget must not be null");
        if (reflectionTarget.isBlank()) {
            throw new IllegalArgumentException("ReflectionScope reflectionTarget must not be empty");
        }
        Objects.requireNonNull(analysisBoundaries, "ReflectionScope analysisBoundaries must not be null");
        Objects.requireNonNull(includedArtifacts, "ReflectionScope includedArtifacts must not be null");
        Objects.requireNonNull(metadata, "ReflectionScope metadata must not be null");
        Objects.requireNonNull(createdAt, "ReflectionScope createdAt must not be null");
    }

    /**
     * Returns the unique identifier of this reflection scope.
     *
     * @return the reflection scope identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the target of this reflection scope.
     *
     * @return the reflection target
     */
    public String reflectionTarget() {
        return reflectionTarget;
    }

    /**
     * Returns an unmodifiable view of the analysis boundaries for this reflection scope.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the analysis boundaries
     */
    public Map<String, Object> analysisBoundaries() {
        return Map.copyOf(analysisBoundaries);
    }

    /**
     * Returns an unmodifiable list of the included artifacts for this reflection scope.
     *
     * <p>The returned list is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable list of included artifacts
     */
    public List<String> includedArtifacts() {
        return List.copyOf(includedArtifacts);
    }

    /**
     * Returns an unmodifiable view of the metadata for this reflection scope.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this reflection scope was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns a string representation of this reflection scope.
     *
     * <p>Includes the identifier, reflection target, and creation timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ReflectionScope{" +
                "id=" + id +
                ", reflectionTarget='" + reflectionTarget + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}