package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * <b>DefaultTradeoffAnalysisEngine</b>
 *
 * <p>The I2 implementation of {@link TradeoffAnalysisEngine}: a stateless,
 * thread-safe, fully deterministic comparator of solution alternatives. It
 * produces one immutable comparison matrix per candidate and never selects a
 * winner - selection belongs to I3 Decision Optimization.</p>
 *
 * <p><b>Locked evaluation pipeline</b> (one score per locked
 * {@link TradeoffDimension}, in locked enum order):</p>
 * <ol>
 *   <li><b>TIME</b> - deterministic path-length band: at most three steps
 *       {@code 1.00}, four steps {@code 0.80}, five steps {@code 0.60},
 *       six or more steps {@code 0.40}. More steps always score lower.</li>
 *   <li><b>COMPLEXITY</b> - verified prerequisite density, normalized to
 *       {@code [0,1]}: verified concept references per step over the
 *       candidate's verified breadth, {@code references / (steps × distinct)}.
 *       Packing prerequisites into fewer, denser steps raises complexity;
 *       extra anchor steps at the same breadth lower it.</li>
 *   <li><b>PRACTICALITY</b> - locked constants by strategy type:
 *       PRACTICAL {@code 1.00}, BALANCED {@code 0.90}, ACCELERATED
 *       {@code 0.80}, SEQUENTIAL {@code 0.70}, THEORETICAL {@code 0.60}.</li>
 *   <li><b>LEARNING_DEPTH</b> - verified breadth covered:
 *       {@code coveredVerifiedNodes / allVerifiedNodes}, clamped to
 *       {@code [0,1]}. Covering more unique verified knowledge scores higher;
 *       without verification data the score is {@code 0.00}.</li>
 *   <li><b>RISK</b> - {@code 1 - uncertainty}, clamped to {@code [0,1]}, where
 *       {@code uncertainty = 1 - overallCertainty}. A fully certain context
 *       scores {@code 1.00}; a missing uncertainty graph means no verified
 *       uncertainty, so the score is {@code 1.00}.</li>
 * </ol>
 *
 * <p><b>Canonical form:</b> analyses follow the input alternative order exactly
 * (never score order) and scores follow the locked dimension order. Every score
 * is rounded to four decimals, so identical inputs always produce structurally
 * identical sets.</p>
 *
 * <p><b>User constraints:</b> constraints never change a score - the locked
 * dimensions are constraint-independent. Explicit constraints are restated in
 * the deterministic summary text only; constraint-driven weighting happens
 * downstream in I3 Decision Optimization.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I2 Trade-off Analysis</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultTradeoffAnalysisEngine implements TradeoffAnalysisEngine {

    /** Locked time band: at most this many steps is the fastest band. */
    private static final int FAST_STEP_LIMIT = 3;

    /** Locked time score for at most {@link #FAST_STEP_LIMIT} steps. */
    private static final double TIME_FAST = 1.00;

    /** Locked time score for exactly four steps. */
    private static final double TIME_FOUR_STEPS = 0.80;

    /** Locked time score for exactly five steps. */
    private static final double TIME_FIVE_STEPS = 0.60;

    /** Locked time score for six or more steps. */
    private static final double TIME_SLOW = 0.40;

    /** Locked practicality constant for the PRACTICAL strategy. */
    private static final double PRACTICALITY_PRACTICAL = 1.00;

    /** Locked practicality constant for the BALANCED strategy. */
    private static final double PRACTICALITY_BALANCED = 0.90;

    /** Locked practicality constant for the ACCELERATED strategy. */
    private static final double PRACTICALITY_ACCELERATED = 0.80;

    /** Locked practicality constant for the SEQUENTIAL strategy. */
    private static final double PRACTICALITY_SEQUENTIAL = 0.70;

    /** Locked practicality constant for the THEORETICAL strategy. */
    private static final double PRACTICALITY_THEORETICAL = 0.60;

    /** Locked risk score when no uncertainty graph is available. */
    private static final double RISK_WITHOUT_UNCERTAINTY = 1.00;

    @Override
    public TradeoffAnalysisSet analyze(AlternativeSet alternatives,
                                       UserConstraints constraints,
                                       VerificationGraph verificationGraph,
                                       UncertaintyGraph uncertaintyGraph) {
        if (alternatives == null || alternatives.isEmpty()) {
            return TradeoffAnalysisSet.empty();
        }

        Set<String> verifiedNodes = verifiedNodeIds(verificationGraph);
        double risk = riskScore(uncertaintyGraph);

        List<TradeoffAnalysis> analyses = new ArrayList<>(alternatives.size());
        for (AlternativeCandidate candidate : alternatives.alternatives()) {
            List<TradeoffScore> scores = List.of(
                    new TradeoffScore(TradeoffDimension.TIME, timeScore(candidate.stepCount())),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY,
                            complexityScore(candidate, verifiedNodes)),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY,
                            practicalityScore(candidate.type())),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH,
                            learningDepthScore(candidate, verifiedNodes)),
                    new TradeoffScore(TradeoffDimension.RISK, risk));
            analyses.add(new TradeoffAnalysis(candidate.candidateId(), scores,
                    summary(candidate, scores, constraints)));
        }

        // Canonical form: input alternative order, never score order.
        return new TradeoffAnalysisSet(analyses);
    }

    // ------------------------------------------------------------------
    // Stage 1 - verified knowledge
    // ------------------------------------------------------------------

    /** Verified node ids in lexicographic order (empty without a graph). */
    private static Set<String> verifiedNodeIds(VerificationGraph verificationGraph) {
        Set<String> verified = new TreeSet<>();
        if (verificationGraph == null) {
            return verified;
        }
        verificationGraph.nodes().stream()
                .filter(node -> node.status() == VerificationStatus.VERIFIED)
                .forEach(node -> verified.add(node.nodeId()));
        return verified;
    }

    /** Verified nodes covered by one step title, in lexicographic order. */
    private static Set<String> coveredNodes(String title, Set<String> verifiedNodes) {
        Set<String> covered = new TreeSet<>();
        for (String nodeId : verifiedNodes) {
            if (title.equals(nodeId) || title.contains(nodeId)) {
                covered.add(nodeId);
            }
        }
        return covered;
    }

    /** Distinct verified nodes covered by every step of a candidate. */
    private static Set<String> coveredNodes(AlternativeCandidate candidate,
                                            Set<String> verifiedNodes) {
        Set<String> covered = new TreeSet<>();
        for (AlternativeStep step : candidate.steps()) {
            covered.addAll(coveredNodes(step.title(), verifiedNodes));
        }
        return covered;
    }

    // ------------------------------------------------------------------
    // Stage 2 - locked dimension scores
    // ------------------------------------------------------------------

    /** Locked time band: more steps always score lower. */
    private static double timeScore(int stepCount) {
        if (stepCount <= FAST_STEP_LIMIT) {
            return TIME_FAST;
        }
        return switch (stepCount) {
            case 4 -> TIME_FOUR_STEPS;
            case 5 -> TIME_FIVE_STEPS;
            default -> TIME_SLOW;
        };
    }

    /**
     * Locked complexity: verified prerequisite density, normalized to
     * {@code [0,1]} - verified concept references per step divided by the
     * candidate's verified breadth, {@code references / (steps × distinct)}.
     * Packing prerequisites into fewer, denser steps raises the score.
     */
    private static double complexityScore(AlternativeCandidate candidate,
                                          Set<String> verifiedNodes) {
        if (verifiedNodes.isEmpty()) {
            return 0.0;
        }
        int references = 0;
        Set<String> distinct = new TreeSet<>();
        for (AlternativeStep step : candidate.steps()) {
            Set<String> covered = coveredNodes(step.title(), verifiedNodes);
            references += covered.size();
            distinct.addAll(covered);
        }
        if (references == 0 || distinct.isEmpty()) {
            return 0.0;
        }
        double density = (double) references / ((double) candidate.stepCount() * distinct.size());
        return round4(clamp(density));
    }

    /** Locked practicality constants by strategy type. */
    private static double practicalityScore(AlternativeType type) {
        return switch (type) {
            case PRACTICAL -> PRACTICALITY_PRACTICAL;
            case BALANCED -> PRACTICALITY_BALANCED;
            case ACCELERATED -> PRACTICALITY_ACCELERATED;
            case SEQUENTIAL -> PRACTICALITY_SEQUENTIAL;
            case THEORETICAL -> PRACTICALITY_THEORETICAL;
        };
    }

    /** Locked learning depth: covered verified nodes over all verified nodes. */
    private static double learningDepthScore(AlternativeCandidate candidate,
                                             Set<String> verifiedNodes) {
        if (verifiedNodes.isEmpty()) {
            return 0.0;
        }
        int covered = coveredNodes(candidate, verifiedNodes).size();
        return round4(clamp((double) covered / verifiedNodes.size()));
    }

    /** Locked risk: {@code 1 - uncertainty} taken from the uncertainty graph. */
    private static double riskScore(UncertaintyGraph uncertaintyGraph) {
        if (uncertaintyGraph == null) {
            return RISK_WITHOUT_UNCERTAINTY;
        }
        double uncertainty = clamp(1.0 - uncertaintyGraph.overallCertainty());
        return round4(clamp(1.0 - uncertainty));
    }
