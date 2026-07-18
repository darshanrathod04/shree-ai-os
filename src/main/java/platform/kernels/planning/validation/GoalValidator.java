package platform.kernels.planning.validation;

import platform.kernels.planning.model.Goal;
import platform.kernels.planning.model.PlanningId;
import platform.kernels.planning.model.PlanningObjective;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>GoalValidator</b>
 *
 * <p>Stateless validator for {@link Goal} models within the Planning Kernel.
 * This validator performs structural validation only — it never decomposes goals
 * or evaluates goal achievement strategies.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates {@link PlanningId} presence on a goal.</li>
 *   <li>Validates {@link PlanningObjective} presence on a goal.</li>
 *   <li>Validates constraint consistency on a goal.</li>
 *   <li>Validates metadata structural integrity.</li>
 *   <li>Verifies constructor invariants.</li>
 *   <li>Must never decompose goals.</li>
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
public final class GoalValidator {

    private GoalValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link Goal} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Goal must not be {@code null}</li>
     *   <li>PlanningId must not be {@code null}</li>
     *   <li>PlanningObjective must not be {@code null}</li>
     *   <li>GoalConstraints must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param goal the goal to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code goal} is {@code null}
     */
    public static PlanningValidationResult validateGoal(Goal goal) {
        if (goal == null) {
            throw new NullPointerException("Goal must not be null");
        }

        List<String> violations = new ArrayList<>();

        // PlanningId must not be null
        PlanningId planningId = goal.planningId();
        if (planningId == null) {
            violations.add("Goal planningId must not be null");
        }

        // PlanningObjective must not be null
        PlanningObjective objective = goal.objective();
        if (objective == null) {
            violations.add("Goal objective must not be null");
        }

        // GoalConstraints must not be null
        if (goal.constraints() == null) {
            violations.add("Goal constraints must not be null");
        }

        // Metadata map must not be null
        if (goal.metadata() == null) {
            violations.add("Goal metadata map must not be null");
        }

        // Recurse into sub-validations
        if (planningId != null) {
            var idResult = PlanningValidator.validatePlanningId(planningId);
            if (!idResult.isValid()) {
                for (String v : idResult.violations()) {
                    violations.add("Goal planningId: " + v);
                }
            }
        }

        if (objective != null) {
            var objResult = PlanningValidator.validatePlanningObjective(objective);
            if (!objResult.isValid()) {
                for (String v : objResult.violations()) {
                    violations.add("Goal objective: " + v);
                }
            }
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "GoalValidator.validateGoal")
        );
    }
}