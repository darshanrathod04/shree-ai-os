package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>KnowledgeType</b>
 *
 * <p>Defines the type classification of a knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible types of knowledge entities.</li>
 *   <li>Provides type safety for knowledge classification.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Knowledge Types:</b></p>
 * <ul>
 *   <li>CONCEPT - A semantic concept or idea</li>
 *   <li>FACT - A factual statement or assertion</li>
 *   <li>RULE - A logical rule or constraint</li>
 *   <li>DEFINITION - A formal definition</li>
 *   <li>RELATIONSHIP - A semantic relationship descriptor</li>
 *   <li>CATEGORY - A categorical classification</li>
 *   <li>PROCEDURE - A procedural knowledge entity</li>
 *   <li>REFERENCE - A reference to external knowledge</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 */
public enum KnowledgeType {
    /**
     * A semantic concept or idea.
     */
    CONCEPT,

    /**
     * A factual statement or assertion.
     */
    FACT,

    /**
     * A logical rule or constraint.
     */
    RULE,

    /**
     * A formal definition.
     */
    DEFINITION,

    /**
     * A semantic relationship descriptor.
     */
    RELATIONSHIP,

    /**
     * A categorical classification.
     */
    CATEGORY,

    /**
     * A procedural knowledge entity.
     */
    PROCEDURE,

    /**
     * A reference to external knowledge.
     */
    REFERENCE
}