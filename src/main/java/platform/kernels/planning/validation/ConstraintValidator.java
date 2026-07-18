package platform.kernels.planning.validation;

import platform.kernels.planning.model.GoalConstraints;
import platform.kernels.planning.model.PlanningConstraints;
import platform.kernels.planning.model.SchedulingConstraints;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ConstraintValidator</b>
 *
 * <p>Stateless validator for planning constraint models within the Planning Kernel.
 * This validator performs structural validation only — it never evaluates constraint
 * feasibility or optimization quality.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates {@link PlanningConstraints} structural integrity.</li>
 *   <li>Validates {@link GoalConstraints} structural integrity.</li>
 *   <li>Validates {@link SchedulingConstraints} structural integrity.</li>
 *   <li>Verifies constraint completeness and immutable collection integrity.</li>
 *   <li>Must never evaluate constraint feasibility.</li>
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
public final class ConstraintValidator {

    private ConstraintValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link PlanningConstraints} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>PlanningConstraints must not be {@code null}</li>
     *   <li>Time constraints map must not be {@code null}</li>
     *   <li>Dependency constraints map must not be {@code null}</li>
     *   <li>Policy constraints map must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param constraints the planning constraints to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code constraints} is {@code null}
     */
    public static PlanningValidationResult validatePlanningConstraints(PlanningConstraints constraints) {
        if (constraints == null) {
            throw new NullPointerException("PlanningConstraints must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (constraints.timeConstraints() == null) {
            violations.add("PlanningConstraints timeConstraints map must not be null");
        }
        if (constraints.dependencyConstraints() == null) {
            violations.add("PlanningConstraints dependencyConstraints map must not be null");
        }
        if (constraints.policyConstraints() == null) {
            violations.add("PlanningConstraints policyConstraints map must not be null");
        }
        if (constraints.metadata() == null) {
            violations.add("PlanningConstraints metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ConstraintValidator.validatePlanningConstraints")
        );
    }

    /**
     * Validates a {@link GoalConstraints} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>GoalConstraints must not be {@code null}</li>
     *   <li>Completion constraints map must not be {@code null}</li>
     *   <li>Dependency limits map must not be {@code null}</li>
     *   <li>Resource limits map must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param constraints the goal constraints to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code constraints} is {@code null}
     */
    public static PlanningValidationResult validateGoalConstraints(GoalConstraints constraints) {
        if (constraints == null) {
            throw new NullPointerException("GoalConstraints must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (constraints.completionConstraints() == null) {
            violations.add("GoalConstraints completionConstraints map must not be null");
        }
        if (constraints.dependencyLimits() == null) {
            violations.add("GoalConstraints dependencyLimits map must not be null");
        }
        if (constraints.resourceLimits() == null) {
            violations.add("GoalConstraints resourceLimits map must not be null");
        }
        if (constraints.metadata() == null) {
            violations.add("GoalConstraints metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ConstraintValidator.validateGoalConstraints")
        );
    }

    /**
     * Validates a {@link SchedulingConstraints} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>SchedulingConstraints must not be {@code null}</li>
     *   <li>Timing rules map must not be {@code null}</li>
     *   <li>Ordering rules map must not be {@code null}</li>
     *   <li>Dependency rules map must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param constraints the scheduling constraints to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code constraints} is {@code null}
     */
    public static PlanningValidationResult validateSchedulingConstraints(SchedulingConstraints constraints) {
        if (constraints == null) {
            throw new NullPointerException("SchedulingConstraints must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (constraints.timingRules() == null) {
            violations.add("SchedulingConstraints timingRules map must not be null");
        }
        if (constraints.orderingRules() == null) {
            violations.add("SchedulingConstraints orderingRules map must not be null");
        }
        if (constraints.dependencyRules() == null) {
            violations.add("SchedulingConstraints dependencyRules map must not be null");
        }
        if (constraints.metadata() == null) {
            violations.add("SchedulingConstraints metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ConstraintValidator.validateSchedulingConstraints")
        );
    }
}