package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.reasoning.model.CausalChain;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;

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
 * DefaultCausalReasoningEngine
 *
 * The R3 implementation of CausalReasoningEngine. Discovers cause-effect
 * relationships from the R1 ReasoningGraph and R2 SynthesisGraph through
 * a deterministic five-stage pipeline.
 *
 * Ownership: Reasoning Kernel - R3 Causal Reasoning
 * Version: 1.0
 */
public final class DefaultCausalReasoningEngine implements CausalReasoningEngine {

    /** Maximum causal chain depth (locked constant). */
    public static final int MAX_CHAIN_DEPTH = 4;

    /** Strength for explicit dependency relations. */
    public static final double STRENGTH_DEPENDENCY = 1.00;

    /** Strength for prerequisite relations. */
    public static final double STRENGTH_PREREQUISITE = 0.90;

    /** Strength for part-of relations. */
    public static final double STRENGTH_PART_OF = 0.80;

    /** Strength for related-to relations. */
    public static final double STRENGTH_RELATED = 0.60;

    /** Strength for implements relations. */
    public static final double STRENGTH_IMPLEMENTS = 0.70;

    /** Strength for uses relations. */
    public static final double STRENGTH_USES = 0.60;

    @Override
    public CausalGraph analyze(ReasoningGraph reasoningGraph, SynthesisGraph synthesisGraph, ConceptGraph conceptGraph) {
        Objects.requireNonNull(reasoningGraph, "reasoningGraph must not be null");
        Objects.requireNonNull(synthesisGraph, "synthesisGraph must not be null");
        Objects.requireNonNull(conceptGraph, "conceptGraph must not be null");

        // Build concept name -> id index (concept graph IDs take priority)
        Map<String, String> conceptIdByName = new HashMap<>();
        for (ReasoningNode node : reasoningGraph.nodes()) {
            if (node.type() == ReasoningNodeType.CONCEPT) {
                conceptIdByName.put(node.title().toLowerCase(), node.nodeId());
            }
        }
        for (var gc : conceptGraph.concepts()) {
            conceptIdByName.put(gc.name().toLowerCase(), gc.conceptId());
        }
        for (var cluster : synthesisGraph.clusters()) {
            for (String concept : cluster.concepts()) {
                conceptIdByName.putIfAbsent(concept.toLowerCase(), sha256("CAUSE|" + concept));
            }
        }

        List<CausalNode> nodes = new ArrayList<>();
        List<CausalEdge> edges = new ArrayList<>();
        Set<String> causeNames = new LinkedHashSet<>();
        Set<String> effectNames = new LinkedHashSet<>();
        Set<String> intermediateNames = new LinkedHashSet<>();

        // ---- Stage 1: cause identification --------------------------------------------
        for (SynthesizedFact fact : synthesisGraph.facts()) {
            parseFactForCauses(fact.statement(), causeNames, conceptIdByName);
        }

        // ---- Stage 2: effect identification ------------------------------------------
        for (SynthesizedFact fact : synthesisGraph.facts()) {
            parseFactForEffects(fact.statement(), effectNames, conceptIdByName);
        }

        // Fallback: if no causes found from facts, use synthesis cluster concepts
        if (causeNames.isEmpty()) {
            for (var cluster : synthesisGraph.clusters()) {
                for (String concept : cluster.concepts()) {
                    causeNames.add(concept);
                }
            }
        }

        // Build concept ID -> name mapping from concept graph
        Map<String, String> conceptNameById = new HashMap<>();
        for (var gc : conceptGraph.concepts()) {
            conceptNameById.put(gc.conceptId().toLowerCase(), gc.name());
        }

        // ---- Stage 3: build edges directly from concept graph relationships ------------
        for (var rel : conceptGraph.relationships()) {
            double strength = switch (rel.type()) {
                case DEPENDS_ON -> STRENGTH_DEPENDENCY;
                case PREREQUISITE -> STRENGTH_PREREQUISITE;
                case PART_OF -> STRENGTH_PART_OF;
                case RELATED_TO -> STRENGTH_RELATED;
                case IMPLEMENTS -> STRENGTH_IMPLEMENTS;
                case USES -> STRENGTH_USES;
                default -> STRENGTH_RELATED;
            };
            String fromName = conceptNameById.getOrDefault(rel.fromConcept().toLowerCase(), rel.fromConcept());
            String toName = conceptNameById.getOrDefault(rel.toConcept().toLowerCase(), rel.toConcept());
            String fromId = conceptIdByName.get(fromName.toLowerCase());
            String toId = conceptIdByName.get(toName.toLowerCase());
            if (fromId != null && toId != null) {
                edges.add(new CausalEdge(fromId, toId, strength));
                causeNames.add(fromName);
                effectNames.add(toName);
            }
        }

        // ---- Stage 4: BFS for intermediate links and chains --------------------------
        Map<String, List<String>> adjacency = new HashMap<>();
        for (CausalEdge edge : edges) {
            adjacency.computeIfAbsent(edge.fromNode(), k -> new ArrayList<>())
                    .add(edge.toNode());
        }

        Map<String, String> titleByNodeId = new HashMap<>();
        for (var gc : conceptGraph.concepts()) {
            titleByNodeId.put(gc.conceptId(), gc.name());
        }
        for (ReasoningNode node : reasoningGraph.nodes()) {
            titleByNodeId.putIfAbsent(node.nodeId(), node.title());
        }

        List<List<String>> chains = new ArrayList<>();
        for (String causeName : causeNames) {
            String causeNodeId = conceptIdByName.get(causeName.toLowerCase());
            if (causeNodeId == null) continue;

            Set<String> visited = new HashSet<>();
            visited.add(causeNodeId);
            Queue<Object[]> queue = new LinkedList<>();
            queue.add(new Object[]{causeNodeId, 0, new ArrayList<>(List.of(causeName))});

            while (!queue.isEmpty()) {
                Object[] entry = queue.poll();
                String currentId = (String) entry[0];
                int depth = (int) entry[1];
                @SuppressWarnings("unchecked")
                List<String> path = (List<String>) entry[2];

                List<String> neighbors = adjacency.getOrDefault(currentId, List.of());
                for (String neighborId : neighbors) {
                    if (visited.contains(neighborId)) continue;
                    visited.add(neighborId);

                    String neighborTitle = titleByNodeId.get(neighborId);
                    if (neighborTitle == null) continue;

                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighborTitle);

                    if (newPath.size() >= 2) {
                        chains.add(newPath);
                        for (int i = 1; i < newPath.size() - 1; i++) {
                            intermediateNames.add(newPath.get(i));
                        }
                    }

                    if (depth + 1 < MAX_CHAIN_DEPTH) {
                        queue.add(new Object[]{neighborId, depth + 1, newPath});
                    }
                }
            }
        }

