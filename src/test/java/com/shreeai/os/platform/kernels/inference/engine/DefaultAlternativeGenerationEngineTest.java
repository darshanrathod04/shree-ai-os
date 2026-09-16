package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic tests for the I1 {@link DefaultAlternativeGenerationEngine}.
 * Every test is fixed-input and side-effect free: identical inputs always
 * produce structurally identical {@link AlternativeSet}s.
 */
public class DefaultAlternativeGenerationEngineTest {

    private DefaultAlternativeGenerationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultAlternativeGenerationEngine();
    }

    // ------------------------------------------------------------------
    // Fixture helpers
    // ------------------------------------------------------------------

    private static ReasoningNode concept(String title) {
        return new ReasoningNode(title, title, ReasoningNodeType.CONCEPT, List.of());
    }

    private static ReasoningNode evidenceNode(String title) {
        return new ReasoningNode("ev-" + title, title, ReasoningNodeType.EVIDENCE, List.of());
    }

    private static ReasoningNode hypothesisNode(String title) {
        return new ReasoningNode("hyp-" + title, title, ReasoningNodeType.HYPOTHESIS, List.of());
    }

    private static ReasoningGraph graphOf(ReasoningNode... nodes) {
        return new ReasoningGraph(List.of(nodes), List.of(), List.of());
    }

    private static VerificationNode verifiedNode(String nodeId) {
        return new VerificationNode(nodeId, VerificationStatus.VERIFIED, List.of());
    }

    private static VerificationNode partialNode(String nodeId) {
        return new VerificationNode(nodeId, VerificationStatus.PARTIALLY_VERIFIED, List.of());
    }

    private static VerificationGraph verificationOf(VerificationNode... nodes) {
        return new VerificationGraph(List.of(nodes), List.of(), 1.0);
    }

    private static VerificationGraph allVerified(String... nodeIds) {
        List<VerificationNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            nodes.add(verifiedNode(nodeId));
        }
        return new VerificationGraph(nodes, List.of(), 1.0);
    }

    private static CausalGraph causal(String[][] edges) {
        TreeSet<String> titles = new TreeSet<>();
        for (String[] edge : edges) {
            titles.add(edge[0]);
            titles.add(edge[1]);
        }
        List<CausalNode> nodes = new ArrayList<>();
        for (String title : titles) {
            nodes.add(new CausalNode(title, title, CausalNodeType.INTERMEDIATE));
        }
        List<CausalEdge> causalEdges = new ArrayList<>();
        for (String[] edge : edges) {
            causalEdges.add(new CausalEdge(edge[0], edge[1], 0.8));
        }
        return new CausalGraph(nodes, causalEdges, List.of());
    }

    private static final SynthesisGraph EMPTY_SYNTHESIS =
            new SynthesisGraph(List.of(), List.of(), List.of());

    private static SynthesisGraph factsOf(String... statements) {
        List<SynthesizedFact> facts = new ArrayList<>();
        int index = 0;
        for (String statement : statements) {
            facts.add(new SynthesizedFact("fact-" + index++, statement, List.of("chunk"), 1.0));
        }
        return new SynthesisGraph(List.of(), facts, List.of());
    }

    /** Java → OOP → Collections → Streams. */
    private ReasoningGraph javaChainGraph() {
        return graphOf(concept("Java"), concept("OOP"), concept("Collections"), concept("Streams"));
    }

    private static final VerificationGraph JAVA_CHAIN_VERIFIED =
            allVerified("Java", "OOP", "Collections", "Streams");

    private static final CausalGraph JAVA_CHAIN_CAUSAL = causal(new String[][] {
            {"Java", "OOP"}, {"OOP", "Collections"}, {"Collections", "Streams"}});

    private AlternativeSet generateJavaChain() {
        return engine.generate(javaChainGraph(), EMPTY_SYNTHESIS, JAVA_CHAIN_CAUSAL,
                JAVA_CHAIN_VERIFIED);
    }

    // ------------------------------------------------------------------
    // Strategy variant generation
    // ------------------------------------------------------------------

    @Nested
    class StrategyGeneration {

        @Test
        void sequentialIsGenerated() {
            AlternativeSet set = generateJavaChain();
            assertTrue(set.alternatives().stream()
                    .anyMatch(candidate -> candidate.type() == AlternativeType.SEQUENTIAL));
        }

        @Test
        void acceleratedIsGenerated() {
            AlternativeSet set = generateJavaChain();
            assertTrue(set.alternatives().stream()
                    .anyMatch(candidate -> candidate.type() == AlternativeType.ACCELERATED));
        }

        @Test
        void practicalIsGenerated() {
            AlternativeSet set = generateJavaChain();
            assertTrue(set.alternatives().stream()
                    .anyMatch(candidate -> candidate.type() == AlternativeType.PRACTICAL));
        }

        @Test
        void theoreticalIsGenerated() {
            AlternativeSet set = generateJavaChain();
            assertTrue(set.alternatives().stream()
                    .anyMatch(candidate -> candidate.type() == AlternativeType.THEORETICAL));
        }

        @Test
        void balancedIsGenerated() {
            AlternativeSet set = generateJavaChain();
            assertTrue(set.alternatives().stream()
                    .anyMatch(candidate -> candidate.type() == AlternativeType.BALANCED));
        }

        @Test
        void exactlyFiveCandidatesForRichInput() {
            assertEquals(5, generateJavaChain().size());
        }

        @Test
        void atMostFiveCandidatesAlways() {
            assertEquals(5, generateJavaChain().size());
            assertEquals(5, engine.generate(graphOf(concept("Java")), EMPTY_SYNTHESIS,
                    null, allVerified("Java")).size());
        }

        @Test
        void oneCandidatePerStrategyType() {
            List<AlternativeType> types = generateJavaChain().alternatives().stream()
                    .map(AlternativeCandidate::type).toList();
            assertEquals(5, types.stream().distinct().count());
        }

        @Test
        void sequentialFollowsStrictPrerequisiteOrder() {
            AlternativeCandidate sequential = candidateOf(generateJavaChain(),
                    AlternativeType.SEQUENTIAL);
            assertEquals(List.of("Java", "OOP", "Collections", "Streams"),
                    titles(sequential));
        }

        @Test
        void acceleratedMergesIndependentBranches() {
            ReasoningGraph graph = graphOf(concept("Java"), concept("OOP"),
                    concept("JDBC"), concept("Collections"));
            CausalGraph causal = causal(new String[][] {
                    {"Java", "OOP"}, {"Java", "JDBC"}, {"OOP", "Collections"}});
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, causal,
                    allVerified("Java", "OOP", "JDBC", "Collections"));

            AlternativeCandidate accelerated = candidateOf(set, AlternativeType.ACCELERATED);
            assertEquals(List.of("Java", "JDBC + OOP", "Collections"), titles(accelerated));
        }

        @Test
        void acceleratedKeepsSingleChainUnmerged() {
            AlternativeCandidate accelerated = candidateOf(generateJavaChain(),
                    AlternativeType.ACCELERATED);
            assertEquals(List.of("Java", "OOP", "Collections", "Streams"),
                    titles(accelerated));
        }

        @Test
        void practicalIsProjectFirst() {
            AlternativeCandidate practical = candidateOf(generateJavaChain(),
                    AlternativeType.PRACTICAL);
            assertEquals("Java", practical.steps().get(0).title());
            assertEquals("Mini Project", practical.steps().get(1).title());
            assertEquals("Portfolio",
                    practical.steps().get(practical.stepCount() - 1).title());
        }

        @Test
        void practicalKeepsPrerequisiteOrderOfConcepts() {
            AlternativeCandidate practical = candidateOf(generateJavaChain(),
                    AlternativeType.PRACTICAL);
            assertEquals(List.of("Java", "Mini Project", "OOP", "Collections",
                    "Streams", "Portfolio"), titles(practical));
        }

        @Test
        void theoreticalIsConceptHeavy() {
            AlternativeCandidate theoretical = candidateOf(generateJavaChain(),
                    AlternativeType.THEORETICAL);
            assertEquals(List.of("Collections", "Java", "OOP", "Streams"),
                    titles(theoretical));
        }

        @Test
        void balancedMixesTheoryAndPractice() {
            AlternativeCandidate balanced = candidateOf(generateJavaChain(),
                    AlternativeType.BALANCED);
            assertEquals(List.of("Java", "Collections", "OOP", "Streams"),
                    titles(balanced));
        }

        @Test
        void stepsAreOneBasedAndSequential() {
            for (AlternativeCandidate candidate : generateJavaChain().alternatives()) {
                for (int i = 0; i < candidate.steps().size(); i++) {
                    assertEquals(i + 1, candidate.steps().get(i).order(),
                            candidate.type() + " step order must be 1..n");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Verified knowledge only
    // ------------------------------------------------------------------

    @Nested
    class VerifiedKnowledgeSelection {

        @Test
        void unverifiedConceptsAreExcluded() {
            ReasoningGraph graph = graphOf(concept("Java"), concept("OOP"));
            VerificationGraph verification = verificationOf(verifiedNode("Java"),
                    partialNode("OOP"));
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, null, verification);

            for (AlternativeCandidate candidate : set.alternatives()) {
                assertTrue(titles(candidate).stream().noneMatch(title -> title.contains("OOP")),
                        "PARTIALLY_VERIFIED knowledge must never generate steps");
            }
        }

        @Test
        void contradictedConceptsAreExcluded() {
            ReasoningGraph graph = graphOf(concept("Java"), concept("OOP"));
            VerificationGraph verification = verificationOf(verifiedNode("Java"),
                    new VerificationNode("OOP", VerificationStatus.CONTRADICTED, List.of()));
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, null, verification);

            assertEquals(5, set.size());
            for (AlternativeCandidate candidate : set.alternatives()) {
                assertTrue(titles(candidate).stream().noneMatch(title -> title.contains("OOP")));
            }
        }

        @Test
        void evidenceNodesAreNeverSteps() {
            ReasoningGraph graph = graphOf(concept("Java"), evidenceNode("Java Notes"));
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, null,
                    allVerified("Java", "ev-Java Notes"));

            for (AlternativeCandidate candidate : set.alternatives()) {
                assertTrue(titles(candidate).stream().noneMatch(title -> title.contains("Notes")));
            }
        }

        @Test
        void hypothesisNodesAreNeverSteps() {
            ReasoningGraph graph = graphOf(concept("Java"), hypothesisNode("Maybe Frameworks"));
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, null,
                    allVerified("Java", "hyp-Maybe Frameworks"));

            for (AlternativeCandidate candidate : set.alternatives()) {
                assertTrue(titles(candidate).stream().noneMatch(title -> title.contains("Maybe")));
            }
        }

        @Test
        void synthesizedFactsAreTheDeterministicFallback() {
            ReasoningGraph graph = graphOf(evidenceNode("Some evidence"));
            SynthesisGraph synthesis = factsOf("Trusted fact one", "Trusted fact two");
            AlternativeSet set = engine.generate(graph, synthesis, null, null);

            assertEquals(5, set.size());
            AlternativeCandidate sequential = candidateOf(set, AlternativeType.SEQUENTIAL);
            assertEquals(List.of("Trusted fact one", "Trusted fact two"), titles(sequential));
        }

        @Test
        void conceptsTakePrecedenceOverFacts() {
            ReasoningGraph graph = graphOf(concept("Java"));
            SynthesisGraph synthesis = factsOf("Some fact");
            AlternativeSet set = engine.generate(graph, synthesis, null,
                    allVerified("Java"));

            AlternativeCandidate sequential = candidateOf(set, AlternativeType.SEQUENTIAL);
            assertEquals(List.of("Java"), titles(sequential));
        }
    }

    // ------------------------------------------------------------------
    // Feasibility
    // ------------------------------------------------------------------

    @Nested
    class Feasibility {

        @Test
        void feasibilityWithinUnitRangeForEveryCandidate() {
            for (AlternativeCandidate candidate : generateJavaChain().alternatives()) {
                assertTrue(candidate.feasibility() >= 0.0 && candidate.feasibility() <= 1.0,
                        candidate.type() + " feasibility must be clamped to [0,1]");
            }
        }

        @Test
        void sequentialIsFullyFeasible() {
            assertEquals(1.0, candidateOf(generateJavaChain(),
                    AlternativeType.SEQUENTIAL).feasibility());
        }

        @Test
        void acceleratedIsFullyFeasible() {
            assertEquals(1.0, candidateOf(generateJavaChain(),
                    AlternativeType.ACCELERATED).feasibility());
        }

        @Test
        void theoreticalFeasibilityIsDeterministicRatio() {
            // Collections precedes its prerequisite OOP -> 3 of 4 steps complete.
            assertEquals(0.75, candidateOf(generateJavaChain(),
                    AlternativeType.THEORETICAL).feasibility());
        }

        @Test
        void balancedFeasibilityIsDeterministicRatio() {
            assertEquals(0.75, candidateOf(generateJavaChain(),
                    AlternativeType.BALANCED).feasibility());
        }

        @Test
        void feasibilityIsNotConfidenceAndNeverMinusOne() {
            for (AlternativeCandidate candidate : generateJavaChain().alternatives()) {
                assertNotEquals(-1.0, candidate.feasibility());
            }
        }

        @Test
        void cycleLeftoversKeepFeasibilityClamped() {
            ReasoningGraph graph = graphOf(concept("A"), concept("B"));
            CausalGraph cyclic = causal(new String[][] {{"A", "B"}, {"B", "A"}});
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, cyclic,
                    allVerified("A", "B"));

            for (AlternativeCandidate candidate : set.alternatives()) {
                assertTrue(candidate.feasibility() >= 0.0 && candidate.feasibility() <= 1.0);
            }
        }
    }

    // ------------------------------------------------------------------
    // Empty and degenerate inputs
    // ------------------------------------------------------------------

    @Nested
    class EmptyAndDegenerateInputs {

        @Test
        void emptyReasoningGraphReturnsEmptySet() {
            AlternativeSet set = engine.generate(
                    new ReasoningGraph(List.of(), List.of(), List.of()),
                    EMPTY_SYNTHESIS, null, null);
            assertTrue(set.isEmpty());
            assertEquals(0, set.size());
            assertEquals(AlternativeSet.empty(), set);
        }

        @Test
        void nullGraphsReturnEmptySet() {
            assertTrue(engine.generate(null, null, null, null).isEmpty());
        }

        @Test
        void noVerifiedConceptsReturnsEmptySet() {
            ReasoningGraph graph = graphOf(concept("Java"));
            VerificationGraph verification = verificationOf(partialNode("Java"));
            assertTrue(engine.generate(graph, EMPTY_SYNTHESIS, null, verification).isEmpty());
        }

        @Test
        void noConceptNodesReturnsEmptySetWithoutFacts() {
            ReasoningGraph graph = graphOf(evidenceNode("Just evidence"));
            assertTrue(engine.generate(graph, EMPTY_SYNTHESIS, null,
                    allVerified("ev-Just evidence")).isEmpty());
        }

        @Test
        void singleConceptProducesFiveCandidates() {
            AlternativeSet set = engine.generate(graphOf(concept("Java")),
                    EMPTY_SYNTHESIS, null, allVerified("Java"));
            assertEquals(5, set.size());
            for (AlternativeCandidate candidate : set.alternatives()) {
                assertEquals(1, candidate.stepCount());
                assertEquals("Java", candidate.steps().get(0).title());
            }
        }

        @Test
        void twoConceptsProduceFiveCandidates() {
            AlternativeSet set = engine.generate(graphOf(concept("Java"), concept("OOP")),
                    EMPTY_SYNTHESIS, causal(new String[][] {{"Java", "OOP"}}),
                    allVerified("Java", "OOP"));
            assertEquals(5, set.size());
        }

        @Test
        void causalNodesOutsideConceptSetAreIgnored() {
            ReasoningGraph graph = graphOf(concept("Java"), concept("OOP"));
            CausalGraph causal = causal(new String[][] {
                    {"Java", "OOP"}, {"Spring", "OOP"}, {"Java", "Hibernate"}});
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, causal,
                    allVerified("Java", "OOP", "Spring", "Hibernate"));

            AlternativeCandidate sequential = candidateOf(set, AlternativeType.SEQUENTIAL);
            assertEquals(List.of("Java", "OOP"), titles(sequential));
        }

        @Test
        void selfLoopCausalEdgesAreIgnored() {
            ReasoningGraph graph = graphOf(concept("Java"), concept("OOP"));
            CausalGraph causal = causal(new String[][] {{"Java", "OOP"}, {"Java", "Java"}});
            AlternativeSet set = engine.generate(graph, EMPTY_SYNTHESIS, causal,
                    allVerified("Java", "OOP"));

            AlternativeCandidate sequential = candidateOf(set, AlternativeType.SEQUENTIAL);
            assertEquals(List.of("Java", "OOP"), titles(sequential));
        }
    }

    // ------------------------------------------------------------------
    // Determinism and identity
    // ------------------------------------------------------------------

    @Nested
    class DeterminismAndIdentity {

        @Test
        void sameInputProducesIdenticalOutput() {
            AlternativeSet first = generateJavaChain();
            AlternativeSet second = generateJavaChain();
            assertEquals(first, second);
        }

        @Test
        void candidateIdsAreDeterministicSha256Hex() {
            for (AlternativeCandidate candidate : generateJavaChain().alternatives()) {
                assertEquals(64, candidate.candidateId().length(),
                        candidate.type() + " id must be a SHA-256 hex digest");
                assertTrue(candidate.candidateId().matches("[0-9a-f]{64}"),
                        candidate.type() + " id must be lowercase hex");
            }
        }

        @Test
        void candidateIdsDifferPerStrategy() {
            List<String> ids = generateJavaChain().alternatives().stream()
                    .map(AlternativeCandidate::candidateId).distinct().toList();
            assertEquals(5, ids.size());
        }

        @Test
        void differentInputsProduceDifferentCandidates() {
            AlternativeCandidate chain = candidateOf(generateJavaChain(),
                    AlternativeType.SEQUENTIAL);
            AlternativeSet single = engine.generate(graphOf(concept("Java")),
                    EMPTY_SYNTHESIS, null, allVerified("Java"));
            assertNotEquals(chain.candidateId(),
                    candidateOf(single, AlternativeType.SEQUENTIAL).candidateId());
        }

        @Test
        void independentEngineInstancesProduceIdenticalOutput() {
            DefaultAlternativeGenerationEngine other = new DefaultAlternativeGenerationEngine();
            assertEquals(generateJavaChain(),
                    other.generate(javaChainGraph(), EMPTY_SYNTHESIS, JAVA_CHAIN_CAUSAL,
                            JAVA_CHAIN_VERIFIED));
        }

        @Test
        void alternativeSetIsImmutable() {
            AlternativeSet set = generateJavaChain();
            assertThrows(UnsupportedOperationException.class,
                    () -> set.alternatives().add(set.alternatives().get(0)));
        }

        @Test
        void candidateStepsAreImmutable() {
            for (AlternativeCandidate candidate : generateJavaChain().alternatives()) {
                assertThrows(UnsupportedOperationException.class,
                        () -> candidate.steps().add(candidate.steps().get(0)));
            }
        }

        @Test
        void mutatedInputListDoesNotChangeResult() {
            List<ReasoningNode> nodes = new ArrayList<>();
            nodes.add(concept("Java"));
            nodes.add(concept("OOP"));
            ReasoningGraph graph = new ReasoningGraph(nodes, List.of(), List.of());
            AlternativeSet before = engine.generate(graph, EMPTY_SYNTHESIS, null,
                    allVerified("Java", "OOP"));
            nodes.add(concept("Streams"));
            AlternativeSet after = engine.generate(graph, EMPTY_SYNTHESIS, null,
                    allVerified("Java", "OOP"));
            assertEquals(before, after, "The engine must copy its inputs defensively");
        }
    }

    // ------------------------------------------------------------------
    // Stable ordering
    // ------------------------------------------------------------------

    @Nested
    class StableOrdering {

        @Test
        void lockedOutputOrderIsEnforced() {
            List<AlternativeType> types = generateJavaChain().alternatives().stream()
                    .map(AlternativeCandidate::type).toList();
            assertEquals(List.of(AlternativeType.BALANCED, AlternativeType.SEQUENTIAL,
                    AlternativeType.PRACTICAL, AlternativeType.ACCELERATED,
                    AlternativeType.THEORETICAL), types);
        }

        @Test
        void outputOrderIsNeverScoreOrder() {
            // Balanced/Theoretical (0.75) precede Sequential/Accelerated (1.0):
            // the set is never reordered by feasibility.
            List<Double> feasibilities = generateJavaChain().alternatives().stream()
                    .map(AlternativeCandidate::feasibility).toList();
            assertEquals(List.of(0.75, 1.0, 1.0, 1.0, 0.75), feasibilities);
        }

        @Test
        void orderingHoldsForDegenerateInput() {
            List<AlternativeType> types = engine.generate(graphOf(concept("Java")),
                    EMPTY_SYNTHESIS, null, allVerified("Java")).alternatives().stream()
                    .map(AlternativeCandidate::type).toList();
            assertEquals(List.of(AlternativeType.BALANCED, AlternativeType.SEQUENTIAL,
                    AlternativeType.PRACTICAL, AlternativeType.ACCELERATED,
                    AlternativeType.THEORETICAL), types);
        }

        @Test
        void orderingHoldsWithEmptyDag() {
            List<AlternativeType> types = engine.generate(javaChainGraph(),
                    EMPTY_SYNTHESIS, null, JAVA_CHAIN_VERIFIED).alternatives().stream()
                    .map(AlternativeCandidate::type).toList();
            assertEquals(List.of(AlternativeType.BALANCED, AlternativeType.SEQUENTIAL,
                    AlternativeType.PRACTICAL, AlternativeType.ACCELERATED,
                    AlternativeType.THEORETICAL), types);
        }
    }

    // ------------------------------------------------------------------
    // Model contracts
    // ------------------------------------------------------------------

    @Nested
    class ModelContracts {

        @Test
        void alternativeTypeHasLockedValues() {
            assertEquals(5, AlternativeType.values().length);
            assertEquals("SEQUENTIAL", AlternativeType.values()[0].name());
            assertEquals("ACCELERATED", AlternativeType.values()[1].name());
            assertEquals("PRACTICAL", AlternativeType.values()[2].name());
            assertEquals("THEORETICAL", AlternativeType.values()[3].name());
            assertEquals("BALANCED", AlternativeType.values()[4].name());
        }

        @Test
        void stepRejectsNonPositiveOrder() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeStep(0, "Java"));
        }

        @Test
        void stepRejectsBlankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeStep(1, " "));
        }

        @Test
        void candidateRejectsEmptySteps() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeCandidate("id", AlternativeType.SEQUENTIAL,
                            "Title", List.of(), 1.0));
        }

        @Test
        void candidateRejectsFeasibilityOutOfRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeCandidate("id", AlternativeType.SEQUENTIAL,
                            "Title", List.of(new AlternativeStep(1, "Java")), 1.5));
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeCandidate("id", AlternativeType.SEQUENTIAL,
                            "Title", List.of(new AlternativeStep(1, "Java")), -0.1));
        }

        @Test
        void candidateRejectsBlankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlternativeCandidate("id", AlternativeType.SEQUENTIAL,
                            " ", List.of(new AlternativeStep(1, "Java")), 1.0));
        }

        @Test
        void setRejectsNullCandidates() {
            assertThrows(NullPointerException.class, () -> new AlternativeSet(null));
        }

        @Test
        void emptySetIsCanonical() {
            AlternativeSet empty = AlternativeSet.empty();
            assertTrue(empty.isEmpty());
            assertEquals(0, empty.size());
            assertEquals(new AlternativeSet(List.of()), empty);
        }
    }

    // ------------------------------------------------------------------
    // CognitiveState integration
    // ------------------------------------------------------------------

    @Nested
    class CognitiveStateIntegration {

        @Test
        void alternativeSetIsStoredInCognitiveState() {
            AlternativeSet set = generateJavaChain();
            CognitiveState state = CognitiveState.empty().withAlternativeSet(set);
            assertEquals(set, state.alternativeSet());
        }

        @Test
        void withAlternativeSetRejectsNull() {
            assertThrows(NullPointerException.class,
                    () -> CognitiveState.empty().withAlternativeSet(null));
        }

        @Test
        void emptyStateHasNoAlternativeSet() {
            assertEquals(null, CognitiveState.empty().alternativeSet());
        }

        @Test
        void backwardCompatibleConstructorStillWorks() {
            CognitiveState state = new CognitiveState(
                    null, null, null, null, 0, List.of(), null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null);
            assertEquals(null, state.alternativeSet());
        }

        @Test
        void withAlternativeSetPreservesOtherArtifacts() {
            AlternativeSet set = generateJavaChain();
            CognitiveState state = CognitiveState.empty()
                    .withAlternativeSet(AlternativeSet.empty())
                    .withAlternativeSet(set);
            assertEquals(set, state.alternativeSet());
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static AlternativeCandidate candidateOf(AlternativeSet set, AlternativeType type) {
        return set.alternatives().stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing candidate: " + type));
    }

    private static List<String> titles(AlternativeCandidate candidate) {
        return candidate.steps().stream().map(AlternativeStep::title).toList();
    }
}
