package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>ReasoningGraph</b>
 *
 * <p>The canonical reasoning artifact of the Reasoning Kernel: evidence
 * nodes, concept nodes, multi-hop chain edges and deterministic hypotheses in
 * one immutable, stably ordered structure. It contains no conclusions, no
 * verification and no decisions - downstream stages (ReasoningResult,
 * Inference) consume it as input.</p>
 *
 * <p><b>Canonical ordering:</b> nodes by node id, edges by source, target and
 * type, hypotheses by hypothesis id - identical inputs always produce
 * structurally equal graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodes      the ordered immutable node list (never null)
 * @param edges      the ordered immutable edge list (never null)
 * @param hypotheses the ordered immutable hypothesis list (never null)
 */
public record ReasoningGraph(
        List<ReasoningNode> nodes,
        List<ReasoningEdge> edges,
        List<Hypothesis> hypotheses) {

    /**
     * Compact constructor that validates, canonically orders and defensively
     * copies all three lists.
     *
     * @throws NullPointerException if any list is null
     */
    public ReasoningGraph {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(edges, "edges must not be null");
        Objects.requireNonNull(hypotheses, "hypotheses must not be null");
        nodes = nodes.stream()
                .sorted(Comparator.comparing(ReasoningNode::nodeId))
                .toList();
        edges = edges.stream()
                .sorted(Comparator.comparing(ReasoningEdge::fromNode)
                        .thenComparing(ReasoningEdge::toNode)
                        .thenComparing(edge -> edge.type().name()))
                .toList();
        hypotheses = hypotheses.stream()
                .sorted(Comparator.comparing(Hypothesis::hypothesisId))
                .toList();
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the node count (never negative)
     */
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return the edge count (never negative)
     */
    public int edgeCount() {
        return edges.size();
    }

    @Override
    public String toString() {
        return String.format("ReasoningGraph{nodes=%d, edges=%d, hypotheses=%d}",
                nodes.size(), edges.size(), hypotheses.size());
    }
}
