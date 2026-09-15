package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * An immutable node within a {@link CausalGraph}.
 *
 * <p>Each node represents a single concept participating in a causal
 * relationship, classified by {@link CausalNodeType}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record CausalNode(
        String nodeId,
        String title,
        CausalNodeType type) {

    public CausalNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }
}
