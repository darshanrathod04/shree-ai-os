package com.shreeai.os.platform.kernels.planning.validation;

import com.shreeai.os.platform.kernels.planning.model.Schedule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ScheduleValidator</b>
 *
 * <p>Stateless validator for {@link Schedule} models within the Planning Kernel.
 * This validator performs structural validation only — it never optimizes schedules
 * or evaluates scheduling quality.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates schedule structure.</li>
 *   <li>Validates scheduling constraint references.</li>
 *   <li>Verifies immutable collection integrity.</li>
 *   <li>Validates metadata structural integrity.</li>
 *   <li>Must never optimize schedules.</li>
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
public final class ScheduleValidator {

    private ScheduleValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link Schedule} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Schedule must not be {@code null}</li>
     *   <li>Planned sequence must not be {@code null}</li>
     *   <li>SchedulingConstraints must not be {@code null}</li>
     *   <li>Resource allocation references must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param schedule the schedule to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code schedule} is {@code null}
     */
    public static PlanningValidationResult validateSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new NullPointerException("Schedule must not be null");
        }

        List<String> violations = new ArrayList<>();

        // Planned sequence must not be null
        if (schedule.plannedSequence() == null) {
            violations.add("Schedule plannedSequence must not be null");
        }

        // SchedulingConstraints must not be null
        if (schedule.schedulingConstraints() == null) {
            violations.add("Schedule schedulingConstraints must not be null");
        }

        // Resource allocation references must not be null
        if (schedule.resourceAllocationReferences() == null) {
            violations.add("Schedule resourceAllocationReferences must not be null");
        }

        // Metadata map must not be null
        if (schedule.metadata() == null) {
            violations.add("Schedule metadata map must not be null");
        }

        // Recurse into scheduling constraints if present
        if (schedule.schedulingConstraints() != null) {
            var conResult = ConstraintValidator.validateSchedulingConstraints(
                    schedule.schedulingConstraints());
            if (!conResult.isValid()) {
                for (String v : conResult.violations()) {
                    violations.add("Schedule schedulingConstraints: " + v);
                }
            }
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ScheduleValidator.validateSchedule")
        );
    }
}