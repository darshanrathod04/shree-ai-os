package com.shreeai.os.platform.execution;

/**
 * Execution status enumeration for the Runtime Layer.
 *
 * <p>This enum represents the final state of an execution attempt.
 * Every capability execution must return one of these statuses.</p>
 *
 * <p>This is part of the stable execution contract (ABI) for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
public enum ExecutionStatus {

    /**
     * Execution completed successfully.
     */
    SUCCESS,

    /**
     * Execution failed due to an error.
     */
    FAILED,

    /**
     * Execution was cancelled before completion.
     */
    CANCELLED,

    /**
     * Execution exceeded the allowed time limit.
     */
    TIMEOUT,

    /**
     * Execution failed but should be retried.
     */
    RETRY,

    /**
     * Execution completed with partial success.
     * Some operations succeeded while others failed.
     */
    PARTIAL_SUCCESS,

    /**
     * Execution status is unknown or could not be determined.
     */
    UNKNOWN
}