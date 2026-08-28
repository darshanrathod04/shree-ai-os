package com.shreeai.os.platform.core.health.engine;

import com.shreeai.os.platform.core.health.model.HealthReport;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>EvaluationResult</b>
 *
 * <p>Immutable result returned by the {@link HealthEvaluationEngine}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the result of a health evaluation.</li>
 *   <li>Provides success/failure status with optional report or failure message.</li>
 *   <li>Enables consistent evaluation result handling.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Either success with report, or failure with message.</p>
 *
 * @see HealthEvaluationEngine
 */
public final class EvaluationResult {

    private final boolean success;
    private final HealthReport report;
    private final String failureMessage;
    private final Instant timestamp;

    private EvaluationResult(boolean success, HealthReport report, String failureMessage, Instant timestamp) {
        this.success = success;
        this.report = report;
        this.failureMessage = failureMessage;
        this.timestamp = timestamp;
    }

    /**
     * Creates a successful evaluation result with the given report.
     *
     * @param report the health report (must not be null)
     * @return a successful evaluation result
     * @throws IllegalArgumentException if {@code report} is {@code null}
     */
    public static EvaluationResult success(HealthReport report) {
        return new EvaluationResult(
                true,
                Objects.requireNonNull(report, "HealthReport must not be null"),
                null,
                Instant.now()
        );
    }

    /**
     * Creates a failed evaluation result with the given failure message.
     *
     * @param failureMessage the failure message (must not be null or blank)
     * @return a failed evaluation result
     * @throws IllegalArgumentException if {@code failureMessage} is {@code null} or blank
     */
    public static EvaluationResult failure(String failureMessage) {
        if (failureMessage == null || failureMessage.isBlank()) {
            throw new IllegalArgumentException("Failure message must not be null or blank");
        }
        return new EvaluationResult(false, null, failureMessage, Instant.now());
    }

    /**
     * Returns whether the evaluation was successful.
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the health report if successful, or {@code null} if failed.
     *
     * @return the health report, or {@code null} if failed
     */
    public HealthReport report() {
        return report;
    }

    /**
     * Returns the failure message if failed, or {@code null} if successful.
     *
     * @return the failure message, or {@code null} if successful
     */
    public String failureMessage() {
        return failureMessage;
    }

    /**
     * Returns the evaluation timestamp.
     *
     * @return the timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationResult that = (EvaluationResult) o;
        return success == that.success
                && Objects.equals(report, that.report)
                && Objects.equals(failureMessage, that.failureMessage)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, report, failureMessage, timestamp);
    }

    @Override
    public String toString() {
        if (success) {
            return "EvaluationResult{success=true, report=" + report + ", timestamp=" + timestamp + '}';
        } else {
            return "EvaluationResult{success=false, failureMessage='" + failureMessage + "', timestamp=" + timestamp + '}';
        }
    }
}