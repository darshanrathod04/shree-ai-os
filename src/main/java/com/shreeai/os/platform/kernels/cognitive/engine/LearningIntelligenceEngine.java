package com.shreeai.os.platform.kernels.cognitive.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Learning Intelligence Engine.
 *
 * <p>Transforms structured reflection intelligence into reusable learning
 * signals for future adaptation, decision-making and replanning.</p>
 *
 * <p>This engine is intentionally non-mutating. It does not directly modify
 * Memory, Knowledge, goals, plans or user state.</p>
 *
 * <p>Architecture:</p>
 *
 * <pre>
 * Outcome
 *    ↓
 * Reflection
 *    ↓
 * Learning Intelligence
 *    ↓
 * Learning Analysis
 *    ↓
 * Adaptation / Memory / Replanning
 * </pre>
 */
public final class LearningIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    /**
     * Analyze reflection intelligence and derive structured learning.
     *
     * @param reflection reflection analysis
     * @return learning analysis
     */
    public LearningAnalysis analyze(
            ReflectionIntelligenceEngine.ReflectionAnalysis reflection) {

        Objects.requireNonNull(
                reflection,
                "reflection must not be null"
        );

        List<String> patterns = new ArrayList<>();
        List<String> successfulStrategies = new ArrayList<>();
        List<String> failurePatterns = new ArrayList<>();
        List<String> knowledgeGaps = new ArrayList<>();
        List<String> behavioralSignals = new ArrayList<>();
        List<String> adaptationRecommendations = new ArrayList<>();
        List<String> futureDecisionGuidance = new ArrayList<>();
        List<String> learningSignals = new ArrayList<>();

        /*
         * ================================================================
         * 1. REFLECTION VERDICT → LEARNING PATTERN
         * ================================================================
         */

        switch (reflection.verdict()) {

            case VERIFIED_SUCCESS -> {

                patterns.add(
                        "VERIFIED_SUCCESS_PATTERN"
                );

                successfulStrategies.add(
                        "Preserve the strategy that produced the verified outcome"
                );

                behavioralSignals.add(
                        "SUCCESSFUL_EXECUTION_PATTERN"
                );

                learningSignals.add(
                        "SUCCESS_PATTERN_CONFIRMED"
                );
            }

            case EXECUTED_UNVERIFIED -> {

                patterns.add(
                        "UNVERIFIED_EXECUTION_PATTERN"
                );

                knowledgeGaps.add(
                        "Post-execution verification evidence is insufficient"
                );

                adaptationRecommendations.add(
                        "Strengthen outcome verification before classifying future executions as successful"
                );

                futureDecisionGuidance.add(
                        "Do not treat execution completion alone as proof of goal achievement"
                );

                learningSignals.add(
                        "VERIFICATION_GAP_LEARNED"
                );
            }

            case PARTIAL_OUTCOME -> {

                patterns.add(
                        "PARTIAL_OUTCOME_PATTERN"
                );

                failurePatterns.add(
                        "Execution completed only part of the intended objective"
                );

                adaptationRecommendations.add(
                        "Decompose incomplete objectives into explicitly measurable remaining outcomes"
                );

                futureDecisionGuidance.add(
                        "Prefer incremental execution when objectives contain multiple independently verifiable outcomes"
                );

                learningSignals.add(
                        "PARTIAL_COMPLETION_PATTERN"
                );
            }

            case EXECUTION_FAILURE -> {

                patterns.add(
                        "EXECUTION_FAILURE_PATTERN"
                );

                failurePatterns.add(
                        "Execution failure requires causal analysis before repeating the same strategy"
                );

                adaptationRecommendations.add(
                        "Avoid blind retry and perform failure-cause analysis first"
                );

                futureDecisionGuidance.add(
                        "Prefer an alternative strategy when the same failure condition remains present"
                );

                learningSignals.add(
                        "FAILURE_PATTERN_DETECTED"
                );
            }

            case NEEDS_IMPROVEMENT -> {

                patterns.add(
                        "IMPROVEMENT_REQUIRED_PATTERN"
                );

                adaptationRecommendations.add(
                        "Apply the highest-value improvement identified by reflection"
                );

                futureDecisionGuidance.add(
                        "Use reflection findings as constraints for the next planning cycle"
                );

                learningSignals.add(
                        "IMPROVEMENT_PATTERN_DETECTED"
                );
            }

            case INSUFFICIENT_EVIDENCE -> {

                patterns.add(
                        "EVIDENCE_INSUFFICIENCY_PATTERN"
                );

                knowledgeGaps.add(
                        "The available lifecycle evidence is insufficient for reliable learning"
                );

                adaptationRecommendations.add(
                        "Improve evidence collection before updating future behavior"
                );

                futureDecisionGuidance.add(
                        "Reduce decision confidence when supporting evidence is incomplete"
                );

                learningSignals.add(
                        "EVIDENCE_GAP_DETECTED"
                );
            }

            case STABLE -> {

                patterns.add(
                        "STABLE_BEHAVIOR_PATTERN"
                );

                successfulStrategies.add(
                        "Preserve the currently stable execution pattern"
                );

                learningSignals.add(
                        "STABLE_PATTERN_CONFIRMED"
                );
            }
        }

        /*
         * ================================================================
         * 2. STRENGTHS → REUSABLE STRATEGIES
         * ================================================================
         */

        for (String strength : reflection.strengths()) {

            if (strength == null
                    || strength.isBlank()) {
                continue;
            }

            String normalized =
                    normalize(strength);

            successfulStrategies.add(
                    normalized
            );

            patterns.add(
                    "POSITIVE_SIGNAL:" + normalized
            );
        }

        /*
         * ================================================================
         * 3. RISKS → FAILURE PATTERNS
         * ================================================================
         */

        for (String risk : reflection.risks()) {

            if (risk == null
                    || risk.isBlank()) {
                continue;
            }

            String normalized =
                    normalize(risk);

            failurePatterns.add(
                    normalized
            );

            patterns.add(
                    "RISK_PATTERN:" + normalized
            );
        }

        /*
         * ================================================================
         * 4. MISSING INFORMATION → KNOWLEDGE GAPS
         * ================================================================
         */

        for (String missing : reflection.missingInformation()) {

            if (missing == null
                    || missing.isBlank()) {
                continue;
            }

            String normalized =
                    normalize(missing);

            knowledgeGaps.add(
                    normalized
            );

            behavioralSignals.add(
                    "INFORMATION_GAP"
            );
        }

        /*
         * ================================================================
         * 5. IMPROVEMENT OPPORTUNITIES → ADAPTATION
         * ================================================================
         */

        for (String improvement :
                reflection.improvementOpportunities()) {

            if (improvement == null
                    || improvement.isBlank()) {
                continue;
            }

            String normalized =
                    normalize(improvement);

            adaptationRecommendations.add(
                    normalized
            );

            behavioralSignals.add(
                    "IMPROVEMENT_REQUIRED"
            );
        }

        /*
         * ================================================================
         * 6. NEXT ACTIONS → FUTURE GUIDANCE
         * ================================================================
         */

        for (String nextAction :
                reflection.nextActions()) {

            if (nextAction == null
                    || nextAction.isBlank()) {
                continue;
            }

            futureDecisionGuidance.add(
                    normalize(nextAction)
            );
        }

        /*
         * ================================================================
         * 7. REFLECTION LEARNING SIGNALS
         * ================================================================
         */

        for (String signal :
                reflection.learningSignals()) {

            if (signal == null
                    || signal.isBlank()) {
                continue;
            }

            learningSignals.add(
                    normalize(signal)
            );
        }

        /*
         * ================================================================
         * 8. CONFIDENCE TRANSFORMATION
         * ================================================================
         *
         * Reflection confidence is not copied blindly.
         *
         * Learning confidence is reduced when:
         *
         * - evidence is missing
         * - failure patterns dominate
         * - reflection confidence is low
         *
         * This prevents weak reflection from becoming strong learned
         * behavior.
         */

        double learningConfidence =
                calculateLearningConfidence(
                        reflection
                );

        /*
         * ================================================================
         * 9. LEARNING PRIORITY
         * ================================================================
         */

        LearningPriority priority =
                determinePriority(
                        reflection,
                        knowledgeGaps,
                        failurePatterns,
                        adaptationRecommendations
                );

        /*
         * ================================================================
         * 10. LEARNING TYPE
         * ================================================================
         */

        LearningType learningType =
                determineLearningType(
                        reflection
                );

        /*
         * ================================================================
         * 11. NORMALIZATION
         * ================================================================
         */

        deduplicate(patterns);
        deduplicate(successfulStrategies);
        deduplicate(failurePatterns);
        deduplicate(knowledgeGaps);
        deduplicate(behavioralSignals);
        deduplicate(adaptationRecommendations);
        deduplicate(futureDecisionGuidance);
        deduplicate(learningSignals);

        /*
         * ================================================================
         * 12. SAFE FALLBACK
         * ================================================================
         */

        if (learningSignals.isEmpty()) {

            learningSignals.add(
                    "NO_ACTIONABLE_LEARNING_SIGNAL"
            );
        }

        if (futureDecisionGuidance.isEmpty()) {

            futureDecisionGuidance.add(
                    "No additional decision guidance can be derived from the current evidence"
            );
        }

        if (adaptationRecommendations.isEmpty()) {

            adaptationRecommendations.add(
                    "No adaptation is currently justified by available evidence"
            );
        }

        /*
         * ================================================================
         * 13. METADATA
         * ================================================================
         */

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "engine",
                "LearningIntelligenceEngine"
        );

        metadata.put(
                "version",
                "1.0"
        );

        metadata.put(
                "reflectionVerdict",
                reflection.verdict().name()
        );

        metadata.put(
                "reflectionConfidence",
                reflection.confidence()
        );

        metadata.put(
                "learningConfidence",
                learningConfidence
        );

        metadata.put(
                "learningConfidenceBand",
                confidenceBand(
                        learningConfidence
                )
        );

        metadata.put(
                "learningType",
                learningType.name()
        );

        metadata.put(
                "learningPriority",
                priority.name()
        );

        metadata.put(
                "patternCount",
                patterns.size()
        );

        metadata.put(
                "successfulStrategyCount",
                successfulStrategies.size()
        );

        metadata.put(
                "failurePatternCount",
                failurePatterns.size()
        );

        metadata.put(
                "knowledgeGapCount",
                knowledgeGaps.size()
        );

        metadata.put(
                "adaptationRecommendationCount",
                adaptationRecommendations.size()
        );

        metadata.put(
                "learningSignalCount",
                learningSignals.size()
        );

        metadata.put(
                "memoryMutationPerformed",
                false
        );

        metadata.put(
                "planMutationPerformed",
                false
        );

        metadata.put(
                "decisionMutationPerformed",
                false
        );

        metadata.put(
                "determinism",
                "DETERMINISTIC"
        );

        return new LearningAnalysis(
                learningType,
                priority,
                learningConfidence,
                confidenceBand(
                        learningConfidence
                ),
                List.copyOf(patterns),
                List.copyOf(successfulStrategies),
                List.copyOf(failurePatterns),
                List.copyOf(knowledgeGaps),
                List.copyOf(behavioralSignals),
                List.copyOf(adaptationRecommendations),
                List.copyOf(futureDecisionGuidance),
                List.copyOf(learningSignals),
                Map.copyOf(metadata)
        );
    }

    /**
     * Calculates learning confidence conservatively.
     */
    private double calculateLearningConfidence(
            ReflectionIntelligenceEngine.ReflectionAnalysis reflection) {

        double confidence =
                clamp(
                        reflection.confidence(),
                        0.0,
                        1.0
                );

        int evidenceGaps =
                reflection.missingInformation().size();

        int risks =
                reflection.risks().size();

        double gapPenalty =
                Math.min(
                        0.30,
                        evidenceGaps * 0.05
                );

        double riskPenalty =
                Math.min(
                        0.20,
                        risks * 0.025
                );

        double learningConfidence =
                confidence
                        - gapPenalty
                        - riskPenalty;

        /*
         * Successful verified outcomes are allowed a modest confidence
         * reinforcement, but never above the platform maximum.
         */
        if (reflection.verdict()
                == ReflectionIntelligenceEngine.ReflectionVerdict.VERIFIED_SUCCESS) {

            learningConfidence += 0.05;
        }

        return clamp(
                learningConfidence,
                MIN_CONFIDENCE,
                MAX_CONFIDENCE
        );
    }

    /**
     * Determines learning priority.
     */
    private LearningPriority determinePriority(
            ReflectionIntelligenceEngine.ReflectionAnalysis reflection,
            List<String> knowledgeGaps,
            List<String> failurePatterns,
            List<String> adaptations) {

        if (reflection.verdict()
                == ReflectionIntelligenceEngine.ReflectionVerdict.EXECUTION_FAILURE) {

            return LearningPriority.CRITICAL;
        }

        if (!failurePatterns.isEmpty()
                && !adaptations.isEmpty()) {

            return LearningPriority.HIGH;
        }

        if (!knowledgeGaps.isEmpty()) {

            return LearningPriority.MEDIUM;
        }

        if (!adaptations.isEmpty()) {

            return LearningPriority.MEDIUM;
        }

        return LearningPriority.LOW;
    }

    /**
     * Determines the type of learning.
     */
    private LearningType determineLearningType(
            ReflectionIntelligenceEngine.ReflectionAnalysis reflection) {

        return switch (reflection.verdict()) {

            case VERIFIED_SUCCESS ->
                    LearningType.SUCCESS_REINFORCEMENT;

            case EXECUTED_UNVERIFIED ->
                    LearningType.EVIDENCE_IMPROVEMENT;

            case PARTIAL_OUTCOME ->
                    LearningType.PARTIAL_OUTCOME_LEARNING;

            case EXECUTION_FAILURE ->
                    LearningType.FAILURE_LEARNING;

            case NEEDS_IMPROVEMENT ->
                    LearningType.ADAPTATION_LEARNING;

            case INSUFFICIENT_EVIDENCE ->
                    LearningType.UNCERTAINTY_LEARNING;

            case STABLE ->
                    LearningType.STABILITY_LEARNING;
        };
    }

    /**
     * Converts confidence to a stable confidence band.
     */
    private String confidenceBand(
            double confidence) {

        if (confidence < 0.25) {
            return "VERY_LOW";
        }

        if (confidence < 0.50) {
            return "LOW";
        }

        if (confidence < 0.75) {
            return "MODERATE";
        }

        if (confidence < 0.90) {
            return "HIGH";
        }

        return "VERY_HIGH";
    }

    /**
     * Normalizes a text signal.
     */
    private String normalize(
            String value) {

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Deduplicates a list while preserving insertion order.
     */
    private void deduplicate(
            List<String> values) {

        if (values.isEmpty()) {
            return;
        }

        Set<String> unique =
                new LinkedHashSet<>(
                        values
                );

        values.clear();
        values.addAll(unique);
    }

    /**
     * Clamps a numeric value.
     */
    private double clamp(
            double value,
            double minimum,
            double maximum) {

        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }

    /**
     * Learning type.
     */
    public enum LearningType {

        SUCCESS_REINFORCEMENT,

        FAILURE_LEARNING,

        PARTIAL_OUTCOME_LEARNING,

        EVIDENCE_IMPROVEMENT,

        UNCERTAINTY_LEARNING,

        ADAPTATION_LEARNING,

        STABILITY_LEARNING
    }

    /**
     * Learning priority.
     */
    public enum LearningPriority {

        LOW,

        MEDIUM,

        HIGH,

        CRITICAL
    }

    /**
     * Immutable learning analysis.
     */
    public record LearningAnalysis(
            LearningType learningType,
            LearningPriority priority,
            double confidence,
            String confidenceBand,
            List<String> patterns,
            List<String> successfulStrategies,
            List<String> failurePatterns,
            List<String> knowledgeGaps,
            List<String> behavioralSignals,
            List<String> adaptationRecommendations,
            List<String> futureDecisionGuidance,
            List<String> learningSignals,
            Map<String, Object> metadata) {

        public LearningAnalysis {

            Objects.requireNonNull(
                    learningType,
                    "learningType must not be null"
            );

            Objects.requireNonNull(
                    priority,
                    "priority must not be null"
            );

            Objects.requireNonNull(
                    confidenceBand,
                    "confidenceBand must not be null"
            );

            if (confidence < 0.0
                    || confidence > 1.0) {

                throw new IllegalArgumentException(
                        "confidence must be between 0.0 and 1.0"
                );
            }

            patterns =
                    patterns == null
                            ? List.of()
                            : List.copyOf(patterns);

            successfulStrategies =
                    successfulStrategies == null
                            ? List.of()
                            : List.copyOf(
                            successfulStrategies
                    );

            failurePatterns =
                    failurePatterns == null
                            ? List.of()
                            : List.copyOf(
                            failurePatterns
                    );

            knowledgeGaps =
                    knowledgeGaps == null
                            ? List.of()
                            : List.copyOf(
                            knowledgeGaps
                    );

            behavioralSignals =
                    behavioralSignals == null
                            ? List.of()
                            : List.copyOf(
                            behavioralSignals
                    );

            adaptationRecommendations =
                    adaptationRecommendations == null
                            ? List.of()
                            : List.copyOf(
                            adaptationRecommendations
                    );

            futureDecisionGuidance =
                    futureDecisionGuidance == null
                            ? List.of()
                            : List.copyOf(
                            futureDecisionGuidance
                    );

            learningSignals =
                    learningSignals == null
                            ? List.of()
                            : List.copyOf(
                            learningSignals
                    );

            metadata =
                    metadata == null
                            ? Map.of()
                            : Map.copyOf(metadata);
        }
    }
}