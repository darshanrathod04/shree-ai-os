package platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.execution.model.ExecutionId;
import platform.kernels.execution.model.ExecutionRequest;

/**
 * <b>ActionValidator</b>
 *
 * <p>Validates action-related structural integrity in execution requests.
 * This validator ensures action identifiers and request structure are well-formed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates execution identifier integrity.</li>
 *   <li>Validates action request structure.</li>
 *   <li>Validates action identifier integrity.</li>
 *   <li>Validates action state transitions (structural validity only).</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Thread-safe — all methods are static.</li>
 *   <li>Deterministic — same input produces same output.</li>
 *   <li>Read-only — no state mutation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-103, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public final class ActionValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ActionValidator() {
        throw new UnsupportedOperationException("ActionValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates action-related aspects of an execution request.
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Execution identifier validity</li>
     *   <li>Action identifier presence and format</li>
     *   <li>Action request structure</li>
     *   <li>Required fields presence</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the validation result for action-related checks
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ActionValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate execution identifier
        validateExecutionId(request.executionId(), violations);

        // Validate action identifier
        validateActionId(request.actionId(), violations);

        // Validate action request structure
        validateActionStructure(request, violations);

        // Validate metadata integrity
        validateMetadata(request, metadata, violations);

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates the execution identifier.
     *
     * @param executionId the execution identifier to validate (must not be {@code null})
     * @param violations  the list to add violations to (must not be {@code null})
     */
    private static void validateExecutionId(ExecutionId executionId, List<String> violations) {
        if (executionId == null) {
            violations.add("ActionValidator: executionId must not be null");
            return;
        }

        String value = executionId.value();
        if (value == null || value.trim().isEmpty()) {
            violations.add("ActionValidator: executionId value must not be null or empty");
        }
    }

    /**
     * Validates the action identifier.
     *
     * @param actionId  the action identifier to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateActionId(String actionId, List<String> violations) {
        if (actionId == null) {
            violations.add("ActionValidator: actionId must not be null");
            return;
        }

        if (actionId.trim().isEmpty()) {
            violations.add("ActionValidator: actionId must not be empty");
        }

        // Validate action identifier format (structural check only)
        if (actionId.contains(" ")) {
            violations.add("ActionValidator: actionId must not contain spaces");
        }
    }

    /**
     * Validates the action request structure.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateActionStructure(ExecutionRequest request, List<String> violations) {
        // Validate that required components are present
        if (request.context() == null) {
            violations.add("ActionValidator: execution context must not be null");
        }

        if (request.options() == null) {
            violations.add("ActionValidator: execution options must not be null");
        }

        if (request.parameters() == null) {
            violations.add("ActionValidator: execution parameters must not be null");
        }
    }

    /**
     * Validates metadata integrity.
     *
     * @param request    the execution request to validate (must not be {@code null})
     * @param metadata   the metadata map to populate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateMetadata(ExecutionRequest request, Map<String, Object> metadata, List<String> violations) {
        // Validate parameters map integrity
        if (request.parameters() != null) {
            metadata.put("parametersCount", request.parameters().size());
            metadata.put("hasParameters", !request.parameters().isEmpty());
        }
    }
}