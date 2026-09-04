package com.shreeai.os.platform.runtime.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DiagnosticReport</b>
 *
 * <p>Immutable report produced by {@code DiagnosisAgent} describing the
 * runtime state of the workspace, memory, knowledge, project and execution
 * before any kernel work begins.</p>
 *
 * @since Sprint 18
 */
public final class DiagnosticReport {

    /** Per-check status. */
    public enum CheckStatus { PASS, WARN, FAIL, SKIPPED }

    /** Logical area being diagnosed. */
    public enum DiagnosticArea {
        WORKSPACE,
        MEMORY,
        KNOWLEDGE,
        PROJECT,
        EXECUTION
    }

    private final String reportId;
    private final Map<DiagnosticArea, CheckStatus> statuses;
    private final List<String> recommendations;
    private final Map<String, Object> details;
    private final long producedAtMillis;

    private DiagnosticReport(Builder b) {
        this.reportId = Objects.requireNonNull(b.reportId, "reportId must not be null");
        this.statuses = Collections.unmodifiableMap(new LinkedHashMap<>(b.statuses));
        this.recommendations = List.copyOf(b.recommendations);
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(b.details));
        this.producedAtMillis = b.producedAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String reportId() { return reportId; }
    public Map<DiagnosticArea, CheckStatus> statuses() { return statuses; }
    public List<String> recommendations() { return recommendations; }
    public Map<String, Object> details() { return details; }
    public long producedAtMillis() { return producedAtMillis; }

    public CheckStatus statusOf(DiagnosticArea area) {
        return statuses.getOrDefault(area, CheckStatus.SKIPPED);
    }

    public boolean isHealthy() {
        return statuses.values().stream().allMatch(s -> s == CheckStatus.PASS || s == CheckStatus.SKIPPED);
    }

    public boolean hasFailures() {
        return statuses.values().contains(CheckStatus.FAIL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagnosticReport that)) return false;
        return producedAtMillis == that.producedAtMillis
                && Objects.equals(reportId, that.reportId)
                && Objects.equals(statuses, that.statuses)
                && Objects.equals(recommendations, that.recommendations)
                && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, statuses, recommendations, details, producedAtMillis);
    }

    @Override
    public String toString() {
        return "DiagnosticReport{reportId='" + reportId
                + "', statuses=" + statuses
                + ", healthy=" + isHealthy() + '}';
    }

    public static final class Builder {
        private String reportId = "diag-" + java.util.UUID.randomUUID();
        private Map<DiagnosticArea, CheckStatus> statuses = new LinkedHashMap<>();
        private List<String> recommendations = new ArrayList<>();
        private Map<String, Object> details = new LinkedHashMap<>();
        private long producedAtMillis = System.currentTimeMillis();

        public Builder reportId(String reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder putStatus(DiagnosticArea area, CheckStatus status) {
            this.statuses.put(area, status);
            return this;
        }

        public Builder addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
            return this;
        }

        public Builder addDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = new LinkedHashMap<>(details);
            return this;
        }

        public Builder producedAtMillis(long producedAtMillis) {
            this.producedAtMillis = producedAtMillis;
            return this;
        }

        public DiagnosticReport build() {
            return new DiagnosticReport(this);
        }
    }
}
