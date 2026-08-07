package com.shreeai.os.platform.kernels.identity.api;

/**
 * <b>IdentityKernel</b>
 *
 * <p>The primary public entry point for the Identity Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the unified public interface for all Identity operations.</li>
 *   <li>Serves as the single entry point through which other kernels communicate with Identity.</li>
 *   <li>Aggregates command, query, and event contracts into a cohesive kernel interface.</li>
 *   <li>Enforces the principle that no kernel accesses Identity internals directly.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All Identity operations MUST flow through this interface.
 * Direct access to Identity internals is prohibited (KERNEL-ISO-001).</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106, KERNEL-ISO-001</p>
 *
 * @see IdentityCommands
 * @see IdentityQueries
 * @see IdentityEvents
 * @see IdentityContract
 */
public interface IdentityKernel {

    /**
     * Returns the command interface for Identity operations that modify state.
     *
     * <p>Commands request changes to Identity. All mutating operations MUST
     * be performed through this interface.</p>
     *
     * @return the IdentityCommands interface for state-modifying operations
     */
    IdentityCommands commands();

    /**
     * Returns the query interface for Identity read-only operations.
     *
     * <p>Queries retrieve Identity data without modifying state. All read
     * operations MUST be performed through this interface.</p>
     *
     * @return the IdentityQueries interface for read-only operations
     */
    IdentityQueries queries();

    /**
     * Returns the event interface for Identity event definitions.
     *
     * <p>Events represent significant occurrences within the Identity Kernel.
     * This interface provides access to all event types that Identity can publish.</p>
     *
     * @return the IdentityEvents interface for event definitions
     */
    IdentityEvents events();
}