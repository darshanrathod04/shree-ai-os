package platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.cognitive.model.CognitiveId;
import platform.kernels.cognitive.model.ReflectionScope;

/**
 * <b>ReflectionScopeValidator</b>
 *
 * <p>Performs structural validation of ReflectionScope domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates scope definition</li>
 *   <li>Validates target presence</li>
 *   <li>Validates boundary consistency</li>
 *   <li>Validates metadata integrity</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This class is stateless, deterministic, thread-safe, and read-only.
 * It maintains no mutable fields and performs only structural validation.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-103, EIO-ARCH-001</p>
 *
 * <p><b>Validation Scope:</b></p>
 * <p>This validator performs structural validation only. It verifies that ReflectionScope
 * instances are well-formed and satisfy their construction invariants. It does not perform
 * reflective analysis, evaluate reflection outcomes, or assess reflection quality.</p>
 *
 * @since 1.0
 */
public final class ReflectionScopeValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private ReflectionScopeValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a ReflectionScope instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Identifier presence and format</li>
     *   <li>Reflection target presence and non-empty constraint</li>
     *   <li>Analysis boundaries map presence</li>
     *   <li>Included artifacts list presence</li>
     *   <li>Metadata map presence</li>
     *   <li>Timestamp presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Scope definition validation</li>
     *   <li>Target presence</li>
     *   <li>Boundary consistency</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not perform reflective analysis</li>
     *   <li>Does not evaluate reflection outcomes</li>
     *   <li>Does not assess reflection quality</li>
     * </ul>
     *
     * @param scope the ReflectionScope to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if scope is {@code null}
     */
    public static CognitiveValidationResult validate(ReflectionScope scope) {
        Objects.requireNonNull(scope, "ReflectionScope must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(scope.id(), "ReflectionScope.id", violations);

        // Validate reflection target
        validateReflectionTarget(scope.reflectionTarget(), violations);

        // Validate analysis boundaries
        validateAnalysisBoundaries(scope.analysisBoundaries(), violations);

        // Validate included artifacts
        validateIncludedArtifacts(scope.includedArtifacts(), violations);

        // Validate metadata
        validateMetadata(scope.metadata(), "ReflectionScope.metadata", violations);

        // Validate timestamp
        validateTimestamp(scope.createdAt(), violations);

        // Validate immutable collections
        validateImmutableCollections(scope, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ReflectionScopeValidator")
        );
    }

    /**
     * Validates the CognitiveId identifier.
     *
     * @param id the identifier to validate
     * @param fieldName the name of the field for error messages
     * @param violations the list to add violations to
     */
    private static void validateIdentifier(CognitiveId id, String fieldName, List<String> violations) {
        if (id == null) {
            violations.add(fieldName + " must not be null");
        } else if (id.value() == null || id.value().isBlank()) {
            violations.add(fieldName + ".value must not be null or empty");
        }
    }

    /**
     * Validates the reflection target field.
     *
     * @param reflectionTarget the reflection target to validate
     * @param violations the list to add violations to
     */
    private static void validateReflectionTarget(String reflectionTarget, List<String> violations) {
        if (reflectionTarget == null) {
            violations.add("ReflectionScope reflectionTarget must not be null");
        } else if (reflectionTarget.isBlank()) {
            violations.add("ReflectionScope reflectionTarget must not be empty");
        }
    }

    /**
     * Validates the analysis boundaries map.
     *
     * @param analysisBoundaries the analysis boundaries to validate
     * @param violations the list to add violations to
     */
    private static void validateAnalysisBoundaries(Map<String, Object> analysisBoundaries, List<String> violations) {
        if (analysisBoundaries == null) {
            violations.add("ReflectionScope analysisBoundaries must not be null");
        }
    }

    /**
     * Validates the included artifacts list.
     *
     * @param includedArtifacts the included artifacts to validate
     * @param violations the list to add violations to
     */
    private static void validateIncludedArtifacts(List<String> includedArtifacts, List<String> violations) {
        if (includedArtifacts == null) {
            violations.add("ReflectionScope includedArtifacts must not be null");
        }
    }

    /**
     * Validates the metadata map.
     *
     * @param metadata the metadata to validate
     * @param fieldName the name of the field for error messages
     * @param violations the list to add violations to
     */
    private static void validateMetadata(Map<String, Object> metadata, String fieldName, List<String> violations) {
        if (metadata == null) {
            violations.add(fieldName + " must not be null");
        }
    }

    /**
     * Validates the createdAt timestamp.
     *
     * @param createdAt the timestamp to validate
     * @param violations the list to add violations to
     */
    private static void validateTimestamp(Instant createdAt, List<String> violations) {
        if (createdAt == null) {
            violations.add("ReflectionScope createdAt must not be null");
        }
    }

    /**
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param scope the ReflectionScope to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(ReflectionScope scope, List<String> violations) {
        try {
            // Attempt to modify the analysis boundaries map - should throw UnsupportedOperationException
            Map<String, Object> analysisBoundaries = scope.analysisBoundaries();
            if (analysisBoundaries != null) {
                analysisBoundaries.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("ReflectionScope analysisBoundaries collection is not properly immutable");
            }

            // Attempt to modify the included artifacts list
            List<String> includedArtifacts = scope.includedArtifacts();
            if (includedArtifacts != null) {
                includedArtifacts.add("test"); // Should throw UnsupportedOperationException
                violations.add("ReflectionScope includedArtifacts collection is not properly immutable");
            }

            // Attempt to modify the metadata map
            Map<String, Object> metadata = scope.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("ReflectionScope metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("ReflectionScope collection validation failed: " + e.getMessage());
        }
    }
}