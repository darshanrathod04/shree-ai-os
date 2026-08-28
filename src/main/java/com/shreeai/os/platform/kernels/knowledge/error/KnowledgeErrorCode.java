package com.shreeai.os.platform.kernels.knowledge.error;

/**
 * <b>KnowledgeErrorCode</b>
 *
 * <p>Standardized error identifiers for the Knowledge Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides standardized error codes for Knowledge operations.</li>
 *   <li>Ensures consistent error identification across the platform.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Error Codes:</b></p>
 * <ul>
 *   <li>KNOWLEDGE_NOT_FOUND - Requested knowledge entity does not exist</li>
 *   <li>INVALID_KNOWLEDGE - Knowledge entity structure is invalid</li>
 *   <li>INVALID_NODE - Knowledge node structure is invalid</li>
 *   <li>INVALID_RELATIONSHIP - Knowledge relationship structure is invalid</li>
 *   <li>INVALID_GRAPH - Knowledge graph structure is invalid</li>
 *   <li>GRAPH_VALIDATION_FAILED - Knowledge graph validation failed</li>
 *   <li>EXTRACTION_FAILED - Knowledge extraction operation failed</li>
 *   <li>VALIDATION_FAILED - Knowledge validation failed</li>
 *   <li>UNKNOWN_ERROR - Unknown or unspecified error</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-104, EIO-ARCH-001</p>
 */
public enum KnowledgeErrorCode {
    /**
     * Requested knowledge entity does not exist.
     */
    KNOWLEDGE_NOT_FOUND,

    /**
     * Knowledge entity structure is invalid.
     */
    INVALID_KNOWLEDGE,

    /**
     * Knowledge node structure is invalid.
     */
    INVALID_NODE,

    /**
     * Knowledge relationship structure is invalid.
     */
    INVALID_RELATIONSHIP,

    /**
     * Knowledge graph structure is invalid.
     */
    INVALID_GRAPH,

    /**
     * Knowledge graph validation failed.
     */
    GRAPH_VALIDATION_FAILED,

    /**
     * Knowledge extraction operation failed.
     */
    EXTRACTION_FAILED,

    /**
     * Knowledge validation failed.
     */
    VALIDATION_FAILED,

    /**
     * Unknown or unspecified error.
     */
    UNKNOWN_ERROR
}