package platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.execution.model.ExecutionRequest;

/**
 * <b>WorkflowValidator</b>
 *
 * <p>Validates workflow-related structural integrity in execution requests.
 * This validator ensures workflow definitions and state consistency are well-formed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates workflow definition structure.</li>
 *   <li>Validates workflow state consistency.</li>
 *   <li>Validates workflow dependency references.</li>
 *   <li>Validates immutable collections.</li>
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
public final class WorkflowValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private WorkflowValidator() {
        throw new UnsupportedOperationException("WorkflowValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates workflow-related aspects of an execution request.
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Workflow definition structure</li>
     *   <li>Workflow state consistency</li>
     *   <li>Workflow dependency references</li>
     *   <li>Immutable collections integrity</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the validation result for workflow-related checks
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("WorkflowValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate workflow structure
        validateWorkflowStructure(request, violations, metadata);

        // Validate workflow state consistency
        validateWorkflowState(request, violations);

        // Validate workflow dependencies
        validateWorkflowDependencies(request, violations);

        // Validate immutable collections
        validateImmutableCollections(request, violations);

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates workflow structure.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateWorkflowStructure(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        // Validate execution context structure
        if (request.context() != null) {
            String planId = request.context().planId();
            if (planId == null || planId.trim().isEmpty()) {
                violations.add("WorkflowValidator: context planId must not be null or empty");
            }

            String objectiveId = request.context().objectiveId();
            if (objectiveId == null || objectiveId.trim().isEmpty()) {
                violations.add("WorkflowValidator: context objectiveId must not be null or empty");
            }

            metadata.put("planId", planId);
            metadata.put("objectiveId", objectiveId);
        }

        // Validate execution options structure
        if (request.options() != null) {
            metadata.put("timeoutMs", request.options().timeoutMs());
            metadata.put("maxRetries", request.options().maxRetries());
            metadata.put("allowPartial", request.options().allowPartial());
        }
    }

    /**
     * Validates workflow state consistency.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateWorkflowState(ExecutionRequest request, List<String> violations) {
        // Structural validation of execution options
        if (request.options() != null) {
            long timeoutMs = request.options().timeoutMs();
            if (timeoutMs < 0) {
                violations.add("WorkflowValidator: timeoutMs must not be negative");
            }

            int maxRetries = request.options().maxRetries();
            if (maxRetries < 0) {
                violations.add("WorkflowValidator: maxRetries must not be negative");
            }

            long retryDelayMs = request.options().retryDelayMs();
            if (retryDelayMs < 0) {
                violations.add("WorkflowValidator: retryDelayMs must not be negative");
            }
        }
    }

    /**
     * Validates workflow dependencies.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateWorkflowDependencies(ExecutionRequest request, List<String> violations) {
        // Validate that execution identifier is consistent across components
        if (request.executionId() != null && request.context() != null) {
            String executionIdValue = request.executionId().value();
            String contextExecutionId = request.context().executionId() != null
                    ? request.context().executionId().value()
                    : null;

            if (!Objects.equals(executionIdValue, contextExecutionId)) {
                violations.add("WorkflowValidator: executionId must be consistent across request and context");
            }
        }
    }

    /**
     * Validates immutable collections.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateImmutableCollections(ExecutionRequest request, List<String> violations) {
        // Validate that collections are properly immutable (structural check)
        // This is a defensive check - the domain model should already enforce this
        if (request.parameters() != null) {
            try {
                request.parameters().put("test", "value");
                violations.add("WorkflowValidator: parameters map must be unmodifiable");
            } catch (UnsupportedOperationException e) {
                // Expected - map is unmodifiable
            }
        }

        if (request.context() != null && request.context().contextData() != null) {
            try {
                request.context().contextData().put("test", "value");
                violations.add("WorkflowValidator: contextData map must be unmodifiable");
            } catch (UnsupportedOperationException e) {
                // Expected - map is unmodifiable
            }
        }
    }
}