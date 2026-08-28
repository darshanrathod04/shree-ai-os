package com.shreeai.os.platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;

/**
 * <b>TaskExecutionValidator</b>
 *
 * <p>Validates task execution-related structural integrity in execution requests.
 * This validator ensures task associations and execution prerequisites are well-formed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates planned task association.</li>
 *   <li>Validates execution prerequisites (presence and structure only).</li>
 *   <li>Validates task references.</li>
 *   <li>Validates metadata integrity.</li>
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
public final class TaskExecutionValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private TaskExecutionValidator() {
        throw new UnsupportedOperationException("TaskExecutionValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates task execution-related aspects of an execution request.
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Planned task association</li>
     *   <li>Execution prerequisites (presence and structure only)</li>
     *   <li>Task references</li>
     *   <li>Metadata integrity</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the validation result for task execution-related checks
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TaskExecutionValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate task association
        validateTaskAssociation(request, violations, metadata);

        // Validate execution prerequisites
        validateExecutionPrerequisites(request, violations);

        // Validate task references
        validateTaskReferences(request, violations);

        // Validate metadata integrity
        validateMetadataIntegrity(request, metadata, violations);

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates task association.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateTaskAssociation(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        // Validate that execution context provides task association information
        if (request.context() != null) {
            // Check for task-related metadata in context
            Map<String, Object> contextData = request.context().contextData();
            if (contextData != null) {
                // Validate task association presence (structural check)
                boolean hasTaskId = contextData.containsKey("taskId") ||
                                   contextData.containsKey("taskIdentifier") ||
                                   contextData.containsKey("taskReference");
                metadata.put("hasTaskAssociation", hasTaskId);

                if (!hasTaskId) {
                    // This is informational, not necessarily a violation
                    metadata.put("taskAssociationWarning", "No explicit task identifier found in context");
                }
            }
        }

        // Validate action identifier as task reference
        if (request.actionId() != null) {
            metadata.put("actionId", request.actionId());
            metadata.put("actionIdLength", request.actionId().length());

            // Structural validation of action identifier as task reference
            if (request.actionId().trim().isEmpty()) {
                violations.add("TaskExecutionValidator: actionId must not be empty for task association");
            }
        }
    }

    /**
     * Validates execution prerequisites.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateExecutionPrerequisites(ExecutionRequest request, List<String> violations) {
        // Validate execution context prerequisites
        if (request.context() == null) {
            violations.add("TaskExecutionValidator: execution context is required for task execution");
            return;
        }

        // Validate context data presence
        Map<String, Object> contextData = request.context().contextData();
        if (contextData == null || contextData.isEmpty()) {
            violations.add("TaskExecutionValidator: context data must not be empty for task execution");
        }

        // Validate execution options prerequisites
        if (request.options() == null) {
            violations.add("TaskExecutionValidator: execution options are required for task execution");
        } else {
            // Structural validation of options
            long timeoutMs = request.options().timeoutMs();
            if (timeoutMs <= 0) {
                violations.add("TaskExecutionValidator: timeoutMs must be positive for task execution");
            }
        }
    }

    /**
     * Validates task references.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateTaskReferences(ExecutionRequest request, List<String> violations) {
        // Validate that action identifier can serve as task reference
        String actionId = request.actionId();
        if (actionId != null) {
            // Structural checks for task reference validity
            if (actionId.length() < 3) {
                violations.add("TaskExecutionValidator: actionId must be at least 3 characters for valid task reference");
            }

            if (actionId.length() > 256) {
                violations.add("TaskExecutionValidator: actionId must not exceed 256 characters for task reference");
            }

            // Validate format (alphanumeric with separators)
            if (!actionId.matches("^[a-zA-Z0-9._-]+$")) {
                violations.add("TaskExecutionValidator: actionId must contain only alphanumeric characters, dots, underscores, and hyphens");
            }
        }

        // Validate execution identifier consistency
        if (request.executionId() != null && request.context() != null) {
            String execId = request.executionId().value();
            String contextExecId = request.context().executionId() != null
                    ? request.context().executionId().value()
                    : null;

            if (!Objects.equals(execId, contextExecId)) {
                violations.add("TaskExecutionValidator: executionId must be consistent for task tracking");
            }
        }
    }

    /**
     * Validates metadata integrity.
     *
     * @param request    the execution request to validate (must not be {@code null})
     * @param metadata   the metadata map to populate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateMetadataIntegrity(ExecutionRequest request, Map<String, Object> metadata, List<String> violations) {
        // Validate parameters map integrity
        if (request.parameters() != null) {
            metadata.put("parametersCount", request.parameters().size());
            metadata.put("hasParameters", !request.parameters().isEmpty());

            // Check for task-specific parameters
            boolean hasTaskParams = request.parameters().containsKey("taskId") ||
                                   request.parameters().containsKey("taskConfig") ||
                                   request.parameters().containsKey("taskInput");
            metadata.put("hasTaskParameters", hasTaskParams);
        }

        // Validate context metadata
        if (request.context() != null && request.context().contextData() != null) {
            metadata.put("contextDataCount", request.context().contextData().size());
        }
    }
}