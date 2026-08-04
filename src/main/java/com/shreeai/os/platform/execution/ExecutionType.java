package com.shreeai.os.platform.execution;

/**
 * Execution type enumeration for the Runtime Layer.
 *
 * <p>This enum defines how a capability should be executed.
 * The execution type determines the execution strategy and behavior.</p>
 *
 * <p>This is part of the stable execution contract (ABI) for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
public enum ExecutionType {

    /**
     * Synchronous execution - blocks until completion.
     * The caller waits for the result.
     */
    SYNC,

    /**
     * Asynchronous execution - returns immediately with a future/promise.
     * The caller can poll or wait for completion.
     */
    ASYNC,

    /**
     * Background execution - fire-and-forget pattern.
     * No result is expected or returned to the caller.
     */
    BACKGROUND,

    /**
     * Streaming execution - returns a stream of results.
     * Used for long-running operations that produce incremental results.
     */
    STREAMING,

    /**
     * Execution type is unknown or not specified.
     */
    UNKNOWN
}