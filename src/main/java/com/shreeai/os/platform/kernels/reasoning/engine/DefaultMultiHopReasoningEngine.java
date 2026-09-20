package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdge;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdgeType;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * <b>DefaultMultiHopReasoningEngine</b>
 *
 * <p>The R1 implementation of {@link MultiHopReasoningEngine}. Converts a
 * {@link ReliabilityResult} and a {@link ConceptGraph} into an immutable
 * {@link ReasoningGraph} through a deterministic five-stage pipeline:</p>
 * <ol>
 *   <li><b>Evidence linking</b> - every trusted evidence becomes an
 *       {@link ReasoningNodeType#EVIDENCE} node; content is never transformed.</li>
 *   <li><b>Concept resolution</b> - graph concepts whose canonical name appears
 *       in an evidence item content become {@link ReasoningNodeType#CONCEPT}
 *       nodes, linked by {@link ReasoningEdgeType#SUPPORTS} edges.</li>
 *   <li><b>Multi-hop traversal</b> - breadth-first expansion through the
 *       concept graph from every resolved concept, up to {@link #MAX_HOP_DEPTH}
 *       hops; never revisits a concept.</li>
 *   <li><b>Hypothesis generation</b> - every completed chain produces one
 *       deterministic {@link ReasoningNodeType#HYPOTHESIS} node.</li>
 *   <li><b>Graph assembly</b> - immutable, stably ordered {@link ReasoningGraph}.</li>
 * </ol>
 *
 * <p>The engine is stateless, thread-safe, deterministic and contains no LLM,
 * no embeddings and no NLP libraries. Identical inputs always produce
 * structurally equal reasoning graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultMultiHopReasoningEngine implements MultiHopReasoningEngine {

    /** Maximum multi-hop traversal depth (locked constant). */
    public static final int MAX_HOP_DEPTH = 3;

    @Override
    public ReasoningGraph reason(ReliabilityResult reliability, ConceptGraph conceptGraph) {
        Objects.requireNonNull(reliability, "reliability must not be null");
        Objects.requireNonNull(conceptGraph, "conceptGraph must not be null");

        List<TrustedEvidence> evidenceList = reliability.evidence();
        List<ReasoningNode> nodes = new ArrayList<>();
        List<ReasoningEdge> edges = new ArrayList<>();
        Map<String, String> evidenceNodeIdByChunkId = new HashMap<>();

        // ---- Stage 1: evidence linking ------------------------------------------------
        for (TrustedEvidence te : evidenceList) {
            String chunkId = te.evidence().chunkId();
            String content = te.evidence().content();
            String nodeId = sha256("EVIDENCE|" + chunkId + "|" + content);
            nodes.add(new ReasoningNode(nodeId, content, ReasoningNodeType.EVIDENCE, List.of(chunkId)));
            evidenceNodeIdByChunkId.put(chunkId, nodeId);
        }

        // Index concepts by lower-case name for resolution
        Map<String, GraphConcept> conceptsByLowerName = new HashMap<>();
        for (GraphConcept c : conceptGraph.concepts()) {
            conceptsByLowerName.put(c.name().toLowerCase(), c);
        }

        // ---- Stage 2: concept resolution ---------------------------------------------
        Map<String, Set<String>> evidenceConceptNames = new HashMap<>();
        Map<String, Set<String>> evidenceChunkIdsByConcept = new HashMap<>();

        for (TrustedEvidence te : evidenceList) {
            String chunkId = te.evidence().chunkId();
            String content = te.evidence().content();
            Set<String> found = findConceptsInText(content, conceptsByLowerName);
            evidenceConceptNames.put(chunkId, found);
            for (String conceptName : found) {
                evidenceChunkIdsByConcept
                        .computeIfAbsent(conceptName, k -> new LinkedHashSet<>())
                        .add(chunkId);
            }
        }

        // Create CONCEPT nodes and SUPPORTS edges
        for (TrustedEvidence te : evidenceList) {
            String chunkId = te.evidence().chunkId();
            Set<String> found = evidenceConceptNames.getOrDefault(chunkId, Set.of());
            for (String conceptName : found) {
                if (conceptName == null) {
                    continue;
                }
                GraphConcept gc = conceptsByLowerName.get(conceptName.toLowerCase());
                if (gc == null) {
                    continue;
                }
                String conceptNodeId = sha256("CONCEPT|" + gc.name());
                if (nodes.stream().noneMatch(n -> n.nodeId().equals(conceptNodeId))) {
                    Set<String> chunkIds = evidenceChunkIdsByConcept.get(gc.name());
                    List<String> chunkIdList = chunkIds == null ? List.of() : List.copyOf(chunkIds);
                    nodes.add(new ReasoningNode(
                            conceptNodeId,
                            gc.name(),
                            ReasoningNodeType.CONCEPT,
                            chunkIdList));
                }
                String evidenceNodeId = evidenceNodeIdByChunkId.get(chunkId);
                if (evidenceNodeId != null) {
                    edges.add(new ReasoningEdge(evidenceNodeId, conceptNodeId, ReasoningEdgeType.SUPPORTS));
                }
            }
        }

        // ---- Stage 3: multi-hop traversal (BFS, max depth 3, no revisits) -------------
        Map<String, List<String>> adjacency = new HashMap<>();
        for (ConceptRelationship rel : conceptGraph.relationships()) {
            adjacency.computeIfAbsent(rel.fromConcept(), k -> new ArrayList<>())
                    .add(rel.toConcept());
        }

        Map<String, String> nameByConceptId = new HashMap<>();
        for (GraphConcept gc : conceptGraph.concepts()) {
            nameByConceptId.put(gc.conceptId(), gc.name());
        }

        Set<String> resolvedConceptIds = new LinkedHashSet<>();
        for (Set<String> names : evidenceConceptNames.values()) {
            for (String name : names) {
                GraphConcept gc = conceptsByLowerName.get(name.toLowerCase());
                if (gc != null) {
                    resolvedConceptIds.add(gc.conceptId());
                }
            }
        }

        List<List<String>> chains = new ArrayList<>();

        for (String seedId : resolvedConceptIds) {
            Set<String> visited = new HashSet<>();
            visited.add(seedId);
            Queue<Object[]> queue = new LinkedList<>();
            String seedName = nameByConceptId.get(seedId);
            if (seedName == null) {
                continue;
            }
            queue.add(new Object[]{seedId, 0, new ArrayList<>(List.of(seedName))});

            while (!queue.isEmpty()) {
                Object[] entry = queue.poll();
                String currentId = (String) entry[0];
                int depth = (int) entry[1];
                @SuppressWarnings("unchecked")
                List<String> path = (List<String>) entry[2];

                List<String> neighbors = adjacency.getOrDefault(currentId, List.of());
                for (String neighborId : neighbors) {
                    if (visited.contains(neighborId)) {
                        continue;
                    }
                    visited.add(neighborId);
                    String neighborName = nameByConceptId.get(neighborId);
                    if (neighborName == null) {
                        continue;
                    }
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighborName);
                    if (newPath.size() >= 2) {
                        chains.add(newPath);
                    }
                    String fromNodeId = sha256("CONCEPT|" + nameByConceptId.get(currentId));
                    String toNodeId = sha256("CONCEPT|" + neighborName);
                    edges.add(new ReasoningEdge(fromNodeId, toNodeId, ReasoningEdgeType.CONNECTS));
                    if (nodes.stream().noneMatch(n -> n.nodeId().equals(toNodeId))) {
                        nodes.add(new ReasoningNode(
                                toNodeId,
                                neighborName,
                                ReasoningNodeType.CONCEPT,
                                List.of()));
                    }
                    if (depth + 1 < MAX_HOP_DEPTH) {
                        queue.add(new Object[]{neighborId, depth + 1, newPath});
                    }
                }
            }
        }

        // ---- Stage 4: hypothesis generation -------------------------------------------
        List<Hypothesis> hypotheses = new ArrayList<>();
        for (List<String> chain : chains) {
            String secondToLast = chain.get(chain.size() - 2);
            String last = chain.get(chain.size() - 1);
            String statement = "Learn " + secondToLast + " before " + last;
            String hypothesisId = sha256("HYPOTHESIS|" + statement);

            Set<String> supportingChunkIds = new LinkedHashSet<>();
            for (String conceptName : chain) {
                Set<String> chunkIds = evidenceChunkIdsByConcept.getOrDefault(conceptName, Set.of());
                supportingChunkIds.addAll(chunkIds);
            }

            double confidence;
            if (supportingChunkIds.isEmpty()) {
                confidence = 0.5;
            } else {
                double sum = 0.0;
                int count = 0;
                for (TrustedEvidence te : evidenceList) {
                    if (supportingChunkIds.contains(te.evidence().chunkId())) {
                        sum += te.trust().overall();
                        count++;
                    }
                }
                confidence = count > 0 ? round4(sum / count) : 0.5;
            }

            hypotheses.add(new Hypothesis(
                    hypothesisId,
                    statement,
                    new ArrayList<>(supportingChunkIds),
                    confidence));

            nodes.add(new ReasoningNode(
                    hypothesisId,
                    statement,
                    ReasoningNodeType.HYPOTHESIS,
                    new ArrayList<>(supportingChunkIds)));

            String lastConceptNodeId = sha256("CONCEPT|" + last);
            edges.add(new ReasoningEdge(lastConceptNodeId, hypothesisId, ReasoningEdgeType.LEADS_TO));
        }

        // ---- Stage 5: build reasoning graph -------------------------------------------
        return new ReasoningGraph(nodes, edges, hypotheses);
    }

    /**
     * Finds all concepts whose canonical name appears whole-word in the text.
     * Matching is case-insensitive and word-bounded.
     */
    private Set<String> findConceptsInText(String text, Map<String, GraphConcept> conceptsByLowerName) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        Set<String> found = new LinkedHashSet<>();
        String lower = text.toLowerCase();
        for (Map.Entry<String, GraphConcept> entry : conceptsByLowerName.entrySet()) {
            String conceptLower = entry.getKey();
            if (containsWholeWord(lower, conceptLower)) {
                found.add(entry.getValue().name());
            }
        }
        return found;
    }

    /**
     * Returns true if {@code word} appears in {@code text} as a whole word
     * (bounded by non-letter boundaries or string edges).
     */
    private boolean containsWholeWord(String text, String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        int index = text.indexOf(word);
        while (index >= 0) {
            boolean leftBound = index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1));
            int end = index + word.length();
            boolean rightBound = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
            if (leftBound && rightBound) {
                return true;
            }
            index = text.indexOf(word, index + 1);
        }
        return false;
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

    /**
     * Rounds a double to 4 decimal places deterministically.
     */
    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
