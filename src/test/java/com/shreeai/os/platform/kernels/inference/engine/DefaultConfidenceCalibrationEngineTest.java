package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.OutputPreference;
import com.shreeai.os.platform.kernels.context.model.PlatformType;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ConfidenceFactor;
import com.shreeai.os.platform.kernels.inference.model.ConfidenceLevel;
import com.shreeai.os.platform.kernels.inference.model.DecisionDimension;
import com.shreeai.os.platform.kernels.inference.model.DecisionJustification;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyLevel;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic tests for the I4 {@link DefaultConfidenceCalibrationEngine}.
 *
 * <p>Every test is fixed-input and side-effect free: identical inputs always
 * produce the same {@link CalibratedDecision}, the same confidence score and
 * the same ordered factors. The locked formula under test is
 * {@code 0.35*evidence + 0.30*(1-avgUncertainty) + 0.20*margin + 0.15*coverage},
 * clamped to {@code [0,1]} and rounded to 4 decimals.</p>
 */
public class DefaultConfidenceCalibrationEngineTest {

    private DefaultConfidenceCalibrationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultConfidenceCalibrationEngine();
    }

    // ------------------------------------------------------------------
    // Fixture helpers
    // ------------------------------------------------------------------

    private static OptimizedDecision decision(String candidateId, double score) {
        return new OptimizedDecision(candidateId, AlternativeType.BALANCED, score,
                List.of(new DecisionJustification(
                        "highest weighted score", DecisionDimension.TIME, score)));
    }

    private static AlternativeCandidate candidate(String candidateId, int stepCount) {
        List<AlternativeStep> steps = new ArrayList<>();
        for (int order = 1; order <= stepCount; order++) {
            steps.add(new AlternativeStep(order, "Step " + order));
        }
        return new AlternativeCandidate(candidateId, AlternativeType.BALANCED,
                "Balanced strategy (" + stepCount + " steps)", steps, 1.0);
    }

    private static AlternativeSet setOf(AlternativeCandidate... candidates) {
        return new AlternativeSet(List.of(candidates));
    }

    private static TradeoffAnalysisSet analysesOf(TradeoffAnalysis... analyses) {
        return new TradeoffAnalysisSet(List.of(analyses));
    }

    /**
     * One full analysis with the given scores in locked I2 dimension order:
     * TIME, COMPLEXITY, PRACTICALITY, LEARNING_DEPTH, RISK.
     */
    private static TradeoffAnalysis analysis(String candidateId,
                                             double time,
                                             double complexity,
                                             double practicality,
                                             double learningDepth,
                                             double risk) {
        return new TradeoffAnalysis(candidateId, List.of(
                new TradeoffScore(TradeoffDimension.TIME, time),
                new TradeoffScore(TradeoffDimension.COMPLEXITY, complexity),
                new TradeoffScore(TradeoffDimension.PRACTICALITY, practicality),
                new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, learningDepth),
                new TradeoffScore(TradeoffDimension.RISK, risk)),
                "analysis of " + candidateId);
    }

    /** An analysis where every locked dimension scores the same value. */
    private static TradeoffAnalysis flatAnalysis(String candidateId, double value) {
        return analysis(candidateId, value, value, value, value, value);
    }

    private static UserConstraints constraintsOf(String duration,
                                                 String budget,
                                                 ExperienceLevel experience,
                                                 PlatformType platform,
                                                 String language,
                                                 OutputPreference outputPreference) {
        return UserConstraints.of(duration, budget, experience, platform, language,
                outputPreference, List.of());
    }

    private static int nodeIdCounter = 0;

    private static VerificationNode node(VerificationStatus status) {
        nodeIdCounter++;
        return new VerificationNode("v-" + nodeIdCounter, status, List.of("e-1"));
    }

    private static VerificationGraph verificationGraph(double score,
                                                       VerificationNode... nodes) {
        return new VerificationGraph(List.of(nodes), List.of(), score);
    }

    private static UncertaintyNode uncertaintyNode(String id, double certainty) {
        UncertaintyLevel level;
        if (certainty >= 0.90) {
            level = UncertaintyLevel.CERTAIN;
        } else if (certainty >= 0.75) {
            level = UncertaintyLevel.LIKELY;
        } else if (certainty >= 0.50) {
            level = UncertaintyLevel.UNCERTAIN;
        } else {
            level = UncertaintyLevel.UNKNOWN;
        }
        return new UncertaintyNode(id, "uncertainty of " + id, level, certainty);
    }

    private static UncertaintyGraph uncertaintyGraph(double overallCertainty,
                                                     UncertaintyNode... nodes) {
        return new UncertaintyGraph(List.of(nodes), List.of(), overallCertainty);
    }

    // ------------------------------------------------------------------
    // Confidence level boundaries
    // ------------------------------------------------------------------

    @Nested
    class ConfidenceLevelBoundaries {

        @Test
        void highStartsAt080() {
            assertEquals(ConfidenceLevel.HIGH, ConfidenceLevel.of(0.80));
        }

        @Test
        void mediumJustBelowHighBoundary() {
            assertEquals(ConfidenceLevel.MEDIUM, ConfidenceLevel.of(0.7999));
        }

        @Test
        void mediumStartsAt060() {
            assertEquals(ConfidenceLevel.MEDIUM, ConfidenceLevel.of(0.60));
        }

        @Test
        void lowJustBelowMediumBoundary() {
            assertEquals(ConfidenceLevel.LOW, ConfidenceLevel.of(0.5999));
        }

        @Test
        void extremesMapCorrectly() {
            assertEquals(ConfidenceLevel.HIGH, ConfidenceLevel.of(1.0));
            assertEquals(ConfidenceLevel.LOW, ConfidenceLevel.of(0.0));
        }

        @Test
        void rangeDescriptionsAreLocked() {
            assertEquals(">=0.80", ConfidenceLevel.HIGH.range());
            assertEquals("0.60-0.79", ConfidenceLevel.MEDIUM.range());
            assertEquals("<0.60", ConfidenceLevel.LOW.range());
        }
    }

    // ------------------------------------------------------------------
    // Null and empty graph defaults (vacuously maximal)
    // ------------------------------------------------------------------

    @Nested
    class AbsentInputDefaults {

        @Test
        void allGraphsAbsentAreVacuouslyMaximal() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertEquals(1.0, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
            for (ConfidenceFactor factor : result.factors()) {
                assertEquals(1.0, factor.contribution());
            }
        }

        @Test
        void nullAnalysisSetYieldsZeroMargin() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0), null,
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            // 0.35 + 0.30 + 0.20*0.0 + 0.15 = 0.80
            assertEquals(0.80, result.confidence());
            assertEquals(0.0, result.factors().get(2).contribution());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }

        @Test
        void emptyAnalysisSetYieldsZeroMargin() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0), TradeoffAnalysisSet.empty(),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertEquals(0.80, result.confidence());
            assertEquals(0.0, result.factors().get(2).contribution());
        }

        @Test
        void nullAlternativeSetYieldsMaximalMargin() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    null, null, null, null);
            assertEquals(1.0, result.confidence());
            assertEquals(1.0, result.factors().get(2).contribution());
        }

        @Test
        void singleCandidateYieldsMaximalMargin() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0), flatAnalysis("c-2", 0.5)),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertEquals(1.0, result.confidence());
            assertEquals(1.0, result.factors().get(2).contribution());
        }

        @Test
        void candidatesWithoutAnalysisAreSkipped() {
            // c-2 has no analysis - only one score remains, so the
            // margin is vacuously maximal.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            assertEquals(1.0, result.confidence());
            assertEquals(1.0, result.factors().get(2).contribution());
        }

        @Test
        void emptyVerificationGraphIsVacuouslyVerified() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(1.0),
                    null);
            assertEquals(1.0, result.confidence());
            assertEquals(1.0, result.factors().get(0).contribution());
            assertEquals(1.0, result.factors().get(3).contribution());
        }

        @Test
        void emptyUncertaintyGraphIsVacuouslyCertain() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null,
                    uncertaintyGraph(0.0));
            assertEquals(1.0, result.confidence());
            assertEquals(1.0, result.factors().get(1).contribution());
        }

        @Test
        void nullDecisionIsRejected() {
            assertThrows(NullPointerException.class, () -> engine.calibrate(
                    null, null, null, null, null, null));
        }
    }

    // ------------------------------------------------------------------
    // Individual calibration factors
    // ------------------------------------------------------------------

    @Nested
    class Factors {

        @Test
        void factorsAreInLockedOrder() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertEquals(4, result.factors().size());
            assertEquals("Verified Evidence", result.factors().get(0).name());
            assertEquals("Uncertainty", result.factors().get(1).name());
            assertEquals("Tradeoff Margin", result.factors().get(2).name());
            assertEquals("Coverage", result.factors().get(3).name());
        }

        @Test
        void evidenceIsVerifiedShareOfNodes() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(1.0,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.UNSUPPORTED)),
                    null);
            // evidence 0.5: 0.35*0.5 + 0.30 + 0.20 + 0.15 = 0.825
            assertEquals(0.5, result.factors().get(0).contribution());
            assertEquals(0.825, result.confidence());
        }

        @Test
        void onlyFullyVerifiedNodesCount() {
            // PARTIALLY_VERIFIED is not fully verified, so it does not
            // contribute to the evidence share.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(1.0,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.PARTIALLY_VERIFIED)),
                    null);
            assertEquals(0.5, result.factors().get(0).contribution());
            assertEquals(0.825, result.confidence());
        }

        @Test
        void uncertaintyIsAverageCertaintyComplement() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null,
                    uncertaintyGraph(0.8,
                            uncertaintyNode("u-1", 0.9),
                            uncertaintyNode("u-2", 0.7)));
            // avg certainty 0.8 -> uncertainty factor 0.8:
            // 0.35 + 0.30*0.8 + 0.20 + 0.15 = 0.94
            assertEquals(0.8, result.factors().get(1).contribution());
            assertEquals(0.94, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }

        @Test
        void overallCertaintyFieldIsIgnored() {
            // The engine reads node certainty scores only - the graph level
            // overallCertainty summary carries no weight.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null,
                    uncertaintyGraph(0.0,
                            uncertaintyNode("u-1", 1.0)));
            assertEquals(1.0, result.factors().get(1).contribution());
            assertEquals(1.0, result.confidence());
        }

        @Test
        void coverageIsVerificationScore() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(0.8, node(VerificationStatus.VERIFIED)),
                    null);
            // coverage 0.8: 0.35 + 0.30 + 0.20 + 0.15*0.8 = 0.97
            assertEquals(0.8, result.factors().get(3).contribution());
            assertEquals(0.97, result.confidence());
        }

        @Test
        void factorsAreRoundedToFourDecimals() {
            // 1 of 3 verified nodes -> evidence 0.333333... -> 0.3333
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(1.0,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.UNSUPPORTED),
                            node(VerificationStatus.UNSUPPORTED)),
                    null);
            assertEquals(0.3333, result.factors().get(0).contribution());
            // 0.35*(1/3) + 0.65 = 0.766666... -> 0.7667 (MEDIUM)
            assertEquals(0.7667, result.confidence());
            assertEquals(ConfidenceLevel.MEDIUM, result.level());
        }
    }

    // ------------------------------------------------------------------
    // Trade-off margin computation (I3 weight profile reproduction)
    // ------------------------------------------------------------------

    @Nested
    class Margin {

        private static TradeoffAnalysis dimensionOnly(String candidateId,
                                                      TradeoffDimension dimension) {
            return new TradeoffAnalysis(candidateId, List.of(
                    new TradeoffScore(TradeoffDimension.TIME,
                            dimension == TradeoffDimension.TIME ? 1.0 : 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY,
                            dimension == TradeoffDimension.COMPLEXITY ? 1.0 : 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY,
                            dimension == TradeoffDimension.PRACTICALITY ? 1.0 : 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH,
                            dimension == TradeoffDimension.LEARNING_DEPTH ? 1.0 : 0.0),
                    new TradeoffScore(TradeoffDimension.RISK,
                            dimension == TradeoffDimension.RISK ? 1.0 : 0.0)),
                    "only " + dimension + " scores 1.0 for " + candidateId);
        }

        @Test
        void marginIsBestMinusSecondBest() {
            // c-1 scores 1.0 (all dimensions 1.0), c-2 scores 0.5.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0), flatAnalysis("c-2", 0.5)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            // margin 0.5: 0.35 + 0.30 + 0.20*0.5 + 0.15 = 0.90
            assertEquals(0.5, result.factors().get(2).contribution());
            assertEquals(0.90, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }

        @Test
        void marginUsesAbsoluteGap() {
            // Decision score below the field: |0.2 - 0.4| = 0.2.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.2),
                    analysesOf(flatAnalysis("c-1", 0.4), flatAnalysis("c-2", 0.7)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            // margin 0.2: 0.35 + 0.30 + 0.20*0.2 + 0.15 = 0.84
            assertEquals(0.2, result.factors().get(2).contribution());
            assertEquals(0.84, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }

        @Test
        void marginUsesDecisionScoreNotRecomputation() {
            // The decision carries score 0.9 while its analysis recomputes
            // to 1.0 - the engine calibrates the decision as given.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.9),
                    analysesOf(flatAnalysis("c-1", 1.0), flatAnalysis("c-2", 0.5)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            // margin |0.9 - 0.5| = 0.4: 0.65 + 0.08 + 0.15 = 0.88
            assertEquals(0.4, result.factors().get(2).contribution());
            assertEquals(0.88, result.confidence());
        }

        @Test
        void marginFollowsDefaultWeights() {
            // Default TIME weight 0.20 -> c-1 scores 0.20, c-2 scores 0.0.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.2),
                    analysesOf(dimensionOnly("c-1", TradeoffDimension.TIME),
                            flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            assertEquals(0.2, result.factors().get(2).contribution());
            assertEquals(0.84, result.confidence());
        }

        @Test
        void tiedScoresYieldZeroMargin() {
            // Both candidates score 1.0 - no gap, margin 0.0.
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0), flatAnalysis("c-2", 1.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null, null, null);
            assertEquals(0.0, result.factors().get(2).contribution());
            // 0.35 + 0.30 + 0.0 + 0.15 = 0.80 - exactly the HIGH boundary.
            assertEquals(0.80, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }
    }

    // ------------------------------------------------------------------
    // Constraint-driven weight overrides reproduce the I3 profile
    // ------------------------------------------------------------------

    @Nested
    class ConstraintWeightOverrides {

        @Test
        void durationConstraintRaisesTimeWeight() {
            // TIME override 0.30, profile total 1.10 -> 0.30/1.10 = 0.2727.
            UserConstraints constraints = constraintsOf(
                    "30 days", null, null, null, null, null);
            TradeoffAnalysis timeOnly = new TradeoffAnalysis("c-1", List.of(
                    new TradeoffScore(TradeoffDimension.TIME, 1.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY, 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY, 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 0.0),
                    new TradeoffScore(TradeoffDimension.RISK, 0.0)),
                    "time only");
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.2727),
                    analysesOf(timeOnly, flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    constraints, null, null);
            assertEquals(0.2727, result.factors().get(2).contribution());
            // 0.35 + 0.30 + 0.20*0.2727 + 0.15 = 0.85454 -> 0.8545
            assertEquals(0.8545, result.confidence());
        }

        @Test
        void budgetConstraintRaisesRiskWeight() {
            // RISK override 0.25, profile total 1.05 -> 0.25/1.05 = 0.2381.
            UserConstraints constraints = constraintsOf(
                    null, "5000", null, null, null, null);
            TradeoffAnalysis riskOnly = new TradeoffAnalysis("c-1", List.of(
                    new TradeoffScore(TradeoffDimension.TIME, 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY, 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY, 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 0.0),
                    new TradeoffScore(TradeoffDimension.RISK, 1.0)),
                    "risk only");
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.2381),
                    analysesOf(riskOnly, flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    constraints, null, null);
            assertEquals(0.2381, result.factors().get(2).contribution());
            // 0.65 + 0.20*0.2381 + 0.15 = 0.847619 -> 0.8476
            assertEquals(0.8476, result.confidence());
        }

        @Test
        void experienceConstraintRaisesLearningDepthWeight() {
            // LEARNING_DEPTH override 0.20, total 1.05 -> 0.20/1.05 = 0.1905.
            UserConstraints constraints = constraintsOf(
                    null, null, ExperienceLevel.BEGINNER, null, null, null);
            TradeoffAnalysis learningOnly = new TradeoffAnalysis("c-1", List.of(
                    new TradeoffScore(TradeoffDimension.TIME, 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY, 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY, 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 1.0),
                    new TradeoffScore(TradeoffDimension.RISK, 0.0)),
                    "learning only");
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.1905),
                    analysesOf(learningOnly, flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    constraints, null, null);
            assertEquals(0.1905, result.factors().get(2).contribution());
            // 0.65 + 0.20*0.1905 + 0.15 = 0.838095... -> 0.8381
            assertEquals(0.8381, result.confidence());
        }
        @Test
        void platformConstraintRaisesComplexityWeight() {
            // COMPLEXITY override 0.20, total 1.05 -> 0.20/1.05 = 0.1905.
            UserConstraints constraints = constraintsOf(
                    null, null, null, PlatformType.WINDOWS, null, null);
            TradeoffAnalysis complexityOnly = new TradeoffAnalysis("c-1", List.of(
                    new TradeoffScore(TradeoffDimension.TIME, 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY, 1.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY, 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 0.0),
                    new TradeoffScore(TradeoffDimension.RISK, 0.0)),
                    "complexity only");
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.1905),
                    analysesOf(complexityOnly, flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    constraints, null, null);
            assertEquals(0.1905, result.factors().get(2).contribution());
            // 0.65 + 0.20*0.1905 + 0.15 = 0.8381
            assertEquals(0.8381, result.confidence());
        }

        @Test
        void outputPreferenceConstraintRaisesPracticalityWeight() {
            // PRACTICALITY override 0.35, total 1.05 -> 0.35/1.05 = 0.3333.
            UserConstraints constraints = constraintsOf(
                    null, null, null, null, null, OutputPreference.ROADMAP);
            TradeoffAnalysis practicalityOnly = new TradeoffAnalysis("c-1", List.of(
                    new TradeoffScore(TradeoffDimension.TIME, 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY, 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY, 1.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 0.0),
                    new TradeoffScore(TradeoffDimension.RISK, 0.0)),
                    "practicality only");
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.3333),
                    analysesOf(practicalityOnly, flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    constraints, null, null);
            assertEquals(0.3333, result.factors().get(2).contribution());
            // 0.65 + 0.20*0.3333 + 0.15 = 0.866666... -> 0.8667
            assertEquals(0.8667, result.confidence());
        }
    }

    // ------------------------------------------------------------------
    // Confidence formula, rounding and level mapping
    // ------------------------------------------------------------------

    @Nested
    class Confidence {

        @Test
        void perfectInputsProduceFullConfidence() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertEquals(1.0, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.level());
        }

        @Test
        void confidenceFollowsLockedFormula() {
            // evidence 0.5, uncertainty absent (1.0), margin absent-analysis
            // single candidate (1.0), coverage 0.8:
            // 0.35*0.5 + 0.30*1.0 + 0.20*1.0 + 0.15*0.8 = 0.795
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null,
                    verificationGraph(0.8,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.UNSUPPORTED)),
                    null);
            assertEquals(0.795, result.confidence());
            // 0.795 sits in the MEDIUM band [0.60, 0.80).
            assertEquals(ConfidenceLevel.MEDIUM, result.level());
        }

        @Test
        void confidenceStaysWithinUnitRange() {
            List<CalibratedDecision> results = List.of(
                    engine.calibrate(decision("c-1", 0.0), null,
                            setOf(candidate("c-1", 2)), null, null, null),
                    engine.calibrate(decision("c-1", 1.0),
                            analysesOf(flatAnalysis("c-1", 1.0)),
                            setOf(candidate("c-1", 2)), null, null, null),
                    engine.calibrate(decision("c-1", 0.5),
                            analysesOf(flatAnalysis("c-1", 0.5)),
                            setOf(candidate("c-1", 2)),
                            null,
                            verificationGraph(0.5, node(VerificationStatus.VERIFIED)),
                            uncertaintyGraph(0.5,
                                    uncertaintyNode("u-1", 0.5))));
            for (CalibratedDecision result : results) {
                assertTrue(result.confidence() >= 0.0 && result.confidence() <= 1.0,
                        "confidence must stay in [0,1]: " + result.confidence());
                assertEquals(ConfidenceLevel.of(result.confidence()), result.level());
            }
        }

        @Test
        void zeroMarginWithZeroEvidenceProducesLowConfidence() {
            // evidence 0.0, margin 0.0 (tied), uncertainty 1.0, coverage 1.0:
            // 0.30 + 0.15 = 0.45 -> LOW
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 0.0),
                    analysesOf(flatAnalysis("c-1", 0.0), flatAnalysis("c-2", 0.0)),
                    setOf(candidate("c-1", 2), candidate("c-2", 3)),
                    null,
                    verificationGraph(1.0,
                            node(VerificationStatus.UNSUPPORTED),
                            node(VerificationStatus.UNSUPPORTED)),
                    null);
            assertEquals(0.0, result.factors().get(0).contribution());
            assertEquals(0.45, result.confidence());
            assertEquals(ConfidenceLevel.LOW, result.level());
        }
    }

    // ------------------------------------------------------------------
    // Engine contract: determinism, attribution, immutability
    // ------------------------------------------------------------------

    @Nested
    class Contract {

        @Test
        void candidateIdIsEchoed() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-42", 1.0),
                    analysesOf(flatAnalysis("c-42", 1.0)),
                    setOf(candidate("c-42", 2)),
                    null, null, null);
            assertEquals("c-42", result.candidateId());
        }

        @Test
        void sameInputsProduceIdenticalOutput() {
            OptimizedDecision d = decision("c-1", 0.9);
            TradeoffAnalysisSet analyses = analysesOf(
                    flatAnalysis("c-1", 0.9), flatAnalysis("c-2", 0.4));
            AlternativeSet alternatives = setOf(candidate("c-1", 2), candidate("c-2", 3));
            VerificationGraph verification = verificationGraph(0.75,
                    node(VerificationStatus.VERIFIED),
                    node(VerificationStatus.PARTIALLY_VERIFIED));
            UncertaintyGraph uncertainty = uncertaintyGraph(0.7,
                    uncertaintyNode("u-1", 0.7));

            CalibratedDecision first = engine.calibrate(
                    d, analyses, alternatives, null, verification, uncertainty);
            CalibratedDecision second = new DefaultConfidenceCalibrationEngine()
                    .calibrate(d, analyses, alternatives, null, verification, uncertainty);
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void factorListIsUnmodifiable() {
            CalibratedDecision result = engine.calibrate(
                    decision("c-1", 1.0),
                    analysesOf(flatAnalysis("c-1", 1.0)),
                    setOf(candidate("c-1", 2)),
                    null, null, null);
            assertThrows(UnsupportedOperationException.class,
                    () -> result.factors().add(
                            new ConfidenceFactor("Injected", 1.0)));
        }

        @Test
        void engineImplementsInterface() {
            assertTrue(engine instanceof ConfidenceCalibrationEngine);
        }
    }

    // ------------------------------------------------------------------
    // ConfidenceFactor validation
    // ------------------------------------------------------------------

    @Nested
    class ConfidenceFactorValidation {

        @Test
        void nullNameIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ConfidenceFactor(null, 0.5));
        }

        @Test
        void blankNameIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ConfidenceFactor("  ", 0.5));
        }

        @Test
        void contributionBelowRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ConfidenceFactor("Evidence", -0.0001));
        }

        @Test
        void contributionAboveRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ConfidenceFactor("Evidence", 1.0001));
        }

        @Test
        void boundariesAreAccepted() {
            ConfidenceFactor low = new ConfidenceFactor("Low", 0.0);
            ConfidenceFactor high = new ConfidenceFactor("High", 1.0);
            assertEquals(0.0, low.contribution());
            assertEquals(1.0, high.contribution());
        }
    }

    // ------------------------------------------------------------------
    // CalibratedDecision validation and immutability
    // ------------------------------------------------------------------

    @Nested
    class CalibratedDecisionValidation {

        private static final ConfidenceFactor FACTOR =
                new ConfidenceFactor("Verified Evidence", 1.0);

        @Test
        void nullCandidateIdIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new CalibratedDecision(null, 0.5, ConfidenceLevel.MEDIUM,
                            List.of(FACTOR)));
        }

        @Test
        void blankCandidateIdIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CalibratedDecision(" ", 0.5, ConfidenceLevel.MEDIUM,
                            List.of(FACTOR)));
        }

        @Test
        void nanConfidenceIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CalibratedDecision("c-1", Double.NaN,
                            ConfidenceLevel.LOW, List.of(FACTOR)));
        }

        @Test
        void confidenceBelowRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CalibratedDecision("c-1", -0.0001,
                            ConfidenceLevel.LOW, List.of(FACTOR)));
        }

        @Test
        void confidenceAboveRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CalibratedDecision("c-1", 1.0001,
                            ConfidenceLevel.HIGH, List.of(FACTOR)));
        }

        @Test
        void emptyFactorListIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CalibratedDecision("c-1", 0.5,
                            ConfidenceLevel.MEDIUM, List.of()));
        }

        @Test
        void nullFactorListIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new CalibratedDecision("c-1", 0.5,
                            ConfidenceLevel.MEDIUM, null));
        }
        @Test
        void factorListIsDefensivelyCopied() {
            List<ConfidenceFactor> mutable = new ArrayList<>();
            mutable.add(FACTOR);
            CalibratedDecision result = new CalibratedDecision(
                    "c-1", 0.5, ConfidenceLevel.MEDIUM, mutable);
            mutable.add(new ConfidenceFactor("Injected", 1.0));
            assertEquals(1, result.factors().size());
            assertThrows(UnsupportedOperationException.class,
                    () -> result.factors().add(FACTOR));
        }

        @Test
        void confidenceAccessorRoundsToFourDecimals() {
            CalibratedDecision result = new CalibratedDecision(
                    "c-1", 0.123456, ConfidenceLevel.LOW, List.of(FACTOR));
            assertEquals(0.1235, result.confidence());
        }

        @Test
        void toStringContainsKeyData() {
            CalibratedDecision result = new CalibratedDecision(
                    "c-1", 0.5, ConfidenceLevel.MEDIUM, List.of(FACTOR));
            String text = result.toString();
            assertTrue(text.contains("c-1"));
            assertTrue(text.contains("0.5000"));
            assertTrue(text.contains("MEDIUM"));
        }
    }

    // ------------------------------------------------------------------
    // CognitiveState integration (I4 wiring)
    // ------------------------------------------------------------------

    @Nested
    class CognitiveStateWiring {

        @Test
        void emptyStateHasNoCalibratedDecision() {
            assertNull(CognitiveState.empty().calibratedDecision());
        }

        @Test
        void withCalibratedDecisionStoresArtifact() {
            CalibratedDecision calibrated = new CalibratedDecision(
                    "c-1", 0.9, ConfidenceLevel.HIGH, List.of(
                    new ConfidenceFactor("Verified Evidence", 1.0)));
            CognitiveState state = CognitiveState.empty()
                    .withCalibratedDecision(calibrated);
            assertSame(calibrated, state.calibratedDecision());
            assertEquals(0, state.reflectionIteration());
            assertTrue(state.qualityHistory().isEmpty());
            assertNull(state.optimizedDecision());
        }

        @Test
        void nullCalibratedDecisionIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> CognitiveState.empty().withCalibratedDecision(null));
        }

        @Test
        void otherWithMethodsPreserveCalibratedDecision() {
            CalibratedDecision calibrated = new CalibratedDecision(
                    "c-1", 0.9, ConfidenceLevel.HIGH, List.of(
                    new ConfidenceFactor("Verified Evidence", 1.0)));
            CognitiveState state = CognitiveState.empty()
                    .withCalibratedDecision(calibrated)
                    .withUserConstraints(UserConstraints.empty());
            assertSame(calibrated, state.calibratedDecision());
            assertEquals(UserConstraints.empty(), state.userConstraints());
        }
    }
}