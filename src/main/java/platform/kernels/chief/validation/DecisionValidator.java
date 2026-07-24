package platform.kernels.chief.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DecisionValidator</b>
 *
 * <p>Validates DecisionContext and DecisionResult domain models.
 * This class performs structural validation only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates DecisionContext structure.</li>
 *   <li>Validates DecisionResult structure.</li>
 *   <li>Validates participating kernel references.</li>
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
public final class DecisionValidator {

    private DecisionValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a DecisionContext and returns an immutable validation result.
     *
     * @param context the decision context to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if context is {@code null}
     */
    public static ChiefValidationResult validate(platform.kernels.chief.model.DecisionContext context) {
        Objects.requireNonNull(context, "DecisionContext must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (context.chiefId() == null) {
            issues.add("DecisionContext chiefId must not be null");
        }

        // Validate decisionType
        if (context.decisionType() == null || context.decisionType().trim().isEmpty()) {
            issues.add("DecisionContext decisionType must not be null or empty");
        }

        // Validate participatingKernels
        if (context.participatingKernels() == null) {
            issues.add("DecisionContext participatingKernels must not be null");
        } else if (context.participatingKernels().isEmpty()) {
            warnings.add("DecisionContext participatingKernels is empty");
        }

        // Validate contextualData
        if (context.contextualData() == null) {
            issues.add("DecisionContext contextualData must not be null");
        }

        // Validate metadata
        if (context.metadata() == null) {
            issues.add("DecisionContext metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "DecisionValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }

    /**
     * Validates a DecisionResult and returns an immutable validation result.
     *
     * @param result the decision result to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if result is {@code null}
     */
    public static ChiefValidationResult validate(platform.kernels.chief.model.DecisionResult result) {
        Objects.requireNonNull(result, "DecisionResult must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (result.chiefId() == null) {
            issues.add("DecisionResult chiefId must not be null");
        }

        // Validate coordinationPath
        if (result.coordinationPath() == null || result.coordinationPath().trim().isEmpty()) {
            issues.add("DecisionResult coordinationPath must not be null or empty");
        }

        // Validate selectedKernels
        if (result.selectedKernels() == null) {
            issues.add("DecisionResult selectedKernels must not be null");
        } else if (result.selectedKernels().isEmpty()) {
            warnings.add("DecisionResult selectedKernels is empty");
        }

        // Validate decidedAt
        if (result.decidedAt() == null) {
            issues.add("DecisionResult decidedAt must not be null");
        }

        // Validate metadata
        if (result.metadata() == null) {
            issues.add("DecisionResult metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "DecisionValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}