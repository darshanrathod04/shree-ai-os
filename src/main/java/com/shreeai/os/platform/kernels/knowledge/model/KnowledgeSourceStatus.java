package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>KnowledgeSourceStatus</b>
 *
 * <p>Extensible enumeration of the lifecycle states a registered
 * {@link KnowledgeSource} can occupy within the
 * {@code KnowledgeSourceRegistry}.</p>
 *
 * <p>Lifecycle is expressed purely through this immutable enum value - there
 * are no mutable lifecycle flag fields anywhere in the model.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K1 Universal Knowledge Source Registry</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum KnowledgeSourceStatus {

    /** The source was registered but has not been explicitly enabled yet. */
    REGISTERED,

    /** The source is enabled and eligible for future ingestion pipelines. */
    ACTIVE,

    /** The source was explicitly disabled and must not be ingested. */
    DISABLED
}