package com.shreeai.os.platform.runtime.lifecycle;

/**
 * <b>RuntimeState</b>
 *
 * <p>Enumeration of all possible states in the Runtime lifecycle.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the finite set of states a Runtime instance can occupy.</li>
 *   <li>Enables state validation and transition guards in {@link RuntimeLifecycle}.</li>
 * </ul>
 *
 * <p><b>State Transitions:</b></p>
 * <pre>
 * INITIALIZING → READY → ACTIVE → DRAINING → STOPPED
 *                 ↑        ↓
 *                 └─── IDLE ──┘
 * Any state → FAILED
 * </pre>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 *
 * @see RuntimeLifecycle
 */
public enum RuntimeState {

    /**
     * Runtime is initializing resources. No execution accepted.
     */
    INITIALIZING,

    /**
     * Runtime is ready and accepting execution requests.
     */
    READY,

    /**
     * Runtime has active execution sessions.
     */
    ACTIVE,

    /**
     * Runtime is idle with no active sessions but ready for new work.
     */
    IDLE,

    /**
     * Runtime is draining active sessions before shutdown.
     */
    DRAINING,

    /**
     * Runtime has been stopped. No execution accepted.
     */
    STOPPED,

    /**
     * Runtime has encountered an unrecoverable error.
     */
    FAILED
}