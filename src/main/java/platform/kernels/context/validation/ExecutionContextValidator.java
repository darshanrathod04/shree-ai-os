package platform.kernels.context.validation;

import platform.kernels.context.model.ContextState;
import platform.kernels.context.model.ContextType;
import platform.kernels.context.model.ExecutionContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ExecutionContextValidator</b>
 *
 * <p>A utility validator for ExecutionContext domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates execution status and runtime state.</li>
 *   <li>Validates active operations and execution metadata.</li>
 *   <li>Validates runtime state consistency.</li>
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
 * @see ExecutionContext
 */
public final class ExecutionContextValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ExecutionContextValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates an ExecutionContext instance.
     *
     * <p>Validates all ExecutionContext fields including execution status,
     * runtime state, active operations, and execution metadata.</p>
     *
     * @param context the ExecutionContext to validate (must not be null)
     * @return the validation result
     * @throws NullPointerException if context is null
     */
    public static ContextValidationResult validate(ExecutionContext context) {
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

        // Validate type is EXECUTION
        if (context.type() != ContextType.EXECUTION) {
            violations.add("ExecutionContext type must be EXECUTION, got: " + context.type());
        }

        // Validate executionId
        if (context.executionId() == null || context.executionId().isBlank()) {
            violations.add("ExecutionContext executionId must not be null or blank");
        }

        // Validate operationName
        if (context.operationName() == null || context.operationName().isBlank()) {
            violations.add("ExecutionContext operationName must not be null or blank");
        }

        // Validate stepNumber is not negative
        if (context.stepNumber() < 0) {
            violations.add("ExecutionContext stepNumber must not be negative, got: " + context.stepNumber());
        }

        // Validate execution status
        if (context.state() == ContextState.ACTIVE) {
            // Active execution should have valid operation name and execution ID
            if (context.operationName() == null || context.operationName().isBlank()) {
                violations.add("Active execution must have a valid operationName");
            }
            if (context.executionId() == null || context.executionId().isBlank()) {
                violations.add("Active execution must have a valid executionId");
            }
        }

        // Validate snapshot consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("ExecutionContext updatedAt must not be before createdAt");
            }
        }

        Map<String, Object> metadata = Map.of(
                "contextType", context.type() != null ? context.type().name() : "null",
                "executionId", context.executionId() != null ? context.executionId() : "null",
                "operationName", context.operationName() != null ? context.operationName() : "null",
                "stepNumber", context.stepNumber()
        );

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                metadata
        );
    }

    /**
     * Validates execution status.
     *
     * <p>Validates that the execution status is valid and consistent.</p>
     *
     * @param context the ExecutionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateExecutionStatus(ExecutionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ExecutionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate state is not null
        if (context.state() == null) {
            violations.add("Execution state must not be null");
        }

        // Validate executionId is present
        if (context.executionId() == null || context.executionId().isBlank()) {
            violations.add("Execution ID must not be null or blank");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "state", context.state() != null ? context.state().name() : "null",
                        "executionId", context.executionId() != null ? context.executionId() : "null"
                )
        );
    }

    /**
     * Validates runtime state.
     *
     * <p>Validates that the runtime state is consistent and valid.</p>
     *
     * @param context the ExecutionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateRuntimeState(ExecutionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ExecutionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate operation name
        if (context.operationName() == null || context.operationName().isBlank()) {
            violations.add("Operation name must not be null or blank");
        }

        // Validate step number
        if (context.stepNumber() < 0) {
            violations.add("Step number must not be negative, got: " + context.stepNumber());
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "operationName", context.operationName() != null ? context.operationName() : "null",
                        "stepNumber", context.stepNumber()
                )
        );
    }

    /**
     * Validates active operations.
     *
     * <p>Validates that active operations are properly configured.</p>
     *
     * @param context the ExecutionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateActiveOperations(ExecutionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ExecutionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // For active contexts, validate operation details
        if (context.state() == ContextState.ACTIVE) {
            if (context.operationName() == null || context.operationName().isBlank()) {
                violations.add("Active execution must have a valid operationName");
            }
            if (context.executionId() == null || context.executionId().isBlank()) {
                violations.add("Active execution must have a valid executionId");
            }
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "state", context.state() != null ? context.state().name() : "null",
                        "operationName", context.operationName() != null ? context.operationName() : "null",
                        "executionId", context.executionId() != null ? context.executionId() : "null"
                )
        );
    }

    /**
     * Validates execution metadata.
     *
     * <p>Validates that execution metadata is present and valid.</p>
     *
     * @param context the ExecutionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateExecutionMetadata(ExecutionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ExecutionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate data map
        if (context.data() == null) {
            violations.add("Execution metadata (data) must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "hasMetadata", context.data() != null && !context.data().isEmpty(),
                        "dataSize", context.data() != null ? context.data().size() : 0
                )
        );
    }
}