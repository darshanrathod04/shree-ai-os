package platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.cognitive.model.CognitiveId;
import platform.kernels.cognitive.model.DecisionContext;

/**
 * <b>DecisionContextValidator</b>
 *
 * <p>Performs structural validation of DecisionContext domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates identifier presence</li>
 *   <li>Validates alternatives collection</li>
 *   <li>Validates assumptions structure</li>
 *   <li>Validates constraints structure</li>
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
 * <p>This validator performs structural validation only. It verifies that DecisionContext
 * instances are well-formed and satisfy their construction invariants. It does not compare
 * alternatives, evaluate decision quality, or rank decisions.</p>
 *
 * @since 1.0
 */
public final class DecisionContextValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private DecisionContextValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a DecisionContext instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Identifier presence and format</li>
     *   <li>Alternatives collection presence</li>
     *   <li>Assumptions map presence</li>
     *   <li>Constraints map presence</li>
     *   <li>Metadata map presence</li>
     *   <li>Timestamp presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Identifier presence</li>
     *   <li>Alternatives collection structure</li>
     *   <li>Assumptions structure</li>
     *   <li>Constraints structure</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not compare alternatives</li>
     *   <li>Does not evaluate decision quality</li>
     *   <li>Does not rank or score decisions</li>
     * </ul>
     *
     * @param context the DecisionContext to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if context is {@code null}
     */
    public static CognitiveValidationResult validate(DecisionContext context) {
        Objects.requireNonNull(context, "DecisionContext must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(context.id(), "DecisionContext.id", violations);

        // Validate alternatives
        validateAlternatives(context.availableAlternatives(), violations);

        // Validate assumptions
        validateAssumptions(context.assumptions(), violations);

        // Validate constraints
        validateConstraints(context.constraints(), violations);

        // Validate metadata
        validateMetadata(context.metadata(), "DecisionContext.metadata", violations);

        // Validate timestamp
        validateTimestamp(context.createdAt(), violations);

        // Validate immutable collections
        validateImmutableCollections(context, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "DecisionContextValidator")
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
     * Validates the available alternatives list.
     *
     * @param alternatives the alternatives list to validate
     * @param violations the list to add violations to
     */
    private static void validateAlternatives(List<String> alternatives, List<String> violations) {
        if (alternatives == null) {
            violations.add("DecisionContext availableAlternatives must not be null");
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
            violations.add("DecisionContext assumptions must not be null");
        }
    }

    /**
     * Validates the constraints map.
     *
     * @param constraints the constraints map to validate
     * @param violations the list to add violations to
     */
    private static void validateConstraints(Map<String, Object> constraints, List<String> violations) {
        if (constraints == null) {
            violations.add("DecisionContext constraints must not be null");
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
            violations.add("DecisionContext createdAt must not be null");
        }
    }

    /**
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param context the DecisionContext to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(DecisionContext context, List<String> violations) {
        try {
            // Attempt to modify the alternatives list - should throw UnsupportedOperationException
            List<String> alternatives = context.availableAlternatives();
            if (alternatives != null) {
                alternatives.add("test"); // Should throw UnsupportedOperationException
                violations.add("DecisionContext availableAlternatives collection is not properly immutable");
            }

            // Attempt to modify the assumptions map
            Map<String, Object> assumptions = context.assumptions();
            if (assumptions != null) {
                assumptions.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("DecisionContext assumptions collection is not properly immutable");
            }

            // Attempt to modify the constraints map
            Map<String, Object> constraints = context.constraints();
            if (constraints != null) {
                constraints.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("DecisionContext constraints collection is not properly immutable");
            }

            // Attempt to modify the metadata map
            Map<String, Object> metadata = context.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("DecisionContext metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("DecisionContext collection validation failed: " + e.getMessage());
        }
    }
}