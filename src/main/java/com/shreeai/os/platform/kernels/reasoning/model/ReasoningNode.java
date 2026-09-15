package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ReasoningNode</b>
 *
 * <p>One immutable node of a {@link ReasoningGraph}: an evidence artifact, a
 * knowledge-graph concept or a deterministic hypothesis. Evidence titles
 * carry the original text verbatim - reasoning never transforms content.</p>
 *
 * <p><b>Deterministic identity:</b> node ids are SHA-256 over the node type
 * and a stable key (chunk id for evidence, canonical concept name for
 * concepts, hypothesis statement for hypotheses).</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodeId      the deterministic node id (never null)
 * @param title       the node title (never null or blank)
 * @param type        the node classification (never null)
 * @param evidenceIds the chunk ids of the evidence behind this node (never
 *                    null)
 */
public record ReasoningNode(String nodeId, String title, ReasoningNodeType type,
                            List<String> evidenceIds) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if id or title is blank
     */
    public ReasoningNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(evidenceIds, "evidenceIds must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        evidenceIds = List.copyOf(evidenceIds);
    }

    @Override
    public String toString() {
        return String.format("ReasoningNode{type=%s, title=%s}", type, title);
    }
}
