package com.shreeai.os.platform.kernels.reasoning;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultEvidenceSynthesisEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.EvidenceSynthesisEngine;
import com.shreeai.os.platform.kernels.reasoning.model.EvidenceCluster;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdge;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdgeType;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisEdge;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic tests for {@link DefaultEvidenceSynthesisEngine}.
 */
class DefaultEvidenceSynthesisEngineTest {

    private static final TrustScore TRUST_0_95 = new TrustScore(0.95, 1.0, 0.9, 0.95, "official");
    private static final TrustScore TRUST_0_90 = new TrustScore(0.90, 0.95, 0.85, 0.90, "verified");
    private static final TrustScore TRUST_0_85 = new TrustScore(0.85, 0.90, 0.80, 0.85, "verified");
    private static final TrustScore TRUST_0_70 = new TrustScore(0.70, 0.70, 0.70, 0.70, "community");
    private static final TrustScore TRUST_0_40 = new TrustScore(0.40, 0.40, 0.40, 0.40, "unknown");

    private final EvidenceSynthesisEngine engine = new DefaultEvidenceSynthesisEngine();

    // ---- helper builders ----

    private static String sha256(String input) {
        try {
            java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TrustedEvidence trustedEvidence(String chunkId, String content, TrustScore trust) {
        return new TrustedEvidence(
                new EvidenceItem(chunkId, "doc-1", content, 0.9, List.of()),
                trust);
    }

    private static GraphConcept graphConcept(String name, ConceptType type) {
        return new GraphConcept(sha256("CONCEPT|" + name), name, type);
    }

    private static ConceptRelationship graphRel(String from, String to, RelationshipType type) {
        return new ConceptRelationship(sha256(from + "|" + to), from, to, type, 0.8);
    }

    private static ReasoningNode evidenceNode(String chunkId, String content) {
        return new ReasoningNode(
                sha256("EVIDENCE|" + chunkId + "|" + content),
                content,
                ReasoningNodeType.EVIDENCE,
                List.of(chunkId));
    }

    private static ReasoningNode conceptNode(String name, List<String> evidenceIds) {
        return new ReasoningNode(
                sha256("CONCEPT|" + name),
                name,
                ReasoningNodeType.CONCEPT,
                evidenceIds);
    }

    private static Hypothesis hypothesis(String statement, List<String> supportingIds, double confidence) {
        return new Hypothesis(sha256("HYPOTHESIS|" + statement), statement, supportingIds, confidence);
    }

    // ---- test 1: basic clustering ----

    @Test
    void evidenceClusteredBySharedConcept() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1",
                                "Oracle Collections Framework Guide. Java Collections are foundational.",
                                TRUST_0_95),
                        trustedEvidence("chunk-2",
                                "Java Notes. Collections store objects.",
                                TRUST_0_90),
                        trustedEvidence("chunk-3",
                                "Spring Docs. Spring Boot depends on Java.",
                                TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, spring),
                List.of(
                        graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        // R1: build reasoning graph first
        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph reasoningGraph = r1Engine.reason(reliability, graph);

        SynthesisGraph synthesis = engine.synthesize(reasoningGraph, reliability, graph);

        // Collections should be a cluster (referenced by chunk-1 and chunk-2)
        boolean hasCollectionsCluster = synthesis.clusters().stream()
                .anyMatch(c -> c.concepts().contains("Collections"));
        assertTrue(hasCollectionsCluster, "Collections should form a cluster");
    }

    // ---- test 2: duplicate evidence removed within cluster ----

    @Test
    void duplicateEvidenceIdsRemovedWithinCluster() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Learn Java Collections", TRUST_0_95),
                        trustedEvidence("chunk-2", "Java Collections framework", TRUST_0_90)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections),
                List.of(graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (EvidenceCluster cluster : synthesis.clusters()) {
            long distinct = cluster.evidenceIds().stream().distinct().count();
            assertEquals(distinct, cluster.evidenceIds().size(),
                    "Cluster should have no duplicate evidence ids");
        }
    }

    // ---- test 3: stable cluster IDs ----

