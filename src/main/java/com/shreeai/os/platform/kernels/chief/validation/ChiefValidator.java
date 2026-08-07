package com.shreeai.os.platform.kernels.chief.validation;

import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.model.ChiefSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefValidator</b>
 *
 * <p>Primary validation entry point for the Chief Kernel.
 * This class coordinates validation across all specialized validators
 * and aggregates results into an immutable ChiefValidationResult.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Delegates validation to specialized validators.</li>
 *   <li>Aggregates validation results.</li>
 *   <li>Returns immutable validation result.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Static methods only — no instantiation.</li>
 *   <li>Thread-safe — no shared mutable state.</li>
 *   <li>Deterministic — same input produces same output.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-103, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public final class ChiefValidator {

    private ChiefValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a ChiefRequest and returns an immutable validation result.
     *
     * <p>This method delegates to specialized validators and aggregates
     * all validation results.</p>
     *
     * @param request the chief request to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ChiefValidationResult validate(ChiefRequest request) {
        Objects.requireNonNull(request, "ChiefRequest must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Delegate to specialized validators
        ChiefValidationResult criteriaResult = ChiefCriteriaValidator.validate(request);
        issues.addAll(criteriaResult.issues());
        warnings.addAll(criteriaResult.warnings());

        if (request.context() != null) {
            ChiefValidationResult decisionResult = DecisionValidator.validate(request.context());
            issues.addAll(decisionResult.issues());
            warnings.addAll(decisionResult.warnings());
        }

        if (request.goal() != null) {
            ChiefValidationResult goalResult = GoalValidator.validate(request.goal());
            issues.addAll(goalResult.issues());
            warnings.addAll(goalResult.warnings());
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }

    /**
     * Validates a ChiefResponse and returns an immutable validation result.
     *
     * @param response the chief response to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if response is {@code null}
     */
    public static ChiefValidationResult validate(ChiefResponse response) {
        Objects.requireNonNull(response, "ChiefResponse must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        ChiefValidationResult criteriaResult = ChiefCriteriaValidator.validate(response);
        issues.addAll(criteriaResult.issues());
        warnings.addAll(criteriaResult.warnings());

        if (response.decisionResult() != null) {
            ChiefValidationResult decisionResult = DecisionValidator.validate(response.decisionResult());
            issues.addAll(decisionResult.issues());
            warnings.addAll(decisionResult.warnings());
        }

        if (response.delegationResult() != null) {
            ChiefValidationResult delegationResult = DelegationValidator.validate(response.delegationResult());
            issues.addAll(delegationResult.issues());
            warnings.addAll(delegationResult.warnings());
        }

        if (response.coordinationState() != null) {
            ChiefValidationResult coordinationResult = CoordinationValidator.validate(response.coordinationState());
            issues.addAll(coordinationResult.issues());
            warnings.addAll(coordinationResult.warnings());
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }

    /**
     * Validates a ChiefSnapshot and returns an immutable validation result.
     *
     * @param snapshot the chief snapshot to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if snapshot is {@code null}
     */
    public static ChiefValidationResult validate(ChiefSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "ChiefSnapshot must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate all components
        ChiefValidationResult requestResult = validate(snapshot.request());
        issues.addAll(requestResult.issues());
        warnings.addAll(requestResult.warnings());

        if (snapshot.response() != null) {
            ChiefValidationResult responseResult = validate(snapshot.response());
            issues.addAll(responseResult.issues());
            warnings.addAll(responseResult.warnings());
        }

        if (snapshot.coordinationState() != null) {
            ChiefValidationResult coordinationResult = CoordinationValidator.validate(snapshot.coordinationState());
            issues.addAll(coordinationResult.issues());
            warnings.addAll(coordinationResult.warnings());
        }

        if (snapshot.metrics() != null) {
            ChiefValidationResult metricsResult = ChiefCriteriaValidator.validate(snapshot.metrics());
            issues.addAll(metricsResult.issues());
            warnings.addAll(metricsResult.warnings());
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}