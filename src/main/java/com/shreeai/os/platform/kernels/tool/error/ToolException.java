package com.shreeai.os.platform.kernels.tool.error;

/**
 * <b>ToolException</b> — Base exception for the Tool Kernel.
 *
 * <p><b>Ownership:</b> Tool Kernel — Error</p>
 * <p><b>Version:</b> 1.0</p>
 */
public class ToolException extends RuntimeException {

    private final ToolError toolError;

    public ToolException(ToolError toolError) {
        super(toolError.message(), null);
        this.toolError = toolError;
    }

    public ToolException(ToolError toolError, Throwable cause) {
        super(toolError.message(), cause);
        this.toolError = toolError;
    }

    public ToolError toolError() { return toolError; }
}
