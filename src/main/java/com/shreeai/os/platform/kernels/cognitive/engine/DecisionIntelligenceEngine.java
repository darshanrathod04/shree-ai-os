package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.cognitive.model.DecisionContext;
import com.shreeai.os.platform.kernels.cognitive.model.EvaluationCriteria;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DecisionIntelligenceEngine
 *
 * <p>
 * Deterministic decision-intelligence engine for the Cognitive Kernel.
 * </p>
 *
 * <p>
 * This engine evaluates decision readiness, alternative evidence,
 * constraints, criterion configuration, scoring information and
 * recommendation robustness.
 * </p>
 *
 * <p>
 * Important principle:
 * the engine never invents an alternative score. When sufficient
 * discriminating information is unavailable, it explicitly reports
 * that a reliable ranking cannot be produced.
 * </p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Decision Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Legacy dependency:</b> None.</p>
 */
public final class DecisionIntelligenceEngine {

    private static final double HIGH_CONFIDENCE = 0.80;
    private static final double MODERATE_CONFIDENCE = 0.60;

    /**
     * Performs advanced deterministic decision analysis.
     *
     * @param context validated decision context
     * @param criteria validated evaluation criteria
     * @return immutable decision analysis result
     */
    public DecisionAnalysis analyze(
            DecisionContext context,
            EvaluationCriteria criteria) {

        Objects.requireNonNull(
                context,
                "DecisionContext must not be null"
        );

        Objects.requireNonNull(
                criteria,
                "EvaluationCriteria must not be null"
        );

        List<String> alternatives =
                normalizeAlternatives(
                        context.availableAlternatives()
                );

        Map<String, Object> contextMetadata =
                context.metadata();

        Map<String, Object> criterionMetadata =
                criteria.metadata();

        List<String> assumptions =
                normalizeKeys(
                        context.assumptions()
                );

        List<String> constraints =
                normalizeKeys(
                        context.constraints()
                );

        List<AlternativeEvaluation> evaluations =
                evaluateAlternatives(
                        alternatives,
                        contextMetadata,
                        criterionMetadata,
                        criteria
                );

        evaluations =
                evaluations.stream()
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                AlternativeEvaluation::score
                                        )
                                        .reversed()
                                        .thenComparing(
                                                AlternativeEvaluation::alternative
                                        )
                        )
                        .toList();

        DecisionStatus status =
                determineStatus(
                        alternatives,
                        evaluations,
                        criteria
                );

        double confidence =
                calculateConfidence(
                        alternatives,
                        evaluations,
                        criteria,
                        contextMetadata,
                        criterionMetadata
                );

        List<String> risks =
                identifyRisks(
                        alternatives,
                        evaluations,
                        criteria,
                        constraints,
                        assumptions,
                        contextMetadata,
                        criterionMetadata
                );

        List<String> tradeOffs =
                identifyTradeOffs(
                        evaluations,
                        criteria
                );

        List<String> missingInformation =
                identifyMissingInformation(
                        alternatives,
                        evaluations,
                        criteria,
                        contextMetadata,
                        criterionMetadata
                );

        String recommendation =
                buildRecommendation(
                        status,
                        evaluations,
                        criteria
                );

        String rationale =
                buildRationale(
                        status,
                        evaluations,
                        criteria,
                        confidence
                );

        Map<String, Object> metadata =
                buildMetadata(
                        context,
                        criteria,
                        evaluations,
                        confidence,
                        risks,
                        tradeOffs,
                        missingInformation
                );

        return new DecisionAnalysis(
                status,
                recommendation,
                rationale,
                confidence,
                evaluations,
                risks,
                tradeOffs,
                missingInformation,
                metadata
        );
    }

    /**
     * Reads explicit alternative scores from the existing generic metadata
     * extension point.
     *
     * <p>
     * Supported representation:
     *
     * <pre>
     * alternativeScores = {
     *     "Option A": 0.82,
     *     "Option B": 0.67,
     *     "Option C": 0.74
     * }
     * </pre>
     *
     * <p>
     * Values must be numeric and are expected to be in the range 0.0-1.0.
     * Invalid values are ignored instead of being fabricated.
     * </p>
     */
    private List<AlternativeEvaluation> evaluateAlternatives(
            List<String> alternatives,
            Map<String, Object> contextMetadata,
            Map<String, Object> criterionMetadata,
            EvaluationCriteria criteria) {

        Map<String, Double> scores =
                extractAlternativeScores(
                        contextMetadata
                );

        Map<String, Map<String, Object>> criterionScores =
                extractCriterionScores(
                        contextMetadata
                );

        List<AlternativeEvaluation> result =
                new ArrayList<>();

        for (String alternative : alternatives) {

            Double explicitScore =
                    scores.get(
                            alternative
                    );

            double criterionScore =
                    criterionScores
                            .getOrDefault(
                                    alternative,
                                    Map.of()
                            )
                            .values()
                            .stream()
                            .filter(
                                    value ->
                                            value instanceof Number
                            )
                            .mapToDouble(
                                    value ->
                                            normalizeScore(
                                                    ((Number) value)
                                                            .doubleValue()
                                            )
                            )
                            .average()
                            .orElse(
                                    Double.NaN
                            );

            double score;

            boolean scored;

            if (explicitScore != null) {

                score =
                        normalizeScore(
                                explicitScore
                        );

                scored = true;

            } else if (!Double.isNaN(
                    criterionScore
            )) {

                score =
                        criterionScore;

                scored = true;

            } else {

                score = 0.0;

                scored = false;
            }

            List<String> evidence =
                    new ArrayList<>();

            if (explicitScore != null) {

                evidence.add(
                        "Explicit alternative score supplied"
                );
            }

            if (!Double.isNaN(
                    criterionScore
            )) {

                evidence.add(
                        "Criterion-level score evidence supplied"
                );
            }

            if (criteria.weight() != null) {

                evidence.add(
                        "Evaluation criterion weight="
                                + criteria.weight()
                );
            }

            result.add(
                    new AlternativeEvaluation(
                            alternative,
                            score,
                            scored,
                            evidence
                    )
            );
        }

        return result;
    }

    /**
     * Extracts explicit alternative scores from decision metadata.
     */
    private Map<String, Double> extractAlternativeScores(
            Map<String, Object> metadata) {

        Object raw =
                metadata.get(
                        "alternativeScores"
                );

        if (!(raw instanceof Map<?, ?> rawMap)) {

            return Map.of();
        }

        Map<String, Double> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry :
                rawMap.entrySet()) {

            if (entry.getKey() == null
                    || !(entry.getValue()
                    instanceof Number number)) {

                continue;
            }

            String alternative =
                    String.valueOf(
                            entry.getKey()
                    ).trim();

            if (alternative.isBlank()) {
                continue;
            }

            result.put(
                    alternative,
                    normalizeScore(
                            number.doubleValue()
                    )
            );
        }

        return Map.copyOf(
                result
        );
    }

    /**
     * Extracts optional criterion-level scores.
     *
     * <p>
     * Expected representation:
     *
     * <pre>
     * criterionScores = {
     *     "Option A": {
     *         "performance": 0.90,
     *         "cost": 0.70
     *     }
     * }
     * </pre>
     */
    private Map<String, Map<String, Object>>
    extractCriterionScores(
            Map<String, Object> metadata) {

        Object raw =
                metadata.get(
                        "criterionScores"
                );

        if (!(raw instanceof Map<?, ?> rawMap)) {

            return Map.of();
        }

        Map<String, Map<String, Object>> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry :
                rawMap.entrySet()) {

            if (entry.getKey() == null
                    || !(entry.getValue()
                    instanceof Map<?, ?> nested)) {

                continue;
            }

            String alternative =
                    String.valueOf(
                            entry.getKey()
                    ).trim();

            if (alternative.isBlank()) {
                continue;
            }

            Map<String, Object> values =
                    new LinkedHashMap<>();

            for (Map.Entry<?, ?> nestedEntry :
                    nested.entrySet()) {

                if (nestedEntry.getKey() == null) {
                    continue;
                }

                values.put(
                        String.valueOf(
                                nestedEntry.getKey()
                        ),
                        nestedEntry.getValue()
                );
            }

            result.put(
                    alternative,
                    Map.copyOf(
                            values
                    )
            );
        }

        return Map.copyOf(
                result
        );
    }

    private DecisionStatus determineStatus(
            List<String> alternatives,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria) {

        if (alternatives.isEmpty()) {

            return DecisionStatus.NO_ALTERNATIVES;
        }

        if (criteria.criterionName() == null
                || criteria.criterionName().isBlank()) {

            return DecisionStatus.INSUFFICIENT_CRITERIA;
        }

        long scored =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .count();

        if (scored == 0) {

            return DecisionStatus
                    .INSUFFICIENT_DISCRIMINATING_EVIDENCE;
        }

        if (scored < alternatives.size()) {

            return DecisionStatus
                    .PARTIALLY_EVALUATED;
        }

        return DecisionStatus.EVALUATED;
    }

    private double calculateConfidence(
            List<String> alternatives,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria,
            Map<String, Object> contextMetadata,
            Map<String, Object> criterionMetadata) {

        if (alternatives.isEmpty()) {
            return 0.05;
        }

        long scoredCount =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .count();

        if (scoredCount == 0) {
            return 0.10;
        }

        double coverage =
                (double) scoredCount
                        / alternatives.size();

        double scoreSeparation =
                calculateScoreSeparation(
                        evaluations
                );

        double criterionQuality =
                calculateCriterionQuality(
                        criteria
                );

        double metadataQuality =
                calculateMetadataQuality(
                        contextMetadata,
                        criterionMetadata
                );

        double confidence =
                coverage * 0.40
                        + scoreSeparation * 0.25
                        + criterionQuality * 0.20
                        + metadataQuality * 0.15;

        return clamp(
                confidence,
                0.05,
                0.95
        );
    }

    private double calculateScoreSeparation(
            List<AlternativeEvaluation> evaluations) {

        List<Double> scores =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .map(
                                AlternativeEvaluation::score
                        )
                        .sorted(
                                Comparator.reverseOrder()
                        )
                        .toList();

        if (scores.size() < 2) {
            return 0.25;
        }

        double best =
                scores.get(0);

        double second =
                scores.get(1);

        double gap =
                Math.abs(
                        best - second
                );

        /*
         * A very small gap means the recommendation is sensitive
         * to small changes in the underlying evidence.
         */
        return clamp(
                gap * 4.0,
                0.0,
                1.0
        );
    }

    private double calculateCriterionQuality(
            EvaluationCriteria criteria) {

        double weight =
                criteria.weight() == null
                        ? 0.0
                        : normalizeScore(
                        criteria.weight()
                );

        String priority =
                criteria.priority();

        double priorityScore =
                priority == null
                        || priority.isBlank()
                        ? 0.25
                        : 0.75;

        return clamp(
                weight * 0.65
                        + priorityScore * 0.35,
                0.0,
                1.0
        );
    }

    private double calculateMetadataQuality(
            Map<String, Object> contextMetadata,
            Map<String, Object> criterionMetadata) {

        double score = 0.0;

        if (contextMetadata.containsKey(
                "alternativeScores"
        )) {

            score += 0.50;
        }

        if (contextMetadata.containsKey(
                "criterionScores"
        )) {

            score += 0.30;
        }

        if (!criterionMetadata.isEmpty()) {

            score += 0.20;
        }

        return clamp(
                score,
                0.0,
                1.0
        );
    }

    private List<String> identifyRisks(
            List<String> alternatives,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria,
            List<String> constraints,
            List<String> assumptions,
            Map<String, Object> contextMetadata,
            Map<String, Object> criterionMetadata) {

        Set<String> risks =
                new LinkedHashSet<>();

        long scored =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .count();

        if (scored < alternatives.size()) {

            risks.add(
                    "Not every alternative has explicit evaluation evidence"
            );
        }

        if (evaluations.size() >= 2) {

            List<AlternativeEvaluation> ranked =
                    evaluations.stream()
                            .filter(
                                    AlternativeEvaluation::scored
                            )
                            .sorted(
                                    Comparator
                                            .comparingDouble(
                                                    AlternativeEvaluation::score
                                            )
                                            .reversed()
                            )
                            .toList();

            if (ranked.size() >= 2) {

                double gap =
                        ranked.get(0).score()
                                - ranked.get(1).score();

                if (gap < 0.05) {

                    risks.add(
                            "Top alternatives are very close in score; recommendation is sensitive to small evidence changes"
                    );
                }
            }
        }

        if (criteria.weight() != null
                && criteria.weight() <= 0.0) {

            risks.add(
                    "Evaluation criterion has zero weight"
            );
        }

        if (constraints.isEmpty()) {

            risks.add(
                    "No explicit decision constraints were supplied"
            );
        }

        if (assumptions.isEmpty()) {

            risks.add(
                    "No explicit decision assumptions were supplied"
            );
        }

        if (!contextMetadata.containsKey(
                "alternativeScores"
        )
                && !contextMetadata.containsKey(
                "criterionScores"
        )) {

            risks.add(
                    "No discriminating alternative score evidence was supplied"
            );
        }

        return List.copyOf(
                risks
        );
    }

    private List<String> identifyTradeOffs(
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria) {

        List<String> tradeOffs =
                new ArrayList<>();

        List<AlternativeEvaluation> scored =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                AlternativeEvaluation::score
                                        )
                                        .reversed()
                        )
                        .toList();

        if (scored.size() >= 2) {

            AlternativeEvaluation best =
                    scored.get(0);

            AlternativeEvaluation second =
                    scored.get(1);

            double gap =
                    best.score()
                            - second.score();

            if (gap < 0.10) {

                tradeOffs.add(
                        best.alternative()
                                + " has only a small score advantage over "
                                + second.alternative()
                                + " under criterion "
                                + criteria.criterionName()
                );
            }

            if (gap >= 0.10) {

                tradeOffs.add(
                        best.alternative()
                                + " has a stronger evaluated position under "
                                + criteria.criterionName()
                                + ", but the recommendation remains dependent "
                                + "on the quality of the supplied scores"
                );
            }
        }

        return List.copyOf(
                tradeOffs
        );
    }

    private List<String> identifyMissingInformation(
            List<String> alternatives,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria,
            Map<String, Object> contextMetadata,
            Map<String, Object> criterionMetadata) {

        List<String> missing =
                new ArrayList<>();

        if (alternatives.isEmpty()) {

            missing.add(
                    "At least two decision alternatives"
            );
        }

        if (criteria.criterionName() == null
                || criteria.criterionName().isBlank()) {

            missing.add(
                    "A meaningful evaluation criterion"
            );
        }

        long scored =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .count();

        if (scored == 0) {

            missing.add(
                    "Discriminating evidence or scores for the alternatives"
            );
        } else if (scored < alternatives.size()) {

            missing.add(
                    "Evaluation evidence for every alternative"
            );
        }

        if (!contextMetadata.containsKey(
                "alternativeScores"
        )
                && !contextMetadata.containsKey(
                "criterionScores"
        )) {

            missing.add(
                    "Alternative-specific evaluation data"
            );
        }

        if (criterionMetadata.isEmpty()) {

            missing.add(
                    "Additional criterion metadata for deeper evaluation"
            );
        }

        return List.copyOf(
                new LinkedHashSet<>(
                        missing
                )
        );
    }

    private String buildRecommendation(
            DecisionStatus status,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria) {

        if (status != DecisionStatus.EVALUATED
                && status != DecisionStatus.PARTIALLY_EVALUATED) {

            return "No reliable recommendation can be produced yet.";
        }

        AlternativeEvaluation best =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .max(
                                Comparator.comparingDouble(
                                        AlternativeEvaluation::score
                                )
                        )
                        .orElse(null);

        if (best == null) {

            return "No reliable recommendation can be produced yet.";
        }

        if (status ==
                DecisionStatus.PARTIALLY_EVALUATED) {

            return "Provisional recommendation: "
                    + best.alternative()
                    + ". Additional evaluation evidence is required.";
        }

        return "Recommended alternative: "
                + best.alternative()
                + " based on the supplied "
                + criteria.criterionName()
                + " evaluation.";
    }

    private String buildRationale(
            DecisionStatus status,
            List<AlternativeEvaluation> evaluations,
            EvaluationCriteria criteria,
            double confidence) {

        if (status ==
                DecisionStatus.INSUFFICIENT_DISCRIMINATING_EVIDENCE) {

            return "The available decision context does not contain enough "
                    + "alternative-specific evidence to distinguish the options. "
                    + "The engine deliberately avoids fabricating a winner.";
        }

        AlternativeEvaluation best =
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .max(
                                Comparator.comparingDouble(
                                        AlternativeEvaluation::score
                                )
                        )
                        .orElse(null);

        if (best == null) {

            return "Decision analysis could not establish a supported alternative.";
        }

        String confidenceBand =
                confidence >= HIGH_CONFIDENCE
                        ? "high"
                        : confidence >= MODERATE_CONFIDENCE
                        ? "moderate"
                        : "limited";

        return "The leading alternative is "
                + best.alternative()
                + " with a normalized score of "
                + format(best.score())
                + " under criterion '"
                + criteria.criterionName()
                + "'. "
                + "Decision confidence is "
                + confidenceBand
                + " ("
                + format(confidence)
                + ").";
    }

    private Map<String, Object> buildMetadata(
            DecisionContext context,
            EvaluationCriteria criteria,
            List<AlternativeEvaluation> evaluations,
            double confidence,
            List<String> risks,
            List<String> tradeOffs,
            List<String> missingInformation) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "decisionIntelligenceVersion",
                "1.0"
        );

        metadata.put(
                "contextId",
                context.id().value()
        );

        metadata.put(
                "criterionId",
                criteria.id().value()
        );

        metadata.put(
                "criterion",
                criteria.criterionName()
        );

        metadata.put(
                "criterionWeight",
                criteria.weight()
        );

        metadata.put(
                "priority",
                criteria.priority()
        );

        metadata.put(
                "alternativeCount",
                context.availableAlternatives().size()
        );

        metadata.put(
                "evaluatedAlternativeCount",
                evaluations.stream()
                        .filter(
                                AlternativeEvaluation::scored
                        )
                        .count()
        );

        metadata.put(
                "confidence",
                confidence
        );

        metadata.put(
                "confidenceBand",
                confidence >= HIGH_CONFIDENCE
                        ? "HIGH"
                        : confidence >= MODERATE_CONFIDENCE
                        ? "MODERATE"
                        : "LIMITED"
        );

        metadata.put(
                "riskCount",
                risks.size()
        );

        metadata.put(
                "tradeOffCount",
                tradeOffs.size()
        );

        metadata.put(
                "missingInformationCount",
                missingInformation.size()
        );

        metadata.put(
                "alternativeEvaluations",
                evaluations
        );

        return Map.copyOf(
                metadata
        );
    }

    private List<String> normalizeAlternatives(
            List<String> alternatives) {

        if (alternatives == null) {
            return List.of();
        }

        return alternatives.stream()
                .filter(
                        Objects::nonNull
                )
                .map(
                        String::trim
                )
                .filter(
                        value ->
                                !value.isBlank()
                )
                .distinct()
                .toList();
    }

    private List<String> normalizeKeys(
            Map<String, Object> values) {

        if (values == null
                || values.isEmpty()) {

            return List.of();
        }

        return values.keySet()
                .stream()
                .filter(
                        Objects::nonNull
                )
                .map(
                        String::trim
                )
                .filter(
                        value ->
                                !value.isBlank()
                )
                .distinct()
                .toList();
    }

    private double normalizeScore(
            double score) {

        if (Double.isNaN(score)
                || Double.isInfinite(score)) {

            return 0.0;
        }

        if (score >= 0.0
                && score <= 1.0) {

            return score;
        }

        /*
         * Accept 0-100 style scores as a convenience extension.
         */
        if (score > 1.0
                && score <= 100.0) {

            return score / 100.0;
        }

        return clamp(
                score,
                0.0,
                1.0
        );
    }

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

    private String format(
            double value) {

        return String.format(
                java.util.Locale.ROOT,
                "%.3f",
                value
        );
    }

    /**
     * Result of decision intelligence analysis.
     */
    public record DecisionAnalysis(
            DecisionStatus status,
            String recommendation,
            String rationale,
            double confidence,
            List<AlternativeEvaluation> alternativeEvaluations,
            List<String> risks,
            List<String> tradeOffs,
            List<String> missingInformation,
            Map<String, Object> metadata) {

        public DecisionAnalysis {

            Objects.requireNonNull(
                    status,
                    "DecisionAnalysis status must not be null"
            );

            Objects.requireNonNull(
                    recommendation,
                    "DecisionAnalysis recommendation must not be null"
            );

            Objects.requireNonNull(
                    rationale,
                    "DecisionAnalysis rationale must not be null"
            );

            Objects.requireNonNull(
                    alternativeEvaluations,
                    "DecisionAnalysis alternativeEvaluations must not be null"
            );

            Objects.requireNonNull(
                    risks,
                    "DecisionAnalysis risks must not be null"
            );

            Objects.requireNonNull(
                    tradeOffs,
                    "DecisionAnalysis tradeOffs must not be null"
            );

            Objects.requireNonNull(
                    missingInformation,
                    "DecisionAnalysis missingInformation must not be null"
            );

            Objects.requireNonNull(
                    metadata,
                    "DecisionAnalysis metadata must not be null"
            );

            if (confidence < 0.0
                    || confidence > 1.0) {

                throw new IllegalArgumentException(
                        "DecisionAnalysis confidence must be between 0.0 and 1.0"
                );
            }

            alternativeEvaluations =
                    List.copyOf(
                            alternativeEvaluations
                    );

            risks =
                    List.copyOf(
                            risks
                    );

            tradeOffs =
                    List.copyOf(
                            tradeOffs
                    );

            missingInformation =
                    List.copyOf(
                            missingInformation
                    );

            metadata =
                    Map.copyOf(
                            metadata
                    );
        }
    }

    /**
     * Immutable evaluation of one alternative.
     */
    public record AlternativeEvaluation(
            String alternative,
            double score,
            boolean scored,
            List<String> evidence) {

        public AlternativeEvaluation {

            Objects.requireNonNull(
                    alternative,
                    "Alternative name must not be null"
            );

            Objects.requireNonNull(
                    evidence,
                    "Alternative evidence must not be null"
            );

            if (alternative.isBlank()) {

                throw new IllegalArgumentException(
                        "Alternative name must not be blank"
                );
            }

            if (score < 0.0
                    || score > 1.0) {

                throw new IllegalArgumentException(
                        "Alternative score must be between 0.0 and 1.0"
                );
            }

            evidence =
                    List.copyOf(
                            evidence
                    );
        }
    }

    /**
     * Decision analysis status.
     */
    public enum DecisionStatus {

        NO_ALTERNATIVES,

        INSUFFICIENT_CRITERIA,

        INSUFFICIENT_DISCRIMINATING_EVIDENCE,

        PARTIALLY_EVALUATED,

        EVALUATED
    }
}