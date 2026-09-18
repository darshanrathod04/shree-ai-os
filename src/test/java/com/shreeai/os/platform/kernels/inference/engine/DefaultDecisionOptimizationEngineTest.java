package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.OutputPreference;
import com.shreeai.os.platform.kernels.context.model.PlatformType;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.inference.model.DecisionDimension;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysis;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffDimension;
import com.shreeai.os.platform.kernels.inference.model.TradeoffScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic tests for the I3 {@link DefaultDecisionOptimizationEngine}.
 *
 * <p>Every test is fixed-input and side-effect free: identical inputs always
 * produce the same {@link OptimizedDecision}, the same score and the same
 * justifications.</p>
 */
public class DefaultDecisionOptimizationEngineTest {

    private DefaultDecisionOptimizationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultDecisionOptimizationEngine();
    }

    // ------------------------------------------------------------------
    // Fixture helpers
    // ------------------------------------------------------------------

    private static AlternativeCandidate candidate(String candidateId, int stepCount) {
        return candidate(candidateId, AlternativeType.BALANCED, stepCount);
    }

    private static AlternativeCandidate candidate(String candidateId,
                                                  AlternativeType type,
                                                  int stepCount) {
        List<AlternativeStep> steps = new ArrayList<>();
        for (int order = 1; order <= stepCount; order++) {
            steps.add(new AlternativeStep(order, "Step " + order));
        }
        return new AlternativeCandidate(candidateId, type,
                type.name() + " strategy (" + stepCount + " steps)", steps, 1.0);
    }

    private static AlternativeSet setOf(AlternativeCandidate... candidates) {
        return new AlternativeSet(List.of(candidates));
    }

    private static TradeoffAnalysisSet analysesOf(TradeoffAnalysis... analyses) {
        return new TradeoffAnalysisSet(List.of(analyses));
    }

    /** Explicit constraints with every field controlled. */
    private static UserConstraints constraintsOf(String duration,
                                                 String budget,
                                                 ExperienceLevel experience,
                                                 PlatformType platform,
                                                 String language,
                                                 OutputPreference outputPreference) {
        return UserConstraints.of(duration, budget, experience, platform, language,
                outputPreference, List.of());
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

    /** One analysis where only the given dimension scores {@code 1.0}. */
    private static TradeoffAnalysis onlyDimension(String candidateId, TradeoffDimension dimension) {
        return analysis(candidateId,
                dimension == TradeoffDimension.TIME ? 1.0 : 0.0,
                dimension == TradeoffDimension.COMPLEXITY ? 1.0 : 0.0,
                dimension == TradeoffDimension.PRACTICALITY ? 1.0 : 0.0,
                dimension == TradeoffDimension.LEARNING_DEPTH ? 1.0 : 0.0,
                dimension == TradeoffDimension.RISK ? 1.0 : 0.0);
    }

    /** An analysis where every locked dimension scores {@code 1.0}. */
    private static final TradeoffAnalysis ALL_ONES = analysis("c-1", 1.0, 1.0, 1.0, 1.0, 1.0);

    /** Scores a single-dimension candidate under the given constraints. */
    private double scoreOf(TradeoffDimension dimension, UserConstraints constraints) {
        return engine.decide(setOf(candidate("c-1", 2)),
                        analysesOf(onlyDimension("c-1", dimension)),
                        constraints, null, null)
                .optimizationScore();
    }

    /** Scores an all-ones candidate under the given constraints. */
    private double allOnesScore(UserConstraints constraints) {
        return engine.decide(setOf(candidate("c-1", 2)), analysesOf(ALL_ONES),
                constraints, null, null).optimizationScore();
    }

    // ------------------------------------------------------------------
    // Locked default weight profile
    // ------------------------------------------------------------------

    @Nested
    class DefaultWeightProfile {

        @Test
        void timeWeightIsOhTwo() {
            assertEquals(0.2, scoreOf(TradeoffDimension.TIME, null));
        }

        @Test
        void complexityWeightIsOhFifteen() {
            assertEquals(0.15, scoreOf(TradeoffDimension.COMPLEXITY, null));
        }

        @Test
        void practicalityWeightIsOhThree() {
            assertEquals(0.3, scoreOf(TradeoffDimension.PRACTICALITY, null));
        }

        @Test
        void learningDepthWeightIsOhFifteen() {
            assertEquals(0.15, scoreOf(TradeoffDimension.LEARNING_DEPTH, null));
        }

        @Test
        void riskWeightIsOhTwo() {
            assertEquals(0.2, scoreOf(TradeoffDimension.RISK, null));
        }

        @Test
        void emptyConstraintsUseTheDefaultProfile() {
            assertEquals(0.2, scoreOf(TradeoffDimension.TIME, UserConstraints.empty()));
        }

        @Test
        void perfectCandidateScoresOne() {
            assertEquals(1.0, allOnesScore(null));
        }
    }

    // ------------------------------------------------------------------
    // Explicit constraint overrides
    // ------------------------------------------------------------------

    @Nested
    class ConstraintWeightOverrides {

        @Test
        void durationRaisesTheTimeWeight() {
            double constrained = scoreOf(TradeoffDimension.TIME,
                    constraintsOf("30 days", null, null, null, null, null));

            // 0.30 / 1.10 = 0.2727
            assertEquals(0.2727, constrained);
            assertTrue(constrained > scoreOf(TradeoffDimension.TIME, null));
        }

        @Test
        void durationLowersTheUntouchedDimensions() {
            // 0.15 / 1.10 = 0.1364
            assertEquals(0.1364, scoreOf(TradeoffDimension.LEARNING_DEPTH,
                    constraintsOf("30 days", null, null, null, null, null)));
        }

        @Test
        void budgetRaisesTheRiskWeight() {
            // 0.25 / 1.05 = 0.2381
            assertEquals(0.2381, scoreOf(TradeoffDimension.RISK,
                    constraintsOf(null, "$200", null, null, null, null)));
        }

        @Test
        void experienceRaisesTheLearningDepthWeight() {
            // 0.20 / 1.05 = 0.1905
            assertEquals(0.1905, scoreOf(TradeoffDimension.LEARNING_DEPTH,
                    constraintsOf(null, null, ExperienceLevel.BEGINNER, null, null, null)));
        }

        @Test
        void platformRaisesTheComplexityWeight() {
            // 0.20 / 1.05 = 0.1905
            assertEquals(0.1905, scoreOf(TradeoffDimension.COMPLEXITY,
                    constraintsOf(null, null, null, PlatformType.WINDOWS, null, null)));
        }

        @Test
        void outputPreferenceRaisesThePracticalityWeight() {
            // 0.35 / 1.05 = 0.3333
            assertEquals(0.3333, scoreOf(TradeoffDimension.PRACTICALITY,
                    constraintsOf(null, null, null, null, null, OutputPreference.ROADMAP)));
        }

        @Test
        void languageCarriesNoWeight() {
            assertEquals(0.2, scoreOf(TradeoffDimension.TIME,
                    constraintsOf(null, null, null, null, "English", null)));
        }

        @Test
        void everyConstraintTogetherStillSumsToOne() {
            UserConstraints all = constraintsOf("30 days", "$200", ExperienceLevel.BEGINNER,
                    PlatformType.WINDOWS, "English", OutputPreference.ROADMAP);

            assertEquals(1.0, allOnesScore(all));
        }
    }

    // ------------------------------------------------------------------
    // Selection - the highest locked optimization score wins
    // ------------------------------------------------------------------

    @Nested
    class Selection {

        @Test
        void highestScoreIsSelected() {
            AlternativeSet alternatives = setOf(
                    candidate("c-a", 2), candidate("c-b", 2), candidate("c-c", 2));
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-a", 0.4, 0.4, 0.4, 0.4, 0.4),
                    analysis("c-b", 1.0, 1.0, 1.0, 1.0, 1.0),
                    analysis("c-c", 0.6, 0.6, 0.6, 0.6, 0.6));

            OptimizedDecision decision = engine.decide(alternatives, analyses, null, null, null);

            assertEquals("c-b", decision.candidateId());
            assertEquals(1.0, decision.optimizationScore());
        }

        @Test
        void selectionDoesNotDependOnInputOrder() {
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-a", 0.4, 0.4, 0.4, 0.4, 0.4),
                    analysis("c-b", 1.0, 1.0, 1.0, 1.0, 1.0));

            assertEquals("c-b", engine.decide(
                    setOf(candidate("c-a", 2), candidate("c-b", 2)), analyses, null, null, null)
                    .candidateId());
            assertEquals("c-b", engine.decide(
                    setOf(candidate("c-b", 2), candidate("c-a", 2)), analyses, null, null, null)
                    .candidateId());
        }

        @Test
        void candidateWithoutAnalysisIsIgnored() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-a", 2), candidate("c-b", 2)),
                    analysesOf(analysis("c-b", 0.5, 0.5, 0.5, 0.5, 0.5)),
                    null, null, null);

            assertEquals("c-b", decision.candidateId());
        }

        @Test
        void selectedStrategyMatchesTheCandidate() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-a", AlternativeType.PRACTICAL, 3)),
                    analysesOf(analysis("c-a", 1.0, 1.0, 1.0, 1.0, 1.0)),
                    null, null, null);

            assertEquals(AlternativeType.PRACTICAL, decision.strategy());
        }

        @Test
        void declaredDurationCanChangeTheWinner() {
            AlternativeSet alternatives = setOf(candidate("p", 2), candidate("t", 2));
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("p", 0.0, 0.0, 0.9, 0.0, 0.0),
                    analysis("t", 1.0, 0.0, 0.0, 0.0, 0.0));

            // Locked default profile: practicality 0.9 x 0.30 = 0.3000 wins.
            assertEquals("p", engine.decide(alternatives, analyses, null, null, null).candidateId());

            // A declared duration raises the time weight to 0.30 / 1.10, so the
            // time-only candidate overtakes the practicality candidate.
            OptimizedDecision decision = engine.decide(alternatives, analyses,
                    constraintsOf("30 days", null, null, null, null, null), null, null);

            assertEquals("t", decision.candidateId());
            assertEquals(0.2727, decision.optimizationScore());
        }
    }

    // ------------------------------------------------------------------
    // Locked deterministic tie resolution
    // ------------------------------------------------------------------

    @Nested
    class TieResolution {

        @Test
        void equalScoresPreferHigherPracticality() {
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-a", 0.0, 0.0, 1.0, 0.0, 0.0),
                    analysis("c-b", 0.0, 0.0, 0.5, 1.0, 0.0));

            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-a", 2), candidate("c-b", 2)), analyses, null, null, null);

            assertEquals(0.3, decision.optimizationScore());
            assertEquals("c-a", decision.candidateId());
        }

        @Test
        void equalScoresAndPracticalityPreferHigherLearningDepth() {
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-a", 0.0, 0.0, 0.5, 1.0, 0.0),
                    analysis("c-b", 0.75, 0.0, 0.5, 0.0, 0.0));

            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-a", 2), candidate("c-b", 2)), analyses, null, null, null);

            assertEquals(0.3, decision.optimizationScore());
            assertEquals("c-a", decision.candidateId());
        }

        @Test
        void equalScoresPreferFewerSteps() {
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-long", 0.5, 0.5, 0.5, 0.5, 0.5),
                    analysis("c-short", 0.5, 0.5, 0.5, 0.5, 0.5));

            assertEquals("c-short", engine.decide(
                    setOf(candidate("c-long", 5), candidate("c-short", 2)),
                    analyses, null, null, null).candidateId());
        }

        @Test
        void equalScoresPreferAscendingCandidateId() {
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("zebra", 0.5, 0.5, 0.5, 0.5, 0.5),
                    analysis("alpha", 0.5, 0.5, 0.5, 0.5, 0.5));

            assertEquals("alpha", engine.decide(
                    setOf(candidate("zebra", 2), candidate("alpha", 2)),
                    analyses, null, null, null).candidateId());
        }

        @Test
        void repeatedRunsSelectTheSameCandidate() {
            AlternativeSet alternatives = setOf(candidate("c-a", 2), candidate("c-b", 2));
            TradeoffAnalysisSet analyses = analysesOf(
                    analysis("c-a", 0.5, 0.5, 0.5, 0.5, 0.5),
                    analysis("c-b", 0.5, 0.5, 0.5, 0.5, 0.5));

            assertEquals(engine.decide(alternatives, analyses, null, null, null),
                    engine.decide(alternatives, analyses, null, null, null));
        }
    }

    // ------------------------------------------------------------------
    // Deterministic justifications
    // ------------------------------------------------------------------

    @Nested
    class Justifications {

        @Test
        void oneJustificationPerDimension() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)), analysesOf(ALL_ONES), null, null, null);

            assertEquals(DecisionDimension.values().length, decision.justifications().size());
        }

        @Test
        void justificationsFollowLockedDimensionOrder() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)), analysesOf(ALL_ONES), null, null, null);

            assertEquals(List.of(DecisionDimension.TIME, DecisionDimension.COMPLEXITY,
                            DecisionDimension.PRACTICALITY, DecisionDimension.LEARNING_DEPTH,
                            DecisionDimension.RISK),
                    decision.justifications().stream()
                            .map(entry -> entry.dimension())
                            .toList());
        }

        @Test
        void highScoresAreJustifiedAsStrengths() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)), analysesOf(ALL_ONES), null, null, null);

            assertTrue(decision.justifications().stream()
                    .allMatch(entry -> entry.reason().contains("is a strength")));
        }

        @Test
        void midScoresAreJustifiedAsAdequate() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)),
                    analysesOf(analysis("c-1", 0.6, 0.6, 0.6, 0.6, 0.6)), null, null, null);

            assertTrue(decision.justifications().stream()
                    .allMatch(entry -> entry.reason().contains("is adequate")));
        }

        @Test
        void lowScoresAreJustifiedAsLimited() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)),
                    analysesOf(analysis("c-1", 0.4, 0.4, 0.4, 0.4, 0.4)), null, null, null);

            assertTrue(decision.justifications().stream()
                    .allMatch(entry -> entry.reason().contains("is limited")));
        }

        @Test
        void contributionsSumToOneForAPositiveScore() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)),
                    analysesOf(analysis("c-1", 1.0, 0.5, 0.5, 0.5, 0.5)), null, null, null);

            double sum = decision.justifications().stream()
                    .mapToDouble(entry -> entry.contribution())
                    .sum();

            assertEquals(1.0, sum, 0.001);
        }

        @Test
        void zeroScoreYieldsZeroContributions() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)),
                    analysesOf(analysis("c-1", 0.0, 0.0, 0.0, 0.0, 0.0)), null, null, null);

            assertTrue(decision.justifications().stream()
                    .allMatch(entry -> entry.contribution() == 0.0));
        }

        @Test
        void justificationsAreImmutable() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)), analysesOf(ALL_ONES), null, null, null);

            assertThrows(UnsupportedOperationException.class,
                    () -> decision.justifications().clear());
        }

        @Test
        void justificationsAreDeterministic() {
            AlternativeSet alternatives = setOf(candidate("c-1", 2));
            TradeoffAnalysisSet analyses = analysesOf(analysis("c-1", 1.0, 0.5, 0.5, 0.5, 0.5));

            assertEquals(engine.decide(alternatives, analyses, null, null, null).justifications(),
                    engine.decide(alternatives, analyses, null, null, null).justifications());
        }
    }

    // ------------------------------------------------------------------
    // Contract - empty decisions, rounding and identity
    // ------------------------------------------------------------------

    @Nested
    class Contract {

        @Test
        void nullAlternativeSetIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.decide(null, analysesOf(ALL_ONES), null, null, null));
        }

        @Test
        void emptyAlternativeSetIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.decide(AlternativeSet.empty(), analysesOf(ALL_ONES),
                            null, null, null));
        }

        @Test
        void nullAnalysisSetIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.decide(setOf(candidate("c-1", 2)), null, null, null, null));
        }

        @Test
        void emptyAnalysisSetIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.decide(setOf(candidate("c-1", 2)), TradeoffAnalysisSet.empty(),
                            null, null, null));
        }

        @Test
        void unmatchedCandidateIdIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.decide(setOf(candidate("c-1", 2)),
                            analysesOf(analysis("other", 1.0, 1.0, 1.0, 1.0, 1.0)),
                            null, null, null));
        }

        @Test
        void scoreIsRoundedToFourDecimals() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)),
                    analysesOf(analysis("c-1", 0.3333, 0.0, 0.0, 0.0, 0.0)), null, null, null);

            // 0.3333 x 0.20 = 0.06666 -> 0.0667
            assertEquals(0.0667, decision.optimizationScore());
        }

        @Test
        void decisionIsNeverNull() {
            assertNotNull(engine.decide(setOf(candidate("c-1", 2)), analysesOf(ALL_ONES),
                    null, null, null));
        }

        @Test
        void decisionCarriesSelectedCandidateIdentity() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", AlternativeType.SEQUENTIAL, 2)),
                    analysesOf(ALL_ONES), null, null, null);

            assertEquals("c-1", decision.candidateId());
            assertEquals(AlternativeType.SEQUENTIAL, decision.strategy());
        }

        @Test
        void verificationAndUncertaintyGraphsAreNotRequired() {
            OptimizedDecision decision = engine.decide(
                    setOf(candidate("c-1", 2)), analysesOf(ALL_ONES), null, null, null);

            assertEquals(1.0, decision.optimizationScore());
        }
    }
}