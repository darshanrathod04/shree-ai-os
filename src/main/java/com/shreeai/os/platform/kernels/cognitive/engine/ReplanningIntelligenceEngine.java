package com.shreeai.os.platform.kernels.cognitive.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Replanning Intelligence Engine.
 *
 * <p>Transforms adaptation intelligence into a controlled proposal for
 * revising future planning behavior.</p>
 *
 * <p>This engine does not mutate an existing plan. It determines whether
 * replanning is justified and describes the changes that a planning layer
 * should consider.</p>
 */
public final class ReplanningIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    /**
     * Analyze an adaptation proposal and derive replanning intelligence.
     *
     * @param adaptation adaptation analysis
     * @return immutable replanning analysis
     */
    public ReplanningAnalysis analyze(
            AdaptationIntelligenceEngine.AdaptationAnalysis adaptation) {

        Objects.requireNonNull(
                adaptation,
                "adaptation must not be null"
        );

        List<String> triggers =
                new ArrayList<>();

        List<String> proposedPlanChanges =
                new ArrayList<>();

        List<String> preservedConstraints =
                new ArrayList<>();

        List<String> requiredEvidence =
                new ArrayList<>();

        List<String> risks =
                new ArrayList<>();

        List<String> rollbackGuidance =
                new ArrayList<>();

        List<String> nextPlanningGuidance =
                new ArrayList<>();

        List<String> replanningSignals =
                new ArrayList<>();

        /*
         * ================================================================
         * 1. DETERMINE WHETHER REPLANNING IS JUSTIFIED
         * ================================================================
         */

        boolean replanningRequired =
                determineReplanningRequirement(
                        adaptation
                );

        /*
         * ================================================================
         * 2. ADAPTATION TYPE → REPLANNING STRATEGY
         * ================================================================
         */

        switch (adaptation.adaptationType()) {

            case STRATEGY_REINFORCEMENT -> {

                triggers.add(
                        "VERIFIED_SUCCESS_SUPPORTS_STRATEGY_REINFORCEMENT"
                );

                proposedPlanChanges.add(
                        "Increase preference for previously successful planning strategies when context compatibility is established"
                );

                nextPlanningGuidance.add(
                        "Reuse successful strategy patterns before introducing unnecessary alternatives"
                );

                replanningSignals.add(
                        "STRATEGY_REUSE"
                );
            }

            case FAILURE_AVOIDANCE -> {

                triggers.add(
                        "FAILURE_PATTERN_REQUIRES_PLAN_REVISION"
                );

                proposedPlanChanges.add(
                        "Avoid the previously unsuccessful strategy when the same failure conditions remain present"
                );

                proposedPlanChanges.add(
                        "Introduce an alternative execution path or mitigation step"
                );

                risks.add(
                        "Overreacting to a single low-confidence failure may eliminate a potentially valid strategy"
                );

                requiredEvidence.add(
                        "Evidence that the failure condition is relevant to the current planning context"
                );

                rollbackGuidance.add(
                        "Restore the previous strategy when subsequent evidence demonstrates that the failure condition is no longer applicable"
                );

                replanningSignals.add(
                        "FAILURE_AVOIDANCE"
                );
            }

            case PLAN_REFINEMENT -> {

                triggers.add(
                        "PARTIAL_OUTCOME_REQUIRES_PLAN_REFINEMENT"
                );

                proposedPlanChanges.add(
                        "Decompose incomplete objectives into smaller verifiable planning stages"
                );

                proposedPlanChanges.add(
                        "Add intermediate outcome checkpoints"
                );

                nextPlanningGuidance.add(
                        "Prefer measurable intermediate milestones for objectives with multiple completion conditions"
                );

                replanningSignals.add(
                        "PLAN_DECOMPOSITION"
                );
            }

            case EVIDENCE_STRENGTHENING -> {

                triggers.add(
                        "INSUFFICIENT_OUTCOME_EVIDENCE"
                );

                proposedPlanChanges.add(
                        "Add explicit evidence collection or verification steps"
                );

                requiredEvidence.add(
                        "Evidence capable of confirming the intended outcome"
                );

                risks.add(
                        "Excessive verification can introduce unnecessary execution overhead"
                );

                nextPlanningGuidance.add(
                        "Scale verification effort according to task complexity and risk"
                );

                replanningSignals.add(
                        "VERIFICATION_AWARE_PLANNING"
                );
            }

            case UNCERTAINTY_CONTROL -> {

                triggers.add(
                        "KNOWLEDGE_UNCERTAINTY_REQUIRES_INFORMATION_GATHERING"
                );

                proposedPlanChanges.add(
                        "Insert targeted information-gathering steps before high-impact actions"
                );

                requiredEvidence.add(
                        "Reliable information addressing the identified knowledge gap"
                );

                risks.add(
                        "Planning from unsupported assumptions can amplify uncertainty"
                );

                nextPlanningGuidance.add(
                        "Resolve the highest-impact information gap before committing to irreversible actions"
                );

                replanningSignals.add(
                        "INFORMATION_GATHERING"
                );
            }

            case BEHAVIOR_IMPROVEMENT -> {

                triggers.add(
                        "REFLECTION_IDENTIFIED_ACTIONABLE_IMPROVEMENT"
                );

                proposedPlanChanges.add(
                        "Apply the highest-value validated improvement to the next planning cycle"
                );

                requiredEvidence.add(
                        "Evidence that the proposed improvement addresses the observed problem"
                );

                nextPlanningGuidance.add(
                        "Prefer bounded improvements that can be evaluated after execution"
                );

                rollbackGuidance.add(
                        "Revert the planning adjustment when measurable performance decreases"
                );

                replanningSignals.add(
                        "CONTROLLED_IMPROVEMENT"
                );
            }

            case STABILITY_PRESERVATION -> {

                triggers.add(
                        "STABLE_BEHAVIOR_DOES_NOT_JUSTIFY_UNNECESSARY_REPLANNING"
                );

                proposedPlanChanges.add(
                        "Preserve the current planning strategy unless new evidence invalidates stability"
                );

                nextPlanningGuidance.add(
                        "Avoid unnecessary replanning when the current strategy remains supported by evidence"
                );

                replanningSignals.add(
                        "STABILITY_PRESERVATION"
                );
            }
        }

        /*
         * ================================================================
         * 3. PRESERVE SYSTEM CONSTRAINTS
         * ================================================================
         */

        preservedConstraints.add(
                "Do not violate platform safety policies"
        );

        preservedConstraints.add(
                "Do not treat an adaptation proposal as an executed plan"
        );

        preservedConstraints.add(
                "Preserve user and application constraints unless explicitly superseded by a higher-authority policy"
        );

        preservedConstraints.add(
                "Preserve required execution preconditions"
        );

        preservedConstraints.add(
                "Do not silently remove verification requirements"
        );

        /*
         * ================================================================
         * 4. IMPORT ADAPTATION SAFETY CONDITIONS
         * ================================================================
         */

        for (String condition :
                adaptation.safetyConditions()) {

            if (condition == null
                    || condition.isBlank()) {
                continue;
            }

            preservedConstraints.add(
                    normalize(condition)
            );
        }

        /*
         * ================================================================
         * 5. IMPORT ADAPTATION CONSTRAINTS
         * ================================================================
         */

        for (String constraint :
                adaptation.constraints()) {

            if (constraint == null
                    || constraint.isBlank()) {
                continue;
            }

            preservedConstraints.add(
                    normalize(constraint)
            );
        }

        /*
         * ================================================================
         * 6. ADAPTATION RATIONALE → PLANNING RATIONALE
         * ================================================================
         */

        for (String rationale :
                adaptation.rationale()) {

            if (rationale == null
                    || rationale.isBlank()) {
                continue;
            }

            requiredEvidence.add(
                    "Validate rationale: "
                            + normalize(rationale)
            );
        }

        /*
         * ================================================================
         * 7. ADAPTATION ROLLBACK → REPLANNING ROLLBACK
         * ================================================================
         */

        for (String rollback :
                adaptation.rollbackGuidance()) {

            if (rollback == null
                    || rollback.isBlank()) {
                continue;
            }

            rollbackGuidance.add(
                    normalize(rollback)
            );
        }

        /*
         * ================================================================
         * 8. CONFIDENCE
         * ================================================================
         *
         * Replanning is intentionally more conservative than adaptation.
         */

        double replanningConfidence =
                calculateReplanningConfidence(
                        adaptation,
                        replanningRequired
                );

        /*
         * ================================================================
         * 9. PRIORITY
         * ================================================================
         */

        ReplanningPriority priority =
                determinePriority(
                        adaptation,
                        replanningConfidence,
                        replanningRequired
                );

        /*
         * ================================================================
         * 10. SAFETY GATE
         * ================================================================
         */

        boolean safeToReplan =
                replanningRequired
                        && adaptation.safeToPropose()
                        && replanningConfidence >= 0.40;

        if (!safeToReplan) {

            proposedPlanChanges.clear();

            proposedPlanChanges.add(
                    "Do not automatically replace the current plan; gather stronger evidence before replanning"
            );

            requiredEvidence.add(
                    "Additional evidence sufficient to justify plan replacement or modification"
            );

            replanningSignals.add(
                    "REPLANNING_DEFERRED"
            );
        }

        /*
         * ================================================================
         * 11. DEDUPLICATION
         * ================================================================
         */

        deduplicate(triggers);
        deduplicate(proposedPlanChanges);
        deduplicate(preservedConstraints);
        deduplicate(requiredEvidence);
        deduplicate(risks);
        deduplicate(rollbackGuidance);
        deduplicate(nextPlanningGuidance);
        deduplicate(replanningSignals);

        /*
         * ================================================================
         * 12. METADATA
         * ================================================================
         */

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "engine",
                "ReplanningIntelligenceEngine"
        );

        metadata.put(
                "version",
                "1.0"
        );

        metadata.put(
                "adaptationType",
                adaptation.adaptationType().name()
        );

        metadata.put(
                "adaptationPriority",
                adaptation.priority().name()
        );

        metadata.put(
                "adaptationConfidence",
                adaptation.confidence()
        );

        metadata.put(
                "replanningRequired",
                replanningRequired
        );

        metadata.put(
                "replanningConfidence",
                replanningConfidence
        );

        metadata.put(
                "replanningConfidenceBand",
                confidenceBand(
                        replanningConfidence
                )
        );

        metadata.put(
                "replanningPriority",
                priority.name()
        );

        metadata.put(
                "safeToReplan",
                safeToReplan
        );

        metadata.put(
                "planMutationPerformed",
                false
        );

        metadata.put(
                "executionStarted",
                false
        );

        metadata.put(
                "rollbackPerformed",
                false
        );

        metadata.put(
                "determinism",
                "DETERMINISTIC"
        );

        return new ReplanningAnalysis(
                replanningRequired,
                priority,
                replanningConfidence,
                confidenceBand(
                        replanningConfidence
                ),
                safeToReplan,
                List.copyOf(triggers),
                List.copyOf(proposedPlanChanges),
                List.copyOf(preservedConstraints),
                List.copyOf(requiredEvidence),
                List.copyOf(risks),
                List.copyOf(rollbackGuidance),
                List.copyOf(nextPlanningGuidance),
                List.copyOf(replanningSignals),
                Map.copyOf(metadata)
        );
    }

    /**
     * Determines whether the current adaptation justifies replanning.
     */
    private boolean determineReplanningRequirement(
            AdaptationIntelligenceEngine.AdaptationAnalysis adaptation) {

        if (!adaptation.safeToPropose()) {
            return false;
        }

        return switch (adaptation.adaptationType()) {

            case STRATEGY_REINFORCEMENT,
                 FAILURE_AVOIDANCE,
                 PLAN_REFINEMENT,
                 EVIDENCE_STRENGTHENING,
                 UNCERTAINTY_CONTROL,
                 BEHAVIOR_IMPROVEMENT -> true;

            case STABILITY_PRESERVATION ->
                    false;
        };
    }

    /**
     * Calculates conservative replanning confidence.
     */
    private double calculateReplanningConfidence(
            AdaptationIntelligenceEngine.AdaptationAnalysis adaptation,
            boolean replanningRequired) {

        if (!replanningRequired) {

            return clamp(
                    adaptation.confidence() * 0.50,
                    MIN_CONFIDENCE,
                    MAX_CONFIDENCE
            );
        }

        double confidence =
                adaptation.confidence() * 0.85;

        confidence -= Math.min(
                0.20,
                adaptation.constraints().size() * 0.025
        );

        confidence -= Math.min(
                0.15,
                adaptation.safetyConditions().size() * 0.015
        );

        return clamp(
                confidence,
                MIN_CONFIDENCE,
                MAX_CONFIDENCE
        );
    }

    /**
     * Determines replanning priority.
     */
    private ReplanningPriority determinePriority(
            AdaptationIntelligenceEngine.AdaptationAnalysis adaptation,
            double confidence,
            boolean replanningRequired) {

        if (!replanningRequired) {
            return ReplanningPriority.NONE;
        }

        if (adaptation.priority()
                == AdaptationIntelligenceEngine.AdaptationPriority.CRITICAL) {

            return confidence >= 0.65
                    ? ReplanningPriority.CRITICAL
                    : ReplanningPriority.HIGH;
        }

        if (adaptation.priority()
                == AdaptationIntelligenceEngine.AdaptationPriority.HIGH) {

            return confidence >= 0.60
                    ? ReplanningPriority.HIGH
                    : ReplanningPriority.MEDIUM;
        }

        if (adaptation.priority()
                == AdaptationIntelligenceEngine.AdaptationPriority.MEDIUM) {

            return ReplanningPriority.MEDIUM;
        }

        return ReplanningPriority.LOW;
    }

    /**
     * Converts confidence to a stable band.
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
     * Deduplicate while preserving insertion order.
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
     * Clamp numeric value.
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
     * Replanning priority.
     */
    public enum ReplanningPriority {

        NONE,

        LOW,

        MEDIUM,

        HIGH,

        CRITICAL
    }

    /**
     * Immutable replanning analysis.
     */
    public record ReplanningAnalysis(
            boolean replanningRequired,
            ReplanningPriority priority,
            double confidence,
            String confidenceBand,
            boolean safeToReplan,
            List<String> triggers,
            List<String> proposedPlanChanges,
            List<String> preservedConstraints,
            List<String> requiredEvidence,
            List<String> risks,
            List<String> rollbackGuidance,
            List<String> nextPlanningGuidance,
            List<String> replanningSignals,
            Map<String, Object> metadata) {

        public ReplanningAnalysis {

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

            triggers =
                    triggers == null
                            ? List.of()
                            : List.copyOf(triggers);

            proposedPlanChanges =
                    proposedPlanChanges == null
                            ? List.of()
                            : List.copyOf(
                            proposedPlanChanges
                    );

            preservedConstraints =
                    preservedConstraints == null
                            ? List.of()
                            : List.copyOf(
                            preservedConstraints
                    );

            requiredEvidence =
                    requiredEvidence == null
                            ? List.of()
                            : List.copyOf(
                            requiredEvidence
                    );

            risks =
                    risks == null
                            ? List.of()
                            : List.copyOf(risks);

            rollbackGuidance =
                    rollbackGuidance == null
                            ? List.of()
                            : List.copyOf(
                            rollbackGuidance
                    );

            nextPlanningGuidance =
                    nextPlanningGuidance == null
                            ? List.of()
                            : List.copyOf(
                            nextPlanningGuidance
                    );

            replanningSignals =
                    replanningSignals == null
                            ? List.of()
                            : List.copyOf(
                            replanningSignals
                    );

            metadata =
                    metadata == null
                            ? Map.of()
                            : Map.copyOf(metadata);
        }
    }
}