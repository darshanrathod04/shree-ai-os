package com.shreeai.os.platform.kernels.chief.validation;

import com.shreeai.os.platform.kernels.chief.model.DelegationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DelegationValidator</b>
 *
 * <p>Validates DelegationResult domain models.
 * This class performs structural validation only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates DelegationResult structure.</li>
 *   <li>Validates target kernel references.</li>
 *   <li>Validates delegation structure.</li>
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
public final class DelegationValidator {

    private DelegationValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a DelegationResult and returns an immutable validation result.
     *
     * @param result the delegation result to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if result is {@code null}
     */
    public static ChiefValidationResult validate(DelegationResult result) {
        Objects.requireNonNull(result, "DelegationResult must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (result.chiefId() == null) {
            issues.add("DelegationResult chiefId must not be null");
        }

        // Validate taskId
        if (result.taskId() == null || result.taskId().trim().isEmpty()) {
            issues.add("DelegationResult taskId must not be null or empty");
        }

        // Validate targetKernel
        if (result.targetKernel() == null || result.targetKernel().trim().isEmpty()) {
            issues.add("DelegationResult targetKernel must not be null or empty");
        }

        // Validate delegationStatus
        if (result.delegationStatus() == null || result.delegationStatus().trim().isEmpty()) {
            issues.add("DelegationResult delegationStatus must not be null or empty");
        }

        // Validate delegatedAt
        if (result.delegatedAt() == null) {
            issues.add("DelegationResult delegatedAt must not be null");
        }

        // Validate metadata
        if (result.metadata() == null) {
            issues.add("DelegationResult metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "DelegationValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}