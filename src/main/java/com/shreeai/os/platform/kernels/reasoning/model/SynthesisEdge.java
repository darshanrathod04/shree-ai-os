package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * <b>SynthesisEdge</b>
 *
 * <p>An immutable, directed edge in a {@link SynthesisGraph} connecting an
 * evidence cluster to the synthesized fact it produces. Edges carry no
 * semantic type - they represent provenance provenance provenance.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param fromNode the source node id (never null)
 * @param toNode   the target node id (never null)
 */
public record SynthesisEdge(String fromNode, String toNode) {

    /**
     * Compact constructor that validates both endpoints.
     *
     * @throws NullPointerException if either endpoint is null
     */
    public SynthesisEdge {
        Objects.requireNonNull(fromNode, "fromNode must not be null");
        Objects.requireNonNull(toNode, "toNode must not be null");
    }

    @Override
    public String toString() {
        return String.format("SynthesisEdge{from=%s, to=%s}", fromNode, toNode);
    }
}
