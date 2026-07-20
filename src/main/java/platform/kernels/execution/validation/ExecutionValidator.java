package platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.execution.model.ExecutionRequest;

/**
 * <b>ExecutionValidator</b>
 *
 * <p>Primary validation entry point for execution requests.
 * This class coordinates validation across specialized validators and aggregates results.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates specialized validators.</li>
 *   <li>Aggregates validation results.</li>
 *   <li>Exposes unified validation interface.</li>
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
public final class ExecutionValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ExecutionValidator() {
        throw new UnsupportedOperationException("ExecutionValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates an execution request comprehensively.
     *
     * <p>This method coordinates all specialized validators and aggregates their results
     * into a single validation result.</p>
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Structural integrity of the execution request</li>
     *   <li>Action identifier validity</li>
     *   <li>Workflow definition structure</li>
     *   <li>Task execution prerequisites</li>
     *   <li>Recovery strategy configuration</li>
     *   <li>Execution criteria and options</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the aggregated validation result
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ExecutionValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate action
        ExecutionValidationResult actionResult = ActionValidator.validate(request);
        if (!actionResult.valid()) {
            violations.addAll(actionResult.violations());
        }
        metadata.put("actionValid", actionResult.valid());

        // Validate workflow
        ExecutionValidationResult workflowResult = WorkflowValidator.validate(request);
        if (!workflowResult.valid()) {
            violations.addAll(workflowResult.violations());
        }
        metadata.put("workflowValid", workflowResult.valid());

        // Validate task execution
        ExecutionValidationResult taskResult = TaskExecutionValidator.validate(request);
        if (!taskResult.valid()) {
            violations.addAll(taskResult.violations());
        }
        metadata.put("taskValid", taskResult.valid());

        // Validate recovery
        ExecutionValidationResult recoveryResult = RecoveryValidator.validate(request);
        if (!recoveryResult.valid()) {
            violations.addAll(recoveryResult.violations());
        }
        metadata.put("recoveryValid", recoveryResult.valid());

        // Validate execution criteria
        ExecutionValidationResult criteriaResult = ExecutionCriteriaValidator.validate(request);
        if (!criteriaResult.valid()) {
            violations.addAll(criteriaResult.violations());
        }
        metadata.put("criteriaValid", criteriaResult.valid());

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates an execution request with custom metadata.
     *
     * <p>This method performs the same validation as {@link #validate(ExecutionRequest)}
     * but allows additional metadata to be included in the result.</p>
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param metadata  additional validation metadata (must not be {@code null})
     * @return the aggregated validation result with custom metadata
     * @throws IllegalArgumentException if request or metadata is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request, Map<String, Object> metadata) {
        if (request == null) {
            throw new IllegalArgumentException("ExecutionValidator validate request must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ExecutionValidator validate metadata must not be null");
        }

        ExecutionValidationResult result = validate(request);

        // Merge custom metadata
        Map<String, Object> mergedMetadata = new HashMap<>(result.metadata());
        mergedMetadata.putAll(metadata);

        return new ExecutionValidationResult(
                result.valid(),
                result.violations(),
                result.validatedAt(),
                mergedMetadata
        );
    }

    /**
     * Checks if an execution request is structurally valid.
     *
     * <p>This is a convenience method that returns only the validity status
     * without the detailed violations.</p>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return {@code true} if the request is structurally valid, {@code false} otherwise
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static boolean isValid(ExecutionRequest request) {
        return validate(request).valid();
    }
}