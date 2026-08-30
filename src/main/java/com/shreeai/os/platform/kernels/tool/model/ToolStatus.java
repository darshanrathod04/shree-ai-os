package com.shreeai.os.platform.kernels.tool.model;

/**
 * <b>ToolStatus</b>
 *
 * <p>Enumerates the lifecycle status of a tool execution.</p>
 *
 * <p><b>Ownership:</b> Tool Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> TK-103, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum ToolStatus {

    /** The tool execution has not yet started. */
    PENDING("pending"),

    /** The tool execution is in progress. */
    RUNNING("running"),

    /** The tool execution completed successfully. */
    COMPLETED("completed"),

    /** The tool execution failed. */
    FAILED("failed");

    private final String value;

    ToolStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the string value of this tool status.
     *
     * @return the string value
     */
    public String value() {
        return value;
    }

    /**
     * Returns whether this status represents a terminal state.
     *
     * @return {@code true} if this status is terminal (COMPLETED or FAILED)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
