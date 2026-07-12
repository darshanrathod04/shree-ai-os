package platform.resolver;

import platform.capability.Capability;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result of a capability resolution.
 * <p>
 * Contains the resolved capability, confidence score, selection reason,
 * resolution strategy, candidate list, processing metadata, and matched intent.
 * <p>
 * This object is created once per resolution and never modified.
 * Thread-safe by design — all fields are final and immutable.
 * <p>
 * Future-proof: additional fields can be added without breaking existing consumers.
 */
public final class CapabilityResolution {

    private final Capability selectedCapability;
    private final double confidence;
    private final String reason;
    private final ResolutionStrategy strategy;
    private final List<Candidate> candidates;
    private final long processingTimeNanos;
    private final String matchedIntent;
    private final String resolvedCategory;
    private final Instant timestamp;

    /**
     * Constructs an immutable resolution result.
     *
     * @param selectedCapability the resolved capability (may be null for UNKNOWN)
     * @param confidence         confidence score 0.0–1.0
     * @param reason             human-readable reason for selection
     * @param strategy           the resolution strategy used
     * @param candidates         list of candidate capabilities considered (unmodifiable)
     * @param processingTimeNanos time taken for resolution in nanoseconds
     * @param matchedIntent      the intent that was resolved
     * @param resolvedCategory   the resolved category (may be same as intent or broader)
     */
    public CapabilityResolution(
            Capability selectedCapability,
            double confidence,
            String reason,
            ResolutionStrategy strategy,
            List<Candidate> candidates,
            long processingTimeNanos,
            String matchedIntent,
            String resolvedCategory) {
        this.selectedCapability = selectedCapability;
        this.confidence = clamp(confidence, 0.0, 1.0);
        this.reason = Objects.requireNonNullElse(reason, "");
        this.strategy = Objects.requireNonNullElse(strategy, ResolutionStrategy.UNKNOWN);
        this.candidates = candidates != null
                ? Collections.unmodifiableList(candidates)
                : Collections.emptyList();
        this.processingTimeNanos = Math.max(0, processingTimeNanos);
        this.matchedIntent = Objects.requireNonNullElse(matchedIntent, "");
        this.resolvedCategory = Objects.requireNonNullElse(resolvedCategory, "");
        this.timestamp = Instant.now();
    }

    // ── Getters ──

    /**
     * The resolved capability that SHOULD handle the request.
     * May be null if resolution strategy is {@link ResolutionStrategy#UNKNOWN}.
     */
    public Capability getSelectedCapability() { return selectedCapability; }

    /**
     * Confidence score for this resolution (0.0–1.0).
     */
    public double getConfidence() { return confidence; }

    /**
     * Human-readable reason explaining why this capability was selected.
     */
    public String getReason() { return reason; }

    /**
     * The strategy used to reach this resolution.
     */
    public ResolutionStrategy getStrategy() { return strategy; }

    /**
     * Unmodifiable list of candidate capabilities considered during resolution.
     */
    public List<Candidate> getCandidates() { return candidates; }

    /**
     * Processing time in nanoseconds.
     */
    public long getProcessingTimeNanos() { return processingTimeNanos; }

    /**
     * The intent that was resolved.
     */
    public String getMatchedIntent() { return matchedIntent; }

    /**
     * The resolved category (e.g., "LEARNING", "CHAT", "PLANNING").
     */
    public String getResolvedCategory() { return resolvedCategory; }

    /**
     * Timestamp when this resolution was created.
     */
    public Instant getTimestamp() { return timestamp; }

    /**
     * Returns whether a capability was successfully resolved.
     */
    public boolean isResolved() {
        return selectedCapability != null
                && strategy != ResolutionStrategy.UNKNOWN
                && strategy != ResolutionStrategy.FALLBACK;
    }

    /**
     * Returns whether this is a high-confidence resolution (≥ 80%).
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapabilityResolution that)) return false;
        return Double.compare(confidence, that.confidence) == 0
                && processingTimeNanos == that.processingTimeNanos
                && Objects.equals(selectedCapability != null ? selectedCapability.getName() : null,
                                  that.selectedCapability != null ? that.selectedCapability.getName() : null)
                && Objects.equals(reason, that.reason)
                && strategy == that.strategy
                && Objects.equals(matchedIntent, that.matchedIntent)
                && Objects.equals(resolvedCategory, that.resolvedCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                selectedCapability != null ? selectedCapability.getName() : null,
                confidence, reason, strategy, matchedIntent, resolvedCategory
        );
    }

    @Override
    public String toString() {
        return String.format(
                "CapabilityResolution{cap='%s', intent='%s', conf=%.0f%%, strategy=%s}",
                selectedCapability != null ? selectedCapability.getName() : "null",
                matchedIntent, confidence * 100, strategy
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ── Nested Types ──

    /**
     * Represents a candidate capability considered during resolution.
     * Immutable and comparable by score.
     */
    public static final class Candidate implements Comparable<Candidate> {
        private final Capability capability;
        private final double score;
        private final String reason;

        public Candidate(Capability capability, double score, String reason) {
            this.capability = Objects.requireNonNull(capability, "capability must not be null");
            this.score = clamp(score, 0.0, 1.0);
            this.reason = Objects.requireNonNullElse(reason, "");
        }

        public Capability getCapability() { return capability; }
        public double getScore() { return score; }
        public String getReason() { return reason; }

        @Override
        public int compareTo(Candidate other) {
            return Double.compare(other.score, this.score); // Descending by score
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Candidate candidate)) return false;
            return Double.compare(score, candidate.score) == 0
                    && capability.getName().equals(candidate.capability.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(capability.getName(), score);
        }

        @Override
        public String toString() {
            return String.format("Candidate{cap='%s', score=%.0f%%}", capability.getName(), score * 100);
        }
    }
}