        // ---- Stage 5: build nodes, edges, chains --------------------------------------
        Set<String> allNodeTitles = new LinkedHashSet<>();
        allNodeTitles.addAll(causeNames);
        allNodeTitles.addAll(effectNames);
        allNodeTitles.addAll(intermediateNames);

        Map<String, String> nodeIdByTitle = new HashMap<>();
        for (String title : allNodeTitles) {
            CausalNodeType type;
            boolean isCause = causeNames.contains(title);
            boolean isEffect = effectNames.contains(title);
            boolean isIntermediate = intermediateNames.contains(title);

            if (isIntermediate || (isCause && isEffect)) {
                type = CausalNodeType.INTERMEDIATE;
            } else if (isCause) {
                type = CausalNodeType.CAUSE;
            } else {
                type = CausalNodeType.EFFECT;
            }

            String nodeId = sha256("CAUSE|" + title);
            nodeIdByTitle.put(title, nodeId);
            nodes.add(new CausalNode(nodeId, title, type));
        }

        // Re-key edges with proper causal node IDs
        List<CausalEdge> causalEdges = new ArrayList<>();
        for (CausalEdge edge : edges) {
            String fromTitle = titleByNodeId.get(edge.fromNode());
            String toTitle = titleByNodeId.get(edge.toNode());
            if (fromTitle != null && toTitle != null) {
                String fromId = nodeIdByTitle.get(fromTitle);
                String toId = nodeIdByTitle.get(toTitle);
                if (fromId != null && toId != null) {
                    causalEdges.add(new CausalEdge(fromId, toId, edge.strength()));
                }
            }
        }

        // Build causal chains
        List<CausalChain> causalChains = new ArrayList<>();
        for (List<String> chain : chains) {
            List<String> nodeIds = new ArrayList<>();
            for (String title : chain) {
                String id = nodeIdByTitle.get(title);
                if (id != null) nodeIds.add(id);
            }
            if (nodeIds.size() >= 2) {
                String chainId = sha256("CHAIN|" + String.join(",", nodeIds));
                causalChains.add(new CausalChain(chainId, nodeIds));
            }
        }

        return new CausalGraph(nodes, causalEdges, causalChains);
    }

    private void parseFactForCauses(String statement, Set<String> causes,
                                    Map<String, String> conceptIdByName) {
        String lower = statement.toLowerCase();
        if (lower.contains("learn") && lower.contains("before")) {
            int learnIdx = lower.indexOf("learn");
            int beforeIdx = lower.indexOf("before");
            if (learnIdx >= 0 && beforeIdx > learnIdx) {
                String cause = statement.substring(learnIdx + 5, beforeIdx).trim();
                if (conceptIdByName.containsKey(cause.toLowerCase())) {
                    causes.add(cause);
                }
            }
        }
        if (lower.contains("depends on")) {
            int depsIdx = lower.indexOf("depends on");
            String after = statement.substring(depsIdx + 10).trim();
            if (conceptIdByName.containsKey(after.toLowerCase())) {
                causes.add(after);
            }
        }
    }

    private void parseFactForEffects(String statement, Set<String> effects,
                                     Map<String, String> conceptIdByName) {
        String lower = statement.toLowerCase();
        if (lower.contains("learn") && lower.contains("before")) {
            int beforeIdx = lower.indexOf("before");
            if (beforeIdx >= 0) {
                String effect = statement.substring(beforeIdx + 6).trim();
                if (conceptIdByName.containsKey(effect.toLowerCase())) {
                    effects.add(effect);
                }
            }
        }
        if (lower.contains("depends on")) {
            int depsIdx = lower.indexOf("depends on");
            String before = statement.substring(0, depsIdx).trim();
            if (conceptIdByName.containsKey(before.toLowerCase())) {
                effects.add(before);
            }
        }
    }

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
