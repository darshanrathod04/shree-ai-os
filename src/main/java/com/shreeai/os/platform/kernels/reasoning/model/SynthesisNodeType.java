package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>SynthesisNodeType</b>
 *
 * <p>Classification of nodes within a {@link SynthesisGraph}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum SynthesisNodeType {

    /** A cluster of evidence items discussing the same concept. */
    EVIDENCE_CLUSTER,

    /** A deterministic synthesized fact derived from clustered evidence. */
    SYNTHESIZED_FACT
}
