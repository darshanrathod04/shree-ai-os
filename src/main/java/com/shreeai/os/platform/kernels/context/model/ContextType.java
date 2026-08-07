package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>ContextType</b>
 *
 * <p>Defines the type of a Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible types of Context.</li>
 *   <li>Provides type safety for Context categorization.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Context Types:</b></p>
 * <ul>
 *   <li>CONVERSATION - Active conversation context</li>
 *   <li>EXECUTION - Runtime execution context</li>
 *   <li>TASK - Current task context</li>
 *   <li>SESSION - Session context</li>
 *   <li>WORKING - Temporary working context</li>
 *   <li>ENVIRONMENTAL - Environmental context</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101</p>
 */
public enum ContextType {
    /**
     * Active conversation context.
     */
    CONVERSATION,

    /**
     * Runtime execution context.
     */
    EXECUTION,

    /**
     * Current task context.
     */
    TASK,

    /**
     * Session context.
     */
    SESSION,

    /**
     * Temporary working context.
     */
    WORKING,

    /**
     * Environmental context.
     */
    ENVIRONMENTAL
}