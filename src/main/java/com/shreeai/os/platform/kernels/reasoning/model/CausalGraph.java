package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The immutable, canonical artifact produced by R3 Causal Reasoning.
 *
 * <p>Contains cause/intermediate/effect nodes, directed causal edges with
 * deterministic strength values, and enumerated causal chains.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record CausalGraph(
        List<CausalNode> nodes,
        List<CausalEdge> edges,
        List<CausalChain> chains) {

    public CausalGraph {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(edges, "edges must not be null");
        Objects.requireNonNull(chains, "chains must not be null");
        nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        edges = Collections.unmodifiableList(new ArrayList<>(edges));
        chains = Collections.unmodifiableList(new ArrayList<>(chains));
    }
}
