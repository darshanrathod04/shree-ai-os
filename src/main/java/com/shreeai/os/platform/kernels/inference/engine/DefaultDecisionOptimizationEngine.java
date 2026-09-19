package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.DecisionDimension;
import com.shreeai.os.platform.kernels.inference.model.DecisionJustification;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <b>DefaultDecisionOptimizationEngine</b>
 *
 * <p>The I3 implementation of {@link DecisionOptimizationEngine}: a stateless,
 * thread-safe, fully deterministic selector of the single optimized decision.
 * It never uses LLM reasoning, randomness, ML models or hidden heuristics -
 * only the locked weighted formula and the locked tie resolution.</p>
 *
 * <p><b>Locked optimization pipeline:</b></p>
 * <ol>
 *   <li><b>Read Cognitive Artifacts</b> - consume the I1
 *       {@link AlternativeSet}, the I2 {@link TradeoffAnalysisSet} and the
 *       explicit {@link UserConstraints}; the raw prompt is never inspected.
 *       Dimension scores are taken verbatim from the I2 comparison matrix,
 *       which already encodes the verified knowledge
 *       ({@link VerificationGraph}) and the verified uncertainty
 *       ({@link UncertaintyGraph}) into LEARNING_DEPTH and RISK - the graphs
 *       are accepted for contract completeness and are never re-weighed.</li>
 *   <li><b>Determine Weight Profile</b> - the locked default profile is
 *       PRACTICALITY {@code 0.30}, RISK {@code 0.20}, TIME {@code 0.20},
 *       COMPLEXITY {@code 0.15}, LEARNING_DEPTH {@code 0.15}. An explicit
 *       constraint overrides exactly one dimension: duration → TIME
 *       {@code 0.30}, budget → RISK {@code 0.25}, experience → LEARNING_DEPTH
 *       {@code 0.20}, platform → COMPLEXITY {@code 0.20}, output preference →
 *       PRACTICALITY {@code 0.35}. The profile is then normalized so the
 *       weights always sum to {@code 1.0}. Nothing is inferred - only
 *       explicitly extracted constraints are honoured.</li>
 *   <li><b>Compute Optimization Score</b> - locked formula
 *       {@code Σ(score_i × weight_i)} clamped to {@code [0.0, 1.0]} and
 *       rounded to four decimals.</li>
 *   <li><b>Tie Resolution</b> - deterministic only: higher optimization score,
 *       then higher PRACTICALITY score, then higher LEARNING_DEPTH score, then
 *       fewer steps, then ascending candidate id.</li>
 *   <li><b>Generate Justifications</b> - one justification per dimension, in
 *       locked dimension order, with deterministic threshold templates
 *       ({@code >= 0.80} strength, {@code >= 0.50} adequate, otherwise
 *       limited) and a normalized contribution of that dimension to the final
 *       score.</li>
 * </ol>
 *
 * <p><b>Empty input:</b> an {@link OptimizedDecision} always names a selected
 * candidate and always carries at least one justification, so an empty decision
 * cannot be represented. When no candidate with a trade-off analysis is
 * available the engine reports the contract violation with an
 * {@link IllegalArgumentException}; callers that expect a possibly empty
 * decision space skip the engine (the runtime pipeline does exactly that).</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultDecisionOptimizationEngine implements DecisionOptimizationEngine {

    /** Locked default weight of the PRACTICALITY dimension. */
    private static final double DEFAULT_PRACTICALITY_WEIGHT = 0.30;

    /** Locked default weight of the RISK dimension. */
    private static final double DEFAULT_RISK_WEIGHT = 0.20;

    /** Locked default weight of the TIME dimension. */
    private static final double DEFAULT_TIME_WEIGHT = 0.20;

    /** Locked default weight of the COMPLEXITY dimension. */
    private static final double DEFAULT_COMPLEXITY_WEIGHT = 0.15;

    /** Locked default weight of the LEARNING_DEPTH dimension. */
    private static final double DEFAULT_LEARNING_DEPTH_WEIGHT = 0.15;

    /** Locked TIME override applied for an explicit duration constraint. */
    private static final double DURATION_TIME_WEIGHT = 0.30;

    /** Locked RISK override applied for an explicit budget constraint. */
    private static final double BUDGET_RISK_WEIGHT = 0.25;

    /** Locked LEARNING_DEPTH override applied for an explicit experience constraint. */
    private static final double EXPERIENCE_LEARNING_DEPTH_WEIGHT = 0.20;

    /** Locked COMPLEXITY override applied for an explicit platform constraint. */
    private static final double PLATFORM_COMPLEXITY_WEIGHT = 0.20;

    /** Locked PRACTICALITY override applied for an explicit output preference. */
    private static final double OUTPUT_PRACTICALITY_WEIGHT = 0.35;

    /** Locked lower bound of the strength justification band. */
    private static final double STRENGTH_THRESHOLD = 0.80;

    /** Locked lower bound of the adequate justification band. */
    private static final double ADEQUATE_THRESHOLD = 0.50;

    /** Locked strength justification template. */
    private static final String STRENGTH_TEMPLATE =
            "%s is a strength for this decision (score %.4f, weight %.4f).";

    /** Locked adequate justification template. */
    private static final String ADEQUATE_TEMPLATE =
            "%s is adequate for this decision (score %.4f, weight %.4f).";

    /** Locked limited justification template. */
    private static final String LIMITED_TEMPLATE =
            "%s is limited for this decision (score %.4f, weight %.4f).";

    @Override
    public OptimizedDecision decide(AlternativeSet alternativeSet,
                                    TradeoffAnalysisSet analysisSet,
                                    UserConstraints constraints,
                                    VerificationGraph verificationGraph,
                                    UncertaintyGraph uncertaintyGraph) {
        if (alternativeSet == null || alternativeSet.isEmpty()
                || analysisSet == null || analysisSet.isEmpty()) {
            throw new IllegalArgumentException(
                    "at least one candidate with a trade-off analysis is required");
        }

        Map<String, TradeoffAnalysis> analysisById = new LinkedHashMap<>();
        for (TradeoffAnalysis analysis : analysisSet.analyses()) {
            analysisById.put(analysis.candidateId(), analysis);
        }

        Map<DecisionDimension, Double> weights = weightProfile(constraints);

        ScoredCandidate best = null;
        for (AlternativeCandidate candidate : alternativeSet.alternatives()) {
            TradeoffAnalysis analysis = analysisById.get(candidate.candidateId());
            if (analysis == null) {
                continue;
            }
            ScoredCandidate scored = new ScoredCandidate(candidate, analysis,
                    optimizationScore(analysis, weights));
            if (best == null || winsOver(scored, best)) {
                best = scored;
            }
        }

        if (best == null) {
            throw new IllegalArgumentException(
                    "no alternative matches a trade-off analysis by candidate id");
        }

        return new OptimizedDecision(best.candidate().candidateId(),
                best.candidate().type(), best.score(),
                justifications(best.analysis(), weights, best.score()));
    }

    /** One candidate with its I2 analysis and its locked optimization score. */
    private record ScoredCandidate(AlternativeCandidate candidate,
                                   TradeoffAnalysis analysis,
                                   double score) {
    }

    // ------------------------------------------------------------------
    // Stage 2 - locked weight profile
    // ------------------------------------------------------------------

    /**
     * Returns the locked weight profile, normalized to sum to {@code 1.0}.
     * Explicit constraints override exactly one dimension each; nothing is
     * inferred and the language constraint carries no weight. Weights are kept
     * at full precision - only the final optimization score is rounded.
     */
    private static Map<DecisionDimension, Double> weightProfile(UserConstraints constraints) {
        Map<DecisionDimension, Double> weights = new EnumMap<>(DecisionDimension.class);
        weights.put(DecisionDimension.PRACTICALITY, DEFAULT_PRACTICALITY_WEIGHT);
        weights.put(DecisionDimension.RISK, DEFAULT_RISK_WEIGHT);
        weights.put(DecisionDimension.TIME, DEFAULT_TIME_WEIGHT);
        weights.put(DecisionDimension.COMPLEXITY, DEFAULT_COMPLEXITY_WEIGHT);
        weights.put(DecisionDimension.LEARNING_DEPTH, DEFAULT_LEARNING_DEPTH_WEIGHT);

        if (constraints != null) {
            if (constraints.duration() != null) {
                weights.put(DecisionDimension.TIME, DURATION_TIME_WEIGHT);
            }
            if (constraints.budget() != null) {
                weights.put(DecisionDimension.RISK, BUDGET_RISK_WEIGHT);
            }
            if (constraints.experience() != null) {
                weights.put(DecisionDimension.LEARNING_DEPTH, EXPERIENCE_LEARNING_DEPTH_WEIGHT);
            }
            if (constraints.platform() != null) {
                weights.put(DecisionDimension.COMPLEXITY, PLATFORM_COMPLEXITY_WEIGHT);
            }
            if (constraints.outputPreference() != null) {
                weights.put(DecisionDimension.PRACTICALITY, OUTPUT_PRACTICALITY_WEIGHT);
            }
        }

        double total = 0.0;
        for (double weight : weights.values()) {
            total += weight;
        }

        Map<DecisionDimension, Double> normalized = new EnumMap<>(DecisionDimension.class);
        for (Map.Entry<DecisionDimension, Double> entry : weights.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / total);
        }
        return normalized;
    }

    // ------------------------------------------------------------------
    // Stage 3 - locked optimization score
    // ------------------------------------------------------------------

    /** Locked score: {@code Σ(score_i × weight_i)}, clamped and rounded. */
    private static double optimizationScore(TradeoffAnalysis analysis,
                                            Map<DecisionDimension, Double> weights) {
        double total = 0.0;
        for (TradeoffScore score : analysis.scores()) {
            DecisionDimension dimension = dimensionOf(score.dimension());
            total += score.score() * weightOf(weights, dimension);
        }
        return round4(clamp(total));
    }

    /** Bridges the locked I2 dimension to its locked I3 counterpart by name. */
    private static DecisionDimension dimensionOf(TradeoffDimension dimension) {
        return DecisionDimension.valueOf(dimension.name());
    }

    /** Weight of one dimension (0.0 when the profile does not carry it). */
    private static double weightOf(Map<DecisionDimension, Double> weights,
                                   DecisionDimension dimension) {
        Double weight = weights.get(dimension);
        return weight == null ? 0.0 : weight;
    }

    /** Score of one dimension in the analysis (0.0 when the dimension is absent). */
    private static double scoreOf(TradeoffAnalysis analysis, DecisionDimension dimension) {
        for (TradeoffScore score : analysis.scores()) {
            if (dimensionOf(score.dimension()) == dimension) {
                return score.score();
            }
        }
        return 0.0;
    }

    // ------------------------------------------------------------------
    // Stage 4 - locked deterministic tie resolution
    // ------------------------------------------------------------------

    /**
     * Locked tie resolution: higher score, then higher PRACTICALITY, then
     * higher LEARNING_DEPTH, then fewer steps, then ascending candidate id.
     * Never random and never insertion-order dependent.
     */
    private static boolean winsOver(ScoredCandidate challenger, ScoredCandidate incumbent) {
        if (challenger.score() != incumbent.score()) {
            return challenger.score() > incumbent.score();
        }
        double challengerPracticality =
                scoreOf(challenger.analysis(), DecisionDimension.PRACTICALITY);
        double incumbentPracticality =
                scoreOf(incumbent.analysis(), DecisionDimension.PRACTICALITY);
        if (challengerPracticality != incumbentPracticality) {
            return challengerPracticality > incumbentPracticality;
        }
        double challengerDepth =
                scoreOf(challenger.analysis(), DecisionDimension.LEARNING_DEPTH);
        double incumbentDepth =
                scoreOf(incumbent.analysis(), DecisionDimension.LEARNING_DEPTH);
        if (challengerDepth != incumbentDepth) {
            return challengerDepth > incumbentDepth;
        }
        int challengerSteps = challenger.candidate().stepCount();
        int incumbentSteps = incumbent.candidate().stepCount();
        if (challengerSteps != incumbentSteps) {
            return challengerSteps < incumbentSteps;
        }
        return challenger.candidate().candidateId()
                .compareTo(incumbent.candidate().candidateId()) < 0;
    }

    // ------------------------------------------------------------------
    // Stage 5 - deterministic justifications
    // ------------------------------------------------------------------

    /** One deterministic justification per dimension carried by the analysis. */
    private static List<DecisionJustification> justifications(
            TradeoffAnalysis analysis,
            Map<DecisionDimension, Double> weights,
            double optimizationScore) {
        List<DecisionJustification> justifications = new ArrayList<>();
        for (DecisionDimension dimension : DecisionDimension.values()) {
            Double score = scoreOrNull(analysis, dimension);
            if (score == null) {
                continue;
            }
            double weight = weightOf(weights, dimension);
            double contribution = optimizationScore == 0.0
                    ? 0.0
                    : round4(clamp((score * weight) / optimizationScore));
            justifications.add(new DecisionJustification(
                    reason(dimension, score, weight), dimension, contribution));
        }
        if (justifications.isEmpty()) {
            throw new IllegalArgumentException(
                    "the trade-off analysis carries no known dimension scores");
        }
        return justifications;
    }

    /** Score of one dimension, or null when the dimension is absent. */
    private static Double scoreOrNull(TradeoffAnalysis analysis, DecisionDimension dimension) {
        for (TradeoffScore score : analysis.scores()) {
            if (dimensionOf(score.dimension()) == dimension) {
                return score.score();
            }
        }
        return null;
    }

    /** Deterministic template reason based on the locked score thresholds. */
    private static String reason(DecisionDimension dimension, double score, double weight) {
        String template = score >= STRENGTH_THRESHOLD
                ? STRENGTH_TEMPLATE
                : score >= ADEQUATE_THRESHOLD ? ADEQUATE_TEMPLATE : LIMITED_TEMPLATE;
        return String.format(Locale.ROOT, template, label(dimension), score, weight);
    }

    /** Human-readable dimension label used by the reason templates. */
    private static String label(DecisionDimension dimension) {
        return switch (dimension) {
            case TIME -> "Time";
            case COMPLEXITY -> "Complexity";
            case PRACTICALITY -> "Practicality";
            case LEARNING_DEPTH -> "Learning depth";
            case RISK -> "Risk";
        };
    }

    // ------------------------------------------------------------------
    // Locked numeric helpers
    // ------------------------------------------------------------------

    /** Clamps a value to {@code [0.0, 1.0]}. */
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /** Rounds a value to four decimals. */
    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
