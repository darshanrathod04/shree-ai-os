package com.shreeai.os.platform.kernels.planning.validation;

import com.shreeai.os.platform.kernels.planning.model.Goal;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.planning.model.Priority;
import com.shreeai.os.platform.kernels.planning.model.Schedule;
import com.shreeai.os.platform.kernels.planning.model.Task;
import com.shreeai.os.platform.kernels.planning.model.ValidationCriteria;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>PlanningValidator</b>
 *
 * <p>Entry point for Planning validation within the Planning Kernel.
 * This validator coordinates specialized validators and aggregates validation results
 * through a unified validation interface.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates specialized validators (Goal, Task, Schedule, etc.).</li>
 *   <li>Aggregates validation results from subordinate validators.</li>
 *   <li>Exposes a unified validation interface for planning models.</li>
 *   <li>Provides shared validation methods for {@link PlanningId} and
 *       {@link PlanningObjective} used by other validators.</li>
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
 *
 * @see GoalValidator
 * @see TaskValidator
 * @see ScheduleValidator
 * @see PriorityValidator
 * @see ConstraintValidator
 * @see ValidationCriteriaValidator
 */
public final class PlanningValidator {

    public PlanningValidator() {
        // Public constructor for service-layer instantiation
    }

    // -----------------------------------------------------------------------
    // PlanningId
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link PlanningId}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>PlanningId must not be {@code null}</li>
     *   <li>Value must not be {@code null} or blank</li>
     * </ul>
     *
     * @param planningId the planning identifier to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code planningId} is {@code null}
     */
    public static PlanningValidationResult validatePlanningId(PlanningId planningId) {
        if (planningId == null) {
            throw new NullPointerException("PlanningId must not be null");
        }

        List<String> violations = new ArrayList<>();

        String value = planningId.value();
        if (value == null || value.isBlank()) {
            violations.add("PlanningId value must not be null or blank");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "PlanningValidator.validatePlanningId")
        );
    }

    // -----------------------------------------------------------------------
    // PlanningObjective
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link PlanningObjective}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>PlanningObjective must not be {@code null}</li>
     *   <li>PlanningId must not be {@code null}</li>
     *   <li>Description must not be {@code null}</li>
     *   <li>Scope must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param objective the planning objective to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code objective} is {@code null}
     */
    public static PlanningValidationResult validatePlanningObjective(PlanningObjective objective) {
        if (objective == null) {
            throw new NullPointerException("PlanningObjective must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (objective.planningId() == null) {
            violations.add("PlanningObjective planningId must not be null");
        }
        if (objective.description() == null) {
            violations.add("PlanningObjective description must not be null");
        }
        if (objective.scope() == null) {
            violations.add("PlanningObjective scope must not be null");
        }
        if (objective.metadata() == null) {
            violations.add("PlanningObjective metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "PlanningValidator.validatePlanningObjective")
        );
    }

    // -----------------------------------------------------------------------
    // Goal
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link Goal} by delegating to {@link GoalValidator}.
     *
     * @param goal the goal to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code goal} is {@code null}
     */
    public static PlanningValidationResult validateGoal(Goal goal) {
        return GoalValidator.validateGoal(goal);
    }

    // -----------------------------------------------------------------------
    // Task
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link Task} by delegating to {@link TaskValidator}.
     *
     * @param task the task to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code task} is {@code null}
     */
    public static PlanningValidationResult validateTask(Task task) {
        return TaskValidator.validateTask(task);
    }

    // -----------------------------------------------------------------------
    // Schedule
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link Schedule} by delegating to {@link ScheduleValidator}.
     *
     * @param schedule the schedule to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code schedule} is {@code null}
     */
    public static PlanningValidationResult validateSchedule(Schedule schedule) {
        return ScheduleValidator.validateSchedule(schedule);
    }

    // -----------------------------------------------------------------------
    // Priority
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link Priority} by delegating to {@link PriorityValidator}.
     *
     * @param priority the priority to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code priority} is {@code null}
     */
    public static PlanningValidationResult validatePriority(Priority priority) {
        return PriorityValidator.validatePriority(priority);
    }

    // -----------------------------------------------------------------------
    // ValidationCriteria
    // -----------------------------------------------------------------------

    /**
     * Validates a {@link ValidationCriteria} by delegating to {@link ValidationCriteriaValidator}.
     *
     * @param criteria the validation criteria to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code criteria} is {@code null}
     */
    public static PlanningValidationResult validateValidationCriteria(ValidationCriteria criteria) {
        return ValidationCriteriaValidator.validateValidationCriteria(criteria);
    }
}