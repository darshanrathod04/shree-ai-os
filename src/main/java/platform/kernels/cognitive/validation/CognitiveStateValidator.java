package platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.cognitive.model.CognitiveId;
import platform.kernels.cognitive.model.CognitiveState;

/**
 * <b>CognitiveStateValidator</b>
 *
 * <p>Performs structural validation of CognitiveState domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates identifier presence and format</li>
 *   <li>Validates mandatory fields</li>
 *   <li>Validates lifecycle consistency</li>
 *   <li>Validates immutable collections</li>
 *   <li>Validates constructor invariants</li>
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
 * <p>This validator performs structural validation only. It verifies that CognitiveState
 * instances are well-formed and satisfy their construction invariants. It does not evaluate
 * reasoning quality, state correctness, or cognitive performance.</p>
 *
 * @since 1.0
 */
public final class CognitiveStateValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private CognitiveStateValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a CognitiveState instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Identifier presence and non-null format</li>
     *   <li>State name presence and non-empty constraint</li>
     *   <li>Lifecycle status presence</li>
     *   <li>Timestamp presence and consistency</li>
     *   <li>Metadata presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Identifier presence</li>
     *   <li>Mandatory fields validation</li>
     *   <li>Lifecycle consistency</li>
     *   <li>Immutable collections</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not evaluate reasoning quality</li>
     *   <li>Does not determine state correctness</li>
     *   <li>Does not assess cognitive performance</li>
     * </ul>
     *
     * @param state the CognitiveState to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if state is {@code null}
     */
    public static CognitiveValidationResult validate(CognitiveState state) {
        Objects.requireNonNull(state, "CognitiveState must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(state.id(), "CognitiveState.id", violations);

        // Validate stateName
        validateStateName(state.stateName(), violations);

        // Validate lifecycleStatus
        validateLifecycleStatus(state.lifecycleStatus(), violations);

        // Validate timestamps
        validateTimestamps(state.createdAt(), state.updatedAt(), violations);

        // Validate metadata
        validateMetadata(state.metadata(), "CognitiveState.metadata", violations);

        // Validate immutable collections
        validateImmutableCollections(state, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "CognitiveStateValidator")
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
     * Validates the state name field.
     *
     * @param stateName the state name to validate
     * @param violations the list to add violations to
     */
    private static void validateStateName(String stateName, List<String> violations) {
        if (stateName == null) {
            violations.add("CognitiveState stateName must not be null");
        } else if (stateName.isBlank()) {
            violations.add("CognitiveState stateName must not be empty");
        }
    }

    /**
     * Validates the lifecycle status field.
     *
     * @param lifecycleStatus the lifecycle status to validate
     * @param violations the list to add violations to
     */
    private static void validateLifecycleStatus(String lifecycleStatus, List<String> violations) {
        if (lifecycleStatus == null) {
            violations.add("CognitiveState lifecycleStatus must not be null");
        } else if (lifecycleStatus.isBlank()) {
            violations.add("CognitiveState lifecycleStatus must not be empty");
        }
    }

    /**
     * Validates the timestamp fields for presence and consistency.
     *
     * @param createdAt the creation timestamp
     * @param updatedAt the update timestamp
     * @param violations the list to add violations to
     */
    private static void validateTimestamps(Instant createdAt, Instant updatedAt, List<String> violations) {
        if (createdAt == null) {
            violations.add("CognitiveState createdAt must not be null");
        }

        if (updatedAt == null) {
            violations.add("CognitiveState updatedAt must not be null");
        } else if (createdAt != null && updatedAt.isBefore(createdAt)) {
            violations.add("CognitiveState updatedAt must not be before createdAt");
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
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param state the CognitiveState to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(CognitiveState state, List<String> violations) {
        try {
            // Attempt to modify the metadata map - should throw UnsupportedOperationException
            Map<String, Object> metadata = state.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("CognitiveState metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("CognitiveState metadata collection validation failed: " + e.getMessage());
        }
    }
}