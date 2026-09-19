package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>RelationshipType</b>
 *
 * <p>The closed set of deterministic relationship kinds the Knowledge Graph
 * Builder may emit. No semantic types beyond these are ever inferred; every
 * edge type originates from an explicit dictionary rule or a co-occurrence
 * rule defined by the builder.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum RelationshipType {

    /** The source concept must be understood before the target concept. */
    PREREQUISITE,

    /** The source concept is a structural member of the target concept. */
    PART_OF,

    /** The source concept requires the target concept to function. */
    DEPENDS_ON,

    /** The two concepts are connected by deterministic co-occurrence evidence. */
    RELATED_TO,

    /** The source concept realizes the contract of the target concept. */
    IMPLEMENTS,

    /** The source concept makes operational use of the target concept. */
    USES
}
