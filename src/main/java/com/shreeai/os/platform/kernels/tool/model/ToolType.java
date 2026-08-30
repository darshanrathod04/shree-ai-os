package com.shreeai.os.platform.kernels.tool.model;

/**
 * <b>ToolType</b>
 *
 * <p>Enumerates the concrete tool types available within the Tool Kernel.
 * Each tool type corresponds to a category of system operations the AI
 * can perform autonomously (e.g., Git operations, file I/O, browser
 * automation, database queries).</p>
 *
 * <p><b>Ownership:</b> Tool Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> TK-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum ToolType {

    /** Version control operations (clone, commit, push, pull, branch, etc.). */
    GIT("git"),

    /** File system operations (read, write, delete, list, move, copy, etc.). */
    FILES("files"),

    /** Browser automation (navigate, click, type, screenshot, extract, etc.). */
    BROWSER("browser"),

    /** Database operations (query, insert, update, delete, schema, etc.). */
    DB("db");

    private final String value;

    ToolType(String value) {
        this.value = value;
    }

    /**
     * Returns the string value of this tool type.
     *
     * @return the string value
     */
    public String value() {
        return value;
    }

    /**
     * Resolves a tool type from its string value.
     *
     * @param value the string value
     * @return the matching ToolType
     * @throws IllegalArgumentException if the value does not match any tool type
     */
    public static ToolType fromValue(String value) {
        for (ToolType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tool type: " + value);
    }
}
