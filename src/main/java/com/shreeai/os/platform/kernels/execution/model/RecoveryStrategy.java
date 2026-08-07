package com.shreeai.os.platform.kernels.execution.model;

/**
 * <b>RecoveryStrategy</b>
 *
 * <p>Represents immutable recovery information.
 * This enumeration defines all supported recovery strategies.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines recovery strategy types.</li>
 *   <li>Provides clear recovery semantics.</li>
 *   <li>Enables strategy-based recovery selection.</li>
 *   <li>Contains no recovery implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum RecoveryStrategy {

    /**
     * Retry the failed execution without modification.
     */
    RETRY,

    /**
     * Rollback to a previous execution state.
     */
    ROLLBACK,

    /**
     * Execute compensation logic to undo partial execution.
     */
    COMPENSATE,

    /**
     * Skip the failed action and continue.
     */
    SKIP,

    /**
     * Fail the entire workflow.
     */
    FAIL,

    /**
     * Use default recovery strategy based on execution context.
     */
    DEFAULT
}