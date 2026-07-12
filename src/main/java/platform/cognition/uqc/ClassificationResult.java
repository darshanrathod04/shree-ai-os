package platform.cognition.uqc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable classification result from UniversalQueryClassifier.
 * Contains the complete analysis of a user query.
 * Thread-safe by design.
 */
public final class ClassificationResult {

    private final String originalInput;
    private final String normalizedInput;
    private final String predictedIntent;
    private final QueryCategory queryCategory;
    private final List<DetectedEntity> entities;
    private final double confidence;
    private final List<String> matchedRules;
    private final String reason;
    private final long processingTimeNanos;
    private final Instant timestamp;

    public ClassificationResult(String originalInput,
                                String normalizedInput,
                                String predictedIntent,
                                QueryCategory queryCategory,
                                List<DetectedEntity> entities,
                                double confidence,
                                List<String> matchedRules,
                                String reason,
                                long processingTimeNanos) {
        this.originalInput = Objects.requireNonNull(originalInput, "originalInput must not be null");
        this.normalizedInput = Objects.requireNonNull(normalizedInput, "normalizedInput must not be null");
        this.predictedIntent = Objects.requireNonNull(predictedIntent, "predictedIntent must not be null");
        this.queryCategory = queryCategory != null ? queryCategory : QueryCategory.UNKNOWN;
        this.entities = entities != null
                ? Collections.unmodifiableList(List.copyOf(entities))
                : List.of();
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.matchedRules = matchedRules != null
                ? Collections.unmodifiableList(List.copyOf(matchedRules))
                : List.of();
        this.reason = reason != null ? reason : "";
        this.processingTimeNanos = processingTimeNanos;
        this.timestamp = Instant.now();
    }

    public String getOriginalInput() { return originalInput; }
    public String getNormalizedInput() { return normalizedInput; }
    public String getPredictedIntent() { return predictedIntent; }
    public QueryCategory getQueryCategory() { return queryCategory; }
    public List<DetectedEntity> getEntities() { return entities; }
    public double getConfidence() { return confidence; }
    public List<String> getMatchedRules() { return matchedRules; }
    public String getReason() { return reason; }
    public long getProcessingTimeNanos() { return processingTimeNanos; }
    public Instant getTimestamp() { return timestamp; }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    public boolean isMediumConfidence() {
        return confidence >= 0.5 && confidence < 0.8;
    }

    public boolean isLowConfidence() {
        return confidence < 0.5;
    }

    @Override
    public String toString() {
        return "ClassificationResult{" +
                "intent='" + predictedIntent + '\'' +
                ", category=" + queryCategory +
                ", confidence=" + String.format("%.0f%%", confidence * 100) +
                ", entities=" + entities.size() +
                ", rules=" + matchedRules.size() +
                ", time=" + processingTimeNanos / 1_000_000 + "ms" +
                '}';
    }
}