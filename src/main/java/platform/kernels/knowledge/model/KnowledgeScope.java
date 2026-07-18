package platform.kernels.knowledge.model;

/**
 * <b>KnowledgeScope</b>
 *
 * <p>Defines the scope or visibility boundary of a knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible visibility scopes for knowledge entities.</li>
 *   <li>Provides scope safety for knowledge access control.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Knowledge Scopes:</b></p>
 * <ul>
 *   <li>GLOBAL - Knowledge entity is visible across the entire platform</li>
 *   <li>SYSTEM - Knowledge entity is visible within the current system context</li>
 *   <li>SESSION - Knowledge entity is visible within the current session</li>
 *   <li>PRIVATE - Knowledge entity is private to a specific identity or kernel</li>
 *   <li>TRANSIENT - Knowledge entity is temporary and may be discarded</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 */
public enum KnowledgeScope {
    /**
     * Knowledge entity is visible across the entire platform.
     */
    GLOBAL,

    /**
     * Knowledge entity is visible within the current system context.
     */
    SYSTEM,

    /**
     * Knowledge entity is visible within the current session.
     */
    SESSION,

    /**
     * Knowledge entity is private to a specific identity or kernel.
     */
    PRIVATE,

    /**
     * Knowledge entity is temporary and may be discarded.
     */
    TRANSIENT
}