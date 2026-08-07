package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>ContextState</b>
 *
 * <p>Defines the state of a Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible states of a Context.</li>
 *   <li>Provides state safety for Context lifecycle.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Context States:</b></p>
 * <ul>
 *   <li>ACTIVE - Context is active and can be modified</li>
 *   <li>SUSPENDED - Context is preserved but cannot be modified</li>
 *   <li>EXPIRED - Context has reached its end of life</li>
 *   <li>ARCHIVED - Context is archived for historical reference</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101</p>
 */
public enum ContextState {
    /**
     * Context is active and can be modified.
     */
    ACTIVE,

    /**
     * Context is preserved but cannot be modified.
     */
    SUSPENDED,

    /**
     * Context has reached its end of life.
     */
    EXPIRED,

    /**
     * Context is archived for historical reference.
     */
    ARCHIVED
}