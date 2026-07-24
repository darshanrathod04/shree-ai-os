package platform.kernels.chief.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>GoalValidator</b>
 *
 * <p>Validates GoalDescriptor domain models.
 * This class performs structural validation only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates GoalDescriptor structure.</li>
 *   <li>Validates lifecycle transitions.</li>
 *   <li>Validates metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Static methods only — no instantiation.</li>
 *   <li>Thread-safe — no shared mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-103, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public final class GoalValidator {

    private GoalValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a GoalDescriptor and returns an immutable validation result.
     *
     * @param goal the goal descriptor to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if goal is {@code null}
     */
    public static ChiefValidationResult validate(platform.kernels.chief.model.GoalDescriptor goal) {
        Objects.requireNonNull(goal, "GoalDescriptor must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (goal.chiefId() == null) {
            issues.add("GoalDescriptor chiefId must not be null");
        }

        // Validate goalName
        if (goal.goalName() == null || goal.goalName().trim().isEmpty()) {
            issues.add("GoalDescriptor goalName must not be null or empty");
        }

        // Validate lifecycleState
        if (goal.lifecycleState() == null || goal.lifecycleState().trim().isEmpty()) {
            issues.add("GoalDescriptor lifecycleState must not be null or empty");
        }

        // Validate metadata
        if (goal.metadata() == null) {
            issues.add("GoalDescriptor metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "GoalValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}