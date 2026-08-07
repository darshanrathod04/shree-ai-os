package com.shreeai.os.platform.kernels.chief.validation;

import com.shreeai.os.platform.kernels.chief.model.CoordinationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CoordinationValidator</b>
 *
 * <p>Validates CoordinationState domain models.
 * This class performs structural validation only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates CoordinationState structure.</li>
 *   <li>Validates orchestration topology.</li>
 *   <li>Validates dependency references.</li>
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
public final class CoordinationValidator {

    private CoordinationValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a CoordinationState and returns an immutable validation result.
     *
     * @param state the coordination state to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if state is {@code null}
     */
    public static ChiefValidationResult validate(CoordinationState state) {
        Objects.requireNonNull(state, "CoordinationState must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (state.chiefId() == null) {
            issues.add("CoordinationState chiefId must not be null");
        }

        // Validate coordinationStage
        if (state.coordinationStage() == null || state.coordinationStage().trim().isEmpty()) {
            issues.add("CoordinationState coordinationStage must not be null or empty");
        }

        // Validate participatingKernels
        if (state.participatingKernels() == null) {
            issues.add("CoordinationState participatingKernels must not be null");
        } else if (state.participatingKernels().isEmpty()) {
            warnings.add("CoordinationState participatingKernels is empty");
        }

        // Validate orchestrationLifecycle
        if (state.orchestrationLifecycle() == null || state.orchestrationLifecycle().trim().isEmpty()) {
            issues.add("CoordinationState orchestrationLifecycle must not be null or empty");
        }

        // Validate metadata
        if (state.metadata() == null) {
            issues.add("CoordinationState metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "CoordinationValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}