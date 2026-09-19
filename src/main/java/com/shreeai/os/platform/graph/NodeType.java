package com.shreeai.os.platform.graph;

import com.shreeai.os.platform.resolver.CapabilityType;

/**
 * <b>NodeType</b>
 *
 * <p>Classifies {@link ExecutionNode}s by the role they play within the
 * Universal Execution Graph.</p>
 *
 * <p><b>Ownership:</b> Platform Graph</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum NodeType {

    /** Entry point of the graph — Identity. */
    ROOT,

    /** Request and conversation context assembly. */
    CONTEXTUAL,

    /** Long-term memory and knowledge retrieval. */
    RETRIEVAL,

    /** Reasoning, inference, and planning. */
    COGNITIVE,

    /** Action dispatch, tool invocation, and orchestration. */
    EXECUTIVE,

    /** Multi-agent coordination. */
    COLLABORATIVE,

    /** Language and multimodal model invocation. */
    MODEL,

    /** Validation and safety guards. */
    GUARD,

    /** Observability attached alongside every node. */
    OBSERVATION;

    /**
     * Maps a capability to the node type that represents it.
     *
     * @param capability the capability (must not be null)
     * @return the node type for the capability
     */
    public static NodeType forCapability(CapabilityType capability) {
        if (capability == null) {
            throw new IllegalArgumentException("capability must not be null");
        }
        return switch (capability) {
            case IDENTITY -> ROOT;
            case CONTEXT -> CONTEXTUAL;
            case MEMORY, KNOWLEDGE -> RETRIEVAL;
            case REASONING, PLANNING -> COGNITIVE;
            case EXECUTION, TOOLS, ORCHESTRATION -> EXECUTIVE;
            case AGENTS -> COLLABORATIVE;
            case MODELS -> MODEL;
            case VALIDATION, SAFETY -> GUARD;
            case OBSERVABILITY -> OBSERVATION;
        };
    }
}