package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.OutputPreference;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic tests for the I2 {@link DefaultTradeoffAnalysisEngine}.
 *
 * <p>Every test is fixed-input and side-effect free: identical inputs always
 * produce structurally identical {@link TradeoffAnalysisSet}s.</p>
 */
public class DefaultTradeoffAnalysisEngineTest {

    private DefaultTradeoffAnalysisEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultTradeoffAnalysisEngine();
    }

    // ------------------------------------------------------------------
    // Fixture helpers
    // ------------------------------------------------------------------

    private static AlternativeCandidate candidate(AlternativeType type,
                                                  String candidateId,
                                                  String... stepTitles) {
        List<AlternativeStep> steps = new ArrayList<>();
        int order = 1;
        for (String title : stepTitles) {
            steps.add(new AlternativeStep(order++, title));
        }
        return new AlternativeCandidate(candidateId, type,
                type.name() + " strategy (" + steps.size() + " steps)", steps, 1.0);
    }

    private static AlternativeSet setOf(AlternativeCandidate... candidates) {
        return new AlternativeSet(List.of(candidates));
    }

    private static VerificationNode verified(String nodeId) {
        return new VerificationNode(nodeId, VerificationStatus.VERIFIED, List.of());
    }

    private static VerificationNode unsupported(String nodeId) {
        return new VerificationNode(nodeId, VerificationStatus.UNSUPPORTED, List.of());
    }

    private static VerificationGraph verified(String... nodeIds) {
        List<VerificationNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            nodes.add(verified(nodeId));
        }
        return new VerificationGraph(nodes, List.of(), 1.0);
    }

    private static UncertaintyGraph uncertainty(double overallCertainty) {
        return new UncertaintyGraph(List.of(), List.of(), overallCertainty);
    }

    /** Locked time bands exercised by the fixture sets. */
    private static final String[] ONE_STEP = {"Java"};

    private static final String[] THREE_STEPS = {"Java", "OOP", "Collections"};

    private static final String[] FOUR_STEPS = {"Java", "OOP", "Collections", "Streams"};

    private static final String[] FIVE_STEPS = {"Java", "OOP", "Collections", "Streams", "JVM"};

    private static final String[] SIX_STEPS =
            {"Java", "OOP", "Collections", "Streams", "JVM", "GC"};

    private static final VerificationGraph FOUR_VERIFIED = verified(FOUR_STEPS);

    /** One verified concept per step - the least dense prerequisite structure. */
    private static final String[] CONCEPT_PER_STEP = {"Java", "OOP", "Collections", "Streams"};

    /** Independent branches merged per level - denser prerequisite structure. */
    private static final String[] MERGED_STEPS = {"Java + OOP", "Collections + Streams"};

    /** Every verified prerequisite packed into a single dense step. */
    private static final String[] ONE_DENSE_STEP = {"Java + OOP + Collections + Streams"};

    /** Practice anchors carry no verified concept - they only add steps. */
    private static final String[] STEPS_WITH_ANCHORS =
            {"Java", "Mini Project", "OOP", "Collections", "Streams", "Portfolio"};

    /** Explicitly declared user constraints used by the summary tests. */
    private static final UserConstraints DECLARED_CONSTRAINTS = UserConstraints.of(
            "30 days", "$200", ExperienceLevel.BEGINNER, null, "English",
            OutputPreference.ROADMAP, List.of());

    /** The score of one dimension of one analysis in the result set. */
    private static double score(TradeoffAnalysisSet set, int index, TradeoffDimension dimension) {
        return set.analyses().get(index).scores().stream()
                .filter(entry -> entry.dimension() == dimension)
                .mapToDouble(entry -> entry.score())
                .findFirst()
                .orElseThrow();
    }

    private TradeoffAnalysisSet analyze(AlternativeSet alternatives,
                                        VerificationGraph verificationGraph) {
        return engine.analyze(alternatives, null, verificationGraph, uncertainty(1.0));
    }

    // ------------------------------------------------------------------
    // TIME - locked path-length bands
    // ------------------------------------------------------------------

    @Nested
    class TimeScore {

        private double timeFor(String... steps) {
            TradeoffAnalysisSet set = analyze(
                    setOf(candidate(AlternativeType.SEQUENTIAL, "c-1", steps)), FOUR_VERIFIED);
            return score(set, 0, TradeoffDimension.TIME);
        }

        @Test
        void oneStepIsFastest() {
            assertEquals(1.0, timeFor(ONE_STEP));
        }

        @Test
        void threeStepsIsFastest() {
            assertEquals(1.0, timeFor(THREE_STEPS));
        }

        @Test
        void fourStepsScoresOhEight() {
            assertEquals(0.8, timeFor(FOUR_STEPS));
        }

        @Test
        void fiveStepsScoresOhSix() {
            assertEquals(0.6, timeFor(FIVE_STEPS));
        }

        @Test
        void sixStepsScoresOhFour() {
            assertEquals(0.4, timeFor(SIX_STEPS));
        }

        @Test
        void moreStepsNeverScoreHigher() {
            double previous = Double.MAX_VALUE;
            for (String[] steps : List.of(ONE_STEP, THREE_STEPS, FOUR_STEPS, FIVE_STEPS, SIX_STEPS)) {
                double current = timeFor(steps);
                assertTrue(current <= previous, "time score must not increase with more steps");
                previous = current;
            }
        }
    }

    // ------------------------------------------------------------------
    // PRACTICALITY - locked constants by strategy type
    // ------------------------------------------------------------------

    @Nested
    class PracticalityScore {

        @Test
        void lockedConstantsByStrategyType() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.BALANCED, "balanced", ONE_STEP),
                    candidate(AlternativeType.SEQUENTIAL, "sequential", ONE_STEP),
                    candidate(AlternativeType.PRACTICAL, "practical", ONE_STEP),
                    candidate(AlternativeType.ACCELERATED, "accelerated", ONE_STEP),
                    candidate(AlternativeType.THEORETICAL, "theoretical", ONE_STEP)), FOUR_VERIFIED);

            assertEquals(0.9, score(set, 0, TradeoffDimension.PRACTICALITY));
            assertEquals(0.7, score(set, 1, TradeoffDimension.PRACTICALITY));
            assertEquals(1.0, score(set, 2, TradeoffDimension.PRACTICALITY));
            assertEquals(0.8, score(set, 3, TradeoffDimension.PRACTICALITY));
            assertEquals(0.6, score(set, 4, TradeoffDimension.PRACTICALITY));
        }

        @Test
        void practicalityIsIndependentOfStepCount() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.PRACTICAL, "p-3", THREE_STEPS),
                    candidate(AlternativeType.PRACTICAL, "p-6", SIX_STEPS)), FOUR_VERIFIED);

            assertEquals(1.0, score(set, 0, TradeoffDimension.PRACTICALITY));
            assertEquals(1.0, score(set, 1, TradeoffDimension.PRACTICALITY));
        }
    }

    // ------------------------------------------------------------------
    // COMPLEXITY - verified prerequisite density
    // ------------------------------------------------------------------

    @Nested
    class ComplexityScore {

        private double complexityFor(String... steps) {
            TradeoffAnalysisSet set = analyze(
                    setOf(candidate(AlternativeType.ACCELERATED, "c-1", steps)), FOUR_VERIFIED);
            return score(set, 0, TradeoffDimension.COMPLEXITY);
        }

        @Test
        void oneConceptPerStepIsProportionalDensity() {
            // 4 references / (4 steps x 4 concepts) = 0.25
            assertEquals(0.25, complexityFor(CONCEPT_PER_STEP));
        }

        @Test
        void mergedStepsRaiseDensity() {
            // 4 references / (2 steps x 4 concepts) = 0.50
            assertEquals(0.5, complexityFor(MERGED_STEPS));
        }

        @Test
        void oneDenseStepIsMaximumDensity() {
            // 4 references / (1 step x 4 concepts) = 1.00
            assertEquals(1.0, complexityFor(ONE_DENSE_STEP));
        }

        @Test
        void anchorStepsLowerDensity() {
            // 4 references / (6 steps x 4 concepts) = 0.1667
            assertEquals(0.1667, complexityFor(STEPS_WITH_ANCHORS));
        }

        @Test
        void denserStepsScoreHigher() {
            double anchors = complexityFor(STEPS_WITH_ANCHORS);
            double perStep = complexityFor(CONCEPT_PER_STEP);
            double merged = complexityFor(MERGED_STEPS);
            double dense = complexityFor(ONE_DENSE_STEP);

            assertTrue(anchors < perStep);
            assertTrue(perStep < merged);
            assertTrue(merged < dense);
        }

        @Test
        void withoutVerificationDataComplexityIsZero() {
            TradeoffAnalysisSet set = engine.analyze(
                    setOf(candidate(AlternativeType.ACCELERATED, "c-1", CONCEPT_PER_STEP)),
                    null, null, uncertainty(1.0));
            assertEquals(0.0, score(set, 0, TradeoffDimension.COMPLEXITY));
        }

        @Test
        void stepsWithoutVerifiedConceptsScoreZero() {
            assertEquals(0.0, complexityFor("Redis", "Kafka"));
        }
    }

    // ------------------------------------------------------------------
    // LEARNING_DEPTH - verified breadth covered
    // ------------------------------------------------------------------

    @Nested
    class LearningDepthScore {

        private double depthFor(VerificationGraph graph, String... steps) {
            TradeoffAnalysisSet set = engine.analyze(
                    setOf(candidate(AlternativeType.SEQUENTIAL, "c-1", steps)),
                    null, graph, uncertainty(1.0));
            return score(set, 0, TradeoffDimension.LEARNING_DEPTH);
        }

        @Test
        void coveringAllVerifiedNodesIsFull() {
            assertEquals(1.0, depthFor(FOUR_VERIFIED, CONCEPT_PER_STEP));
        }

        @Test
        void partialCoverageIsProportional() {
            assertEquals(0.25, depthFor(FOUR_VERIFIED, "Java"));
            assertEquals(0.5, depthFor(FOUR_VERIFIED, "Java", "OOP"));
        }

        @Test
        void moreConceptsScoreHigher() {
            double oneConcept = depthFor(FOUR_VERIFIED, "Java");
            double twoConcepts = depthFor(FOUR_VERIFIED, "Java", "OOP");
            double fourConcepts = depthFor(FOUR_VERIFIED, CONCEPT_PER_STEP);

            assertTrue(oneConcept < twoConcepts);
            assertTrue(twoConcepts < fourConcepts);
        }

        @Test
        void anchorStepsDoNotAddBreadth() {
            assertEquals(1.0, depthFor(FOUR_VERIFIED, STEPS_WITH_ANCHORS));
        }

        @Test
        void withoutVerificationGraphDepthIsZero() {
            assertEquals(0.0, depthFor(null, CONCEPT_PER_STEP));
        }

        @Test
        void unverifiedNodesDoNotCount() {
            VerificationGraph mixed = new VerificationGraph(
                    List.of(verified("Java"), unsupported("Redis")), List.of(), 1.0);

            assertEquals(1.0, depthFor(mixed, "Java"));
            assertEquals(0.0, depthFor(mixed, "Redis"));
        }
    }

    // ------------------------------------------------------------------
    // RISK - verified uncertainty
    // ------------------------------------------------------------------

    @Nested
    class RiskScore {

        private double riskFor(UncertaintyGraph graph) {
            TradeoffAnalysisSet set = engine.analyze(
                    setOf(candidate(AlternativeType.SEQUENTIAL, "c-1", CONCEPT_PER_STEP)),
                    null, FOUR_VERIFIED, graph);
            return score(set, 0, TradeoffDimension.RISK);
        }

        @Test
        void fullCertaintyIsMaximumRiskScore() {
            assertEquals(1.0, riskFor(uncertainty(1.0)));
        }

        @Test
        void halfCertaintyIsHalfRiskScore() {
            assertEquals(0.5, riskFor(uncertainty(0.5)));
        }

        @Test
        void zeroCertaintyIsZeroRiskScore() {
            assertEquals(0.0, riskFor(uncertainty(0.0)));
        }

        @Test
        void missingUncertaintyGraphScoresMaximum() {
            assertEquals(1.0, riskFor(null));
        }

        @Test
        void everyCandidateSharesTheContextRisk() {
            TradeoffAnalysisSet set = engine.analyze(setOf(
                    candidate(AlternativeType.SEQUENTIAL, "s-1", CONCEPT_PER_STEP),
                    candidate(AlternativeType.ACCELERATED, "a-1", MERGED_STEPS)),
                    null, FOUR_VERIFIED, uncertainty(0.4));

            assertEquals(0.4, score(set, 0, TradeoffDimension.RISK));
            assertEquals(0.4, score(set, 1, TradeoffDimension.RISK));
        }
    }

    // ------------------------------------------------------------------
    // Canonical form, constraints and determinism
    // ------------------------------------------------------------------

    @Nested
    class CanonicalForm {

        @Test
        void emptyAlternativeSetYieldsEmptyAnalysisSet() {
            assertTrue(engine.analyze(AlternativeSet.empty(), null, FOUR_VERIFIED,
                    uncertainty(1.0)).isEmpty());
        }

        @Test
        void nullAlternativeSetYieldsEmptyAnalysisSet() {
            assertTrue(engine.analyze(null, null, FOUR_VERIFIED, uncertainty(1.0)).isEmpty());
        }

        @Test
        void oneAnalysisPerCandidateInInputOrder() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP),
                    candidate(AlternativeType.SEQUENTIAL, "c-b", MERGED_STEPS),
                    candidate(AlternativeType.PRACTICAL, "c-c", ONE_DENSE_STEP)), FOUR_VERIFIED);

            assertEquals(3, set.size());
            assertEquals("c-a", set.analyses().get(0).candidateId());
            assertEquals("c-b", set.analyses().get(1).candidateId());
            assertEquals("c-c", set.analyses().get(2).candidateId());
        }

        @Test
        void scoresFollowLockedDimensionOrder() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP)), FOUR_VERIFIED);

            assertEquals(
                    List.of(TradeoffDimension.TIME, TradeoffDimension.COMPLEXITY,
                            TradeoffDimension.PRACTICALITY, TradeoffDimension.LEARNING_DEPTH,
                            TradeoffDimension.RISK),
                    set.analyses().get(0).scores().stream()
                            .map(entry -> entry.dimension())
                            .toList());
        }

        @Test
        void analysisSetIsImmutable() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP)), FOUR_VERIFIED);

            assertThrows(UnsupportedOperationException.class,
                    () -> set.analyses().add(set.analyses().get(0)));
        }

        @Test
        void summaryIsNonBlankAndDeterministic() {
            AlternativeSet alternatives = setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP));

            String summary = analyze(alternatives, FOUR_VERIFIED).analyses().get(0).summary();

            assertNotNull(summary);
            assertFalse(summary.isBlank());
            assertEquals(summary,
                    analyze(alternatives, FOUR_VERIFIED).analyses().get(0).summary());
        }

        @Test
        void repeatedRunsAreStructurallyEqual() {
            AlternativeSet alternatives = setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP),
                    candidate(AlternativeType.ACCELERATED, "c-b", MERGED_STEPS));

            assertEquals(analyze(alternatives, FOUR_VERIFIED), analyze(alternatives, FOUR_VERIFIED));
        }
    }

    @Nested
    class ExplicitConstraints {

        @Test
        void constraintsDoNotChangeAnyScore() {
            AlternativeSet alternatives = setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP),
                    candidate(AlternativeType.ACCELERATED, "c-b", MERGED_STEPS));

            TradeoffAnalysisSet declared = engine.analyze(
                    alternatives, DECLARED_CONSTRAINTS, FOUR_VERIFIED, uncertainty(1.0));
            TradeoffAnalysisSet undeclared = engine.analyze(
                    alternatives, null, FOUR_VERIFIED, uncertainty(1.0));

            for (int index = 0; index < declared.size(); index++) {
                assertEquals(undeclared.analyses().get(index).scores(),
                        declared.analyses().get(index).scores());
            }
        }

        @Test
        void summaryRestatesExplicitConstraints() {
            TradeoffAnalysisSet set = engine.analyze(
                    setOf(candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP)),
                    DECLARED_CONSTRAINTS, FOUR_VERIFIED, uncertainty(1.0));

            String summary = set.analyses().get(0).summary();
            assertTrue(summary.contains("duration=30 days"));
            assertTrue(summary.contains("budget=$200"));
            assertTrue(summary.contains("experience=BEGINNER"));
            assertTrue(summary.contains("language=English"));
            assertTrue(summary.contains("outputPreference=ROADMAP"));
        }

        @Test
        void summaryOmitsUnsetConstraints() {
            TradeoffAnalysisSet set = analyze(setOf(
                    candidate(AlternativeType.BALANCED, "c-a", CONCEPT_PER_STEP)), FOUR_VERIFIED);

            assertFalse(set.analyses().get(0).summary().contains("constraints:"));
        }
    }
}