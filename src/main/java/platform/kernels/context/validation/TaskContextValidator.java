 package platform.kernels.context.validation;

import platform.kernels.context.model.ContextPriority;
import platform.kernels.context.model.ContextState;
import platform.kernels.context.model.ContextType;
import platform.kernels.context.model.TaskContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>TaskContextValidator</b>
 *
 * <p>A utility validator for TaskContext domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates task state and structure.</li>
 *   <li>Validates parent execution context and execution dependencies.</li>
 *   <li>Validates runtime metadata.</li>
 *   <li>Provides pure validation without side effects.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Static methods only - no instance state.</li>
 *   <li>Stateless and thread-safe.</li>
 *   <li>Pure validation - never mutates objects.</li>
 *   <li>No business logic, persistence, or side effects.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-103</p>
 *
 * @see ContextValidationResult
 * @see TaskContext
 */
public final class TaskContextValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private TaskContextValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates a TaskContext instance.
     *
     * <p>Validates all TaskContext fields including task state,
     * parent execution context, execution dependencies, and runtime metadata.</p>
     *
     * @param context the TaskContext to validate (must not be null)
     * @return the validation result
     * @throws NullPointerException if context is null
     */
    public static ContextValidationResult validate(TaskContext context) {
        List<String> violations = new ArrayList<>();

        // Validate base Context fields directly
        if (context.id() == null) {
            violations.add("Context id must not be null");
        } else {
            ContextValidationResult idResult = ContextValidator.validateContextId(context.id());
            if (!idResult.isValid()) {
                violations.addAll(idResult.getViolations());
            }
        }

        if (context.type() == null) {
            violations.add("Context type must not be null");
        } else {
            ContextValidationResult typeResult = ContextValidator.validateContextType(context.type());
            if (!typeResult.isValid()) {
                violations.addAll(typeResult.getViolations());
            }
        }

        if (context.state() == null) {
            violations.add("Context state must not be null");
        } else {
            ContextValidationResult stateResult = ContextValidator.validateContextState(context.state());
            if (!stateResult.isValid()) {
                violations.addAll(stateResult.getViolations());
            }
        }

        if (context.createdAt() == null) {
            violations.add("Context createdAt must not be null");
        }
        if (context.updatedAt() == null) {
            violations.add("Context updatedAt must not be null");
        } else if (context.createdAt() != null && context.updatedAt().isBefore(context.createdAt())) {
            violations.add("Context updatedAt must not be before createdAt");
        }
        if (context.data() == null) {
            violations.add("Context data must not be null");
        }

        // Validate type is TASK
        if (context.type() != ContextType.TASK) {
            violations.add("TaskContext type must be TASK, got: " + context.type());
        }

        // Validate executionId
        if (context.executionId() == null || context.executionId().isBlank()) {
            violations.add("TaskContext executionId must not be null or blank");
        }

        // Validate operationName
        if (context.operationName() == null || context.operationName().isBlank()) {
            violations.add("TaskContext operationName must not be null or blank");
        }

        // Validate stepNumber is not negative
        if (context.stepNumber() < 0) {
            violations.add("TaskContext stepNumber must not be negative, got: " + context.stepNumber());
        }

        // Validate taskId
        if (context.taskId() == null || context.taskId().isBlank()) {
            violations.add("TaskContext taskId must not be null or blank");
        }

        // Validate taskName
        if (context.taskName() == null || context.taskName().isBlank()) {
            violations.add("TaskContext taskName must not be null or blank");
        }

        // Validate priority
        if (context.priority() == null) {
            violations.add("TaskContext priority must not be null");
        }

        // Validate task state
        if (context.state() == ContextState.ACTIVE) {
            // Active task should have valid task identifiers
            if (context.taskId() == null || context.taskId().isBlank()) {
                violations.add("Active task must have a valid taskId");
            }
            if (context.taskName() == null || context.taskName().isBlank()) {
                violations.add("Active task must have a valid taskName");
            }
            if (context.executionId() == null || context.executionId().isBlank()) {
                violations.add("Active task must have a valid executionId");
            }
        }

        // Validate snapshot consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("TaskContext updatedAt must not be before createdAt");
            }
        }

        Map<String, Object> metadata = Map.of(
                "contextType", context.type() != null ? context.type().name() : "null",
                "executionId", context.executionId() != null ? context.executionId() : "null",
                "operationName", context.operationName() != null ? context.operationName() : "null",
                "stepNumber", context.stepNumber(),
                "taskId", context.taskId() != null ? context.taskId() : "null",
                "taskName", context.taskName() != null ? context.taskName() : "null",
                "priority", context.priority() != null ? context.priority().name() : "null"
        );

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                metadata
        );
    }

    /**
     * Validates task state.
     *
     * <p>Validates that the task state is valid and consistent.</p>
     *
     * @param context the TaskContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateTaskState(TaskContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("TaskContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate state is not null
        if (context.state() == null) {
            violations.add("Task state must not be null");
        }

        // Validate task identifiers
        if (context.taskId() == null || context.taskId().isBlank()) {
            violations.add("Task ID must not be null or blank");
        }

        if (context.taskName() == null || context.taskName().isBlank()) {
            violations.add("Task name must not be null or blank");
        }

        // Validate priority
        if (context.priority() == null) {
            violations.add("Task priority must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "state", context.state() != null ? context.state().name() : "null",
                        "taskId", context.taskId() != null ? context.taskId() : "null",
                        "taskName", context.taskName() != null ? context.taskName() : "null",
                        "priority", context.priority() != null ? context.priority().name() : "null"
                )
        );
    }

    /**
     * Validates parent execution context.
     *
     * <p>Validates that the parent execution context is properly referenced.</p>
     *
     * @param context the TaskContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateParentExecutionContext(TaskContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("TaskContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate executionId is present
        if (context.executionId() == null || context.executionId().isBlank()) {
            violations.add("Execution ID must not be null or blank");
        }

        // Validate operationName is present
        if (context.operationName() == null || context.operationName().isBlank()) {
            violations.add("Operation name must not be null or blank");
        }

        // Validate stepNumber
        if (context.stepNumber() < 0) {
            violations.add("Step number must not be negative, got: " + context.stepNumber());
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "executionId", context.executionId() != null ? context.executionId() : "null",
                        "operationName", context.operationName() != null ? context.operationName() : "null",
                        "stepNumber", context.stepNumber()
                )
        );
    }

    /**
     * Validates execution dependencies.
     *
     * <p>Validates that execution dependencies are properly configured.</p>
     *
     * @param context the TaskContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateExecutionDependencies(TaskContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("TaskContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // For active tasks, validate execution dependencies
        if (context.state() == ContextState.ACTIVE) {
            if (context.executionId() == null || context.executionId().isBlank()) {
                violations.add("Active task must have a valid executionId");
            }
            if (context.operationName() == null || context.operationName().isBlank()) {
                violations.add("Active task must have a valid operationName");
            }
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "state", context.state() != null ? context.state().name() : "null",
                        "executionId", context.executionId() != null ? context.executionId() : "null",
                        "operationName", context.operationName() != null ? context.operationName() : "null"
                )
        );
    }

    /**
     * Validates runtime metadata.
     *
     * <p>Validates that runtime metadata is present and valid.</p>
     *
     * @param context the TaskContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateRuntimeMetadata(TaskContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("TaskContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate data map
        if (context.data() == null) {
            violations.add("Task metadata (data) must not be null");
        }

        // Validate priority is present
        if (context.priority() == null) {
            violations.add("Task priority must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "hasMetadata", context.data() != null && !context.data().isEmpty(),
                        "dataSize", context.data() != null ? context.data().size() : 0,
                        "priority", context.priority() != null ? context.priority().name() : "null"
                )
        );
    }
}