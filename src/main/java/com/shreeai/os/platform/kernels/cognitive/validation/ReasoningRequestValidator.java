package com.shreeai.os.platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.cognitive.model.CognitiveId;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningRequest;

/**
 * <b>ReasoningRequestValidator</b>
 *
 * <p>Performs structural validation of ReasoningRequest domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates request identifier presence</li>
 *   <li>Validates required inputs</li>
 *   <li>Validates constraints structure</li>
 *   <li>Validates metadata structure</li>
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
 * <p>This validator performs structural validation only. It verifies that ReasoningRequest
 * instances are well-formed and satisfy their construction invariants. It does not evaluate
 * reasoning quality, request validity, or reasoning outcomes.</p>
 *
 * @since 1.0
 */
public final class ReasoningRequestValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private ReasoningRequestValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a ReasoningRequest instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including:</p>
     * <ul>
     *   <li>Request identifier presence and format</li>
     *   <li>Reasoning objective presence and non-empty constraint</li>
     *   <li>Inputs map presence</li>
     *   <li>Constraints map presence</li>
     *   <li>Metadata map presence</li>
     *   <li>Timestamp presence</li>
     *   <li>Immutable collection integrity</li>
     * </ul>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Request identifier presence</li>
     *   <li>Required inputs validation</li>
     *   <li>Constraints structure</li>
     *   <li>Metadata structure</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not evaluate reasoning quality</li>
     *   <li>Does not determine request validity</li>
     *   <li>Does not assess reasoning outcomes</li>
     * </ul>
     *
     * @param request the ReasoningRequest to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static CognitiveValidationResult validate(ReasoningRequest request) {
        Objects.requireNonNull(request, "ReasoningRequest must not be null for validation");

        List<String> violations = new ArrayList<>();

        // Validate identifier
        validateIdentifier(request.id(), "ReasoningRequest.id", violations);

        // Validate reasoning objective
        validateReasoningObjective(request.reasoningObjective(), violations);

        // Validate inputs
        validateInputs(request.inputs(), violations);

        // Validate constraints
        validateConstraints(request.constraints(), violations);

        // Validate metadata
        validateMetadata(request.metadata(), "ReasoningRequest.metadata", violations);

        // Validate timestamp
        validateTimestamp(request.requestedAt(), violations);

        // Validate immutable collections
        validateImmutableCollections(request, violations);

        return new CognitiveValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ReasoningRequestValidator")
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
     * Validates the reasoning objective field.
     *
     * @param reasoningObjective the reasoning objective to validate
     * @param violations the list to add violations to
     */
    private static void validateReasoningObjective(String reasoningObjective, List<String> violations) {
        if (reasoningObjective == null) {
            violations.add("ReasoningRequest reasoningObjective must not be null");
        } else if (reasoningObjective.isBlank()) {
            violations.add("ReasoningRequest reasoningObjective must not be empty");
        }
    }

    /**
     * Validates the inputs map.
     *
     * @param inputs the inputs map to validate
     * @param violations the list to add violations to
     */
    private static void validateInputs(Map<String, Object> inputs, List<String> violations) {
        if (inputs == null) {
            violations.add("ReasoningRequest inputs must not be null");
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
            violations.add("ReasoningRequest constraints must not be null");
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
     * Validates the requestedAt timestamp.
     *
     * @param requestedAt the timestamp to validate
     * @param violations the list to add violations to
     */
    private static void validateTimestamp(Instant requestedAt, List<String> violations) {
        if (requestedAt == null) {
            violations.add("ReasoningRequest requestedAt must not be null");
        }
    }

    /**
     * Validates immutable collection integrity.
     *
     * <p>Verifies that collections returned by the model are properly immutable.</p>
     *
     * @param request the ReasoningRequest to validate
     * @param violations the list to add violations to
     */
    private static void validateImmutableCollections(ReasoningRequest request, List<String> violations) {
        try {
            // Attempt to modify the inputs map - should throw UnsupportedOperationException
            Map<String, Object> inputs = request.inputs();
            if (inputs != null) {
                inputs.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("ReasoningRequest inputs collection is not properly immutable");
            }

            // Attempt to modify the constraints map
            Map<String, Object> constraints = request.constraints();
            if (constraints != null) {
                constraints.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("ReasoningRequest constraints collection is not properly immutable");
            }

            // Attempt to modify the metadata map
            Map<String, Object> metadata = request.metadata();
            if (metadata != null) {
                metadata.put("test", "value"); // Should throw UnsupportedOperationException
                violations.add("ReasoningRequest metadata collection is not properly immutable");
            }
        } catch (UnsupportedOperationException e) {
            // Expected - collection is properly immutable
        } catch (Exception e) {
            violations.add("ReasoningRequest collection validation failed: " + e.getMessage());
        }
    }
}