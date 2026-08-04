package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>ContextScope</b>
 *
 * <p>Defines the scope levels for Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible scope levels for Context.</li>
 *   <li>Provides scope safety for Context visibility and lifecycle.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Scope Levels:</b></p>
 * <ul>
 *   <li>LOCAL - Context is local to a single operation</li>
 *   <li>REQUEST - Context spans a single request/response cycle</li>
 *   <li>SESSION - Context spans an entire user session</li>
 *   <li>GLOBAL - Context is globally accessible</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-CTX-102</p>
 */
public enum ContextScope {
    /**
     * Context is local to a single operation.
     */
    LOCAL,

    /**
     * Context spans a single request/response cycle.
     */
    REQUEST,

    /**
     * Context spans an entire user session.
     */
    SESSION,

    /**
     * Context is globally accessible.
     */
    GLOBAL
}