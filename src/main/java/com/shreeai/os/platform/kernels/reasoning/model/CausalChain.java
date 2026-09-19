package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An immutable ordered sequence of node identifiers forming a single causal
 * chain from a root cause to a terminal effect.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record CausalChain(
        String chainId,
        List<String> nodeIds) {

    public CausalChain {
        Objects.requireNonNull(chainId, "chainId must not be null");
        Objects.requireNonNull(nodeIds, "nodeIds must not be null");
        if (chainId.isBlank()) {
            throw new IllegalArgumentException("chainId must not be blank");
        }
        nodeIds = Collections.unmodifiableList(new ArrayList<>(nodeIds));
    }
}
