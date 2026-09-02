package com.shreeai.os.platform.runtime.studio.journal;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>StageTimelineEntry</b>
 *
 * <p>Immutable node of an {@link ExecutionJournal} stage timeline. Each entry
 * represents one pipeline stage (Identity, Context, MemoryRecall, Knowledge,
 * Reasoning, Inference, Planning, ActionExecution, MemoryStore, ChiefReview,
 * Reflection) observed through the runtime event bus.</p>
 *
 * <p>Every entry exposes the PHASE 3 HEATMAP triplet: {@link #durationMs()},
 * {@link #percentile()} (nearest-rank percentile of this duration within all
 * observed runs of the same stage, 0 when not enriched) and
 * {@link #severityColor()} ({@code green | yellow | orange | red}).</p>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class StageTimelineEntry {

    private final String stageName;
    private final int sequence;
    private final Instant beganAt;
    private final Instant completedAt;
    private final long durationMs;
    private final String outcome;
    private final String detailRef;
    private final int percentile;
    private final String severityColor;

    private StageTimelineEntry(Builder builder) {
        this.stageName = Objects.requireNonNull(builder.stageName, "stageName must not be null");
        this.sequence = builder.sequence;
        this.beganAt = Objects.requireNonNull(builder.beganAt, "beganAt must not be null");
        this.completedAt = Objects.requireNonNull(builder.completedAt, "completedAt must not be null");
        this.durationMs = Math.max(0, builder.durationMs);
        this.outcome = builder.outcome == null ? "RUNNING" : builder.outcome;
        this.detailRef = builder.detailRef;
        this.percentile = clamp(builder.percentile, 0, 100);
        this.severityColor = builder.severityColor == null ? "green" : builder.severityColor;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** @return the pipeline stage name (never null) */
    public String stageName() {
        return stageName;
    }

    /** @return the 1-based sequence of this stage within its execution */
    public int sequence() {
        return sequence;
    }

    /** @return when the stage began (never null) */
    public Instant beganAt() {
        return beganAt;
    }

    /** @return when the stage completed (never null) */
    public Instant completedAt() {
        return completedAt;
    }

    /** @return wall-clock duration in milliseconds (never negative) */
    public long durationMs() {
        return durationMs;
    }

    /** @return outcome (SUCCESS / FAILED / SKIPPED / RUNNING) */
    public String outcome() {
        return outcome;
    }

    /** @return execution/request reference metadata (may be null) */
    public String detailRef() {
        return detailRef;
    }

    /** @return heatmap percentile of this duration within its stage bucket (0–100) */
    public int percentile() {
        return percentile;
    }

    /** @return heatmap severity color: green | yellow | orange | red */
    public String severityColor() {
        return severityColor;
    }

    /**
     * Returns a copy of this entry enriched with heatmap percentile and
     * severity color (computed by {@link StageHeatmapService}).
     */
    public StageTimelineEntry withHeatmap(int percentile, String severityColor) {
        return builder()
                .stageName(stageName)
                .sequence(sequence)
                .beganAt(beganAt)
                .completedAt(completedAt)
                .durationMs(durationMs)
                .outcome(outcome)
                .detailRef(detailRef)
                .percentile(percentile)
                .severityColor(severityColor)
                .build();
    }

    /** Creates a timeline entry for a stage that is still running. */
    public static StageTimelineEntry open(String stageName, int sequence, Instant beganAt, String detailRef) {
        return builder()
                .stageName(stageName)
                .sequence(sequence)
                .beganAt(beganAt)
                .completedAt(beganAt)
                .outcome("RUNNING")
                .detailRef(detailRef)
                .build();
    }

    /** Completes an open entry with the observed duration. */
    public StageTimelineEntry complete(Instant completedAt, long durationMs, String outcome) {
        return builder()
                .stageName(stageName)
                .sequence(sequence)
                .beganAt(beganAt)
                .completedAt(completedAt)
                .durationMs(durationMs)
                .outcome(outcome)
                .detailRef(detailRef)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "StageTimelineEntry{stageName='" + stageName + "', sequence=" + sequence
                + ", durationMs=" + durationMs + ", outcome='" + outcome
                + "', percentile=" + percentile + ", severityColor='" + severityColor + "'}";
    }

    /** Fluent builder for {@link StageTimelineEntry}. */
    public static final class Builder {

        private String stageName;
        private int sequence;
        private Instant beganAt;
        private Instant completedAt;
        private long durationMs;
        private String outcome;
        private String detailRef;
        private int percentile;
        private String severityColor;

        private Builder() {
        }

        public Builder stageName(String stageName) {
            this.stageName = stageName;
            return this;
        }

        public Builder sequence(int sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder beganAt(Instant beganAt) {
            this.beganAt = beganAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder outcome(String outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder detailRef(String detailRef) {
            this.detailRef = detailRef;
            return this;
        }

        public Builder percentile(int percentile) {
            this.percentile = percentile;
            return this;
        }

        public Builder severityColor(String severityColor) {
            this.severityColor = severityColor;
            return this;
        }

        public StageTimelineEntry build() {
            return new StageTimelineEntry(this);
        }
    }
}