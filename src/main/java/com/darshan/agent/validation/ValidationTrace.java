package com.darshan.agent.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable validation trace recording each validation step.
 *
 * <p>This class provides detailed observability into the validation process,
 * recording each rule check, its outcome, and timing information.</p>
 *
 * <p>Intended for debugging and monitoring. Not exposed to end users.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>Immutable after construction. All collections are unmodifiable.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
public final class ValidationTrace {

    private final String traceId;
    private final List<ValidationStep> steps;
    private final Instant startTime;
    private final Instant endTime;
    private final long totalDurationNanos;

    /**
     * Represents a single validation step.
     */
    public static final class ValidationStep {
        private final String ruleName;
        private final boolean passed;
        private final String message;
        private final Instant timestamp;
        private final long durationNanos;

        public ValidationStep(String ruleName, boolean passed, String message, Instant timestamp, long durationNanos) {
            this.ruleName = ruleName;
            this.passed = passed;
            this.message = message;
            this.timestamp = timestamp;
            this.durationNanos = durationNanos;
        }

        public String getRuleName() { return ruleName; }
        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }
        public long getDurationNanos() { return durationNanos; }
    }

    public ValidationTrace(String traceId, List<ValidationStep> steps, Instant startTime, Instant endTime, long totalDurationNanos) {
        this.traceId = traceId;
        this.steps = steps != null ? Collections.unmodifiableList(new ArrayList<>(steps)) : Collections.emptyList();
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDurationNanos = totalDurationNanos;
    }

    public String getTraceId() { return traceId; }
    public List<ValidationStep> getSteps() { return steps; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public long getTotalDurationNanos() { return totalDurationNanos; }

    /**
     * Builder for ValidationTrace.
     */
    public static class Builder {
        private String traceId;
        private List<ValidationStep> steps = new ArrayList<>();
        private Instant startTime;
        private Instant endTime;
        private long totalDurationNanos;

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder addStep(ValidationStep step) {
            this.steps.add(step);
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder totalDurationNanos(long totalDurationNanos) {
            this.totalDurationNanos = totalDurationNanos;
            return this;
        }

        public ValidationTrace build() {
            return new ValidationTrace(traceId, steps, startTime, endTime, totalDurationNanos);
        }
    }
}