// ------------------------------------------------------------------
    // Stage 3 - deterministic summary
    // ------------------------------------------------------------------

    /** Deterministic template summary - never LLM-generated. */
    private static String summary(AlternativeCandidate candidate,
                                  List<TradeoffScore> scores,
                                  UserConstraints constraints) {
        StringBuilder text = new StringBuilder();
        text.append(candidate.type()).append(" strategy with ")
                .append(candidate.stepCount())
                .append(candidate.stepCount() == 1 ? " step" : " steps");
        for (TradeoffScore score : scores) {
            text.append(", ").append(label(score.dimension())).append(' ')
                    .append(String.format(Locale.ROOT, "%.4f", score.score()));
        }
        String declared = declaredConstraints(constraints);
        if (!declared.isEmpty()) {
            text.append(" | constraints: ").append(declared);
        }
        return text.toString();
    }

    /** Explicit user constraints in locked order (empty when none are set). */
    private static String declaredConstraints(UserConstraints constraints) {
        if (constraints == null) {
            return "";
        }
        StringBuilder declared = new StringBuilder();
        appendConstraint(declared, "duration", constraints.duration());
        appendConstraint(declared, "budget", constraints.budget());
        if (constraints.experience() != null) {
            appendConstraint(declared, "experience", constraints.experience().name());
        }
        if (constraints.platform() != null) {
            appendConstraint(declared, "platform", constraints.platform().name());
        }
        appendConstraint(declared, "language", constraints.language());
        if (constraints.outputPreference() != null) {
            appendConstraint(declared, "outputPreference",
                    constraints.outputPreference().name());
        }
        return declared.toString();
    }

    /** Appends one explicit constraint when it is set. */
    private static void appendConstraint(StringBuilder declared, String name, String value) {
        if (value == null) {
            return;
        }
        if (!declared.isEmpty()) {
            declared.append(", ");
        }
        declared.append(name).append('=').append(value);
    }

    /** Human-readable dimension label used by the summary template. */
    private static String label(TradeoffDimension dimension) {
        return switch (dimension) {
            case TIME -> "time";
            case COMPLEXITY -> "complexity";
            case PRACTICALITY -> "practicality";
            case LEARNING_DEPTH -> "learningDepth";
            case RISK -> "risk";
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
