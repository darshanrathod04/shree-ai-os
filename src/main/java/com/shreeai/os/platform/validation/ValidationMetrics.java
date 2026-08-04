package com.shreeai.os.platform.validation;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe validation metrics collector.
 *
 * <p>Separates telemetry from validation logic following Single Responsibility Principle.
 * Collects and aggregates validation statistics for monitoring and observability.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>Uses ConcurrentHashMap and AtomicLong for thread-safe metric collection.
 * No external synchronization required.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
public final class ValidationMetrics {

    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong successfulValidations = new AtomicLong(0);
    private final AtomicLong failedValidations = new AtomicLong(0);
    private final AtomicLong warningValidations = new AtomicLong(0);
    private final AtomicLong totalValidationTimeNanos = new AtomicLong(0);

    private final ConcurrentHashMap<ValidationStatus, AtomicLong> statusCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> ruleFailureCounts = new ConcurrentHashMap<>();

    /**
     * Record a validation result.
     *
     * @param result the validation result
     * @param durationNanos validation duration in nanoseconds
     */
    public void recordValidation(ValidationResult result, long durationNanos) {
        totalValidations.incrementAndGet();
        totalValidationTimeNanos.addAndGet(durationNanos);

        statusCounts.computeIfAbsent(result.getStatus(), k -> new AtomicLong(0))
                    .incrementAndGet();

        if (!result.isValid()) {
            failedValidations.incrementAndGet();
        } else if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
            warningValidations.incrementAndGet();
        } else {
            successfulValidations.incrementAndGet();
        }
    }

    /**
     * Record a rule failure.
     *
     * @param ruleName the name of the rule that failed
     */
    public void recordRuleFailure(String ruleName) {
        ruleFailureCounts.computeIfAbsent(ruleName, k -> new AtomicLong(0))
                        .incrementAndGet();
    }

    /**
     * Get total validation count.
     */
    public long getTotalValidations() {
        return totalValidations.get();
    }

    /**
     * Get successful validation count.
     */
    public long getSuccessfulValidations() {
        return successfulValidations.get();
    }

    /**
     * Get failed validation count.
     */
    public long getFailedValidations() {
        return failedValidations.get();
    }

    /**
     * Get warning validation count.
     */
    public long getWarningValidations() {
        return warningValidations.get();
    }

    /**
     * Get average validation time in nanoseconds.
     */
    public double getAverageValidationTimeNanos() {
        long total = totalValidations.get();
        return total > 0 ? (double) totalValidationTimeNanos.get() / total : 0.0;
    }

    /**
     * Get count for a specific validation status.
     */
    public long getStatusCount(ValidationStatus status) {
        AtomicLong count = statusCounts.get(status);
        return count != null ? count.get() : 0;
    }

    /**
     * Get failure count for a specific rule.
     */
    public long getRuleFailureCount(String ruleName) {
        AtomicLong count = ruleFailureCounts.get(ruleName);
        return count != null ? count.get() : 0;
    }

    /**
     * Reset all metrics.
     */
    public void reset() {
        totalValidations.set(0);
        successfulValidations.set(0);
        failedValidations.set(0);
        warningValidations.set(0);
        totalValidationTimeNanos.set(0);
        statusCounts.clear();
        ruleFailureCounts.clear();
    }
}