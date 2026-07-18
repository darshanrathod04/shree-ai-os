package platform.kernels.planning.validation;

import platform.kernels.planning.model.Priority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>PriorityValidator</b>
 *
 * <p>Stateless validator for {@link Priority} models within the Planning Kernel.
 * This validator performs structural validation only — it never computes priorities
 * or evaluates priority correctness.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates priority definition structure.</li>
 *   <li>Validates required field presence.</li>
 *   <li>Validates metadata structural integrity.</li>
 *   <li>Verifies constructor invariants.</li>
 *   <li>Must never compute priorities.</li>
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
public final class PriorityValidator {

    private PriorityValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link Priority} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Priority must not be {@code null}</li>
     *   <li>Level must not be {@code null}</li>
     *   <li>Urgency must not be {@code null}</li>
     *   <li>Importance must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param priority the priority to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code priority} is {@code null}
     */
    public static PlanningValidationResult validatePriority(Priority priority) {
        if (priority == null) {
            throw new NullPointerException("Priority must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (priority.level() == null) {
            violations.add("Priority level must not be null");
        }
        if (priority.urgency() == null) {
            violations.add("Priority urgency must not be null");
        }
        if (priority.importance() == null) {
            violations.add("Priority importance must not be null");
        }
        if (priority.metadata() == null) {
            violations.add("Priority metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "PriorityValidator.validatePriority")
        );
    }
}