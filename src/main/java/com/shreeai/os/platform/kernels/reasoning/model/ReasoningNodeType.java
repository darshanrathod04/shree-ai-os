package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>ReasoningNodeType</b>
 *
 * <p>The closed classification of nodes in a {@link ReasoningGraph}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ReasoningNodeType {

    /** A trusted evidence artifact carried over from reliability evaluation. */
    EVIDENCE,

    /** A knowledge-graph concept resolved from evidence lineage. */
    CONCEPT,

    /** A deterministic hypothesis derived from a completed reasoning chain. */
    HYPOTHESIS
}
