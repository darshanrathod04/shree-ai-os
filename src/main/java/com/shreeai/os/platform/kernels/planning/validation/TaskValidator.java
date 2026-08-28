package com.shreeai.os.platform.kernels.planning.validation;

import com.shreeai.os.platform.kernels.planning.model.Task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>TaskValidator</b>
 *
 * <p>Stateless validator for {@link Task} models within the Planning Kernel.
 * This validator performs structural validation only — it never generates,
 * sequences, or executes tasks.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates task identifier presence.</li>
 *   <li>Validates task requirements reference.</li>
 *   <li>Validates priority reference.</li>
 *   <li>Validates metadata structural integrity.</li>
 *   <li>Verifies constructor invariants.</li>
 *   <li>Must never generate or sequence tasks.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Deterministic — same inputs always produce the same result.</li>
 *   <li>Thread-safe — no mutable fields.</li>
 *   <li>Read-only — never modifies models.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-103, EIO-ARCH-001</p>
 */
public final class TaskValidator {

    private TaskValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link Task} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Task must not be {@code null}</li>
     *   <li>PlanningId must not be {@code null}</li>
     *   <li>Description must not be {@code null}</li>
     *   <li>TaskRequirements must not be {@code null}</li>
     *   <li>Priority must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param task the task to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code task} is {@code null}
     */
    public static PlanningValidationResult validateTask(Task task) {
        if (task == null) {
            throw new NullPointerException("Task must not be null");
        }

        List<String> violations = new ArrayList<>();

        // PlanningId must not be null
        if (task.planningId() == null) {
            violations.add("Task planningId must not be null");
        }

        // Description must not be null
        if (task.description() == null) {
            violations.add("Task description must not be null");
        }

        // TaskRequirements must not be null
        if (task.requirements() == null) {
            violations.add("Task requirements must not be null");
        }

        // Priority must not be null
        if (task.priority() == null) {
            violations.add("Task priority must not be null");
        }

        // Metadata map must not be null
        if (task.metadata() == null) {
            violations.add("Task metadata map must not be null");
        }

        // Recurse into sub-validations
        if (task.planningId() != null) {
            var idResult = PlanningValidator.validatePlanningId(task.planningId());
            if (!idResult.isValid()) {
                for (String v : idResult.violations()) {
                    violations.add("Task planningId: " + v);
                }
            }
        }

        if (task.priority() != null) {
            var priResult = PriorityValidator.validatePriority(task.priority());
            if (!priResult.isValid()) {
                for (String v : priResult.violations()) {
                    violations.add("Task priority: " + v);
                }
            }
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "TaskValidator.validateTask")
        );
    }
}