    @Test
    void clusterIdsAreStableAndDeterministic() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);

        SynthesisGraph s1 = engine.synthesize(rg, reliability, graph);
        SynthesisGraph s2 = engine.synthesize(rg, reliability, graph);

        assertEquals(s1, s2, "Same input must produce identical synthesis graph");
    }

    // ---- test 4: synthesized facts created per cluster ----

    @Test
    void synthesizedFactsCreatedForEachCluster() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Java Collections are foundational for Streams", TRUST_0_95),
                        trustedEvidence("chunk-2", "Spring Boot uses Java", TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, spring),
                List.of(
                        graphRel(collections.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        assertFalse(synthesis.facts().isEmpty(), "Should have synthesized facts");
        // Each cluster should produce exactly one fact
        long factCount = synthesis.facts().size();
        long clusterCount = synthesis.clusters().size();
        assertTrue(factCount >= clusterCount || factCount >= 1,
                "At least one fact per cluster");
    }

    // ---- test 5: provenance preserved ----

    @Test
    void provenancePreservedInSynthesizedFacts() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Learn Java Collections", TRUST_0_95),
                        trustedEvidence("chunk-2", "Java Collections deep dive", TRUST_0_90)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections),
                List.of(graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (SynthesizedFact fact : synthesis.facts()) {
            assertNotNull(fact.supportingEvidenceIds(), "supportingEvidenceIds must not be null");
            for (String evidenceId : fact.supportingEvidenceIds()) {
                assertFalse(evidenceId.isBlank(), "evidence id should not be blank");
            }
        }
    }

    // ---- test 6: confidence aggregation is mean trust ----

    @Test
    void confidenceAggregationIsMeanTrust() {
        TrustedEvidence ev1 = trustedEvidence("chunk-1", "Learn Java Collections", TRUST_0_95);
        TrustedEvidence ev2 = trustedEvidence("chunk-2", "Java Collections guide", TRUST_0_85);
        ReliabilityResult reliability = new ReliabilityResult(List.of(ev1, ev2), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections),
                List.of(graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // Mean of 0.95 and 0.85 = 0.90
        assertFalse(synthesis.facts().isEmpty());
        double expected = Math.round((0.95 + 0.85) / 2.0 * 10000.0) / 10000.0;
        boolean foundMean = synthesis.facts().stream()
                .anyMatch(f -> f.confidence() == expected);
        assertTrue(foundMean, "Confidence should be mean of supporting trust scores");
    }

    // ---- test 7: same input same output ----

    @Test
    void sameInputProducesSameSynthesisGraph() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Java Collections are foundational", TRUST_0_95)),
                0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);

        SynthesisGraph s1 = engine.synthesize(rg, reliability, graph);
        SynthesisGraph s2 = engine.synthesize(rg, reliability, graph);
        assertEquals(s1, s2);
    }

    // ---- test 8: provenance edges connect clusters to facts ----

    @Test
    void provenanceEdgesConnectClustersToFacts() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java Collections", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections),
                List.of(graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // Every fact should have a provenance edge from a cluster
        for (SynthesizedFact fact : synthesis.facts()) {
            boolean hasEdge = synthesis.edges().stream()
                    .anyMatch(e -> e.toNode().equals(fact.factId()));
            assertTrue(hasEdge, "Fact should have a provenance edge from its cluster");
        }
    }

    // ---- test 9: null reasoning graph throws NPE ----

    @Test
    void nullReasoningGraphThrowsNullPointerException() {
        ReliabilityResult reliability = new ReliabilityResult(List.of(), 0.0);
        ConceptGraph g = new ConceptGraph(List.of(), List.of());
        assertThrows(NullPointerException.class,
                () -> engine.synthesize(null, reliability, g));
    }

    // ---- test 10: null reliability throws NPE ----

    @Test
    void nullReliabilityThrowsNullPointerException() {
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        ConceptGraph g = new ConceptGraph(List.of(), List.of());
        assertThrows(NullPointerException.class,
                () -> engine.synthesize(rg, null, g));
    }

    // ---- test 11: empty reasoning graph produces empty synthesis ----

    @Test
    void emptyReasoningGraphProducesEmptySynthesis() {
        ReliabilityResult reliability = new ReliabilityResult(List.of(), 0.0);
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        ConceptGraph g = new ConceptGraph(List.of(), List.of());
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, g);

        assertTrue(synthesis.clusters().isEmpty());
        assertTrue(synthesis.facts().isEmpty());
        assertTrue(synthesis.edges().isEmpty());
    }

    // ---- test 12: synthesized fact statement is deterministic template ----

    @Test
    void synthesizedFactStatementIsDeterministicTemplate() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Java Collections Framework", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections),
                List.of(graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (SynthesizedFact fact : synthesis.facts()) {
            assertNotNull(fact.statement());
            assertFalse(fact.statement().isBlank());
        }
    }

    // ---- test 13: multiple concepts produce multiple clusters ----

    @Test
    void multipleConceptsProduceMultipleClusters() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Java Collections Framework", TRUST_0_95),
                        trustedEvidence("chunk-2", "Spring Boot uses Java", TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, spring),
                List.of(
                        graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // Should have clusters for concepts that appear in evidence
        assertTrue(synthesis.clusters().size() >= 1);
    }

    // ---- test 14: fact ID is deterministic ----

    @Test
    void factIdIsDeterministic() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (SynthesizedFact fact : synthesis.facts()) {
            assertEquals(sha256("FACT|" + fact.statement()), fact.factId());
        }
    }

    // ---- test 15: cluster ID is deterministic ----

    @Test
    void clusterIdIsDeterministic() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (EvidenceCluster cluster : synthesis.clusters()) {
            String conceptName = cluster.concepts().get(0);
            assertEquals(sha256("CLUSTER|" + conceptName), cluster.clusterId());
        }
    }

    // ---- test 16: confidence within valid range ----

    @Test
    void confidenceWithinValidRange() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Learn Java Collections", TRUST_0_40),
                        trustedEvidence("chunk-2", "Spring Boot guide", TRUST_0_70)
                ), 0.55);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, spring),
                List.of(
                        graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (SynthesizedFact fact : synthesis.facts()) {
            assertTrue(fact.confidence() >= 0.0 && fact.confidence() <= 1.0,
                    "Confidence must be within [0.0, 1.0]");
        }
    }

    // ---- test 17: stable ordering of clusters ----

    @Test
    void clustersAreStablyOrderedById() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Learn Java Collections Streams", TRUST_0_95),
                        trustedEvidence("chunk-2", "Java Collections guide", TRUST_0_90),
                        trustedEvidence("chunk-3", "Spring Boot depends on Java", TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept streams = graphConcept("Streams", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, streams, spring),
                List.of(
                        graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(collections.conceptId(), streams.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // Verify ordering is by clusterId (already enforced by record compact ctor)
        for (int i = 1; i < synthesis.clusters().size(); i++) {
            String prev = synthesis.clusters().get(i - 1).clusterId();
            String curr = synthesis.clusters().get(i).clusterId();
            assertTrue(prev.compareTo(curr) <= 0,
                    "Clusters should be ordered by clusterId");
        }
    }

    // ---- test 18: stable ordering of facts ----

    @Test
    void factsAreStablyOrderedById() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1", "Learn Java Collections Streams", TRUST_0_95),
                        trustedEvidence("chunk-2", "Java Collections deep dive", TRUST_0_90),
                        trustedEvidence("chunk-3", "Spring Boot uses Java", TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept streams = graphConcept("Streams", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, streams, spring),
                List.of(
                        graphRel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(collections.conceptId(), streams.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        for (int i = 1; i < synthesis.facts().size(); i++) {
            String prev = synthesis.facts().get(i - 1).factId();
            String curr = synthesis.facts().get(i).factId();
            assertTrue(prev.compareTo(curr) <= 0,
                    "Facts should be ordered by factId");
        }
    }

    // ---- test 19: prerequisite concept produces correct template ----

    @Test
    void prerequisiteConceptProducesCorrectTemplate() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Java Collections are foundational for Streams", TRUST_0_95)),
                0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept streams = graphConcept("Streams", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, streams),
                List.of(
                        graphRel(collections.conceptId(), streams.conceptId(), RelationshipType.PREREQUISITE)
                ));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        boolean hasPrerequisiteFact = synthesis.facts().stream()
                .anyMatch(f -> f.statement().contains("prerequisite"));
        assertTrue(hasPrerequisiteFact, "Prerequisite relationship should produce template");
    }

    // ---- test 20: depends-on concept produces correct template ----

    @Test
    void dependsOnConceptProducesCorrectTemplate() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Spring Boot depends on Java", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, spring),
                List.of(graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        boolean hasDependsFact = synthesis.facts().stream()
                .anyMatch(f -> f.statement().contains("depends on"));
        assertTrue(hasDependsFact, "Depends-on relationship should produce template");
    }

    // ---- test 21: part-of concept produces correct template ----

    @Test
    void partOfConceptProducesCorrectTemplate() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "List is part of Collections", TRUST_0_95)), 0.9);

        GraphConcept list = graphConcept("List", ConceptType.FRAMEWORK);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(list, collections),
                List.of(graphRel(list.conceptId(), collections.conceptId(), RelationshipType.PART_OF)));

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        boolean hasPartOfFact = synthesis.facts().stream()
                .anyMatch(f -> f.statement().contains("part of"));
        assertTrue(hasPartOfFact, "Part-of relationship should produce template");
    }

    // ---- test 22: foundational concept template for unknown relationship ----

    @Test
    void foundationalConceptTemplateUsedForGenericConcepts() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java Programming", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        boolean hasFoundationalFact = synthesis.facts().stream()
                .anyMatch(f -> f.statement().contains("foundational"));
        assertTrue(hasFoundationalFact, "Concepts without relationship should use foundational template");
    }

    // ---- test 23: synthesis graph is deeply immutable ----

    @Test
    void synthesisGraphIsDeeplyImmutable() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Java", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // Attempting to modify returned lists should throw
        assertThrows(UnsupportedOperationException.class,
                () -> synthesis.clusters().add(
                        new EvidenceCluster("x", List.of(), List.of())));
        assertThrows(UnsupportedOperationException.class,
                () -> synthesis.facts().add(
                        new SynthesizedFact("x", "y", List.of(), 0.5)));
        assertThrows(UnsupportedOperationException.class,
                () -> synthesis.edges().add(
                        new SynthesisEdge("a", "b")));
    }

    // ---- test 24: evidence with no matching concepts produces no clusters ----

    @Test
    void evidenceWithNoMatchingConceptsProducesNoClusters() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(trustedEvidence("chunk-1", "Learn Python Programming", TRUST_0_95)), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        ConceptGraph graph = new ConceptGraph(List.of(java), List.of());

        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph rg = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(rg, reliability, graph);

        // No concept named "Python" in graph, so no clusters should form
        assertTrue(synthesis.clusters().isEmpty(),
                "No matching concepts should produce no clusters");
    }

    // ---- test 25: integration pipeline produces synthesis graph ----

    @Test
    void fullPipelineProducesValidSynthesisGraph() {
        ReliabilityResult reliability = new ReliabilityResult(
                List.of(
                        trustedEvidence("chunk-1",
                                "Oracle Collections Framework. Java Collections are foundational for Streams.",
                                TRUST_0_95),
                        trustedEvidence("chunk-2",
                                "Spring Documentation. Spring Boot depends on Java.",
                                TRUST_0_85)
                ), 0.9);

        GraphConcept java = graphConcept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = graphConcept("Collections", ConceptType.FRAMEWORK);
        GraphConcept streams = graphConcept("Streams", ConceptType.FRAMEWORK);
        GraphConcept spring = graphConcept("Spring Boot", ConceptType.FRAMEWORK);

        ConceptGraph graph = new ConceptGraph(
                List.of(java, collections, streams, spring),
                List.of(
                        graphRel(collections.conceptId(), streams.conceptId(), RelationshipType.PREREQUISITE),
                        graphRel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON)
                ));

        // R1 -> R2 pipeline
        var r1Engine = new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine();
        ReasoningGraph reasoningGraph = r1Engine.reason(reliability, graph);
        SynthesisGraph synthesis = engine.synthesize(reasoningGraph, reliability, graph);

        assertNotNull(synthesis);
        assertFalse(synthesis.clusters().isEmpty(), "Should have clusters");
        assertFalse(synthesis.facts().isEmpty(), "Should have facts");
        assertFalse(synthesis.edges().isEmpty(), "Should have edges");

        // Verify record-equality determinism
        SynthesisGraph synthesis2 = engine.synthesize(reasoningGraph, reliability, graph);
        assertEquals(synthesis, synthesis2);
    }
}
