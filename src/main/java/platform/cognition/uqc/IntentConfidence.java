package platform.cognition.uqc;

import java.util.Objects;

/**
 * Immutable confidence scores for a single predicted intent.
 * Part of the ClassificationResult from UniversalQueryClassifier.
 * Thread-safe by design.
 */
public final class IntentConfidence {

    private final String intent;
    private final double score;
    private final String matchedRule;

    public IntentConfidence(String intent, double score, String matchedRule) {
        this.intent = Objects.requireNonNull(intent, "intent must not be null");
        this.score = Math.max(0.0, Math.min(1.0, score));
        this.matchedRule = matchedRule != null ? matchedRule : "unknown";
    }

    public String getIntent() { return intent; }
    public double getScore() { return score; }
    public String getMatchedRule() { return matchedRule; }

    @Override
    public String toString() {
        return String.format("%s (%.0f%% via %s)", intent, score * 100, matchedRule);
    }
}