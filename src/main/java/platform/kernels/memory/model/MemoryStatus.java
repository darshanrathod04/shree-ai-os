package platform.kernels.memory.model;

/**
 * <b>MemoryStatus</b>
 *
 * <p>Defines the lifecycle status of a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable enumeration of Memory lifecycle states.</li>
 *   <li>Enables type-safe status tracking.</li>
 *   <li>Supports memory lifecycle management.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure enumeration with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 */
public enum MemoryStatus {
    /** Memory is active and accessible */
    ACTIVE,
    /** Memory is archived but retrievable */
    ARCHIVED,
    /** Memory is marked for deletion */
    PENDING_DELETION,
    /** Memory has been permanently deleted */
    DELETED
}