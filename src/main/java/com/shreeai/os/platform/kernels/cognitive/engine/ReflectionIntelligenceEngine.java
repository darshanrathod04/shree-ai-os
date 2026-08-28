package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.cognitive.model.CognitiveState;
import com.shreeai.os.platform.kernels.cognitive.model.ReflectionScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Advanced deterministic reflection intelligence for the Cognitive Kernel.
 *
 * <p>This engine analyzes the observable results of a cognitive/execution
 * lifecycle and converts them into structured reflection intelligence.</p>
 *
 * <p>The engine answers five core questions:</p>
 *
 * <ol>
 *     <li>What happened?</li>
 *     <li>How reliable was the result?</li>
 *     <li>What went well?</li>
 *     <li>What went wrong or remains uncertain?</li>
 *     <li>What should change on the next iteration?</li>
 * </ol>
 *
 * <p>The engine is deliberately deterministic. It does not call an LLM,
 * mutate memory, modify plans, execute actions, or make autonomous state
 * changes.</p>
 *
 * <p>Instead, it produces machine-readable reflection signals that can later
 * be consumed by Learning, Adaptation, Replanning, or Meta-Cognition layers.</p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Reflection Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Legacy dependency:</b> None.</p>
 */
public final class ReflectionIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    /**
     * Performs advanced reflection analysis.
     *
     * @param scope reflection scope
     * @param state current cognitive state
     * @return immutable reflection analysis
     */
    public ReflectionAnalysis analyze(
            ReflectionScope scope,
            CognitiveState state) {

        Objects.requireNonNull(
                scope,
                "ReflectionScope must not be null"
        );

        Objects.requireNonNull(
                state,
                "CognitiveState must not be null"
        );

        Map<String, Object> scopeMetadata =
                safeMap(scope.metadata());

        Map<String, Object> stateMetadata =
                safeMap(state.metadata());

        List<String> observations =
                new ArrayList<>();

        List<String> strengths =
                new ArrayList<>();

        List<String> risks =
                new ArrayList<>();

        List<String> missingInformation =
                new ArrayList<>();

        List<String> improvementOpportunities =
                new ArrayList<>();

        List<String> nextActions =
                new ArrayList<>();

        List<String> learningSignals =
                new ArrayList<>();

        int availableSignals = 0;
        int positiveSignals = 0;
        int negativeSignals = 0;

        /*
         * ================================================================
         * 1. REFLECTION SCOPE ANALYSIS
         * ================================================================
         */

        String target =
                normalize(scope.reflectionTarget());

        observations.add(
                "Reflection target: " + target
        );

        if (!scope.includedArtifacts().isEmpty()) {

            availableSignals++;

            observations.add(
                    "Reflection scope contains "
                            + scope.includedArtifacts().size()
                            + " artifact(s)"
            );

            strengths.add(
                    "Reflection has explicit artifacts available for analysis"
            );

            positiveSignals++;

        } else {

            risks.add(
                    "Reflection scope contains no explicit artifacts"
            );

            missingInformation.add(
                    "Observable artifacts or execution results"
            );

            negativeSignals++;
        }

        /*
         * ================================================================
         * 2. EXECUTION OUTCOME ANALYSIS
         * ================================================================
         */

        Object outcomeVerified =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "outcomeVerified"
                );

        Object outcomeState =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "outcomeVerificationState"
                );

        Object outcomeConfidence =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "outcomeVerificationConfidence"
                );

        Object outcomeVerdict =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "outcomeVerificationVerdict"
                );

        if (outcomeState != null) {

            availableSignals++;

            String stateValue =
                    String.valueOf(outcomeState)
                            .toUpperCase(Locale.ROOT);

            observations.add(
                    "Outcome verification state: "
                            + stateValue
            );

            switch (stateValue) {

                case "VERIFIED_SUCCESS",
                     "SUCCESS",
                     "VERIFIED" -> {

                    strengths.add(
                            "The execution outcome has explicit verification support"
                    );

                    positiveSignals++;
                }

                case "PARTIAL_OUTCOME",
                     "PARTIAL" -> {

                    risks.add(
                            "Execution produced only a partial outcome"
                    );

                    improvementOpportunities.add(
                            "Identify the remaining outcome gap and replan only the incomplete portion"
                    );

                    negativeSignals++;
                }

                case "FAILED_EXECUTION",
                     "FAILED",
                     "ERROR" -> {

                    risks.add(
                            "Execution failed and requires causal analysis"
                    );

                    improvementOpportunities.add(
                            "Analyze the execution failure before retrying or replanning"
                    );

                    nextActions.add(
                            "Evaluate retry, rollback, compensation, or replanning"
                    );

                    negativeSignals++;
                }

                case "EXECUTED_UNVERIFIED",
                     "UNVERIFIED",
                     "UNKNOWN" -> {

                    risks.add(
                            "Execution completed without sufficient outcome verification"
                    );

                    missingInformation.add(
                            "Explicit post-execution verification evidence"
                    );

                    improvementOpportunities.add(
                            "Add an explicit outcome verification signal to future executions"
                    );

                    negativeSignals++;
                }

                default -> {

                    risks.add(
                            "Outcome state is not recognized by the reflection policy"
                    );

                    missingInformation.add(
                            "Normalized outcome verification state"
                    );
                }
            }
        } else {

            risks.add(
                    "No outcome verification state is available"
            );

            missingInformation.add(
                    "Outcome verification result"
            );

            negativeSignals++;
        }

        if (outcomeVerified instanceof Boolean verified) {

            availableSignals++;

            if (verified) {

                strengths.add(
                        "Outcome verification explicitly reports success"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Outcome verification did not establish success"
                );

                negativeSignals++;
            }
        }

        Double verificationConfidence =
                numberValue(outcomeConfidence);

        if (verificationConfidence != null) {

            availableSignals++;

            double confidence =
                    clamp(
                            verificationConfidence,
                            0.0,
                            1.0
                    );

            observations.add(
                    "Outcome verification confidence: "
                            + format(confidence)
            );

            if (confidence >= 0.75) {

                strengths.add(
                        "Outcome verification has high confidence"
                );

                positiveSignals++;

            } else if (confidence < 0.40) {

                risks.add(
                        "Outcome verification confidence is low"
                );

                improvementOpportunities.add(
                        "Improve the quality or specificity of post-execution evidence"
                );

                negativeSignals++;
            }
        }

        if (outcomeVerdict != null) {

            observations.add(
                    "Outcome verification verdict: "
                            + String.valueOf(outcomeVerdict)
            );
        }

        /*
         * ================================================================
         * 3. REASONING ANALYSIS
         * ================================================================
         */

        Double reasoningConfidence =
                numberValue(
                        firstValue(
                                scopeMetadata,
                                stateMetadata,
                                "reasoningConfidence"
                        )
                );

        Object reasoningType =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "reasoningType"
                );

        Object reasoningConclusion =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "reasoningConclusion"
                );

        Object reasoningEvidence =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "reasoningEvidence"
                );

        if (reasoningType != null) {

            availableSignals++;

            observations.add(
                    "Reasoning mode: "
                            + reasoningType
            );
        }

        if (reasoningConclusion != null) {

            availableSignals++;

            if (!String.valueOf(
                    reasoningConclusion
            ).isBlank()) {

                strengths.add(
                        "A concrete reasoning conclusion was produced"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Reasoning produced an empty conclusion"
                );

                negativeSignals++;
            }
        }

        if (reasoningEvidence != null) {

            availableSignals++;

            if (hasEvidence(
                    reasoningEvidence
            )) {

                strengths.add(
                        "Reasoning retained supporting evidence"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Reasoning conclusion has insufficient retained evidence"
                );

                missingInformation.add(
                        "Supporting reasoning evidence"
                );

                negativeSignals++;
            }
        }

        if (reasoningConfidence != null) {

            availableSignals++;

            double confidence =
                    clamp(
                            reasoningConfidence,
                            0.0,
                            1.0
                    );

            observations.add(
                    "Reasoning confidence: "
                            + format(confidence)
            );

            if (confidence >= 0.75) {

                strengths.add(
                        "Reasoning confidence is high"
                );

                positiveSignals++;

            } else if (confidence < 0.40) {

                risks.add(
                        "Reasoning confidence is low"
                );

                improvementOpportunities.add(
                        "Increase evidence coverage before committing to high-impact decisions"
                );

                negativeSignals++;
            }
        }

        /*
         * ================================================================
         * 4. INFERENCE ANALYSIS
         * ================================================================
         */

        Object inferenceCompleted =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "inferenceCompleted"
                );

        Object inferenceConfidence =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "inferenceConfidence"
                );

        Object supportingEvidence =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "supportingEvidence"
                );

        if (inferenceCompleted instanceof Boolean completed) {

            availableSignals++;

            if (completed) {

                strengths.add(
                        "Inference completed successfully"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Inference did not complete successfully"
                );

                negativeSignals++;
            }
        }

        Double inferenceScore =
                numberValue(
                        inferenceConfidence
                );

        if (inferenceScore != null) {

            availableSignals++;

            if (inferenceScore >= 0.75) {

                strengths.add(
                        "Inference confidence is high"
                );

                positiveSignals++;

            } else if (inferenceScore < 0.40) {

                risks.add(
                        "Inference confidence is low"
                );

                negativeSignals++;
            }
        }

        if (supportingEvidence != null) {

            availableSignals++;

            if (hasEvidence(
                    supportingEvidence
            )) {

                strengths.add(
                        "Inference retained supporting evidence"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Inference contains no supporting evidence"
                );

                missingInformation.add(
                        "Inference supporting evidence"
                );

                negativeSignals++;
            }
        }

        /*
         * ================================================================
         * 5. PLANNING ANALYSIS
         * ================================================================
         */

        Object planningCompleted =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "planningCompleted"
                );

        Object planningQuality =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "planningQuality"
                );

        Object planId =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "planId"
                );

        if (planningCompleted instanceof Boolean completed) {

            availableSignals++;

            if (completed) {

                strengths.add(
                        "Planning completed"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Planning did not complete"
                );

                improvementOpportunities.add(
                        "Re-evaluate goal decomposition and task dependencies"
                );

                negativeSignals++;
            }
        }

        if (planId != null) {

            availableSignals++;

            if (!String.valueOf(
                    planId
            ).isBlank()) {

                observations.add(
                        "Active plan: " + planId
                );
            }
        }

        Double planQualityScore =
                numberValue(
                        planningQuality
                );

        if (planQualityScore != null) {

            availableSignals++;

            double quality =
                    clamp(
                            planQualityScore,
                            0.0,
                            1.0
                    );

            observations.add(
                    "Plan quality: "
                            + format(quality)
            );

            if (quality >= 0.75) {

                strengths.add(
                        "Planning quality is high"
                );

                positiveSignals++;

            } else if (quality < 0.40) {

                risks.add(
                        "Planning quality is low"
                );

                improvementOpportunities.add(
                        "Improve task decomposition, dependency ordering, or constraint coverage"
                );

                negativeSignals++;
            }
        }

        /*
         * ================================================================
         * 6. DECISION ANALYSIS
         * ================================================================
         */

        Object decisionConfidence =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "decisionConfidence"
                );

        Object recommendationSupported =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "recommendationSupported"
                );

        Object decisionStatus =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "decisionStatus"
                );

        Double decisionScore =
                numberValue(
                        decisionConfidence
                );

        if (decisionScore != null) {

            availableSignals++;

            double confidence =
                    clamp(
                            decisionScore,
                            0.0,
                            1.0
                    );

            observations.add(
                    "Decision confidence: "
                            + format(confidence)
            );

            if (confidence >= 0.75) {

                strengths.add(
                        "Decision confidence is high"
                );

                positiveSignals++;

            } else if (confidence < 0.40) {

                risks.add(
                        "Decision confidence is low"
                );

                improvementOpportunities.add(
                        "Collect stronger alternative-specific evidence before committing"
                );

                negativeSignals++;
            }
        }

        if (recommendationSupported instanceof Boolean supported) {

            availableSignals++;

            if (supported) {

                strengths.add(
                        "Decision recommendation is explicitly supported"
                );

                positiveSignals++;

            } else {

                risks.add(
                        "Decision recommendation is not evidence-supported"
                );

                missingInformation.add(
                        "Discriminating decision evidence"
                );

                negativeSignals++;
            }
        }

        if (decisionStatus != null) {

            observations.add(
                    "Decision status: "
                            + decisionStatus
            );
        }

        /*
         * ================================================================
         * 7. FAILURE / ERROR ANALYSIS
         * ================================================================
         */

        Object executionStatus =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "executionStatus"
                );

        Object error =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "error"
                );

        Object failure =
                firstValue(
                        scopeMetadata,
                        stateMetadata,
                        "failed"
                );

        if (executionStatus != null) {

            String status =
                    String.valueOf(
                            executionStatus
                    ).toUpperCase(Locale.ROOT);

            observations.add(
                    "Execution status: " + status
            );

            if (status.contains("FAILED")
                    || status.contains("ERROR")) {

                risks.add(
                        "Execution lifecycle contains a failure state"
                );

                negativeSignals++;
            }
        }

        if (failure instanceof Boolean failed
                && failed) {

            risks.add(
                    "Execution explicitly reports failure"
            );

            nextActions.add(
                    "Perform failure-cause analysis before another execution attempt"
            );

            negativeSignals++;
        }

        if (error != null) {

            risks.add(
                    "Execution produced an error signal"
            );

            observations.add(
                    "Execution error information is available"
            );

            improvementOpportunities.add(
                    "Use the recorded error as causal evidence for recovery or replanning"
            );

            negativeSignals++;
        }

        /*
         * ================================================================
         * 8. GENERAL REFLECTION SYNTHESIS
         * ================================================================
         */

        deduplicate(
                observations
        );

        deduplicate(
                strengths
        );

        deduplicate(
                risks
        );

        deduplicate(
                missingInformation
        );

        deduplicate(
                improvementOpportunities
        );

        deduplicate(
                nextActions
        );

        if (availableSignals == 0) {

            risks.add(
                    "Reflection has insufficient observable lifecycle data"
            );

            missingInformation.add(
                    "Execution, reasoning, planning, decision, or outcome metadata"
            );

            nextActions.add(
                    "Preserve structured lifecycle metadata before invoking reflection"
            );
        }

        /*
         * ================================================================
         * 9. LEARNING SIGNAL GENERATION
         * ================================================================
         */

        if (!risks.isEmpty()) {

            learningSignals.add(
                    "NEGATIVE_PATTERN_DETECTED"
            );
        }

        if (!improvementOpportunities.isEmpty()) {

            learningSignals.add(
                    "IMPROVEMENT_OPPORTUNITY_DETECTED"
            );
        }

        if (!missingInformation.isEmpty()) {

            learningSignals.add(
                    "INFORMATION_GAP_DETECTED"
            );
        }

        if (verificationConfidence != null
                && verificationConfidence < 0.40) {

            learningSignals.add(
                    "OUTCOME_VERIFICATION_WEAK"
            );
        }

        if (reasoningConfidence != null
                && reasoningConfidence < 0.40) {

            learningSignals.add(
                    "REASONING_CONFIDENCE_WEAK"
            );
        }

        if (decisionScore != null
                && decisionScore < 0.40) {

            learningSignals.add(
                    "DECISION_CONFIDENCE_WEAK"
            );
        }

        if (planQualityScore != null
                && planQualityScore < 0.40) {

            learningSignals.add(
                    "PLANNING_QUALITY_WEAK"
            );
        }

        if (risks.isEmpty()
                && missingInformation.isEmpty()) {

            learningSignals.add(
                    "STABLE_SUCCESS_PATTERN"
            );
        }

        /*
         * ================================================================
         * 10. NEXT ACTION POLICY
         * ================================================================
         */

        if (nextActions.isEmpty()) {

            if (!missingInformation.isEmpty()) {

                nextActions.add(
                        "Collect the highest-value missing information before taking another high-impact action"
                );

            } else if (!improvementOpportunities.isEmpty()) {

                nextActions.add(
                        "Apply the highest-priority improvement opportunity on the next iteration"
                );

            } else {

                nextActions.add(
                        "Preserve the successful pattern as reusable execution evidence"
                );
            }
        }

        /*
         * ================================================================
         * 11. REFLECTION CONFIDENCE
         * ================================================================
         */

        double confidence =
                calculateConfidence(
                        availableSignals,
                        positiveSignals,
                        negativeSignals,
                        missingInformation.size()
                );

        ReflectionVerdict verdict =
                determineVerdict(
                        executionStatus,
                        outcomeVerified,
                        outcomeState,
                        risks,
                        missingInformation
                );

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "engine",
                "ReflectionIntelligenceEngine"
        );

        metadata.put(
                "version",
                "1.0"
        );

        metadata.put(
                "reflectionTarget",
                target
        );

        metadata.put(
                "reflectionVerdict",
                verdict.name()
        );

        metadata.put(
                "reflectionConfidence",
                confidence
        );

        metadata.put(
                "reflectionConfidenceBand",
                confidenceBand(confidence)
        );

        metadata.put(
                "availableSignals",
                availableSignals
        );

        metadata.put(
                "positiveSignals",
                positiveSignals
        );

        metadata.put(
                "negativeSignals",
                negativeSignals
        );

        metadata.put(
                "riskCount",
                risks.size()
        );

        metadata.put(
                "missingInformationCount",
                missingInformation.size()
        );

        metadata.put(
                "improvementOpportunityCount",
                improvementOpportunities.size()
        );

        metadata.put(
                "learningSignalCount",
                learningSignals.size()
        );

        metadata.put(
                "reflectionDeterminism",
                "DETERMINISTIC"
        );

        return new ReflectionAnalysis(
                verdict,
                confidence,
                confidenceBand(confidence),
                List.copyOf(observations),
                List.copyOf(strengths),
                List.copyOf(risks),
                List.copyOf(missingInformation),
                List.copyOf(improvementOpportunities),
                List.copyOf(nextActions),
                List.copyOf(learningSignals),
                Map.copyOf(metadata)
        );
    }

    /**
     * Determines the highest-priority reflection verdict.
     */
    private ReflectionVerdict determineVerdict(
            Object executionStatus,
            Object outcomeVerified,
            Object outcomeState,
            List<String> risks,
            List<String> missingInformation) {

        if (executionStatus != null) {

            String status =
                    String.valueOf(
                            executionStatus
                    ).toUpperCase(Locale.ROOT);

            if (status.contains("FAILED")
                    || status.contains("ERROR")) {

                return ReflectionVerdict.EXECUTION_FAILURE;
            }
        }

        if (outcomeState != null) {

            String state =
                    String.valueOf(
                            outcomeState
                    ).toUpperCase(Locale.ROOT);

            if (state.contains("PARTIAL")) {

                return ReflectionVerdict.PARTIAL_OUTCOME;
            }

            if (state.contains("FAILED")
                    || state.contains("ERROR")) {

                return ReflectionVerdict.EXECUTION_FAILURE;
            }

            if (state.contains("UNVERIFIED")
                    || state.contains("UNKNOWN")) {

                return ReflectionVerdict.EXECUTED_UNVERIFIED;
            }

            if (state.contains("VERIFIED")
                    || state.contains("SUCCESS")) {

                if (Boolean.TRUE.equals(
                        outcomeVerified
                )) {

                    return ReflectionVerdict.VERIFIED_SUCCESS;
                }
            }
        }

        if (!missingInformation.isEmpty()) {

            return ReflectionVerdict.INSUFFICIENT_EVIDENCE;
        }

        if (!risks.isEmpty()) {

            return ReflectionVerdict.NEEDS_IMPROVEMENT;
        }

        return ReflectionVerdict.STABLE;
    }

    /**
     * Calculates reflection confidence from observable signal quality.
     */
    private double calculateConfidence(
            int availableSignals,
            int positiveSignals,
            int negativeSignals,
            int missingInformationCount) {

        if (availableSignals <= 0) {
            return MIN_CONFIDENCE;
        }

        double signalRatio =
                (double) positiveSignals
                        / Math.max(
                        1,
                        positiveSignals + negativeSignals
                );

        double coverage =
                Math.min(
                        1.0,
                        availableSignals / 12.0
                );

        double missingPenalty =
                Math.min(
                        0.30,
                        missingInformationCount * 0.04
                );

        double confidence =
                (0.55 * signalRatio)
                        + (0.45 * coverage)
                        - missingPenalty;

        return clamp(
                confidence,
                MIN_CONFIDENCE,
                MAX_CONFIDENCE
        );
    }

    /**
     * Converts confidence into a stable machine-readable band.
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
     * Returns the first non-null value from the supplied maps.
     */
    private Object firstValue(
            Map<String, Object> first,
            Map<String, Object> second,
            String key) {

        Object value =
                first.get(key);

        if (value != null) {
            return value;
        }

        return second.get(key);
    }

    /**
     * Converts a value to a numeric score when possible.
     */
    private Double numberValue(
            Object value) {

        if (value instanceof Number number) {

            return number.doubleValue();
        }

        if (value instanceof String string) {

            try {

                return Double.parseDouble(
                        string.trim()
                );

            } catch (NumberFormatException ignored) {

                return null;
            }
        }

        return null;
    }

    /**
     * Determines whether an arbitrary evidence representation contains data.
     */
    private boolean hasEvidence(
            Object value) {

        if (value == null) {
            return false;
        }

        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }

        if (value instanceof Set<?> set) {
            return !set.isEmpty();
        }

        if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }

        if (value instanceof String string) {
            return !string.isBlank();
        }

        return true;
    }

    /**
     * Returns an immutable-safe map.
     */
    private Map<String, Object> safeMap(
            Map<String, Object> value) {

        return value == null
                ? Map.of()
                : Map.copyOf(value);
    }

    /**
     * Normalizes text.
     */
    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Deduplicates a mutable list while preserving order.
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
     * Formats a score for human-readable observations.
     */
    private String format(
            double value) {

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    /**
     * Reflection verdict.
     */
    public enum ReflectionVerdict {

        /** The observable lifecycle is stable and successful. */
        STABLE,

        /** Outcome has explicit verification support. */
        VERIFIED_SUCCESS,

        /** Execution completed but outcome verification is insufficient. */
        EXECUTED_UNVERIFIED,

        /** Execution achieved only part of the intended outcome. */
        PARTIAL_OUTCOME,

        /** Execution failed or produced an explicit error state. */
        EXECUTION_FAILURE,

        /** The lifecycle requires improvement despite available evidence. */
        NEEDS_IMPROVEMENT,

        /** Available evidence is insufficient for reliable reflection. */
        INSUFFICIENT_EVIDENCE
    }

    /**
     * Immutable reflection intelligence result.
     */
    public record ReflectionAnalysis(
            ReflectionVerdict verdict,
            double confidence,
            String confidenceBand,
            List<String> observations,
            List<String> strengths,
            List<String> risks,
            List<String> missingInformation,
            List<String> improvementOpportunities,
            List<String> nextActions,
            List<String> learningSignals,
            Map<String, Object> metadata) {

        public ReflectionAnalysis {

            Objects.requireNonNull(
                    verdict,
                    "verdict must not be null"
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

            observations =
                    observations == null
                            ? List.of()
                            : List.copyOf(observations);

            strengths =
                    strengths == null
                            ? List.of()
                            : List.copyOf(strengths);

            risks =
                    risks == null
                            ? List.of()
                            : List.copyOf(risks);

            missingInformation =
                    missingInformation == null
                            ? List.of()
                            : List.copyOf(missingInformation);

            improvementOpportunities =
                    improvementOpportunities == null
                            ? List.of()
                            : List.copyOf(improvementOpportunities);

            nextActions =
                    nextActions == null
                            ? List.of()
                            : List.copyOf(nextActions);

            learningSignals =
                    learningSignals == null
                            ? List.of()
                            : List.copyOf(learningSignals);

            metadata =
                    metadata == null
                            ? Map.of()
                            : Map.copyOf(metadata);
        }
    }
}