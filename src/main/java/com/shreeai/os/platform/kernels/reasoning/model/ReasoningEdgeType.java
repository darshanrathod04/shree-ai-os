package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>ReasoningEdgeType</b>
 *
 * <p>The closed classification of directed edges in a {@link ReasoningGraph}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ReasoningEdgeType {

    /** Evidence supports the concept it matched. */
    SUPPORTS,

    /** Two concepts are chained inside a multi-hop path. */
    CONNECTS,

    /** A completed chain leads to its deterministic hypothesis. */
    LEADS_TO
}
