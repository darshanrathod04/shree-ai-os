package com.shreeai.os.platform.runtime.studio.journal;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>ExecutionJournal</b>
 *
 * <p>Immutable, tenant-aware record of a single runtime execution. Binds the
 * execution identity to its request, tenant and trace lineage, and carries the
 * ordered {@link StageTimelineEntry} timeline observed on the runtime event
 * bus.</p>
 *
 * <p>The journal is the Phase 3 substrate for the Timeline Heatmap (per-stage
 * duration / percentile / severity), inspection, and replay after restart
 * (executions reconstructed from {@link ExecutionJournalStore}).</p>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionJournal {

    private final String journalId;
    private final String requestId;
    private final String executionId;
    private final String tenantId;
    private final String traceId;
    private final JournalStatus status;
    private final String requestSummary;
    private final Instant createdAt;
    private final Instant completedAt;
    private final List<StageTimelineEntry> stageTimeline;

    private ExecutionJournal(Builder builder) {
        this.journalId = Objects.requireNonNull(builder.journalId, "journalId must not be null");
        this.requestId = Objects.requireNonNull(builder.requestId, "requestId must not be null");
        this.executionId = builder.executionId;
        this.tenantId = Objects.requireNonNull(builder.tenantId, "tenantId must not be null");
        this.traceId = builder.traceId;
        this.status = Objects.requireNonNull(builder.status, "status must not be null");
        this.requestSummary = builder.requestSummary;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");
        this.completedAt = builder.completedAt;
        this.stageTimeline = List.copyOf(builder.stageTimeline);
    }

    /** @return the journal identifier (immutable; equals the request id by default) */
    public String journalId() {
        return journalId;
    }

    /** @return the original request identifier (never null) */
    public String requestId() {
        return requestId;
    }

    /** @return the execution identifier (may be null) */
    public String executionId() {
        return executionId;
    }

    /** @return the tenant identifier (never null) */
    public String tenantId() {
        return tenantId;
    }

    /** @return the W3C trace identifier lineage when present (may be null) */
    public String traceId() {
        return traceId;
    }

    /** @return the terminal journal status (never null) */
    public JournalStatus status() {
        return status;
    }

    /** @return a short human-readable request summary */
    public String requestSummary() {
        return requestSummary;
    }

    /** @return when the execution started (never null) */
    public Instant createdAt() {
        return createdAt;
    }

    /** @return when the execution completed (null while IN_PROGRESS) */
    public Instant completedAt() {
        return completedAt;
    }

    /** @return the ordered stage timeline (never null, immutable) */
    public List<StageTimelineEntry> stageTimeline() {
        return stageTimeline;
    }

    /** Returns a copy advanced to the given status and completion timestamp. */
    public ExecutionJournal withStatus(JournalStatus newStatus, Instant completedAt) {
        return builder().from(this).status(newStatus).completedAt(completedAt).build();
    }

    /** Returns a copy with the given stage timeline. */
    public ExecutionJournal withStageTimeline(List<StageTimelineEntry> entries) {
        return builder().from(this).stageTimeline(entries).build();
    }

    @Override
    public String toString() {
        return "ExecutionJournal{requestId='" + requestId + "', executionId='" + executionId
                + "', tenantId='" + tenantId + "', status=" + status
                + ", stages=" + stageTimeline.size() + '}';
    }

    /** Creates a fresh IN_PROGRESS journal for the given execution. */
    public static ExecutionJournal begin(
            String requestId,
            String executionId,
            String tenantId,
            String traceId,
            String requestSummary) {
        return builder()
                .journalId(requestId)
                .requestId(requestId)
                .executionId(executionId)
                .tenantId(tenantId)
                .traceId(traceId)
                .status(JournalStatus.IN_PROGRESS)
                .requestSummary(requestSummary)
                .createdAt(Instant.now())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link ExecutionJournal}. */
    public static final class Builder {

        private String journalId = UUID.randomUUID().toString();
        private String requestId;
        private String executionId;
        private String tenantId;
        private String traceId;
        private JournalStatus status = JournalStatus.IN_PROGRESS;
        private String requestSummary = "";
        private Instant createdAt = Instant.now();
        private Instant completedAt;
        private List<StageTimelineEntry> stageTimeline = List.of();

        private Builder() {
        }

        public Builder from(ExecutionJournal journal) {
            this.journalId = journal.journalId();
            this.requestId = journal.requestId();
            this.executionId = journal.executionId();
            this.tenantId = journal.tenantId();
            this.traceId = journal.traceId();
            this.status = journal.status();
            this.requestSummary = journal.requestSummary();
            this.createdAt = journal.createdAt();
            this.completedAt = journal.completedAt();
            this.stageTimeline = journal.stageTimeline();
            return this;
        }

        public Builder journalId(String journalId) {
            this.journalId = journalId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder status(JournalStatus status) {
            this.status = status;
            return this;
        }

        public Builder requestSummary(String requestSummary) {
            this.requestSummary = requestSummary;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder stageTimeline(List<StageTimelineEntry> stageTimeline) {
            this.stageTimeline = stageTimeline == null ? List.of() : List.copyOf(stageTimeline);
            return this;
        }

        public ExecutionJournal build() {
            return new ExecutionJournal(this);
        }
    }
}