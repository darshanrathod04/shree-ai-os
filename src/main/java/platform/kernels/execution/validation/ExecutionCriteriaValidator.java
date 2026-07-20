package platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.execution.model.ExecutionRequest;

/**
 * <b>ExecutionCriteriaValidator</b>
 *
 * <p>Validates execution criteria-related structural integrity in execution requests.
 * This validator ensures execution options, context, and metrics structure are well-formed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates execution options.</li>
 *   <li>Validates execution context.</li>
 *   <li>Validates execution metrics structure.</li>
 *   <li>Validates immutable collections.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Thread-safe — all methods are static.</li>
 *   <li>Deterministic — same input produces same output.</li>
 *   <li>Read-only — no state mutation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-103, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public final class ExecutionCriteriaValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ExecutionCriteriaValidator() {
        throw new UnsupportedOperationException("ExecutionCriteriaValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates execution criteria-related aspects of an execution request.
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Execution options structure</li>
     *   <li>Execution context structure</li>
     *   <li>Execution metrics structure</li>
     *   <li>Immutable collections integrity</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the validation result for execution criteria-related checks
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ExecutionCriteriaValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate execution options
        validateExecutionOptions(request, violations, metadata);

        // Validate execution context
        validateExecutionContext(request, violations, metadata);

        // Validate execution metrics structure
        validateExecutionMetrics(request, violations, metadata);

        // Validate immutable collections
        validateImmutableCollections(request, violations);

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates execution options.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateExecutionOptions(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        if (request.options() == null) {
            violations.add("ExecutionCriteriaValidator: execution options must not be null");
            return;
        }

        // Validate timeout
        long timeoutMs = request.options().timeoutMs();
        if (timeoutMs <= 0) {
            violations.add("ExecutionCriteriaValidator: timeoutMs must be positive");
        }
        metadata.put("timeoutMs", timeoutMs);

        // Validate retry configuration
        int maxRetries = request.options().maxRetries();
        if (maxRetries < 0) {
            violations.add("ExecutionCriteriaValidator: maxRetries must not be negative");
        }
        metadata.put("maxRetries", maxRetries);

        long retryDelayMs = request.options().retryDelayMs();
        if (retryDelayMs < 0) {
            violations.add("ExecutionCriteriaValidator: retryDelayMs must not be negative");
        }
        metadata.put("retryDelayMs", retryDelayMs);

        // Validate options map
        Map<String, Object> options = request.options().options();
        if (options == null || options.isEmpty()) {
            violations.add("ExecutionCriteriaValidator: options map must not be null or empty");
        } else {
            metadata.put("optionsCount", options.size());
        }
    }

    /**
     * Validates execution context.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateExecutionContext(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        if (request.context() == null) {
            violations.add("ExecutionCriteriaValidator: execution context must not be null");
            return;
        }

        // Validate execution identifier
        if (request.context().executionId() == null) {
            violations.add("ExecutionCriteriaValidator: context executionId must not be null");
        }

        // Validate plan identifier
        String planId = request.context().planId();
        if (planId == null || planId.trim().isEmpty()) {
            violations.add("ExecutionCriteriaValidator: context planId must not be null or empty");
        }
        metadata.put("planId", planId);

        // Validate objective identifier
        String objectiveId = request.context().objectiveId();
        if (objectiveId == null || objectiveId.trim().isEmpty()) {
            violations.add("ExecutionCriteriaValidator: context objectiveId must not be null or empty");
        }
        metadata.put("objectiveId", objectiveId);

        // Validate context data
        Map<String, Object> contextData = request.context().contextData();
        if (contextData == null || contextData.isEmpty()) {
            violations.add("ExecutionCriteriaValidator: context data must not be null or empty");
        } else {
            metadata.put("contextDataCount", contextData.size());
        }

        // Validate priority
        int priority = request.context().priority();
        metadata.put("priority", priority);
    }

    /**
     * Validates execution metrics structure.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateExecutionMetrics(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        // Validate metrics in parameters if present
        if (request.parameters() != null) {
            Object metrics = request.parameters().get("metrics");
            if (metrics != null) {
                metadata.put("hasMetrics", true);

                // Structural validation of metrics
                if (metrics instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metricsMap = (Map<String, Object>) metrics;
                    metadata.put("metricsKeys", metricsMap.keySet().size());

                    // Validate required metrics fields
                    if (!metricsMap.containsKey("startTime")) {
                        violations.add("ExecutionCriteriaValidator: metrics must contain startTime");
                    }
                    if (!metricsMap.containsKey("endTime")) {
                        violations.add("ExecutionCriteriaValidator: metrics must contain endTime");
                    }
                    if (!metricsMap.containsKey("durationMs")) {
                        violations.add("ExecutionCriteriaValidator: metrics must contain durationMs");
                    }
                }
            }
        }

        // Validate execution status if present in parameters
        if (request.parameters() != null && request.parameters().containsKey("status")) {
            Object status = request.parameters().get("status");
            if (status != null) {
                metadata.put("hasStatus", true);
                // Structural check - status presence is validated
            }
        }
    }

    /**
     * Validates immutable collections.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateImmutableCollections(ExecutionRequest request, List<String> violations) {
        // Validate that collections are properly immutable (structural check)
        if (request.parameters() != null) {
            try {
                request.parameters().put("test", "value");
                violations.add("ExecutionCriteriaValidator: parameters map must be unmodifiable");
            } catch (UnsupportedOperationException e) {
                // Expected - map is unmodifiable
            }
        }

        if (request.context() != null && request.context().contextData() != null) {
            try {
                request.context().contextData().put("test", "value");
                violations.add("ExecutionCriteriaValidator: contextData map must be unmodifiable");
            } catch (UnsupportedOperationException e) {
                // Expected - map is unmodifiable
            }
        }

        if (request.options() != null && request.options().options() != null) {
            try {
                request.options().options().put("test", "value");
                violations.add("ExecutionCriteriaValidator: options map must be unmodifiable");
            } catch (UnsupportedOperationException e) {
                // Expected - map is unmodifiable
            }
        }
    }
}