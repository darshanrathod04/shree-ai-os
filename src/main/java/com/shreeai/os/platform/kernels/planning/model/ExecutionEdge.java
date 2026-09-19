package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/**
 * <b>ExecutionEdge</b>
 *
 * <p>A single directed precedence edge between two {@link ExecutionNode}
 * instances of an {@link ExecutablePlanningGraph}. One edge is generated for
 * every {@link TaskDependency}; no edge is ever inferred, weighted, or
 * reordered by meaning.</p>
 *
 * <p><b>Deliberate omissions:</b> there are no weights, no probabilities,
 * and no lag values - the edge is pure dependency topology, mirroring the
 * P2.2 {@link TaskDependency} contract.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - every component is a {@code String}.</li>
 *   <li>Constructor validation - rejects null, blank, non-SHA-256 node
 *       identifiers, and self-edges.</li>
 *   <li>Data-only - contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param fromNodeId the predecessor node identifier (must be a 64-character
 *                   lower-case SHA-256 hex digest)
 * @param toNodeId   the successor node identifier (must be a 64-character
 *                   lower-case SHA-256 hex digest)
 *
 * @since P2.5
 * @see ExecutionNode
 * @see ExecutablePlanningGraph
 */
public record ExecutionEdge(String fromNodeId, String toNodeId) {

    /**
     * Creates a validated, deeply-immutable execution edge.
     *
     * @throws NullPointerException     if either identifier is null
     * @throws IllegalArgumentException if either identifier is not a
     *                                  64-character lower-case SHA-256 hex
     *                                  digest, or if both identifiers are
     *                                  equal (a node may not depend on
     *                                  itself)
     */
    public ExecutionEdge {
        fromNodeId = requireNodeId(fromNodeId, "fromNodeId");
        toNodeId = requireNodeId(toNodeId, "toNodeId");
        if (fromNodeId.equals(toNodeId)) {
            throw new IllegalArgumentException(
                    "a node must not depend on itself: " + fromNodeId);
        }
    }

    /**
     * Returns a stable human-readable key for diagnostics and duplicate
     * detection.
     *
     * @return {@code "fromNodeId -> toNodeId"} (never null)
     */
    public String edgeKey() {
        return fromNodeId + " -> " + toNodeId;
    }

    private static String requireNodeId(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (!ExecutionNode.isValidNodeId(value)) {
            throw new IllegalArgumentException(
                    name + " must be a 64-character lower-case SHA-256 hex digest");
        }
        return value;
    }

    @Override
    public String toString() {
        return "ExecutionEdge{" + fromNodeId.substring(0, 12)
                + " -> " + toNodeId.substring(0, 12) + "}";
    }
}
