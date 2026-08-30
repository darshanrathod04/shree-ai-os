package com.shreeai.os.platform.kernels.tool.error;

/**
 * <b>ToolErrorCode</b> — Enumerates error codes for the Tool Kernel.
 *
 * <p><b>Ownership:</b> Tool Kernel — Error</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ToolErrorCode {
    VALIDATION_FAILURE("EIO-TK-001", "Tool request validation failed"),
    UNSUPPORTED_TOOL("EIO-TK-002", "Unsupported tool type or action"),
    EXECUTION_FAILURE("EIO-TK-003", "Tool execution failed"),
    TIMEOUT("EIO-TK-004", "Tool execution timed out"),
    AUTHENTICATION_FAILED("EIO-TK-005", "Tool authentication failed"),
    RESOURCE_NOT_FOUND("EIO-TK-006", "Tool resource not found");

    private final String code;
    private final String description;

    ToolErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() { return code; }
    public String description() { return description; }
}
