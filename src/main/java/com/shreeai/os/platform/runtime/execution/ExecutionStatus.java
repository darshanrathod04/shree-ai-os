package com.shreeai.os.platform.runtime.execution;

/**
 * <b>ExecutionStatus</b>
 *
 * <p>Enumerates the terminal states of a capability-driven execution that
 * was dispatched through the {@link ExecutionDispatcher}.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public enum ExecutionStatus {

    /** Execution completed successfully. */
    SUCCESS("SUCCESS"),

    /** Execution failed with an error. */
    FAILED("FAILED"),

    /** Execution was denied by the permission policy. */
    DENIED("DENIED"),

    /** Execution is pending an explicit human or autonomous approval. */
    PENDING_APPROVAL("PENDING_APPROVAL");

    private final String value;

    ExecutionStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the canonical string value of this status.
     *
     * @return the canonical value (never null)
     */
    public String value() {
        return value;
    }

    /**
     * Returns whether this status represents a successful execution.
     *
     * @return true when the status is SUCCESS
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
