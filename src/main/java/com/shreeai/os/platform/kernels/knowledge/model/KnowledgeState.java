package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>KnowledgeState</b>
 *
 * <p>Defines the lifecycle state of a knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible states of a knowledge entity.</li>
 *   <li>Provides state safety for knowledge lifecycle management.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Knowledge States:</b></p>
 * <ul>
 *   <li>ACTIVE - Knowledge entity is active and queryable</li>
 *   <li>ARCHIVED - Knowledge entity is archived and not returned in standard queries</li>
 *   <li>DEPRECATED - Knowledge entity is deprecated and should not be used</li>
 *   <li>PENDING - Knowledge entity is pending review or validation</li>
 *   <li>REJECTED - Knowledge entity was rejected during validation</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 */
public enum KnowledgeState {
    /**
     * Knowledge entity is active and queryable.
     */
    ACTIVE,

    /**
     * Knowledge entity is archived and not returned in standard queries.
     */
    ARCHIVED,

    /**
     * Knowledge entity is deprecated and should not be used.
     */
    DEPRECATED,

    /**
     * Knowledge entity is pending review or validation.
     */
    PENDING,

    /**
     * Knowledge entity was rejected during validation.
     */
    REJECTED
}