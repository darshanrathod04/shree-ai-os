package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * <b>ReasoningEdge</b>
 *
 * <p>One immutable directed edge of a {@link ReasoningGraph}. Edge identity is
 * derived from its endpoints and type - duplicate edges are impossible by
 * construction.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param fromNode the deterministic source node id (never null)
 * @param toNode   the deterministic target node id (never null)
 * @param type     the edge classification (never null)
 */
public record ReasoningEdge(String fromNode, String toNode, ReasoningEdgeType type) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if any field is null
     * @throws IllegalArgumentException if endpoints are blank or equal
     */
    public ReasoningEdge {
        Objects.requireNonNull(fromNode, "fromNode must not be null");
        Objects.requireNonNull(toNode, "toNode must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (fromNode.isBlank()) {
            throw new IllegalArgumentException("fromNode must not be blank");
        }
        if (toNode.isBlank()) {
            throw new IllegalArgumentException("toNode must not be blank");
        }
        if (fromNode.equals(toNode)) {
            throw new IllegalArgumentException("self edges are not allowed");
        }
    }

    @Override
    public String toString() {
        return String.format("ReasoningEdge{type=%s}", type);
    }
}
