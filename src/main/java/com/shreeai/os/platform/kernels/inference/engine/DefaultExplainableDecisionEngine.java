package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ConfidenceFactor;
import com.shreeai.os.platform.kernels.inference.model.ExplainableDecision;
import com.shreeai.os.platform.kernels.inference.model.ExplanationSection;
import com.shreeai.os.platform.kernels.inference.model.ExplanationSectionType;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultExplainableDecisionEngine</b>
 *
 * <p>The I5 implementation of {@link ExplainableDecisionEngine}: a stateless,
 * thread-safe, fully deterministic assembler of the canonical
 * {@link ExplainableDecision} artifact. It never uses LLM reasoning,
 * randomness, ML models or hidden heuristics - only the locked five-section
 * explanation pipeline.</p>
 *
 * <p><b>Locked pipeline:</b></p>
 * <ol>
 *   <li><b>Goal Summary</b> - primary goal, explicit duration and sub-goals,
 *         read from the context artifacts only.</li>
 *   <li><b>Selected Strategy</b> - the selected candidate's steps in locked
 *         step order.</li>
 *   <li><b>Trade-off Summary</b> - locked dimension descriptors at locked
 *         thresholds (high &ge; 0.75, moderate &ge; 0.50, low &gt; 0.0,
 *         not assessed at 0.0).</li>
 *   <li><b>Confidence Summary</b> - level, rounded score and locked factor
 *         descriptors at the same thresholds (uncertainty inverted).</li>
 *   <li><b>Evidence Summary</b> - verified and contradiction counts with
 *         evidence coverage.</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Inference Kernel - I5 Explainable Decision</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultExplainableDecisionEngine
        implements ExplainableDecisionEngine {

    /** Locked title of the goal section. */
    private static final String GOAL_TITLE = "Goal Summary";

    /** Locked title of the strategy section. */
    private static final String STRATEGY_TITLE = "Selected Strategy";

    /** Locked title of the trade-off section. */
    private static final String TRADEOFF_TITLE = "Trade-off Summary";

    /** Locked title of the confidence section. */
    private static final String CONFIDENCE_TITLE = "Confidence Summary";

    /** Locked title of the evidence section. */
    private static final String EVIDENCE_TITLE = "Evidence Summary";

    /** Locked fallback bullet when no explicit goal was captured. */
    private static final String NO_GOAL_BULLET = "No explicit goal captured";

    /** Locked fallback bullet when no trade-off analysis exists. */
    private static final String NO_ANALYSIS_BULLET =
            "Trade-off analysis not available";

    /** Locked fallback bullet when no strategy steps exist. */
    private static final String NO_STEPS_BULLET =
            "No steps available for the selected strategy";

    /** Locked fallback bullet when no verification data exists. */
    private static final String NO_EVIDENCE_BULLET =
            "No verification data available";

    /** Locked high threshold for descriptor mapping. */
    private static final double HIGH_THRESHOLD = 0.75;

    /** Locked moderate threshold for descriptor mapping. */
    private static final double MODERATE_THRESHOLD = 0.50;

    /**
     * Creates a stateless, thread-safe explanation assembler.
     */
    public DefaultExplainableDecisionEngine() {
    }

    // ------------------------------------------------------------------
    // Stage 1 - Goal Summary
    // ------------------------------------------------------------------

    /**
     * Stage 1 - Goal Summary: primary goal and explicit constraints, read
     * from the context artifacts only.
     */
    private static ExplanationSection goalSection(GoalStructure goalStructure,
                                                  UserConstraints constraints) {
        List<String> bullets = new ArrayList<>();
        if (goalStructure != null && goalStructure.primaryGoal() != null) {
            String goal = goalStructure.primaryGoal().title();
            if (goal != null && !goal.isBlank()) {
                bullets.add(goal);
            }
            for (GoalNode subGoal : goalStructure.subGoals()) {
                if (subGoal != null && subGoal.title() != null
                        && !subGoal.title().isBlank()) {
                    bullets.add(subGoal.title());
                }
            }
        }
        if (constraints != null) {
            if (constraints.duration() != null) {
                bullets.add("Duration: " + constraints.duration());
            }
            if (constraints.platform() != null) {
                bullets.add("Domain: " + constraints.platform());
            }
        }

        if (bullets.isEmpty()) {
            bullets.add(NO_GOAL_BULLET);
        }
        return new ExplanationSection(
                ExplanationSectionType.GOAL, GOAL_TITLE, bullets);
    }

    // ------------------------------------------------------------------
    // Stage 2 - Selected Strategy
    // ------------------------------------------------------------------

    /**
     * Stage 2 - Selected Strategy: the selected candidate's steps in locked
     * step order. When no steps exist, a locked fallback bullet keeps the
     * section present and honest.
     */
    private static ExplanationSection strategySection(AlternativeSet alternativeSet,
                                                      String candidateId) {
        List<String> bullets = new ArrayList<>();
        if (alternativeSet != null) {
            for (AlternativeCandidate candidate : alternativeSet.alternatives()) {
                if (candidate.candidateId().equals(candidateId)) {
                    for (AlternativeStep step : candidate.steps()) {
                        bullets.add(step.title());
                    }
                    break;
                }
            }
        }
        if (bullets.isEmpty()) {
            bullets.add(NO_STEPS_BULLET);
        }
        return new ExplanationSection(
                ExplanationSectionType.STRATEGY, STRATEGY_TITLE, bullets);
    }

    // ------------------------------------------------------------------
    // Stage 3 - Trade-off Summary
    // ------------------------------------------------------------------

    /** One locked descriptor entry for score-to-word mapping. */
    private record Descriptor(TradeoffDimension dimension,
                              String high,
                              String moderate,
                              String low) {
    }

    /** Locked dimension order with locked descriptor wording. */
    private static final Descriptor[] TRADEOFF_DESCRIPTORS = {
            new Descriptor(TradeoffDimension.PRACTICALITY,
                    "High practical learning", "Moderate practical value",
                    "Limited practical focus"),
            new Descriptor(TradeoffDimension.TIME,
                    "Fast completion", "Moderate duration",
                    "Slow completion"),
            new Descriptor(TradeoffDimension.COMPLEXITY,
                    "High complexity", "Moderate complexity",
                    "Low complexity"),
            new Descriptor(TradeoffDimension.LEARNING_DEPTH,
                    "Deep learning path", "Standard learning depth",
                    "Shallow learning path"),
            new Descriptor(TradeoffDimension.RISK,
                    "High risk", "Moderate risk",
                    "Low risk"),
    };

    /**
     * Stage 3 - Trade-off Summary: locked dimension descriptors for the
     * selected candidate's analysis, in locked dimension order. Unassessed
     * dimensions are omitted deterministically.
     */
    private static ExplanationSection tradeoffSection(TradeoffAnalysisSet analysisSet,
                                                      String candidateId) {
        List<String> bullets = new ArrayList<>();
        TradeoffAnalysis selected = null;
        if (analysisSet != null) {
            for (TradeoffAnalysis analysis : analysisSet.analyses()) {
                if (analysis.candidateId().equals(candidateId)) {
                    selected = analysis;
                    break;
                }
            }
        }
        if (selected == null) {
            bullets.add(NO_ANALYSIS_BULLET);
        } else {
            for (Descriptor descriptor : TRADEOFF_DESCRIPTORS) {
                String bullet = describeScore(
                        scoreOf(selected, descriptor.dimension()),
                        descriptor.high(), descriptor.moderate(), descriptor.low());
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
        return new ExplanationSection(
                ExplanationSectionType.TRADEOFF, TRADEOFF_TITLE, bullets);
    }

    /**
     * Returns the score of the given dimension within an analysis, or
     * {@code NaN} when the dimension is absent.
     */
    private static double scoreOf(TradeoffAnalysis analysis,
                                  TradeoffDimension dimension) {
        for (com.shreeai.os.platform.kernels.inference.model.TradeoffScore score
                : analysis.scores()) {
            if (score.dimension() == dimension) {
                return score.score();
            }
        }
        return Double.NaN;
    }

    /**
     * Maps a score to its locked descriptor: high &ge; 0.75, moderate
     * &ge; 0.50, low &gt; 0.0. Returns {@code null} when the score is absent
     * or zero (dimension not assessed) so the bullet is omitted.
     */
    private static String describeScore(double score,
                                        String high,
                                        String moderate,
                                        String low) {
        if (Double.isNaN(score) || score <= 0.0) {
            return null;
        }
        if (score >= HIGH_THRESHOLD) {
            return high;
        }
        if (score >= MODERATE_THRESHOLD) {
            return moderate;
        }
        return low;
    }

    // ------------------------------------------------------------------
    // Stage 4 - Confidence Summary
    // ------------------------------------------------------------------

    /**
     * Stage 4 - Confidence Summary: locked level, rounded score and locked
     * factor descriptors. The uncertainty factor is inverted (a higher
     * factor value means lower uncertainty) so the wording stays truthful.
     */
    private static ExplanationSection confidenceSection(CalibratedDecision calibrated) {
        List<String> bullets = new ArrayList<>();
        bullets.add("Confidence: " + calibrated.level().name());
        bullets.add("Score: " + String.format(java.util.Locale.ROOT, "%.4f",
                calibrated.confidence()));

        String[] wording = {
                "Strong verified evidence", "Moderate evidence support",
                "Weak evidence support",
                "Low uncertainty", "Moderate uncertainty", "High uncertainty",
                "Clear winning alternative", "Competitive alternatives",
                "No clear winner among alternatives",
                "Complete verification coverage", "Partial verification coverage",
                "Limited verification coverage",
        };
        List<ConfidenceFactor> factors = calibrated.factors();
        for (int i = 0; i < factors.size() && i < wording.length / 3; i++) {
            // The uncertainty factor (index 1) already measures the absence
            // of uncertainty, so a high value truthfully reads as
            // "Low uncertainty" on the same locked scale.
            String bullet = factorDescriptor(factors.get(i).contribution(),
                    wording[i * 3], wording[i * 3 + 1], wording[i * 3 + 2]);
            if (bullet != null) {
                bullets.add(bullet);
            }
        }
        return new ExplanationSection(
                ExplanationSectionType.CONFIDENCE, CONFIDENCE_TITLE, bullets);
    }

    /**
     * Maps a factor value to its locked descriptor: strong &ge; 0.75,
     * moderate &ge; 0.50, weak &gt; 0.0. Returns {@code null} when the value
     * is absent ({@code NaN}) or zero so the bullet is omitted.
     */
    private static String factorDescriptor(double value,
                                           String strong,
                                           String moderate,
                                           String weak) {
        if (Double.isNaN(value) || value <= 0.0) {
            return null;
        }
        if (value >= HIGH_THRESHOLD) {
            return strong;
        }
        if (value >= MODERATE_THRESHOLD) {
            return moderate;
        }
        return weak;
    }

    // ------------------------------------------------------------------
    // Stage 5 - Evidence Summary
    // ------------------------------------------------------------------

    /**
     * Stage 5 - Evidence Summary: verified and contradiction counts with
     * evidence coverage percentage, in locked order.
     */
    private static ExplanationSection evidenceSection(VerificationGraph verificationGraph) {
        List<String> bullets = new ArrayList<>();
        if (verificationGraph == null || verificationGraph.nodes().isEmpty()) {
            bullets.add(NO_EVIDENCE_BULLET);
        } else {
            int verified = 0;
            int contradicted = 0;
            for (VerificationNode node : verificationGraph.nodes()) {
                if (node.status() == VerificationStatus.VERIFIED) {
                    verified++;
                } else if (node.status() == VerificationStatus.CONTRADICTED) {
                    contradicted++;
                }
            }
            bullets.add(verified + " verified concepts");
            bullets.add(contradicted + " contradictions");
            bullets.add(String.format(java.util.Locale.ROOT, "%.0f%% evidence coverage",
                    verificationGraph.verificationScore() * 100.0));
        }
        return new ExplanationSection(
                ExplanationSectionType.EVIDENCE, EVIDENCE_TITLE, bullets);
    }

    // ------------------------------------------------------------------
    // Main entry point
    // ------------------------------------------------------------------

    @Override
    public ExplainableDecision explain(
            OptimizedDecision decision,
            CalibratedDecision calibratedDecision,
            AlternativeSet alternativeSet,
            TradeoffAnalysisSet analysisSet,
            GoalStructure goalStructure,
            UserConstraints constraints,
            VerificationGraph verificationGraph) {

        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(calibratedDecision, "calibratedDecision must not be null");

        String candidateId = decision.candidateId();
        List<ExplanationSection> sections = List.of(
                goalSection(goalStructure, constraints),
                strategySection(alternativeSet, candidateId),
                tradeoffSection(analysisSet, candidateId),
                confidenceSection(calibratedDecision),
                evidenceSection(verificationGraph));

        return new ExplainableDecision(
                candidateId,
                decision.strategy(),
                decision.optimizationScore(),
                calibratedDecision.confidence(),
                calibratedDecision.level(),
                sections);
    }
}