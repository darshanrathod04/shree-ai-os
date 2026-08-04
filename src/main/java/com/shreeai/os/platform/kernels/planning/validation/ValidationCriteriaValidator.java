package com.shreeai.os.platform.kernels.planning.validation;

import com.shreeai.os.platform.kernels.planning.model.ValidationCriteria;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ValidationCriteriaValidator</b>
 *
 * <p>Stateless validator for {@link ValidationCriteria} models within the Planning Kernel.
 * This validator performs structural validation only — it never validates a plan
 * semantically or evaluates plan quality.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates validation criteria definition structure.</li>
 *   <li>Validates required conditions presence.</li>
 *   <li>Validates metadata structural integrity.</li>
 *   <li>Verifies constructor invariants.</li>
 *   <li>Must never validate a plan semantically.</li>
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
public final class ValidationCriteriaValidator {

    private ValidationCriteriaValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a {@link ValidationCriteria} instance.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>ValidationCriteria must not be {@code null}</li>
     *   <li>Validation rules map must not be {@code null}</li>
     *   <li>Required conditions map must not be {@code null}</li>
     *   <li>Completeness requirements map must not be {@code null}</li>
     *   <li>Metadata map must not be {@code null}</li>
     * </ul>
     *
     * @param criteria the validation criteria to validate
     * @return a {@link PlanningValidationResult} containing any violations
     * @throws NullPointerException if {@code criteria} is {@code null}
     */
    public static PlanningValidationResult validateValidationCriteria(ValidationCriteria criteria) {
        if (criteria == null) {
            throw new NullPointerException("ValidationCriteria must not be null");
        }

        List<String> violations = new ArrayList<>();

        if (criteria.validationRules() == null) {
            violations.add("ValidationCriteria validationRules map must not be null");
        }
        if (criteria.requiredConditions() == null) {
            violations.add("ValidationCriteria requiredConditions map must not be null");
        }
        if (criteria.completenessRequirements() == null) {
            violations.add("ValidationCriteria completenessRequirements map must not be null");
        }
        if (criteria.metadata() == null) {
            violations.add("ValidationCriteria metadata map must not be null");
        }

        return new PlanningValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("validator", "ValidationCriteriaValidator.validateValidationCriteria")
        );
    }
}