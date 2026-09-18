package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalEvidence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
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
import com.shreeai.os.platform.kernels.inference.model.ExplainableDecision;
import com.shreeai.os.platform.kernels.inference.model.ExplanationSection;
import com.shreeai.os.platform.kernels.inference.model.ExplanationSectionType;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
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
 * Deterministic tests for the I5 {@link DefaultExplainableDecisionEngine}.
 *
 * <p>Every test is fixed-input and side-effect free: identical inputs always
 * produce the same {@link ExplainableDecision} with the same five locked
 * sections in locked order. The engine generates structure only - never
 * natural language.</p>
 */
public class DefaultExplainableDecisionEngineTest {

    private DefaultExplainableDecisionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultExplainableDecisionEngine();
    }

    // ------------------------------------------------------------------
    // Fixture helpers
    // ------------------------------------------------------------------

    private static final String C1 = "c-1";

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

    private static TradeoffAnalysis flatAnalysis(String candidateId, double value) {
        return new TradeoffAnalysis(candidateId, List.of(
                new TradeoffScore(TradeoffDimension.TIME, value),
                new TradeoffScore(TradeoffDimension.COMPLEXITY, value),
                new TradeoffScore(TradeoffDimension.PRACTICALITY, value),
                new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, value),
                new TradeoffScore(TradeoffDimension.RISK, value)),
                "analysis of " + candidateId);
    }

    private static CalibratedDecision calibrated(double confidence) {
        return new CalibratedDecision(C1, confidence, ConfidenceLevel.of(confidence),
                List.of(
                        new ConfidenceFactor("Verified Evidence", 1.0),
                        new ConfidenceFactor("Uncertainty", 1.0),
                        new ConfidenceFactor("Tradeoff Margin", 1.0),
                        new ConfidenceFactor("Coverage", 1.0)));
    }

    private static UserConstraints constraints(String duration, String platform) {
        return UserConstraints.of(duration, null, null,
                platform == null ? null : PlatformType.WINDOWS, platform, null,
                List.of());
    }

    private static GoalStructure goalStructure(String goalTitle, String... subGoals) {
        List<GoalNode> subs = new ArrayList<>();
        for (String title : subGoals) {
            subs.add(new GoalNode(title, 0.9,
                    new GoalEvidence(title, 0, title.length())));
        }
        return GoalStructure.of(
                new GoalNode(goalTitle, 0.95,
                        new GoalEvidence(goalTitle, 0, goalTitle.length())),
                subs, GoalComplexity.COMPLEX, Instant.parse("2026-01-01T00:00:00Z"),
                "test");
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

    /** Full happy-path explain call with all artifacts present. */
    private ExplainableDecision explainFull() {
        return engine.explain(
                decision(C1, 0.9),
                calibrated(0.8465),
                setOf(candidate(C1, 3), candidate("c-2", 2)),
                analysesOf(flatAnalysis(C1, 0.9), flatAnalysis("c-2", 0.4)),
                goalStructure("Become Java Developer", "Learn OOP"),
                constraints("30 days", "Java"),
                verificationGraph(0.91,
                        node(VerificationStatus.VERIFIED),
                        node(VerificationStatus.VERIFIED),
                        node(VerificationStatus.UNSUPPORTED)));
    }

    /** Returns the section of the given type (sections are in locked order). */
    private static ExplanationSection section(ExplainableDecision result,
                                              ExplanationSectionType type) {
        for (ExplanationSection s : result.sections()) {
            if (s.type() == type) {
                return s;
            }
        }
        throw new AssertionError("missing section: " + type);
    }

    // ------------------------------------------------------------------
    // Artifact structure and locked ordering
    // ------------------------------------------------------------------

    @Nested
    class ArtifactStructure {

        @Test
        void fiveSectionsInLockedOrder() {
            ExplainableDecision result = explainFull();
            assertEquals(5, result.sections().size());
            assertEquals(ExplanationSectionType.GOAL, result.sections().get(0).type());
            assertEquals(ExplanationSectionType.STRATEGY, result.sections().get(1).type());
            assertEquals(ExplanationSectionType.TRADEOFF, result.sections().get(2).type());
            assertEquals(ExplanationSectionType.CONFIDENCE, result.sections().get(3).type());
            assertEquals(ExplanationSectionType.EVIDENCE, result.sections().get(4).type());
        }

        @Test
        void lockedSectionTitles() {
            ExplainableDecision result = explainFull();
            assertEquals("Goal Summary", section(result, ExplanationSectionType.GOAL).title());
            assertEquals("Selected Strategy", section(result, ExplanationSectionType.STRATEGY).title());
            assertEquals("Trade-off Summary", section(result, ExplanationSectionType.TRADEOFF).title());
            assertEquals("Confidence Summary", section(result, ExplanationSectionType.CONFIDENCE).title());
            assertEquals("Evidence Summary", section(result, ExplanationSectionType.EVIDENCE).title());
        }

        @Test
        void echoesCandidateIdStrategyAndScores() {
            ExplainableDecision result = explainFull();
            assertEquals(C1, result.candidateId());
            assertEquals(AlternativeType.BALANCED, result.strategy());
            assertEquals(0.9, result.optimizationScore());
            assertEquals(0.8465, result.confidence());
            assertEquals(ConfidenceLevel.HIGH, result.confidenceLevel());
        }

        @Test
        void worksWithAllArtifactsAbsent() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.5), calibrated(0.5),
                    null, null, null, null, null);
            assertEquals(5, result.sections().size());
            for (ExplanationSection s : result.sections()) {
                assertNotNull(s);
                assertNotNull(s.title());
                assertTrue(!s.bulletPoints().isEmpty());
            }
        }

        @Test
        void nullDecisionIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> engine.explain(null, calibrated(0.5),
                            null, null, null, null, null));
        }

        @Test
        void nullCalibratedDecisionIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> engine.explain(decision(C1, 0.5), null,
                            null, null, null, null, null));
        }
    }

    // ------------------------------------------------------------------
    // Stage 1 - Goal Summary
    // ------------------------------------------------------------------

    @Nested
    class GoalSection {

        @Test
        void goalSectionGenerated() {
            ExplainableDecision result = explainFull();
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertTrue(goal.bulletPoints().contains("Become Java Developer"));
        }

        @Test
        void subGoalsAppearAfterPrimaryGoal() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null,
                    goalStructure("Become Java Developer", "Learn OOP", "Build projects"),
                    null, null);
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            List<String> bullets = goal.bulletPoints();
            assertTrue(bullets.indexOf("Become Java Developer")
                    < bullets.indexOf("Learn OOP"));
            assertTrue(bullets.indexOf("Learn OOP")
                    < bullets.indexOf("Build projects"));
        }

        @Test
        void durationConstraintBullet() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null,
                    constraints("30 days", null), null);
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertTrue(goal.bulletPoints().contains("Duration: 30 days"));
        }

        @Test
        void platformConstraintBullet() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null,
                    constraints(null, "Java"), null);
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertEquals(List.of("Domain: WINDOWS"), goal.bulletPoints());
        }

        @Test
        void absentGoalFallsBackDeterministically() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null, null);
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertEquals(List.of("No explicit goal captured"), goal.bulletPoints());
        }

        @Test
        void blankPrimaryGoalTitleIsSkipped() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null,
                    GoalStructure.of(
                            new GoalNode("   ", 0.5, new GoalEvidence("", 0, 0)),
                            List.of(), GoalComplexity.SIMPLE,
                            Instant.parse("2026-01-01T00:00:00Z"), "test"),
                    null, null);
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertEquals(List.of("No explicit goal captured"), goal.bulletPoints());
        }
    }

    // ------------------------------------------------------------------
    // Stage 2 - Selected Strategy
    // ------------------------------------------------------------------

    @Nested
    class StrategySection {

        @Test
        void strategyStepsInLockedOrder() {
            ExplainableDecision result = explainFull();
            ExplanationSection strategy = section(result, ExplanationSectionType.STRATEGY);
            assertEquals(List.of("Step 1", "Step 2", "Step 3"), strategy.bulletPoints());
        }

        @Test
        void readsStepsOfSelectedCandidateOnly() {
            ExplainableDecision result = engine.explain(
                    decision("c-2", 0.9), calibrated(0.9),
                    setOf(candidate(C1, 3), candidate("c-2", 2)),
                    null, null, null, null);
            ExplanationSection strategy = section(result, ExplanationSectionType.STRATEGY);
            assertEquals(List.of("Step 1", "Step 2"), strategy.bulletPoints());
        }

        @Test
        void absentAlternativeSetFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null, null);
            ExplanationSection strategy = section(result, ExplanationSectionType.STRATEGY);
            assertEquals(List.of("No steps available for the selected strategy"),
                    strategy.bulletPoints());
        }

        @Test
        void candidateMissingFromSetFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision("c-9", 0.9), calibrated(0.9),
                    setOf(candidate(C1, 2)), null, null, null, null);
            ExplanationSection strategy = section(result, ExplanationSectionType.STRATEGY);
            assertEquals(List.of("No steps available for the selected strategy"),
                    strategy.bulletPoints());
        }

        @Test
        void candidateWithEmptyStepsFallsBack() {
            // AlternativeCandidate rejects empty step lists, so a missing
            // candidate from the set is the only way steps can be absent.
            ExplainableDecision result = engine.explain(
                    decision("c-missing", 0.9), calibrated(0.9),
                    setOf(candidate(C1, 1)), null, null, null, null);
            ExplanationSection strategy = section(result, ExplanationSectionType.STRATEGY);
            assertEquals(List.of("No steps available for the selected strategy"),
                    strategy.bulletPoints());
        }
    }

    // ------------------------------------------------------------------
    // Stage 3 - Trade-off Summary
    // ------------------------------------------------------------------

    @Nested
    class TradeoffSection {

        @Test
        void tradeoffSectionDeterministic() {
            ExplainableDecision first = explainFull();
            ExplainableDecision second = new DefaultExplainableDecisionEngine()
                    .explain(decision(C1, 0.9), calibrated(0.8465),
                            setOf(candidate(C1, 3), candidate("c-2", 2)),
                            analysesOf(flatAnalysis(C1, 0.9), flatAnalysis("c-2", 0.4)),
                            goalStructure("Become Java Developer", "Learn OOP"),
                            constraints("30 days", "Java"),
                            verificationGraph(0.91,
                                    node(VerificationStatus.VERIFIED),
                                    node(VerificationStatus.VERIFIED),
                                    node(VerificationStatus.UNSUPPORTED)));
            assertEquals(section(first, ExplanationSectionType.TRADEOFF),
                    section(second, ExplanationSectionType.TRADEOFF));
        }

        @Test
        void highScoreMapsToHighDescriptor() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.9)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().contains("High practical learning"));
            assertTrue(tradeoff.bulletPoints().contains("Fast completion"));
        }

        @Test
        void moderateScoreMapsToModerateDescriptor() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.6)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().contains("Moderate practical value"));
            assertTrue(tradeoff.bulletPoints().contains("Moderate duration"));
        }

        @Test
        void lowScoreMapsToLowDescriptor() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.3)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().contains("Limited practical focus"));
            assertTrue(tradeoff.bulletPoints().contains("Slow completion"));
        }

        @Test
        void zeroScoresAreOmitted() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.0)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().isEmpty());
        }

        @Test
        void bulletOrderIsLockedDimensionOrder() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.9)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertEquals(List.of("High practical learning", "Fast completion",
                            "High complexity", "Deep learning path", "High risk"),
                    tradeoff.bulletPoints());
        }

        @Test
        void absentAnalysisSetFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertEquals(List.of("Trade-off analysis not available"),
                    tradeoff.bulletPoints());
        }

        @Test
        void candidateMissingFromAnalysesFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision("c-9", 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.9)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertEquals(List.of("Trade-off analysis not available"),
                    tradeoff.bulletPoints());
        }

        @Test
        void boundaryAt075IsHigh() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.75)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().contains("High practical learning"));
        }

        @Test
        void justBelow075IsModerate() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(flatAnalysis(C1, 0.7499)),
                    null, null, null);
            ExplanationSection tradeoff = section(result, ExplanationSectionType.TRADEOFF);
            assertTrue(tradeoff.bulletPoints().contains("Moderate practical value"));
        }
    }

    // ------------------------------------------------------------------
    // Stage 4 - Confidence Summary
    // ------------------------------------------------------------------

    @Nested
    class ConfidenceSection {

        @Test
        void confidenceSectionCorrect() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.8465),
                    null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertTrue(confidence.bulletPoints().contains("Confidence: HIGH"));
            assertTrue(confidence.bulletPoints().contains("Score: 0.8465"));
        }

        @Test
        void strongFactorUsesStrongWording() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertTrue(confidence.bulletPoints().contains("Strong verified evidence"));
            assertTrue(confidence.bulletPoints().contains("Low uncertainty"));
            assertTrue(confidence.bulletPoints().contains("Clear winning alternative"));
            assertTrue(confidence.bulletPoints().contains("Complete verification coverage"));
        }

        @Test
        void moderateFactorUsesModerateWording() {
            CalibratedDecision mid = new CalibratedDecision(C1, 0.7,
                    ConfidenceLevel.MEDIUM, List.of(
                    new ConfidenceFactor("Verified Evidence", 0.6),
                    new ConfidenceFactor("Uncertainty", 0.6),
                    new ConfidenceFactor("Tradeoff Margin", 0.6),
                    new ConfidenceFactor("Coverage", 0.6)));
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), mid, null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertTrue(confidence.bulletPoints().contains("Moderate evidence support"));
            assertTrue(confidence.bulletPoints().contains("Moderate uncertainty"));
            assertTrue(confidence.bulletPoints().contains("Competitive alternatives"));
            assertTrue(confidence.bulletPoints().contains("Partial verification coverage"));
        }

        @Test
        void weakFactorUsesWeakWording() {
            CalibratedDecision low = new CalibratedDecision(C1, 0.3,
                    ConfidenceLevel.LOW, List.of(
                    new ConfidenceFactor("Verified Evidence", 0.3),
                    new ConfidenceFactor("Uncertainty", 0.3),
                    new ConfidenceFactor("Tradeoff Margin", 0.3),
                    new ConfidenceFactor("Coverage", 0.3)));
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), low, null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertTrue(confidence.bulletPoints().contains("Weak evidence support"));
            assertTrue(confidence.bulletPoints().contains("High uncertainty"));
            assertTrue(confidence.bulletPoints().contains("No clear winner among alternatives"));
            assertTrue(confidence.bulletPoints().contains("Limited verification coverage"));
        }

        @Test
        void boundaryAt075IsStrong() {
            CalibratedDecision atBoundary = new CalibratedDecision(C1, 0.8,
                    ConfidenceLevel.HIGH, List.of(
                    new ConfidenceFactor("Verified Evidence", 0.75)));
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), atBoundary, null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertTrue(confidence.bulletPoints().contains("Strong verified evidence"));
        }

        @Test
        void uncertaintyInversionAtBoundary() {
            CalibratedDecision atBoundary = new CalibratedDecision(C1, 0.8,
                    ConfidenceLevel.HIGH, List.of(
                    new ConfidenceFactor("Verified Evidence", 0.0),
                    new ConfidenceFactor("Uncertainty", 0.75),
                    new ConfidenceFactor("Tradeoff Margin", 0.0),
                    new ConfidenceFactor("Coverage", 0.0)));
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), atBoundary, null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            // The uncertainty factor already measures the absence of
            // uncertainty: 0.75 at the strong boundary reads as
            // "Low uncertainty".
            assertTrue(confidence.bulletPoints().contains("Low uncertainty"));
        }

        @Test
        void zeroFactorIsOmitted() {
            CalibratedDecision zero = new CalibratedDecision(C1, 0.8,
                    ConfidenceLevel.HIGH, List.of(
                    new ConfidenceFactor("Verified Evidence", 0.0)));
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), zero, null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertEquals(List.of("Confidence: HIGH", "Score: 0.8000"),
                    confidence.bulletPoints());
        }

        @Test
        void levelAndScoreAreAlwaysPresent() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.7),
                    null, null, null, null, null);
            ExplanationSection confidence =
                    section(result, ExplanationSectionType.CONFIDENCE);
            assertEquals("Confidence: MEDIUM", confidence.bulletPoints().get(0));
            assertEquals("Score: 0.7000", confidence.bulletPoints().get(1));
        }
    }

    // ------------------------------------------------------------------
    // Stage 5 - Evidence Summary
    // ------------------------------------------------------------------

    @Nested
    class EvidenceSection {

        @Test
        void evidenceSectionCorrect() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(0.91,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.UNSUPPORTED)));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals(List.of("2 verified concepts", "0 contradictions",
                    "91% evidence coverage"), evidence.bulletPoints());
        }

        @Test
        void contradictionsAreCounted() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(0.5,
                            node(VerificationStatus.VERIFIED),
                            node(VerificationStatus.CONTRADICTED),
                            node(VerificationStatus.CONTRADICTED)));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals("1 verified concepts", evidence.bulletPoints().get(0));
            assertEquals("2 contradictions", evidence.bulletPoints().get(1));
        }

        @Test
        void partiallyVerifiedIsNeither() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(0.5,
                            node(VerificationStatus.PARTIALLY_VERIFIED)));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals("0 verified concepts", evidence.bulletPoints().get(0));
            assertEquals("0 contradictions", evidence.bulletPoints().get(1));
            assertEquals("50% evidence coverage", evidence.bulletPoints().get(2));
        }

        @Test
        void zeroCoverageFormatsAsZeroPercent() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(0.0, node(VerificationStatus.VERIFIED)));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals("0% evidence coverage", evidence.bulletPoints().get(2));
        }

        @Test
        void fullCoverageFormatsAsHundredPercent() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(1.0, node(VerificationStatus.VERIFIED)));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals("100% evidence coverage", evidence.bulletPoints().get(2));
        }

        @Test
        void nullVerificationGraphFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null, null);
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals(List.of("No verification data available"),
                    evidence.bulletPoints());
        }

        @Test
        void emptyVerificationGraphFallsBack() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null, null, null, null,
                    verificationGraph(0.5));
            ExplanationSection evidence =
                    section(result, ExplanationSectionType.EVIDENCE);
            assertEquals(List.of("No verification data available"),
                    evidence.bulletPoints());
        }
    }

    // ------------------------------------------------------------------
    // Engine contract: determinism and immutability
    // ------------------------------------------------------------------

    @Nested
    class Contract {

        @Test
        void sameInputIdenticalOutput() {
            OptimizedDecision d = decision(C1, 0.9);
            CalibratedDecision c = calibrated(0.8465);
            AlternativeSet alternatives = setOf(candidate(C1, 3), candidate("c-2", 2));
            TradeoffAnalysisSet analyses = analysesOf(
                    flatAnalysis(C1, 0.9), flatAnalysis("c-2", 0.4));
            GoalStructure goal = goalStructure("Become Java Developer", "Learn OOP");
            UserConstraints constraints = constraints("30 days", "Java");
            VerificationGraph verification = verificationGraph(0.91,
                    node(VerificationStatus.VERIFIED),
                    node(VerificationStatus.VERIFIED),
                    node(VerificationStatus.UNSUPPORTED));

            ExplainableDecision first = engine.explain(
                    d, c, alternatives, analyses, goal, constraints, verification);
            ExplainableDecision second = new DefaultExplainableDecisionEngine()
                    .explain(d, c, alternatives, analyses, goal, constraints, verification);
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            assertEquals(first.sections(), second.sections());
        }

        @Test
        void sectionListIsUnmodifiable() {
            ExplainableDecision result = explainFull();
            assertThrows(UnsupportedOperationException.class,
                    () -> result.sections().add(null));
        }

        @Test
        void bulletListIsUnmodifiable() {
            ExplainableDecision result = explainFull();
            ExplanationSection goal = section(result, ExplanationSectionType.GOAL);
            assertThrows(UnsupportedOperationException.class,
                    () -> goal.bulletPoints().add("injected"));
        }

        @Test
        void toStringContainsKeyData() {
            ExplainableDecision result = explainFull();
            String text = result.toString();
            assertTrue(text.contains("c-1"));
            assertTrue(text.contains("BALANCED"));
            assertTrue(text.contains("HIGH"));
        }
    }

    // ------------------------------------------------------------------
    // Model validation: ExplanationSection and ExplainableDecision
    // ------------------------------------------------------------------

    @Nested
    class ModelValidation {

        private static final List<String> BULLETS = List.of("bullet one");

        private static ExplainableDecision build(String candidateId,
                                                 AlternativeType strategy,
                                                 double optimizationScore,
                                                 double confidence,
                                                 ConfidenceLevel level,
                                                 List<ExplanationSection> sections) {
            return new ExplainableDecision(candidateId, strategy,
                    optimizationScore, confidence, level, sections);
        }

        private static List<ExplanationSection> oneSection() {
            return List.of(new ExplanationSection(
                    ExplanationSectionType.GOAL, "T", BULLETS));
        }

        @Test
        void sectionNullTypeIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ExplanationSection(null, "Title", BULLETS));
        }

        @Test
        void sectionNullTitleIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ExplanationSection(
                            ExplanationSectionType.GOAL, null, BULLETS));
        }

        @Test
        void sectionNullBulletsIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ExplanationSection(
                            ExplanationSectionType.GOAL, "Title", null));
        }

        @Test
        void sectionBlankTitleIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ExplanationSection(
                            ExplanationSectionType.GOAL, "  ", BULLETS));
        }

        @Test
        void sectionBulletsDefensivelyCopied() {
            List<String> mutable = new ArrayList<>();
            mutable.add("original");
            ExplanationSection section = new ExplanationSection(
                    ExplanationSectionType.GOAL, "Title", mutable);
            mutable.add("injected");
            assertEquals(List.of("original"), section.bulletPoints());
            assertThrows(UnsupportedOperationException.class,
                    () -> section.bulletPoints().add("x"));
        }

        @Test
        void sectionEmptyBulletsAllowed() {
            ExplanationSection section = new ExplanationSection(
                    ExplanationSectionType.EVIDENCE, "Title", List.of());
            assertTrue(section.bulletPoints().isEmpty());
        }

        @Test
        void sectionTypeValuesInLockedOrder() {
            ExplanationSectionType[] types = ExplanationSectionType.values();
            assertEquals(5, types.length);
            assertEquals(ExplanationSectionType.GOAL, types[0]);
            assertEquals(ExplanationSectionType.STRATEGY, types[1]);
            assertEquals(ExplanationSectionType.TRADEOFF, types[2]);
            assertEquals(ExplanationSectionType.CONFIDENCE, types[3]);
            assertEquals(ExplanationSectionType.EVIDENCE, types[4]);
        }

        @Test
        void sectionTypeValueOfRoundTrip() {
            for (ExplanationSectionType type : ExplanationSectionType.values()) {
                assertEquals(type, ExplanationSectionType.valueOf(type.name()));
            }
        }

        @Test
        void decisionNullCandidateIdIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> build(null, AlternativeType.BALANCED,
                            0.5, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionBlankCandidateIdIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(" ", AlternativeType.BALANCED,
                            0.5, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionNullStrategyIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> build(C1, null,
                            0.5, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionNullLevelIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            0.5, 0.5, null, oneSection()));
        }

        @Test
        void decisionNullSectionsIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            0.5, 0.5, ConfidenceLevel.MEDIUM, null));
        }

        @Test
        void decisionEmptySectionsIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            0.5, 0.5, ConfidenceLevel.MEDIUM, List.of()));
        }

        @Test
        void decisionNanOptimizationScoreIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            Double.NaN, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionOptimizationScoreBelowRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            -0.0001, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionOptimizationScoreAboveRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            1.0001, 0.5, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionNanConfidenceIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            0.5, Double.NaN, ConfidenceLevel.MEDIUM, oneSection()));
        }

        @Test
        void decisionConfidenceAboveRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> build(C1, AlternativeType.BALANCED,
                            0.5, 1.0001, ConfidenceLevel.HIGH, oneSection()));
        }

        @Test
        void decisionValidBoundariesAccepted() {
            ExplainableDecision decision = build(C1, AlternativeType.BALANCED,
                    0.0, 1.0, ConfidenceLevel.HIGH, oneSection());
            assertEquals(0.0, decision.optimizationScore());
            assertEquals(1.0, decision.confidence());
        }

        @Test
        void decisionSectionsDefensivelyCopied() {
            List<ExplanationSection> mutable = new ArrayList<>();
            mutable.add(new ExplanationSection(
                    ExplanationSectionType.GOAL, "T", BULLETS));
            ExplainableDecision decision = build(C1, AlternativeType.BALANCED,
                    0.5, 0.5, ConfidenceLevel.MEDIUM, mutable);
            mutable.add(new ExplanationSection(
                    ExplanationSectionType.STRATEGY, "T2", BULLETS));
            assertEquals(1, decision.sections().size());
            assertThrows(UnsupportedOperationException.class,
                    () -> decision.sections().add(null));
        }
    }

    // ------------------------------------------------------------------
    // CognitiveState integration (I5 wiring)
    // ------------------------------------------------------------------

    @Nested
    class CognitiveStateWiring {

        @Test
        void emptyStateHasNoExplainableDecision() {
            assertNull(CognitiveState.empty().explainableDecision());
        }

        @Test
        void withExplainableDecisionStoresArtifact() {
            ExplainableDecision decision = new ExplainableDecision(
                    C1, AlternativeType.BALANCED, 0.9, 0.8465,
                    ConfidenceLevel.HIGH,
                    List.of(new ExplanationSection(
                            ExplanationSectionType.GOAL, "Goal Summary",
                            List.of("Become Java Developer"))));
            CognitiveState state = CognitiveState.empty()
                    .withExplainableDecision(decision);
            assertSame(decision, state.explainableDecision());
            assertEquals(0, state.reflectionIteration());
            assertTrue(state.qualityHistory().isEmpty());
        }

        @Test
        void nullExplainableDecisionIsRejected() {
            assertThrows(NullPointerException.class,
                    () -> CognitiveState.empty().withExplainableDecision(null));
        }

        @Test
        void otherWithMethodsPreserveExplainableDecision() {
            ExplainableDecision decision = new ExplainableDecision(
                    C1, AlternativeType.BALANCED, 0.9, 0.8465,
                    ConfidenceLevel.HIGH,
                    List.of(new ExplanationSection(
                            ExplanationSectionType.GOAL, "Goal Summary",
                            List.of("Become Java Developer"))));
            CognitiveState state = CognitiveState.empty()
                    .withExplainableDecision(decision)
                    .withUserConstraints(UserConstraints.empty());
            assertSame(decision, state.explainableDecision());
            assertEquals(UserConstraints.empty(), state.userConstraints());
        }

        @Test
        void fullInferenceChainSettableOnState() {
            CognitiveState state = CognitiveState.empty()
                    .withExplainableDecision(explainFull());
            assertNotNull(state.explainableDecision());
            assertEquals(5, state.explainableDecision().sections().size());
        }
    }

    // ------------------------------------------------------------------
    // Determinism matrix: every descriptor band for every locked input
    // ------------------------------------------------------------------

    @Nested
    class DeterminismMatrix {

        @Test
        void tradeoffBandsExhaustive() {
            double[] bands = {0.9, 0.7499, 0.6, 0.4999, 0.2, 0.0};
            for (double band : bands) {
                ExplainableDecision result = engine.explain(
                        decision(C1, 0.9), calibrated(0.9),
                        null, analysesOf(flatAnalysis(C1, band)),
                        null, null, null);
                ExplanationSection tradeoff =
                        section(result, ExplanationSectionType.TRADEOFF);
                if (band == 0.0) {
                    assertTrue(tradeoff.bulletPoints().isEmpty());
                } else {
                    assertTrue(!tradeoff.bulletPoints().isEmpty(),
                            "band " + band + " produced no bullets");
                    for (String bullet : tradeoff.bulletPoints()) {
                        assertTrue(
                                bullet.contains("practical")
                                        || bullet.contains("completion")
                                        || bullet.contains("duration")
                                        || bullet.contains("complexity")
                                        || bullet.contains("learning")
                                        || bullet.contains("risk"),
                                "unexpected bullet: " + bullet);
                    }
                }
            }
        }

        @Test
        void confidenceBandsExhaustive() {
            double[] bands = {1.0, 0.7499, 0.6, 0.4999, 0.2, 0.0001};
            for (double band : bands) {
                CalibratedDecision calibratedBand = new CalibratedDecision(C1,
                        band, ConfidenceLevel.of(band), List.of(
                        new ConfidenceFactor("Verified Evidence", band),
                        new ConfidenceFactor("Uncertainty", band),
                        new ConfidenceFactor("Tradeoff Margin", band),
                        new ConfidenceFactor("Coverage", band)));
                ExplainableDecision result = engine.explain(
                        decision(C1, 0.9), calibratedBand,
                        null, null, null, null, null);
                ExplanationSection confidence =
                        section(result, ExplanationSectionType.CONFIDENCE);
                assertEquals("Confidence: " + ConfidenceLevel.of(band),
                        confidence.bulletPoints().get(0));
                assertTrue(confidence.bulletPoints().size() >= 2);
            }
        }

        @Test
        void strategyBandForEachStepCount() {
            for (int steps = 1; steps <= 5; steps++) {
                ExplainableDecision result = engine.explain(
                        decision(C1, 0.9), calibrated(0.9),
                        setOf(candidate(C1, steps)), null, null, null, null);
                ExplanationSection strategy =
                        section(result, ExplanationSectionType.STRATEGY);
                assertEquals(steps, strategy.bulletPoints().size());
                assertEquals("Step 1", strategy.bulletPoints().get(0));
            }
        }

        @Test
        void evidenceBandForEachStatusMix() {
            VerificationStatus[][] mixes = {
                    {VerificationStatus.VERIFIED},
                    {VerificationStatus.PARTIALLY_VERIFIED},
                    {VerificationStatus.UNSUPPORTED},
                    {VerificationStatus.CONTRADICTED},
                    {VerificationStatus.VERIFIED, VerificationStatus.CONTRADICTED},
            };
            for (VerificationStatus[] mix : mixes) {
                VerificationNode[] nodes = new VerificationNode[mix.length];
                for (int i = 0; i < mix.length; i++) {
                    nodes[i] = node(mix[i]);
                }
                ExplainableDecision result = engine.explain(
                        decision(C1, 0.9), calibrated(0.9),
                        null, null, null, null,
                        verificationGraph(0.5, nodes));
                ExplanationSection evidence =
                        section(result, ExplanationSectionType.EVIDENCE);
                assertEquals(3, evidence.bulletPoints().size());
            }
        }

        @Test
        void fiveRepeatsProduceIdenticalArtifact() {
            ExplainableDecision expected = explainFull();
            for (int i = 0; i < 5; i++) {
                assertEquals(expected, explainFull());
            }
        }

        @Test
        void differentInputsProduceDifferentArtifacts() {
            ExplainableDecision a = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    setOf(candidate(C1, 3)), null, null, null, null);
            ExplainableDecision b = engine.explain(
                    decision("c-2", 0.5), calibrated(0.5),
                    setOf(candidate(C1, 1)), null, null, null, null);
            assertNotEquals(a, b);
        }

        @Test
        void strategyTypeIsEchoedExactly() {
            for (AlternativeType type : AlternativeType.values()) {
                OptimizedDecision d = new OptimizedDecision(C1, type, 0.9,
                        List.of(new DecisionJustification(
                                "test", DecisionDimension.TIME, 0.9)));
                ExplainableDecision result = engine.explain(
                        d, calibrated(0.9), null, null, null, null, null);
                assertEquals(type, result.strategy());
            }
        }

        @Test
        void confidenceLevelIsEchoedExactly() {
            for (ConfidenceLevel level : ConfidenceLevel.values()) {
                CalibratedDecision c = new CalibratedDecision(C1, 0.9, level,
                        List.of(new ConfidenceFactor("Verified Evidence", 1.0)));
                ExplainableDecision result = engine.explain(
                        decision(C1, 0.9), c, null, null, null, null, null);
                assertEquals(level, result.confidenceLevel());
            }
        }
    }

    // ------------------------------------------------------------------
    // Locked wording: exact descriptor per dimension per band
    // ------------------------------------------------------------------

    @Nested
    class WordingLock {

        private static TradeoffAnalysis singleDimension(String candidateId,
                                                        TradeoffDimension dimension,
                                                        double score) {
            return new TradeoffAnalysis(candidateId, List.of(
                    new TradeoffScore(TradeoffDimension.TIME,
                            dimension == TradeoffDimension.TIME ? score : 0.0),
                    new TradeoffScore(TradeoffDimension.COMPLEXITY,
                            dimension == TradeoffDimension.COMPLEXITY ? score : 0.0),
                    new TradeoffScore(TradeoffDimension.PRACTICALITY,
                            dimension == TradeoffDimension.PRACTICALITY ? score : 0.0),
                    new TradeoffScore(TradeoffDimension.LEARNING_DEPTH,
                            dimension == TradeoffDimension.LEARNING_DEPTH ? score : 0.0),
                    new TradeoffScore(TradeoffDimension.RISK,
                            dimension == TradeoffDimension.RISK ? score : 0.0)),
                    "single dimension " + dimension);
        }

        private String bulletFor(TradeoffDimension dimension, double score) {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(singleDimension(C1, dimension, score)),
                    null, null, null);
            return section(result, ExplanationSectionType.TRADEOFF)
                    .bulletPoints().get(0);
        }

        @Test
        void timeHighBand() {
            assertEquals("Fast completion",
                    bulletFor(TradeoffDimension.TIME, 0.9));
        }

        @Test
        void timeModerateBand() {
            assertEquals("Moderate duration",
                    bulletFor(TradeoffDimension.TIME, 0.6));
        }

        @Test
        void timeLowBand() {
            assertEquals("Slow completion",
                    bulletFor(TradeoffDimension.TIME, 0.2));
        }

        @Test
        void complexityHighBand() {
            assertEquals("High complexity",
                    bulletFor(TradeoffDimension.COMPLEXITY, 0.9));
        }

        @Test
        void complexityModerateBand() {
            assertEquals("Moderate complexity",
                    bulletFor(TradeoffDimension.COMPLEXITY, 0.6));
        }

        @Test
        void complexityLowBand() {
            assertEquals("Low complexity",
                    bulletFor(TradeoffDimension.COMPLEXITY, 0.2));
        }

        @Test
        void learningDepthHighBand() {
            assertEquals("Deep learning path",
                    bulletFor(TradeoffDimension.LEARNING_DEPTH, 0.9));
        }

        @Test
        void learningDepthModerateBand() {
            assertEquals("Standard learning depth",
                    bulletFor(TradeoffDimension.LEARNING_DEPTH, 0.6));
        }

        @Test
        void learningDepthLowBand() {
            assertEquals("Shallow learning path",
                    bulletFor(TradeoffDimension.LEARNING_DEPTH, 0.2));
        }

        @Test
        void riskHighBand() {
            assertEquals("High risk", bulletFor(TradeoffDimension.RISK, 0.9));
        }

        @Test
        void riskModerateBand() {
            assertEquals("Moderate risk", bulletFor(TradeoffDimension.RISK, 0.6));
        }

        @Test
        void riskLowBand() {
            assertEquals("Low risk", bulletFor(TradeoffDimension.RISK, 0.2));
        }

        @Test
        void mixedBandAnalysisMapsEachDimensionIndependently() {
            ExplainableDecision result = engine.explain(
                    decision(C1, 0.9), calibrated(0.9),
                    null,
                    analysesOf(new TradeoffAnalysis(C1, List.of(
                            new TradeoffScore(TradeoffDimension.TIME, 0.9),
                            new TradeoffScore(TradeoffDimension.COMPLEXITY, 0.6),
                            new TradeoffScore(TradeoffDimension.PRACTICALITY, 0.2),
                            new TradeoffScore(TradeoffDimension.LEARNING_DEPTH, 0.0),
                            new TradeoffScore(TradeoffDimension.RISK, 0.6)),
                            "mixed")),
                    null, null, null);
            assertEquals(List.of("Limited practical focus", "Fast completion",
                            "Moderate complexity", "Moderate risk"),
                    section(result, ExplanationSectionType.TRADEOFF).bulletPoints());
        }
    }
}