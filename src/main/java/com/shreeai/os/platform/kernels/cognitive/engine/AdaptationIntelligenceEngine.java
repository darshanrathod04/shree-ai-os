package com.shreeai.os.platform.kernels.cognitive.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Adaptation Intelligence Engine.
 *
 * <p>Transforms structured Learning Intelligence into controlled,
 * evidence-aware adaptation proposals.</p>
 *
 * <p>This engine does not directly mutate:</p>
 * <ul>
 *     <li>Memory</li>
 *     <li>Knowledge</li>
 *     <li>Goals</li>
 *     <li>Plans</li>
 *     <li>Decisions</li>
 *     <li>Execution state</li>
 * </ul>
 *
 * <p>It produces an immutable {@link AdaptationAnalysis} which can later
 * be evaluated by a dedicated adaptation executor or policy layer.</p>
 */
public final class AdaptationIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    /**
     * Analyze learning intelligence and derive controlled adaptation.
     *
     * @param learning learning analysis
     * @return adaptation analysis
     */
    public AdaptationAnalysis analyze(
            LearningIntelligenceEngine.LearningAnalysis learning) {

        Objects.requireNonNull(
                learning,
                "learning must not be null"
        );

        List<String> detectedPatterns =
                new ArrayList<>();

        List<String> proposedChanges =
                new ArrayList<>();

        List<String> affectedLayers =
                new ArrayList<>();

        List<String> rationale =
                new ArrayList<>();

        List<String> constraints =
                new ArrayList<>();

        List<String> safetyConditions =
                new ArrayList<>();

        List<String> rollbackGuidance =
                new ArrayList<>();

        List<String> futureGuidance =
                new ArrayList<>();

        List<String> adaptationSignals =
                new ArrayList<>();

        /*
         * ================================================================
         * 1. LEARNING TYPE
         * ================================================================
         */

        switch (learning.learningType()) {

            case SUCCESS_REINFORCEMENT -> {

                detectedPatterns.add(
                        "SUCCESSFUL_STRATEGY"
                );

                proposedChanges.add(
                        "Increase preference for strategies associated with verified successful outcomes"
                );

                affectedLayers.add(
                        "DECISION"
                );

                affectedLayers.add(
                        "PLANNING"
                );

                rationale.add(
                        "Verified success provides positive evidence for future strategy selection"
                );

                safetyConditions.add(
                        "Only reinforce the strategy when future contexts remain sufficiently similar"
                );

                rollbackGuidance.add(
                        "Reduce strategy preference if subsequent executions invalidate the observed success"
                );

                adaptationSignals.add(
                        "STRATEGY_REINFORCEMENT"
                );
            }

            case FAILURE_LEARNING -> {

                detectedPatterns.add(
                        "REPEATED_OR_SIGNIFICANT_FAILURE"
                );

                proposedChanges.add(
                        "Reduce preference for the strategy associated with the observed failure"
                );

                proposedChanges.add(
                        "Require causal analysis before repeating the failed strategy"
                );

                affectedLayers.add(
                        "DECISION"
                );

                affectedLayers.add(
                        "PLANNING"
                );

                affectedLayers.add(
                        "EXECUTION"
                );

                rationale.add(
                        "Failure evidence indicates that repeating the same strategy without modification may be unsafe or ineffective"
                );

                constraints.add(
                        "Do not permanently reject a strategy based on a single low-confidence failure"
                );

                safetyConditions.add(
                        "Require sufficient evidence before applying persistent negative adaptation"
                );

                rollbackGuidance.add(
                        "Restore the previous strategy preference when later evidence demonstrates successful recovery"
                );

                adaptationSignals.add(
                        "FAILURE_AVERSION"
                );
            }

            case PARTIAL_OUTCOME_LEARNING -> {

                detectedPatterns.add(
                        "PARTIAL_OBJECTIVE_COMPLETION"
                );

                proposedChanges.add(
                        "Increase decomposition of multi-stage objectives"
                );

                proposedChanges.add(
                        "Introduce explicit intermediate outcome verification"
                );

                affectedLayers.add(
                        "PLANNING"
                );

                affectedLayers.add(
                        "EXECUTION"
                );

                rationale.add(
                        "Partial completion indicates that objective decomposition or intermediate verification can be improved"
                );

                safetyConditions.add(
                        "Do not increase decomposition when the task is already atomic"
                );

                rollbackGuidance.add(
                        "Return to the previous planning granularity if decomposition increases unnecessary complexity"
                );

                adaptationSignals.add(
                        "PLAN_DECOMPOSITION_IMPROVEMENT"
                );
            }

            case EVIDENCE_IMPROVEMENT -> {

                detectedPatterns.add(
                        "OUTCOME_VERIFICATION_GAP"
                );

                proposedChanges.add(
                        "Increase evidence collection around outcome verification"
                );

                affectedLayers.add(
                        "EXECUTION"
                );

                affectedLayers.add(
                        "REFLECTION"
                );

                rationale.add(
                        "Execution evidence is insufficient to reliably determine outcome quality"
                );

                constraints.add(
                        "Evidence collection must remain proportional to task risk and complexity"
                );

                safetyConditions.add(
                        "Do not treat execution completion as equivalent to verified success"
                );

                rollbackGuidance.add(
                        "Reduce additional evidence requirements when verification quality becomes consistently sufficient"
                );

                adaptationSignals.add(
                        "VERIFICATION_STRENGTHENING"
                );
            }

            case UNCERTAINTY_LEARNING -> {

                detectedPatterns.add(
                        "HIGH_INFORMATION_UNCERTAINTY"
                );

                proposedChanges.add(
                        "Increase information gathering before high-impact decisions"
                );

                affectedLayers.add(
                        "KNOWLEDGE"
                );

                affectedLayers.add(
                        "REASONING"
                );

                affectedLayers.add(
                        "DECISION"
                );

                rationale.add(
                        "Available evidence is insufficient to justify strong behavioral adaptation"
                );

                constraints.add(
                        "Do not manufacture knowledge to compensate for missing evidence"
                );

                safetyConditions.add(
                        "Lower confidence must propagate into downstream decisions"
                );

                rollbackGuidance.add(
                        "Remove temporary information-gathering requirements once evidence becomes sufficient"
                );

                adaptationSignals.add(
                        "UNCERTAINTY_HANDLING"
                );
            }

            case ADAPTATION_LEARNING -> {

                detectedPatterns.add(
                        "EXPLICIT_IMPROVEMENT_SIGNAL"
                );

                proposedChanges.add(
                        "Apply the highest-value improvement identified by reflection"
                );

                affectedLayers.add(
                        "PLANNING"
                );

                affectedLayers.add(
                        "DECISION"
                );

                rationale.add(
                        "Reflection identified an actionable improvement opportunity"
                );

                constraints.add(
                        "Adaptation must remain bounded by existing system policies"
                );

                safetyConditions.add(
                        "Validate proposed changes before persistent application"
                );

                rollbackGuidance.add(
                        "Revert the adaptation if measurable performance degrades"
                );

                adaptationSignals.add(
                        "IMPROVEMENT_APPLICATION"
                );
            }

            case STABILITY_LEARNING -> {

                detectedPatterns.add(
                        "STABLE_BEHAVIOR"
                );

                proposedChanges.add(
                        "Preserve the currently stable strategy"
                );

                affectedLayers.add(
                        "DECISION"
                );

                rationale.add(
                        "Current evidence does not justify unnecessary behavioral change"
                );

                safetyConditions.add(
                        "Avoid adaptation when stability is supported by sufficient evidence"
                );

                rollbackGuidance.add(
                        "No rollback required unless future evidence invalidates stability"
                );

                adaptationSignals.add(
                        "STABILITY_PRESERVATION"
                );
            }
        }

        /*
         * ================================================================
         * 2. LEARNING PATTERNS
         * ================================================================
         */

        for (String pattern : learning.patterns()) {

            if (pattern == null || pattern.isBlank()) {
                continue;
            }

            detectedPatterns.add(
                    normalize(pattern)
            );
        }

        /*
         * ================================================================
         * 3. SUCCESSFUL STRATEGIES
         * ================================================================
         */

        if (!learning.successfulStrategies().isEmpty()) {

            affectedLayers.add(
                    "STRATEGY_SELECTION"
            );

            futureGuidance.add(
                    "Prefer previously successful strategies when context compatibility is established"
            );
        }

        /*
         * ================================================================
         * 4. FAILURE PATTERNS
         * ================================================================
         */

        if (!learning.failurePatterns().isEmpty()) {

            affectedLayers.add(
                    "RISK_CONTROL"
            );

            constraints.add(
                    "Avoid blindly repeating known failure patterns"
            );

            futureGuidance.add(
                    "Check known failure conditions before executing similar strategies"
            );
        }

        /*
         * ================================================================
         * 5. KNOWLEDGE GAPS
         * ================================================================
         */

        if (!learning.knowledgeGaps().isEmpty()) {

            affectedLayers.add(
                    "KNOWLEDGE"
            );

            proposedChanges.add(
                    "Increase targeted information acquisition for identified knowledge gaps"
            );

            safetyConditions.add(
                    "Information acquisition must not be interpreted as confirmation of an unsupported hypothesis"
            );
        }

        /*
         * ================================================================
         * 6. BEHAVIORAL SIGNALS
         * ================================================================
         */

        for (String signal :
                learning.behavioralSignals()) {

            if (signal == null || signal.isBlank()) {
                continue;
            }

            adaptationSignals.add(
                    normalize(signal)
            );
        }

        /*
         * ================================================================
         * 7. EXISTING ADAPTATION RECOMMENDATIONS
         * ================================================================
         */

        for (String recommendation :
                learning.adaptationRecommendations()) {

            if (recommendation == null
                    || recommendation.isBlank()) {
                continue;
            }

            proposedChanges.add(
                    normalize(recommendation)
            );
        }

        /*
         * ================================================================
         * 8. FUTURE DECISION GUIDANCE
         * ================================================================
         */

        for (String guidance :
                learning.futureDecisionGuidance()) {

            if (guidance == null
                    || guidance.isBlank()) {
                continue;
            }

            futureGuidance.add(
                    normalize(guidance)
            );
        }

        /*
         * ================================================================
         * 9. CONFIDENCE
         * ================================================================
         *
         * Adaptation confidence is deliberately more conservative than
         * learning confidence.
         */

        double adaptationConfidence =
                calculateAdaptationConfidence(
                        learning
                );

        /*
         * ================================================================
         * 10. ADAPTATION TYPE
         * ================================================================
         */

        AdaptationType adaptationType =
                determineAdaptationType(
                        learning
                );

        /*
         * ================================================================
         * 11. PRIORITY
         * ================================================================
         */

        AdaptationPriority priority =
                determinePriority(
                        learning,
                        adaptationConfidence
                );

        /*
         * ================================================================
         * 12. SAFETY GATE
         * ================================================================
         */

        boolean safeToPropose =
                adaptationConfidence >= 0.40
                        && !proposedChanges.isEmpty();

        if (!safeToPropose) {

            safetyConditions.add(
                    "Adaptation confidence is insufficient for automatic application"
            );

            proposedChanges.add(
                    "Defer persistent adaptation until stronger evidence is available"
            );

            adaptationSignals.add(
                    "ADAPTATION_DEFERRED"
            );
        }

        /*
         * ================================================================
         * 13. DEDUPLICATION
         * ================================================================
         */

        deduplicate(detectedPatterns);
        deduplicate(proposedChanges);
        deduplicate(affectedLayers);
        deduplicate(rationale);
        deduplicate(constraints);
        deduplicate(safetyConditions);
        deduplicate(rollbackGuidance);
        deduplicate(futureGuidance);
        deduplicate(adaptationSignals);

        /*
         * ================================================================
         * 14. METADATA
         * ================================================================
         */

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "engine",
                "AdaptationIntelligenceEngine"
        );

        metadata.put(
                "version",
                "1.0"
        );

        metadata.put(
                "learningType",
                learning.learningType().name()
        );

        metadata.put(
                "learningPriority",
                learning.priority().name()
        );

        metadata.put(
                "learningConfidence",
                learning.confidence()
        );

        metadata.put(
                "adaptationConfidence",
                adaptationConfidence
        );

        metadata.put(
                "adaptationConfidenceBand",
                confidenceBand(
                        adaptationConfidence
                )
        );

        metadata.put(
                "adaptationType",
                adaptationType.name()
        );

        metadata.put(
                "adaptationPriority",
                priority.name()
        );

        metadata.put(
                "safeToPropose",
                safeToPropose
        );

        metadata.put(
                "persistentMutationPerformed",
                false
        );

        metadata.put(
                "memoryMutationPerformed",
                false
        );

        metadata.put(
                "decisionMutationPerformed",
                false
        );

        metadata.put(
                "planningMutationPerformed",
                false
        );

        metadata.put(
                "rollbackRequired",
                false
        );

        metadata.put(
                "determinism",
                "DETERMINISTIC"
        );

        return new AdaptationAnalysis(
                adaptationType,
                priority,
                adaptationConfidence,
                confidenceBand(
                        adaptationConfidence
                ),
                safeToPropose,
                List.copyOf(detectedPatterns),
                List.copyOf(proposedChanges),
                List.copyOf(affectedLayers),
                List.copyOf(rationale),
                List.copyOf(constraints),
                List.copyOf(safetyConditions),
                List.copyOf(rollbackGuidance),
                List.copyOf(futureGuidance),
                List.copyOf(adaptationSignals),
                Map.copyOf(metadata)
        );
    }

    /**
     * Calculate conservative adaptation confidence.
     */
    private double calculateAdaptationConfidence(
            LearningIntelligenceEngine.LearningAnalysis learning) {

        double confidence =
                clamp(
                        learning.confidence(),
                        0.0,
                        1.0
                );

        double penalty = 0.0;

        penalty += Math.min(
                0.20,
                learning.knowledgeGaps().size() * 0.04
        );

        penalty += Math.min(
                0.15,
                learning.failurePatterns().size() * 0.025
        );

        /*
         * Adaptation must be more conservative than learning.
         */
        confidence *= 0.90;

        confidence -= penalty;

        return clamp(
                confidence,
                MIN_CONFIDENCE,
                MAX_CONFIDENCE
        );
    }

    /**
     * Determine adaptation type.
     */
    private AdaptationType determineAdaptationType(
            LearningIntelligenceEngine.LearningAnalysis learning) {

        return switch (learning.learningType()) {

            case SUCCESS_REINFORCEMENT ->
                    AdaptationType.STRATEGY_REINFORCEMENT;

            case FAILURE_LEARNING ->
                    AdaptationType.FAILURE_AVOIDANCE;

            case PARTIAL_OUTCOME_LEARNING ->
                    AdaptationType.PLAN_REFINEMENT;

            case EVIDENCE_IMPROVEMENT ->
                    AdaptationType.EVIDENCE_STRENGTHENING;

            case UNCERTAINTY_LEARNING ->
                    AdaptationType.UNCERTAINTY_CONTROL;

            case ADAPTATION_LEARNING ->
                    AdaptationType.BEHAVIOR_IMPROVEMENT;

            case STABILITY_LEARNING ->
                    AdaptationType.STABILITY_PRESERVATION;
        };
    }

    /**
     * Determine adaptation priority.
     */
    private AdaptationPriority determinePriority(
            LearningIntelligenceEngine.LearningAnalysis learning,
            double confidence) {

        if (learning.priority()
                == LearningIntelligenceEngine.LearningPriority.CRITICAL) {

            return confidence >= 0.65
                    ? AdaptationPriority.CRITICAL
                    : AdaptationPriority.HIGH;
        }

        if (learning.priority()
                == LearningIntelligenceEngine.LearningPriority.HIGH) {

            return confidence >= 0.60
                    ? AdaptationPriority.HIGH
                    : AdaptationPriority.MEDIUM;
        }

        if (learning.priority()
                == LearningIntelligenceEngine.LearningPriority.MEDIUM) {

            return AdaptationPriority.MEDIUM;
        }

        return AdaptationPriority.LOW;
    }

    /**
     * Confidence band.
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
     * Normalize text.
     */
    private String normalize(
            String value) {

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Deduplicate while preserving order.
     */
    private void deduplicate(
            List<String> values) {

        Set<String> unique =
                new LinkedHashSet<>(
                        values
                );

        values.clear();
        values.addAll(unique);
    }

    /**
     * Clamp value.
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
     * Adaptation types.
     */
    public enum AdaptationType {

        STRATEGY_REINFORCEMENT,

        FAILURE_AVOIDANCE,

        PLAN_REFINEMENT,

        EVIDENCE_STRENGTHENING,

        UNCERTAINTY_CONTROL,

        BEHAVIOR_IMPROVEMENT,

        STABILITY_PRESERVATION
    }

    /**
     * Adaptation priority.
     */
    public enum AdaptationPriority {

        LOW,

        MEDIUM,

        HIGH,

        CRITICAL
    }

    /**
     * Immutable adaptation analysis.
     */
    public record AdaptationAnalysis(
            AdaptationType adaptationType,
            AdaptationPriority priority,
            double confidence,
            String confidenceBand,
            boolean safeToPropose,
            List<String> detectedPatterns,
            List<String> proposedChanges,
            List<String> affectedLayers,
            List<String> rationale,
            List<String> constraints,
            List<String> safetyConditions,
            List<String> rollbackGuidance,
            List<String> futureGuidance,
            List<String> adaptationSignals,
            Map<String, Object> metadata) {

        public AdaptationAnalysis {

            Objects.requireNonNull(
                    adaptationType,
                    "adaptationType must not be null"
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

            detectedPatterns =
                    detectedPatterns == null
                            ? List.of()
                            : List.copyOf(
                            detectedPatterns
                    );

            proposedChanges =
                    proposedChanges == null
                            ? List.of()
                            : List.copyOf(
                            proposedChanges
                    );

            affectedLayers =
                    affectedLayers == null
                            ? List.of()
                            : List.copyOf(
                            affectedLayers
                    );

            rationale =
                    rationale == null
                            ? List.of()
                            : List.copyOf(
                            rationale
                    );

            constraints =
                    constraints == null
                            ? List.of()
                            : List.copyOf(
                            constraints
                    );

            safetyConditions =
                    safetyConditions == null
                            ? List.of()
                            : List.copyOf(
                            safetyConditions
                    );

            rollbackGuidance =
                    rollbackGuidance == null
                            ? List.of()
                            : List.copyOf(
                            rollbackGuidance
                    );

            futureGuidance =
                    futureGuidance == null
                            ? List.of()
                            : List.copyOf(
                            futureGuidance
                    );

            adaptationSignals =
                    adaptationSignals == null
                            ? List.of()
                            : List.copyOf(
                            adaptationSignals
                    );

            metadata =
                    metadata == null
                            ? Map.of()
                            : Map.copyOf(metadata);
        }
    }
}