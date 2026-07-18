package platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CognitiveValidationResult</b>
 *
 * <p>Represents the immutable result of cognitive domain model validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates validation outcomes for cognitive structures.</li>
 *   <li>Provides immutable validation results with violation tracking.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object. All collections are unmodifiable.
 * Defensive copying is applied to all mutable inputs.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-103, EIO-ARCH-001</p>
 *
 * @param valid indicates whether validation passed ({@code true}) or failed ({@code false})
 * @param violations list of validation violation messages (must not be {@code null}, may be empty)
 * @param validatedAt the timestamp when validation was performed (must not be {@code null})
 * @param metadata additional validation metadata (must not be {@code null}, values may be {@code null})
 */
public record CognitiveValidationResult(
        boolean valid,
        List<String> violations,
        Instant validatedAt,
        Map<String, Object> metadata
) {

    /**
     * Creates a new CognitiveValidationResult with the specified parameters.
     *
     * <p>Performs defensive validation and creates immutable copies of all collections.</p>
     *
     * @param valid indicates whether validation passed ({@code true}) or failed ({@code false})
     * @param violations list of validation violation messages (must not be {@code null}, may be empty)
     * @param validatedAt the timestamp when validation was performed (must not be {@code null})
     * @param metadata additional validation metadata (must not be {@code null}, values may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public CognitiveValidationResult {
        Objects.requireNonNull(violations, "CognitiveValidationResult violations must not be null");
        Objects.requireNonNull(validatedAt, "CognitiveValidationResult validatedAt must not be null");
        Objects.requireNonNull(metadata, "CognitiveValidationResult metadata must not be null");
    }

    /**
     * Returns an unmodifiable list of validation violations.
     *
     * <p>The returned list is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable list of violation messages
     */
    public List<String> violations() {
        return Collections.unmodifiableList(violations);
    }

    /**
     * Returns an unmodifiable view of the validation metadata.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns a string representation of this validation result.
     *
     * <p>Includes the validation status, number of violations, and timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveValidationResult{" +
                "valid=" + valid +
                ", violations=" + violations.size() +
                ", validatedAt=" + validatedAt +
                '}';
    }
}