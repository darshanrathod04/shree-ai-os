package com.shreeai.os.platform.runtime.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>VerificationReport</b>
 *
 * <p>Immutable report produced by {@code VerificationAgent}. Assigns a
 * per-item verification status and an overall confidence tier + score
 * using {@code ConfidenceCalculator}.</p>
 *
 * @since Sprint 18
 */
public final class VerificationReport {

    /** Per-evidence verification status. */
    public enum ItemStatus { VERIFIED, UNVERIFIED, FAILED }

    /** The 4-tier confidence scale (Sprint 18). */
    public enum ConfidenceTier {
        VERIFIED_PROJECT(0.95),
        VERIFIED_KB(0.80),
        INFERRED(0.60),
        INSUFFICIENT(0.15);

        private final double defaultScore;

        ConfidenceTier(double defaultScore) {
            this.defaultScore = defaultScore;
        }

        public double defaultScore() {
            return defaultScore;
        }
    }

    private final String reportId;
    private final Map<String, ItemStatus> perItemStatus;
    private final ConfidenceTier tier;
    private final double confidence;
    private final List<String> citations;
    private final List<String> gaps;
    private final Map<String, Object> metadata;
    private final long producedAtMillis;

    private VerificationReport(Builder b) {
        this.reportId = Objects.requireNonNull(b.reportId, "reportId must not be null");
        this.perItemStatus = Collections.unmodifiableMap(new LinkedHashMap<>(b.perItemStatus));
        this.tier = Objects.requireNonNull(b.tier, "tier must not be null");
        this.confidence = clamp(b.confidence);
        this.citations = List.copyOf(b.citations);
        this.gaps = List.copyOf(b.gaps);
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata));
        this.producedAtMillis = b.producedAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String reportId() { return reportId; }
    public Map<String, ItemStatus> perItemStatus() { return perItemStatus; }
    public ConfidenceTier tier() { return tier; }
    public double confidence() { return confidence; }
    public List<String> citations() { return citations; }
    public List<String> gaps() { return gaps; }
    public Map<String, Object> metadata() { return metadata; }
    public long producedAtMillis() { return producedAtMillis; }

    public boolean isVerified() {
        return tier == ConfidenceTier.VERIFIED_PROJECT || tier == ConfidenceTier.VERIFIED_KB;
    }

    public boolean isInsufficient() {
        return tier == ConfidenceTier.INSUFFICIENT;
    }

    private static double clamp(double v) {
        if (Double.isNaN(v)) return 0.0;
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationReport that)) return false;
        return Double.compare(that.confidence, confidence) == 0
                && producedAtMillis == that.producedAtMillis
                && Objects.equals(reportId, that.reportId)
                && Objects.equals(perItemStatus, that.perItemStatus)
                && tier == that.tier
                && Objects.equals(citations, that.citations)
                && Objects.equals(gaps, that.gaps)
                && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, perItemStatus, tier, confidence, citations, gaps, metadata, producedAtMillis);
    }

    @Override
    public String toString() {
        return "VerificationReport{reportId='" + reportId
                + "', tier=" + tier
                + ", confidence=" + confidence
                + ", itemCount=" + perItemStatus.size() + '}';
    }

    public static final class Builder {
        private String reportId = "verify-" + java.util.UUID.randomUUID();
        private Map<String, ItemStatus> perItemStatus = new LinkedHashMap<>();
        private ConfidenceTier tier = ConfidenceTier.INSUFFICIENT;
        private double confidence = 0.15;
        private List<String> citations = new ArrayList<>();
        private List<String> gaps = new ArrayList<>();
        private Map<String, Object> metadata = new LinkedHashMap<>();
        private long producedAtMillis = System.currentTimeMillis();

        public Builder reportId(String reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder putItemStatus(String itemId, ItemStatus status) {
            this.perItemStatus.put(itemId, status);
            return this;
        }

        public Builder tier(ConfidenceTier tier) {
            this.tier = tier;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder citations(List<String> citations) {
            this.citations = new ArrayList<>(citations);
            return this;
        }

        public Builder addCitation(String citation) {
            if (!(this.citations instanceof ArrayList)) {
                this.citations = new ArrayList<>(this.citations);
            }
            this.citations.add(citation);
            return this;
        }

        public Builder gaps(List<String> gaps) {
            this.gaps = new ArrayList<>(gaps);
            return this;
        }

        public Builder addGap(String gap) {
            if (!(this.gaps instanceof ArrayList)) {
                this.gaps = new ArrayList<>(this.gaps);
            }
            this.gaps.add(gap);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new LinkedHashMap<>(metadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder producedAtMillis(long producedAtMillis) {
            this.producedAtMillis = producedAtMillis;
            return this;
        }

        public VerificationReport build() {
            return new VerificationReport(this);
        }
    }
}
