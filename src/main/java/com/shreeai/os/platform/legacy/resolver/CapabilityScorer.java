package com.shreeai.os.platform.legacy.resolver;

import com.shreeai.os.platform.legacy.capability.Capability;
import com.shreeai.os.platform.legacy.capability.CapabilityContext;

/**
 * Deterministic scoring engine for capability resolution.
 * <p>
 * Evaluates a capability against an intent and context, producing a weighted score.
 * All scoring is deterministic — no randomness, no LLM, no external dependencies.
 * <p>
 * Scoring weights (configurable via constants):
 * <ul>
 *   <li>Intent Match: 40% — Does the capability support the intent?</li>
 *   <li>Priority: 20% — Higher priority capabilities score higher</li>
 *   <li>Context: 20% — Is the capability relevant to current context?</li>
 *   <li>Health: 10% — Healthy capabilities preferred over degraded</li>
 *   <li>Availability: 10% — Enabled capabilities preferred over disabled</li>
 * </ul>
 * <p>
 * Thread-safe: no mutable state.
 * Performance target: < 100μs per evaluation.
 */
public final class CapabilityScorer {

    // ── Scoring Weights ──
    static final double WEIGHT_INTENT_MATCH = 0.40;
    static final double WEIGHT_PRIORITY = 0.20;
    static final double WEIGHT_CONTEXT = 0.20;
    static final double WEIGHT_HEALTH = 0.10;
    static final double WEIGHT_AVAILABILITY = 0.10;

    // ── Score Constants ──
    static final double SCORE_INTENT_MATCH = 1.0;
    static final double SCORE_NO_INTENT_MATCH = 0.0;
    static final double SCORE_PRIORITY_MAX = 1.0;
    static final double SCORE_PRIORITY_MIN = 0.1;
    static final double SCORE_HEALTH_HEALTHY = 1.0;
    static final double SCORE_HEALTH_DEGRADED = 0.5;
    static final double SCORE_HEALTH_UNHEALTHY = 0.0;
    static final double SCORE_HEALTH_UNKNOWN = 0.5;
    static final double SCORE_AVAILABILITY_ENABLED = 1.0;
    static final double SCORE_AVAILABILITY_DISABLED = 0.0;
    static final double PRIORITY_MAX_VALUE = 100.0;

    private CapabilityScorer() {
        // Utility class — no instantiation
    }

    /**
     * Score a capability for handling a given intent with context.
     *
     * @param capability the capability to evaluate
     * @param intent     the detected intent
     * @param context    the current capability context (may be null)
     * @return score between 0.0 and 1.0
     */
    public static double score(Capability capability, String intent, CapabilityContext context) {
        if (capability == null || intent == null || intent.isBlank()) {
            return 0.0;
        }

        boolean supportsIntent = scoreIntentMatch(capability, intent) == SCORE_INTENT_MATCH;
        double intentScore = supportsIntent ? SCORE_INTENT_MATCH * WEIGHT_INTENT_MATCH : 0.0;
        double priorityScore = scorePriority(capability) * WEIGHT_PRIORITY;
        double contextScore = scoreContext(capability, intent, context, supportsIntent) * WEIGHT_CONTEXT;
        double healthScore = scoreHealth(capability) * WEIGHT_HEALTH;
        double availabilityScore = scoreAvailability(capability) * WEIGHT_AVAILABILITY;

        return intentScore + priorityScore + contextScore + healthScore + availabilityScore;
    }

    /**
     * Score whether the capability supports the intent.
     * Returns 1.0 if supported, 0.0 otherwise.
     */
    static double scoreIntentMatch(Capability capability, String intent) {
        return capability.getSupportedIntents().stream()
                .anyMatch(si -> si.equalsIgnoreCase(intent))
                ? SCORE_INTENT_MATCH
                : SCORE_NO_INTENT_MATCH;
    }

    /**
     * Score based on capability priority.
     * Normalizes priority to 0.0–1.0 range.
     */
    static double scorePriority(Capability capability) {
        int priority = capability.getPriority();
        if (priority <= 0) return SCORE_PRIORITY_MIN;
        return Math.min(SCORE_PRIORITY_MAX, priority / PRIORITY_MAX_VALUE);
    }

    /**
     * Score based on context relevance.
     * Currently provides basic context boost for matched intents.
     * Extensible for future context-aware scoring.
     */
    static double scoreContext(Capability capability, String intent, CapabilityContext context, boolean supportsIntent) {
        // Base context score: intent match indicator
        if (!supportsIntent) {
            return 0.0;
        }

        // Future: use context to boost/discount score
        // e.g., if active course exists, boost LearningCapability
        if (context != null && context.getCourseState() != null && context.getCourseState().hasActiveCourse()) {
            String capName = capability.getName().toLowerCase();
            if (capName.contains("learn")) {
                return 0.8; // Context boost for learning when course is active
            }
        }

        return 0.5; // Neutral context score for supported intents
    }

    /**
     * Score based on capability health status.
     */
    static double scoreHealth(Capability capability) {
        return switch (capability.getHealthStatus()) {
            case HEALTHY -> SCORE_HEALTH_HEALTHY;
            case DEGRADED -> SCORE_HEALTH_DEGRADED;
            case UNHEALTHY -> SCORE_HEALTH_UNHEALTHY;
            case UNKNOWN -> SCORE_HEALTH_UNKNOWN;
        };
    }

    /**
     * Score based on capability availability (enabled/disabled).
     */
    static double scoreAvailability(Capability capability) {
        return capability.isEnabled()
                ? SCORE_AVAILABILITY_ENABLED
                : SCORE_AVAILABILITY_DISABLED;
    }
}