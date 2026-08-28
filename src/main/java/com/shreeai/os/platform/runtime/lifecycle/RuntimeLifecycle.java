package com.shreeai.os.platform.runtime.lifecycle;

import com.shreeai.os.platform.runtime.api.Runtime;

/**
 * <b>RuntimeLifecycle</b>
 *
 * <p>Manages the lifecycle state transitions of a {@link Runtime}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Owns the current {@link RuntimeState} of the Runtime instance.</li>
 *   <li>Validates and enforces all state transitions.</li>
 *   <li>Provides lifecycle hooks for pre- and post-transition actions.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> The Runtime MUST always be in exactly one valid RuntimeState.</p>
 *
 * @see RuntimeState
 */
public interface RuntimeLifecycle {

    /**
     * Returns the current state of the Runtime.
     *
     * @return the current RuntimeState
     */
    RuntimeState currentState();

    /**
     * Transitions the Runtime to the READY state.
     *
     * @throws IllegalStateException if transition is not valid from the current state
     */
    void start();

    /**
     * Transitions the Runtime to the DRAINING state, beginning graceful shutdown.
     *
     * @throws IllegalStateException if transition is not valid from the current state
     */
    void stop();

    /**
     * Transitions the Runtime to the STOPPED state immediately.
     *
     * @throws IllegalStateException if transition is not valid from the current state
     */
    void shutdown();

    /**
     * Transitions the Runtime to the FAILED state.
     *
     * @param cause the cause of the failure
     * @throws IllegalStateException if transition is not valid from the current state
     */
    void fail(Throwable cause);

    /**
     * Returns whether the Runtime is currently accepting execution requests.
     *
     * @return true if the Runtime is in READY or IDLE state
     */
    boolean isAcceptingRequests();

    /**
     * Registers a listener to be notified of state transitions.
     *
     * @param listener the listener to register
     */
    void addListener(RuntimeLifecycleListener listener);

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(RuntimeLifecycleListener listener);
}