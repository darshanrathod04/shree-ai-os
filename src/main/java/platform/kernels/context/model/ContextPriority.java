package platform.kernels.context.model;

/**
 * <b>ContextPriority</b>
 *
 * <p>Defines the priority levels for Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible priority levels for Context.</li>
 *   <li>Provides priority safety for Context lifecycle management.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Priority Levels:</b></p>
 * <ul>
 *   <li>LOW - Low priority context</li>
 *   <li>NORMAL - Standard priority context</li>
 *   <li>HIGH - High priority context</li>
 *   <li>CRITICAL - Critical priority context</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-CTX-102</p>
 */
public enum ContextPriority {
    /**
     * Low priority context.
     */
    LOW,

    /**
     * Standard priority context.
     */
    NORMAL,

    /**
     * High priority context.
     */
    HIGH,

    /**
     * Critical priority context.
     */
    CRITICAL
}
