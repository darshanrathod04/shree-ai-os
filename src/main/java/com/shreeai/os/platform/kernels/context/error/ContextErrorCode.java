package com.shreeai.os.platform.kernels.context.error;

/**
 * <b>ContextErrorCode</b>
 *
 * <p>Standardized error identifiers for the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides standardized error codes for Context operations.</li>
 *   <li>Ensures consistent error identification across the platform.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Error Codes:</b></p>
 * <ul>
 *   <li>CONTEXT_NOT_FOUND - Requested context does not exist</li>
 *   <li>INVALID_CONTEXT - Context structure is invalid</li>
 *   <li>INVALID_STATE - Context state is invalid</li>
 *   <li>INVALID_PRIORITY - Context priority is invalid</li>
 *   <li>INVALID_SCOPE - Context scope is invalid</li>
 *   <li>SNAPSHOT_FAILED - Context snapshot operation failed</li>
 *   <li>LIFECYCLE_FAILED - Context lifecycle operation failed</li>
 *   <li>VALIDATION_FAILED - Context validation failed</li>
 *   <li>UNKNOWN_ERROR - Unknown or unspecified error</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-104</p>
 */
public enum ContextErrorCode {
    /**
     * Requested context does not exist.
     */
    CONTEXT_NOT_FOUND,

    /**
     * Context structure is invalid.
     */
    INVALID_CONTEXT,

    /**
     * Context state is invalid.
     */
    INVALID_STATE,

    /**
     * Context priority is invalid.
     */
    INVALID_PRIORITY,

    /**
     * Context scope is invalid.
     */
    INVALID_SCOPE,

    /**
     * Context snapshot operation failed.
     */
    SNAPSHOT_FAILED,

    /**
     * Context lifecycle operation failed.
     */
    LIFECYCLE_FAILED,

    /**
     * Context validation failed.
     */
    VALIDATION_FAILED,

    /**
     * Unknown or unspecified error.
     */
    UNKNOWN_ERROR
}