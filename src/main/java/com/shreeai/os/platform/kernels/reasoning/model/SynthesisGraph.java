package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>SynthesisGraph</b>
 *
 * <p>The canonical evidence synthesis artifact of the Reasoning Kernel:
 * evidence clusters, synthesized facts and provenance edges in one immutable,
 * stably ordered structure. It contains no conclusions, no verification and
 * no decisions - downstream stages (Inference) consume it as input.</p>
 *
 * <p><b>Canonical ordering:</b> clusters by cluster id, facts by fact id,
 * edges by source then target - identical inputs always produce structurally
 * equal graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param clusters the ordered immutable cluster list (never null)
 * @param facts    the ordered immutable synthesized fact list (never null)
 * @param edges    the ordered immutable edge list (never null)
 */
public record SynthesisGraph(
        List<EvidenceCluster> clusters,
        List<SynthesizedFact> facts,
        List<SynthesisEdge> edges) {

    /**
     * Compact constructor that validates, canonically orders and defensively
     * copies all three lists.
     *
     * @throws NullPointerException if any list is null
     */
    public SynthesisGraph {
        Objects.requireNonNull(clusters, "clusters must not be null");
        Objects.requireNonNull(facts, "facts must not be null");
        Objects.requireNonNull(edges, "edges must not be null");
        clusters = clusters.stream()
                .sorted(Comparator.comparing(EvidenceCluster::clusterId))
                .toList();
        facts = facts.stream()
                .sorted(Comparator.comparing(SynthesizedFact::factId))
                .toList();
        edges = edges.stream()
                .sorted(Comparator.comparing(SynthesisEdge::fromNode)
                        .thenComparing(SynthesisEdge::toNode))
                .toList();
    }

    /**
     * Returns the number of clusters in the graph.
     *
     * @return the cluster count (never negative)
     */
    public int clusterCount() {
        return clusters.size();
    }

    /**
     * Returns the number of synthesized facts in the graph.
     *
     * @return the fact count (never negative)
     */
    public int factCount() {
        return facts.size();
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
        return String.format("SynthesisGraph{clusters=%d, facts=%d, edges=%d}",
                clusters.size(), facts.size(), edges.size());
    }
}
