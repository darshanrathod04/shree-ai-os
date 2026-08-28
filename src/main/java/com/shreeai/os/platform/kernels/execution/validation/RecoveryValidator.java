package com.shreeai.os.platform.kernels.execution.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.RecoveryStrategy;

/**
 * <b>RecoveryValidator</b>
 *
 * <p>Validates recovery-related structural integrity in execution requests.
 * This validator ensures recovery strategy definitions and rollback configurations are well-formed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates recovery strategy definition.</li>
 *   <li>Validates rollback request structure.</li>
 *   <li>Validates retry configuration structure.</li>
 *   <li>Validates metadata integrity.</li>
 *   <li>Contains no recovery logic.</li>
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
public final class RecoveryValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private RecoveryValidator() {
        throw new UnsupportedOperationException("RecoveryValidator is a static utility class and cannot be instantiated");
    }

    /**
     * Validates recovery-related aspects of an execution request.
     *
     * <p><b>Validation Scope:</b></p>
     * <ul>
     *   <li>Recovery strategy definition</li>
     *   <li>Rollback request structure</li>
     *   <li>Retry configuration structure</li>
     *   <li>Metadata integrity</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * @param request the execution request to validate (must not be {@code null})
     * @return the validation result for recovery-related checks
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static ExecutionValidationResult validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RecoveryValidator validate request must not be null");
        }

        List<String> violations = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Validate recovery strategy
        validateRecoveryStrategy(request, violations, metadata);

        // Validate rollback configuration
        validateRollbackConfiguration(request, violations);

        // Validate retry configuration
        validateRetryConfiguration(request, violations, metadata);

        // Validate metadata integrity
        validateMetadataIntegrity(request, metadata, violations);

        boolean valid = violations.isEmpty();
        Instant validatedAt = Instant.now();

        return new ExecutionValidationResult(valid, violations, validatedAt, metadata);
    }

    /**
     * Validates recovery strategy definition.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateRecoveryStrategy(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        // Validate recovery strategy in execution options
        if (request.options() != null) {
            Map<String, Object> options = request.options().options();
            if (options != null) {
                // Check for recovery strategy configuration
                Object strategyObj = options.get("recoveryStrategy");
                if (strategyObj != null) {
                    metadata.put("recoveryStrategy", strategyObj.toString());

                    // Validate strategy format (structural check)
                    if (strategyObj instanceof String) {
                        String strategy = (String) strategyObj;
                        if (strategy.trim().isEmpty()) {
                            violations.add("RecoveryValidator: recoveryStrategy must not be empty");
                        } else {
                            // Validate against known strategies
                            boolean validStrategy = isValidRecoveryStrategy(strategy);
                            metadata.put("validRecoveryStrategy", validStrategy);
                            if (!validStrategy) {
                                violations.add("RecoveryValidator: recoveryStrategy must be a valid strategy: " + strategy);
                            }
                        }
                    }
                }

                // Check for recovery-related options
                metadata.put("hasMaxRetries", request.options().maxRetries() > 0);
                metadata.put("hasRetryDelay", request.options().retryDelayMs() > 0);
            }
        }

        // Validate recovery configuration in parameters
        if (request.parameters() != null) {
            Object recoveryConfig = request.parameters().get("recoveryConfig");
            if (recoveryConfig != null) {
                metadata.put("hasRecoveryConfig", true);
                // Structural validation of recovery configuration
                if (recoveryConfig instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) recoveryConfig;
                    metadata.put("recoveryConfigKeys", config.keySet().size());
                }
            }
        }
    }

    /**
     * Validates rollback configuration.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateRollbackConfiguration(ExecutionRequest request, List<String> violations) {
        // Validate rollback configuration in parameters
        if (request.parameters() != null) {
            Object rollbackConfig = request.parameters().get("rollbackConfig");
            if (rollbackConfig != null) {
                // Structural validation of rollback configuration
                if (rollbackConfig instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) rollbackConfig;

                    // Validate rollback point presence
                    if (!config.containsKey("rollbackPoint")) {
                        violations.add("RecoveryValidator: rollbackConfig must specify rollbackPoint");
                    }

                    // Validate rollback scope
                    Object scope = config.get("rollbackScope");
                    if (scope != null && scope instanceof String) {
                        String scopeStr = (String) scope;
                        if (scopeStr.trim().isEmpty()) {
                            violations.add("RecoveryValidator: rollbackScope must not be empty");
                        }
                    }
                }
            }
        }

        // Validate rollback configuration in context
        if (request.context() != null && request.context().contextData() != null) {
            Map<String, Object> contextData = request.context().contextData();
            if (contextData.containsKey("rollbackEnabled")) {
                Object rollbackEnabled = contextData.get("rollbackEnabled");
                if (rollbackEnabled instanceof Boolean) {
                    // Structural check - boolean value is valid
                }
            }
        }
    }

    /**
     * Validates retry configuration.
     *
     * @param request   the execution request to validate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     * @param metadata  the metadata map to populate (must not be {@code null})
     */
    private static void validateRetryConfiguration(ExecutionRequest request, List<String> violations, Map<String, Object> metadata) {
        // Validate retry configuration in execution options
        if (request.options() != null) {
            int maxRetries = request.options().maxRetries();
            long retryDelayMs = request.options().retryDelayMs();

            // Structural validation of retry configuration
            if (maxRetries < 0) {
                violations.add("RecoveryValidator: maxRetries must not be negative");
            }

            if (retryDelayMs < 0) {
                violations.add("RecoveryValidator: retryDelayMs must not be negative");
            }

            // Validate retry configuration consistency
            if (maxRetries > 0 && retryDelayMs == 0) {
                violations.add("RecoveryValidator: retryDelayMs must be positive when maxRetries > 0");
            }

            metadata.put("maxRetries", maxRetries);
            metadata.put("retryDelayMs", retryDelayMs);
        }

        // Validate retry configuration in parameters
        if (request.parameters() != null) {
            Object retryConfig = request.parameters().get("retryConfig");
            if (retryConfig != null) {
                metadata.put("hasRetryConfig", true);

                // Structural validation of retry configuration
                if (retryConfig instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) retryConfig;

                    // Validate retry limits
                    if (config.containsKey("maxRetries")) {
                        Object maxRetries = config.get("maxRetries");
                        if (maxRetries instanceof Integer) {
                            int retries = (Integer) maxRetries;
                            if (retries < 0) {
                                violations.add("RecoveryValidator: retryConfig.maxRetries must not be negative");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates metadata integrity.
     *
     * @param request    the execution request to validate (must not be {@code null})
     * @param metadata   the metadata map to populate (must not be {@code null})
     * @param violations the list to add violations to (must not be {@code null})
     */
    private static void validateMetadataIntegrity(ExecutionRequest request, Map<String, Object> metadata, List<String> violations) {
        // Validate recovery-related metadata in parameters
        if (request.parameters() != null) {
            boolean hasRecoveryParams = request.parameters().containsKey("recoveryStrategy") ||
                                       request.parameters().containsKey("recoveryConfig") ||
                                       request.parameters().containsKey("rollbackConfig") ||
                                       request.parameters().containsKey("retryConfig");
            metadata.put("hasRecoveryParameters", hasRecoveryParams);
        }
    }

    /**
     * Checks if a recovery strategy string is valid.
     *
     * @param strategy the strategy string to validate (must not be {@code null} or empty)
     * @return {@code true} if the strategy is valid, {@code false} otherwise
     */
    private static boolean isValidRecoveryStrategy(String strategy) {
        if (strategy == null || strategy.trim().isEmpty()) {
            return false;
        }

        String upperStrategy = strategy.trim().toUpperCase();
        try {
            RecoveryStrategy.valueOf(upperStrategy);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}