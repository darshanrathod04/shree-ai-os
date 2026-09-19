package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.reasoning.model.EvidenceCluster;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisEdge;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultEvidenceSynthesisEngine</b>
 *
 * <p>The R2 implementation of {@link EvidenceSynthesisEngine}. Converts a
 * {@link ReasoningGraph}, the original {@link ReliabilityResult} and the
 * {@link ConceptGraph} into an immutable {@link SynthesisGraph} through a
 * deterministic five-stage pipeline:</p>
 * <ol>
 *   <li><b>Cluster evidence</b> - group trusted evidence by shared graph
 *       concepts; one concept becomes one cluster.</li>
 *   <li><b>Merge supporting evidence</b> - within each cluster, remove
 *       duplicate evidence ids and preserve original evidence with stable
 *       ordering.</li>
 *   <li><b>Create synthesized facts</b> - each cluster produces one
 *       deterministic synthesized fact via a locked template selected from
 *       the concept graph relationships.</li>
 *   <li><b>Confidence aggregation</b> - confidence is the mean trust of
 *       supporting evidence.</li>
 *   <li><b>Build synthesis graph</b> - immutable, stably ordered
 *       {@link SynthesisGraph}.</li>
 * </ol>
 *
 * <p>The engine is stateless, thread-safe, deterministic and contains no
 * LLM, no embeddings and no NLP libraries. Identical inputs always produce
 * structurally equal synthesis graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultEvidenceSynthesisEngine implements EvidenceSynthesisEngine {

    @Override
    public SynthesisGraph synthesize(ReasoningGraph reasoningGraph,
                                     ReliabilityResult reliability,
                                     ConceptGraph conceptGraph) {
        Objects.requireNonNull(reasoningGraph, "reasoningGraph must not be null");
        Objects.requireNonNull(reliability, "reliability must not be null");
        Objects.requireNonNull(conceptGraph, "conceptGraph must not be null");

        List<TrustedEvidence> evidenceList = reliability.evidence();

        // Build lookup: chunkId -> TrustedEvidence for confidence aggregation
        Map<String, TrustedEvidence> evidenceByChunkId = new HashMap<>();
        for (TrustedEvidence te : evidenceList) {
            evidenceByChunkId.put(te.evidence().chunkId(), te);
        }

        // Extract concept nodes from the reasoning graph
        Map<String, ReasoningNode> conceptNodes = new HashMap<>();
        for (ReasoningNode node : reasoningGraph.nodes()) {
            if (node.type() == ReasoningNodeType.CONCEPT) {
                conceptNodes.put(node.title(), node);
            }
        }

        // Build concept graph relationship lookup: fromConceptName -> list of (toConceptName, type)
        Map<String, List<ConceptRelationship>> relationshipsByFrom = new HashMap<>();
        Map<String, GraphConcept> conceptsById = new HashMap<>();
        for (GraphConcept gc : conceptGraph.concepts()) {
            conceptsById.put(gc.conceptId(), gc);
        }
        for (ConceptRelationship rel : conceptGraph.relationships()) {
            GraphConcept fromGc = conceptsById.get(rel.fromConcept());
            GraphConcept toGc = conceptsById.get(rel.toConcept());
            if (fromGc != null && toGc != null) {
                relationshipsByFrom
                        .computeIfAbsent(fromGc.name(), k -> new ArrayList<>())
                        .add(new ConceptRelationship(rel.relationshipId(), fromGc.name(), toGc.name(), rel.type(), rel.confidence()));
            }
        }

        // Collect hypothesis supporting evidence (chunk ids referenced by hypotheses)
        Set<String> hypothesisEvidenceIds = new LinkedHashSet<>();
        for (Hypothesis h : reasoningGraph.hypotheses()) {
            hypothesisEvidenceIds.addAll(h.supportingEvidenceIds());
        }

        // ---- Stage 1: Cluster evidence by shared graph concepts ---------------
        Map<String, Set<String>> evidenceIdsByConcept = new HashMap<>();

        for (Map.Entry<String, ReasoningNode> entry : conceptNodes.entrySet()) {
            String conceptName = entry.getKey();
            ReasoningNode node = entry.getValue();
            Set<String> chunkIds = new LinkedHashSet<>();
            if (node.evidenceIds() != null) {
                chunkIds.addAll(node.evidenceIds());
            }
            for (Hypothesis h : reasoningGraph.hypotheses()) {
                if (h.statement().contains(conceptName)) {
                    chunkIds.addAll(h.supportingEvidenceIds());
                }
            }
            if (!chunkIds.isEmpty()) {
                evidenceIdsByConcept.put(conceptName, chunkIds);
            }
        }

        // Build clusters (one concept -> one cluster)
        List<EvidenceCluster> clusters = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : evidenceIdsByConcept.entrySet()) {
            String conceptName = entry.getKey();
            Set<String> chunkIds = entry.getValue();
            String clusterId = sha256("CLUSTER|" + conceptName);
            clusters.add(new EvidenceCluster(clusterId, new ArrayList<>(chunkIds), List.of(conceptName)));
        }

        // ---- Stage 2: Merge supporting evidence --------------------------------
        // EvidenceCluster constructor already deduplicates and sorts.

        // ---- Stage 3: Create synthesized facts ----------------------------------
        List<SynthesizedFact> facts = new ArrayList<>();
        List<SynthesisEdge> edges = new ArrayList<>();

        for (EvidenceCluster cluster : clusters) {
            String conceptName = cluster.concepts().get(0);
            String statement = buildStatement(conceptName, conceptGraph, relationshipsByFrom);
            String factId = sha256("FACT|" + statement);

            facts.add(new SynthesizedFact(
                    factId,
                    statement,
                    cluster.evidenceIds(),
                    computeMeanTrust(cluster.evidenceIds(), evidenceByChunkId)));

            edges.add(new SynthesisEdge(cluster.clusterId(), factId));
        }

        // ---- Stage 4: Confidence aggregation -----------------------------------
        // Already computed in Stage 3 via computeMeanTrust.

        // ---- Stage 5: Build synthesis graph -------------------------------------
        return new SynthesisGraph(clusters, facts, edges);
    }

    /**
     * Builds a deterministic statement for a concept based on its relationships
     * in the concept graph. Priority: PREREQUISITE > DEPENDS_ON > PART_OF > generic.
     */
    private String buildStatement(String conceptName, ConceptGraph conceptGraph,
                                  Map<String, List<ConceptRelationship>> relationshipsByFrom) {
        List<ConceptRelationship> rels = relationshipsByFrom.getOrDefault(conceptName, List.of());

        // Check for prerequisite target
        for (ConceptRelationship rel : rels) {
            if (rel.type() == RelationshipType.PREREQUISITE) {
                return conceptName + " is a prerequisite for " + rel.toConcept();
            }
        }
        // Check for dependency target
        for (ConceptRelationship rel : rels) {
            if (rel.type() == RelationshipType.DEPENDS_ON) {
                return conceptName + " depends on " + rel.toConcept();
            }
        }
        // Check for part-of target
        for (ConceptRelationship rel : rels) {
            if (rel.type() == RelationshipType.PART_OF) {
                return conceptName + " is part of " + rel.toConcept();
            }
        }
        // Generic foundational template
        return conceptName + " is a foundational concept for further learning";
    }

    /**
     * Computes the mean trust score for the given chunk ids.
     * Returns 0.5 if no matching evidence is found.
     */
    private double computeMeanTrust(List<String> chunkIds,
                                    Map<String, TrustedEvidence> evidenceByChunkId) {
        double sum = 0.0;
        int count = 0;
        for (String chunkId : chunkIds) {
            TrustedEvidence te = evidenceByChunkId.get(chunkId);
            if (te != null) {
                sum += te.trust().overall();
                count++;
            }
        }
        if (count == 0) {
            return 0.5;
        }
        return Math.round((sum / count) * 10000.0) / 10000.0;
    }

    /**
     * Deterministic SHA-256 hash, lowercase hex.
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
