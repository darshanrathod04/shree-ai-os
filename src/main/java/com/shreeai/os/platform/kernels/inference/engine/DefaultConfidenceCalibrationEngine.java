package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ConfidenceFactor;
import com.shreeai.os.platform.kernels.inference.model.ConfidenceLevel;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultConfidenceCalibrationEngine</b>
 *
 * <p>The I4 implementation of {@link ConfidenceCalibrationEngine}: a stateless,
 * thread-safe, fully deterministic calibrator of decision confidence. It
 * never uses LLM reasoning, randomness, ML models or hidden heuristics -
 * only the locked weighted formula.</p>
 *
 * <p><b>Locked calibration pipeline:</b></p>
 * <ol>
 *   <li><b>Verified Evidence (0.35)</b> - {@code verifiedConcepts / totalConcepts}
 *       from the {@link VerificationGraph}.</li>
 *   <li><b>Uncertainty Penalty (0.30)</b> - {@code 1 - averageUncertainty}
 *       from the {@link UncertaintyGraph} nodes.</li>
 *   <li><b>Tradeoff Margin (0.20)</b> - absolute gap between the best and
 *       second-best optimization scores.</li>
 *   <li><b>Coverage (0.15)</b> - {@link VerificationGraph#verificationScore()}
 *       representing verification completeness.</li>
 *   <li><b>Final (5)</b> - weighted sum clamped to {@code [0,1]} and rounded
 *       to 4 decimals.</li>
 *   <li><b>Level (6)</b> - {@code >=0.80} HIGH, {@code 0.60-0.79} MEDIUM,
 *       {@code <0.60} LOW.</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Inference Kernel - I4 Confidence Calibration</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultConfidenceCalibrationEngine
        implements ConfidenceCalibrationEngine {

    /** Locked weight of the Verified Evidence factor. */
    private static final double EVIDENCE_WEIGHT = 0.35;

    /** Locked weight of the Uncertainty factor. */
    private static final double UNCERTAINTY_WEIGHT = 0.30;

    /** Locked weight of the Tradeoff Margin factor. */
    private static final double MARGIN_WEIGHT = 0.20;

    /** Locked weight of the Coverage factor. */
    private static final double COVERAGE_WEIGHT = 0.15;

    // --- Locked I3 weight profile (reproduced for margin computation) ----

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

    // --- Locked numeric helpers ------------------------------------------

    /** Clamps a value to {@code [0.0, 1.0]}. */
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /** Rounds a value to four decimals. */
    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /**
     * Returns the locked weight profile, normalized to sum to {@code 1.0}.
     * Explicit constraints override exactly one dimension each; nothing is
     * inferred and the language constraint carries no weight. This mirrors
     * the I3 engine exactly so that margin scores are consistent.
     */
    private static Map<TradeoffDimension, Double> weightProfile(UserConstraints constraints) {
        Map<TradeoffDimension, Double> weights = new EnumMap<>(TradeoffDimension.class);
        weights.put(TradeoffDimension.PRACTICALITY, DEFAULT_PRACTICALITY_WEIGHT);
        weights.put(TradeoffDimension.RISK, DEFAULT_RISK_WEIGHT);
        weights.put(TradeoffDimension.TIME, DEFAULT_TIME_WEIGHT);
        weights.put(TradeoffDimension.COMPLEXITY, DEFAULT_COMPLEXITY_WEIGHT);
        weights.put(TradeoffDimension.LEARNING_DEPTH, DEFAULT_LEARNING_DEPTH_WEIGHT);

        if (constraints != null) {
            if (constraints.duration() != null) {
                weights.put(TradeoffDimension.TIME, DURATION_TIME_WEIGHT);
            }
            if (constraints.budget() != null) {
                weights.put(TradeoffDimension.RISK, BUDGET_RISK_WEIGHT);
            }
            if (constraints.experience() != null) {
                weights.put(TradeoffDimension.LEARNING_DEPTH, EXPERIENCE_LEARNING_DEPTH_WEIGHT);
            }
            if (constraints.platform() != null) {
                weights.put(TradeoffDimension.COMPLEXITY, PLATFORM_COMPLEXITY_WEIGHT);
            }
            if (constraints.outputPreference() != null) {
                weights.put(TradeoffDimension.PRACTICALITY, OUTPUT_PRACTICALITY_WEIGHT);
            }
        }

        double total = 0.0;
        for (double weight : weights.values()) {
            total += weight;
        }

        Map<TradeoffDimension, Double> normalized = new EnumMap<>(TradeoffDimension.class);
        for (Map.Entry<TradeoffDimension, Double> entry : weights.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / total);
        }
        return normalized;
    }

    /**
     * Locked score: {@code Σ(score_i × weight_i)}, clamped and rounded.
     * Mirrors the I3 engine formula exactly.
     */
    private static double optimizationScore(TradeoffAnalysis analysis,
                                            Map<TradeoffDimension, Double> weights) {
        double total = 0.0;
        for (TradeoffScore score : analysis.scores()) {
            Double weight = weights.get(score.dimension());
            total += score.score() * (weight != null ? weight : 0.0);
        }
        return round4(clamp(total));
    }

        /**
     * Computes the optimization score for one candidate by looking up its
     * trade-off analysis.
     *
     * @return the score, or {@code NaN} when the candidate has no analysis
     */
    private static double scoreCandidate(AlternativeCandidate candidate,
                                         Map<String, TradeoffAnalysis> analysisById,
                                         Map<TradeoffDimension, Double> weights) {
        TradeoffAnalysis analysis = analysisById.get(candidate.candidateId());
        if (analysis == null) {
            return Double.NaN;
        }
        return optimizationScore(analysis, weights);
    }

    // ------------------------------------------------------------------
    // Calibration stages
    // ------------------------------------------------------------------

    /**
     * Stage 1 - Verified Evidence: fraction of verified concepts.
     * {@code verifiedConcepts / totalConcepts}. When the graph is absent or
     * empty, evidence is vacuously maximal (1.0).
     */
    private static double evidenceFactor(VerificationGraph verificationGraph) {
        if (verificationGraph == null || verificationGraph.nodes().isEmpty()) {
            return 1.0;
        }
        int verified = 0;
        for (VerificationNode node : verificationGraph.nodes()) {
            if (node.status() == VerificationStatus.VERIFIED) {
                verified++;
            }
        }
        return (double) verified / verificationGraph.nodes().size();
    }

    /**
     * Stage 2 - Uncertainty Penalty: {@code 1 - averageUncertainty}.
     * When the graph is absent or has no nodes, uncertainty is zero (1.0).
     */
    private static double uncertaintyFactor(UncertaintyGraph uncertaintyGraph) {
        if (uncertaintyGraph == null || uncertaintyGraph.nodes().isEmpty()) {
            return 1.0;
        }
        double sumCertainty = 0.0;
        for (UncertaintyNode node : uncertaintyGraph.nodes()) {
            sumCertainty += node.certaintyScore();
        }
        double averageCertainty = sumCertainty / uncertaintyGraph.nodes().size();
        double averageUncertainty = 1.0 - averageCertainty;
        return clamp(1.0 - averageUncertainty);
    }

    /**
     * Stage 3 - Tradeoff Margin: absolute gap between the best and
     * second-best optimization scores. When only one candidate is available,
     * the margin is maximal (1.0) because there is no competition.
     */
    private static double marginFactor(OptimizedDecision decision,
                                       TradeoffAnalysisSet analysisSet,
                                       AlternativeSet alternativeSet,
                                       UserConstraints constraints) {
        if (analysisSet == null || analysisSet.isEmpty()) {
            return 0.0;
        }
        if (alternativeSet == null || alternativeSet.alternatives().size() <= 1) {
            return 1.0;
        }

        Map<String, TradeoffAnalysis> analysisById = new LinkedHashMap<>();
        for (TradeoffAnalysis analysis : analysisSet.analyses()) {
            analysisById.put(analysis.candidateId(), analysis);
        }

        Map<TradeoffDimension, Double> weights = weightProfile(constraints);

        List<Double> scores = new ArrayList<>();
        for (AlternativeCandidate candidate : alternativeSet.alternatives()) {
            double s = scoreCandidate(candidate, analysisById, weights);
            if (!Double.isNaN(s)) {
                scores.add(s);
            }
        }

        if (scores.size() < 2) {
            return 1.0;
        }

        scores.sort(Double::compare);

        double secondBest = scores.get(scores.size() - 2);
        double margin = decision.optimizationScore() - secondBest;
        return clamp(Math.abs(margin));
    }

    /**
     * Stage 4 - Coverage: verification completeness from the
     * {@link VerificationGraph}. When the graph is absent, coverage is
     * vacuously maximal (1.0).
     */
    private static double coverageFactor(VerificationGraph verificationGraph) {
        if (verificationGraph == null) {
            return 1.0;
        }
        return clamp(verificationGraph.verificationScore());
    }

    // ------------------------------------------------------------------
    // Main entry point
    // ------------------------------------------------------------------

    /**
     * Creates a stateless, thread-safe calibrator.
     */
    public DefaultConfidenceCalibrationEngine() {
    }

    @Override
    public CalibratedDecision calibrate(
            OptimizedDecision decision,
            TradeoffAnalysisSet analysisSet,
            AlternativeSet alternativeSet,
            UserConstraints constraints,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph) {

        Objects.requireNonNull(decision, "decision must not be null");

        // Stage 1 - Verified Evidence
        double evidence = evidenceFactor(verificationGraph);

        // Stage 2 - Uncertainty Penalty
        double uncertainty = uncertaintyFactor(uncertaintyGraph);

        // Stage 3 - Tradeoff Margin
        double margin = marginFactor(decision, analysisSet, alternativeSet, constraints);

        // Stage 4 - Coverage
        double coverage = coverageFactor(verificationGraph);

        // Stage 5 - Final Confidence (locked weighted formula)
        double rawConfidence = evidence * EVIDENCE_WEIGHT
                + uncertainty * UNCERTAINTY_WEIGHT
                + margin * MARGIN_WEIGHT
                + coverage * COVERAGE_WEIGHT;
        double confidence = round4(clamp(rawConfidence));

        // Stage 6 - Confidence Level
        ConfidenceLevel level = ConfidenceLevel.of(confidence);

        // Build ordered explainable factors
        List<ConfidenceFactor> factors = new ArrayList<>(4);
        factors.add(new ConfidenceFactor("Verified Evidence", round4(evidence)));
        factors.add(new ConfidenceFactor("Uncertainty", round4(uncertainty)));
        factors.add(new ConfidenceFactor("Tradeoff Margin", round4(margin)));
        factors.add(new ConfidenceFactor("Coverage", round4(coverage)));

        return new CalibratedDecision(
                decision.candidateId(),
                confidence,
                level,
                factors);
    }
}