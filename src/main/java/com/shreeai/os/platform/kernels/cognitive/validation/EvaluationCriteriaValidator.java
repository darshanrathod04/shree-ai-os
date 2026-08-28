package com.shreeai.os.platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.cognitive.model.CognitiveId;
import com.shreeai.os.platform.kernels.cognitive.model.EvaluationCriteria;

/**
 * <b>EvaluationCriteriaValidator</b>
 *
 * <p>Performs structural validation of EvaluationCriteria domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates criterion definitions</li>
 *   <li>Validates weights</li>
 *   <li>Validates priorities</li>
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
 * <p>This validator performs structural validation only. It verifies that EvaluationCriteria
 * instances are well-formed and satisfy their construction invariants. It does not score or
 * rank criteria, evaluate criteria quality, or determine criteria effectiveness.</p>
 *
 * @since 1.0
 */
public final class EvaluationCriteriaValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private EvaluationCriteriaValidator() {
        // Prevent instantiation
    }

    /**
     * Validates an EvaluationCriteria instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Identifier presence and format</li>
     *   <li>Criterion name presence and non-empty constraint</li>
     *   <li>Weight presence and range validation</li>
     *   <li>Priority presence and non-empty constraint</li>
     *   <li>Metadata map presence</li>
     *   <li>Timestamp presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Criterion definitions validation</li>
     *   <li>Weights validation</li>
     *   <li>Priorities validation</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not score or rank criteria</li>
     *   <li>Does not evaluate criteria quality</li>
     *   <li>Does not determine criteria effectiveness</li>
     * </ul>
     *
     * @param criteria the EvaluationCriteria to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if criteria is {@code null}
     */
    public static CognitiveValidationResult validate(EvaluationCriteria criteria) {
        Objects.requireNonNull(criteria, "EvaluationCriteria must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(criteria.id(), "EvaluationCriteria.id", violations);

        // Validate criterion name
        validateCriterionName(criteria.criterionName(), violations);

        // Validate weight
        validateWeight(criteria.weight(), violations);

        // Validate priority
        validatePriority(criteria.priority(), violations);

        // Validate metadata
        validateMetadata(criteria.metadata(), "EvaluationCriteria.metadata", violations);

        // Validate timestamp
        validateTimestamp(criteria.createdAt(), violations);

        // Validate immutable collections
        validateImmutableCollections(criteria, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "EvaluationCriteriaValidator")
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
     * Validates the criterion name field.
     *
     * @param criterionName the criterion name to validate
     * @param violations the list to add violations to
     */
    private static void validateCriterionName(String criterionName, List<String> violations) {
        if (criterionName == null) {
            violations.add("EvaluationCriteria criterionName must not be null");
        } else if (criterionName.isBlank()) {
            violations.add("EvaluationCriteria criterionName must not be empty");
        }
    }

    /**
     * Validates the weight field.
     *
     * @param weight the weight to validate
     * @param violations the list to add violations to
     */
    private static void validateWeight(Double weight, List<String> violations) {
        if (weight == null) {
            violations.add("EvaluationCriteria weight must not be null");
        } else if (weight < 0.0 || weight > 1.0) {
            violations.add("EvaluationCriteria weight must be between 0.0 and 1.0");
        }
    }

    /**
     * Validates the priority field.
     *
     * @param priority the priority to validate
     * @param violations the list to add violations to
     */
    private static void validatePriority(String priority, List<String> violations) {
        if (priority == null) {
            violations.add("EvaluationCriteria priority must not be null");
        } else if (priority.isBlank()) {
            violations.add("EvaluationCriteria priority must not be empty");
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
            violations.add("EvaluationCriteria createdAt must not be null");
        }
    }

    /**
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param criteria the EvaluationCriteria to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(EvaluationCriteria criteria, List<String> violations) {
        try {
            // Attempt to modify the metadata map - should throw UnsupportedOperationException
            Map<String, Object> metadata = criteria.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("EvaluationCriteria metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("EvaluationCriteria collection validation failed: " + e.getMessage());
        }
    }
}