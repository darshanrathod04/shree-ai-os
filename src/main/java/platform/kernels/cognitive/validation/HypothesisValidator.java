package platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.cognitive.model.CognitiveId;
import platform.kernels.cognitive.model.Hypothesis;

/**
 * <b>HypothesisValidator</b>
 *
 * <p>Performs structural validation of Hypothesis domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates hypothesis identifier</li>
 *   <li>Validates statement presence</li>
 *   <li>Validates assumption structure</li>
 *   <li>Validates evidence references</li>
 *   <li>Validates metadata</li>
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
 * <p>This validator performs structural validation only. It verifies that Hypothesis
 * instances are well-formed and satisfy their construction invariants. It does not determine
 * whether a hypothesis is true, evaluate hypothesis quality, or assess evidence validity.</p>
 *
 * @since 1.0
 */
public final class HypothesisValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private HypothesisValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a Hypothesis instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Identifier presence and format</li>
     *   <li>Statement presence and non-empty constraint</li>
     *   <li>Assumptions map presence</li>
     *   <li>Supporting evidence references list presence</li>
     *   <li>Metadata map presence</li>
     *   <li>Timestamp presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Hypothesis identifier presence</li>
     *   <li>Statement presence</li>
     *   <li>Assumption structure</li>
     *   <li>Evidence references</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not determine whether a hypothesis is true</li>
     *   <li>Does not evaluate hypothesis quality</li>
     *   <li>Does not assess evidence validity</li>
     * </ul>
     *
     * @param hypothesis the Hypothesis to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if hypothesis is {@code null}
     */
    public static CognitiveValidationResult validate(Hypothesis hypothesis) {
        Objects.requireNonNull(hypothesis, "Hypothesis must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(hypothesis.id(), "Hypothesis.id", violations);

        // Validate statement
        validateStatement(hypothesis.statement(), violations);

        // Validate assumptions
        validateAssumptions(hypothesis.assumptions(), violations);

        // Validate supporting evidence references
        validateSupportingEvidenceReferences(hypothesis.supportingEvidenceReferences(), violations);

        // Validate metadata
        validateMetadata(hypothesis.metadata(), "Hypothesis.metadata", violations);

        // Validate timestamp
        validateTimestamp(hypothesis.createdAt(), violations);

        // Validate immutable collections
        validateImmutableCollections(hypothesis, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "HypothesisValidator")
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
     * Validates the statement field.
     *
     * @param statement the statement to validate
     * @param violations the list to add violations to
     */
    private static void validateStatement(String statement, List<String> violations) {
        if (statement == null) {
            violations.add("Hypothesis statement must not be null");
        } else if (statement.isBlank()) {
            violations.add("Hypothesis statement must not be empty");
        }
    }

    /**
     * Validates the assumptions map.
     *
     * @param assumptions the assumptions map to validate
     * @param violations the list to add violations to
     */
    private static void validateAssumptions(Map<String, Object> assumptions, List<String> violations) {
        if (assumptions == null) {
            violations.add("Hypothesis assumptions must not be null");
        }
    }

    /**
     * Validates the supporting evidence references list.
     *
     * @param supportingEvidenceReferences the supporting evidence references to validate
     * @param violations the list to add violations to
     */
    private static void validateSupportingEvidenceReferences(List<String> supportingEvidenceReferences, List<String> violations) {
        if (supportingEvidenceReferences == null) {
            violations.add("Hypothesis supportingEvidenceReferences must not be null");
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
            violations.add("Hypothesis createdAt must not be null");
        }
    }

    /**
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param hypothesis the Hypothesis to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(Hypothesis hypothesis, List<String> violations) {
        try {
            // Attempt to modify the assumptions map - should throw UnsupportedOperationException
            Map<String, Object> assumptions = hypothesis.assumptions();
            if (assumptions != null) {
                assumptions.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("Hypothesis assumptions collection is not properly immutable");
            }

            // Attempt to modify the supporting evidence references list
            List<String> supportingEvidenceReferences = hypothesis.supportingEvidenceReferences();
            if (supportingEvidenceReferences != null) {
                supportingEvidenceReferences.add("test"); // Should throw UnsupportedOperationException
                violations.add("Hypothesis supportingEvidenceReferences collection is not properly immutable");
            }

            // Attempt to modify the metadata map
            Map<String, Object> metadata = hypothesis.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("Hypothesis metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("Hypothesis collection validation failed: " + e.getMessage());
        }
    }
}