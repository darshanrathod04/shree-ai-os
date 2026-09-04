package com.shreeai.os.platform.runtime.api;

import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionPipeline;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeState;

/**
 * <b>Runtime</b>
 *
 * <p>The primary Runtime interface for Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the public API for interacting with the Runtime Kernel.</li>
 *   <li>Owns the Runtime lifecycle and state transitions.</li>
 *   <li>Orchestrates execution sessions through the ExecutionPipeline.</li>
 *   <li>Enforces the RuntimeContract on all execution.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> A Runtime instance MUST only accept new execution requests
 * when its state is {@link RuntimeState#READY}.</p>
 *
 * @see RuntimeBuilder
 * @see RuntimeConfiguration
 * @see RuntimeLifecycle
 * @see ExecutionPipeline
 */
public interface Runtime {

    /**
     * Returns the RuntimeConfiguration used to configure this Runtime instance.
     *
     * @return the runtime configuration
     */
    RuntimeConfiguration configuration();

    /**
     * Returns the RuntimeLifecycle that manages lifecycle transitions.
     *
     * @return the runtime lifecycle manager
     */
    RuntimeLifecycle lifecycle();

    /**
     * Returns the RuntimeContract that governs all execution within this Runtime.
     *
     * @return the runtime contract
     */
    RuntimeContract contract();

    /**
     * Returns the ExecutionPipeline used to process execution requests.
     *
     * @return the execution pipeline
     */
    ExecutionPipeline pipeline();

    /**
     * Submits an ExecutionRequest for processing.
     *
     * <p>The Runtime MUST be in {@link RuntimeState#READY} state to accept requests.
     *
     * @param request the execution request to process
     * @return the session created for tracking this execution
     * @throws IllegalStateException if Runtime is not in READY state
     */
    ExecutionSession submit(ExecutionRequest request);

    /**
     * Begins the Runtime lifecycle, transitioning from INITIALIZING to READY.
     *
     * @throws IllegalStateException if starting is not possible from the current state
     */
    void start();

    /**
     * Gracefully stops the Runtime, completing all active sessions.
     *
     * @throws IllegalStateException if stopping is not possible from the current state
     */
    void stop();

    /**
     * Forces an immediate shutdown of the Runtime, aborting active sessions.
     *
     * @throws IllegalStateException if shutdown is not possible from the current state
     */
    void shutdown();

    /**
     * Binds the given SDK event bus to this Runtime so runtime-side event
     * consumers (e.g. the knowledge ingestion consumer) can act on SDK
     * published events.
     *
     * <p>This is an additive, non-breaking extension point: the default
     * implementation is a no-op, so every existing Runtime implementation
     * remains source- and binary-compatible. The default
     * {@code DefaultRuntimeService} subscribes its event-driven kernel
     * consumers here.</p>
     *
     * @param eventBus the SDK event bus to bind (may be null; implementations
     *                 MUST tolerate null by doing nothing)
     */
    default void bindEventBus(com.shreeai.os.platform.sdk.events.RuntimeEventBus eventBus) {
        // no-op by default — additive extension point
    }
}