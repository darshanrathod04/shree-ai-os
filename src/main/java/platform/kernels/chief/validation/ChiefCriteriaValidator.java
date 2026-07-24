package platform.kernels.chief.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefCriteriaValidator</b>
 *
 * <p>Validates ChiefRequest, ChiefResponse, ChiefMetrics, and ChiefSnapshot domain models.
 * This class performs structural validation only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates ChiefRequest structure.</li>
 *   <li>Validates ChiefResponse structure.</li>
 *   <li>Validates ChiefMetrics structure.</li>
 *   <li>Validates ChiefSnapshot structure.</li>
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
public final class ChiefCriteriaValidator {

    private ChiefCriteriaValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates a ChiefRequest and returns an immutable validation result.
     *
     * @param request the chief request to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ChiefValidationResult validate(platform.kernels.chief.model.ChiefRequest request) {
        Objects.requireNonNull(request, "ChiefRequest must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (request.chiefId() == null) {
            issues.add("ChiefRequest chiefId must not be null");
        }

        // Validate requestType
        if (request.requestType() == null || request.requestType().trim().isEmpty()) {
            issues.add("ChiefRequest requestType must not be null or empty");
        }

        // Validate payload
        if (request.payload() == null) {
            issues.add("ChiefRequest payload must not be null");
        }

        // Validate metadata
        if (request.metadata() == null) {
            issues.add("ChiefRequest metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefCriteriaValidator",
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
    public static ChiefValidationResult validate(platform.kernels.chief.model.ChiefResponse response) {
        Objects.requireNonNull(response, "ChiefResponse must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (response.chiefId() == null) {
            issues.add("ChiefResponse chiefId must not be null");
        }

        // Validate message
        if (response.message() == null) {
            issues.add("ChiefResponse message must not be null");
        }

        // Validate completedAt
        if (response.completedAt() == null) {
            issues.add("ChiefResponse completedAt must not be null");
        }

        // Validate metadata
        if (response.metadata() == null) {
            issues.add("ChiefResponse metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefCriteriaValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }

    /**
     * Validates a ChiefMetrics and returns an immutable validation result.
     *
     * @param metrics the chief metrics to validate (must not be {@code null})
     * @return immutable validation result
     * @throws IllegalArgumentException if metrics is {@code null}
     */
    public static ChiefValidationResult validate(platform.kernels.chief.model.ChiefMetrics metrics) {
        Objects.requireNonNull(metrics, "ChiefMetrics must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (metrics.chiefId() == null) {
            issues.add("ChiefMetrics chiefId must not be null");
        }

        // Validate measuredAt
        if (metrics.measuredAt() == null) {
            issues.add("ChiefMetrics measuredAt must not be null");
        }

        // Validate metadata
        if (metrics.metadata() == null) {
            issues.add("ChiefMetrics metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefCriteriaValidator",
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
    public static ChiefValidationResult validate(platform.kernels.chief.model.ChiefSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "ChiefSnapshot must not be null");

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate chiefId
        if (snapshot.chiefId() == null) {
            issues.add("ChiefSnapshot chiefId must not be null");
        }

        // Validate request
        if (snapshot.request() == null) {
            issues.add("ChiefSnapshot request must not be null");
        }

        // Validate response
        if (snapshot.response() == null) {
            issues.add("ChiefSnapshot response must not be null");
        }

        // Validate coordinationState
        if (snapshot.coordinationState() == null) {
            issues.add("ChiefSnapshot coordinationState must not be null");
        }

        // Validate metrics
        if (snapshot.metrics() == null) {
            issues.add("ChiefSnapshot metrics must not be null");
        }

        // Validate capturedAt
        if (snapshot.capturedAt() == null) {
            issues.add("ChiefSnapshot capturedAt must not be null");
        }

        // Validate metadata
        if (snapshot.metadata() == null) {
            issues.add("ChiefSnapshot metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        Map<String, Object> metadata = Map.of(
                "validator", "ChiefCriteriaValidator",
                "validatedAt", System.currentTimeMillis()
        );

        return new ChiefValidationResult(valid, issues, warnings, metadata);
    }
}