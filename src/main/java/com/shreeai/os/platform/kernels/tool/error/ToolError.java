package com.shreeai.os.platform.kernels.tool.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ToolError</b> — Immutable error value object for the Tool Kernel.
 *
 * <p><b>Ownership:</b> Tool Kernel — Error</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ToolError {

    private final ToolErrorCode errorCode;
    private final String message;
    private final String toolId;
    private final Instant occurredAt;
    private final Map<String, Object> metadata;

    public ToolError(
            ToolErrorCode errorCode,
            String message,
            String toolId,
            Instant occurredAt,
            Map<String, Object> metadata) {
        if (errorCode == null) throw new IllegalArgumentException("errorCode must not be null");
        if (message == null) throw new IllegalArgumentException("message must not be null");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("metadata must not be null");

        this.errorCode = errorCode;
        this.message = message;
        this.toolId = toolId;
        this.occurredAt = occurredAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ToolError(
            ToolErrorCode errorCode,
            String message,
            String toolId,
            Instant occurredAt) {
        this(errorCode, message, toolId, occurredAt, Collections.emptyMap());
    }

    public ToolErrorCode errorCode() { return errorCode; }
    public String message() { return message; }
    public String toolId() { return toolId; }
    public Instant occurredAt() { return occurredAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ToolError toolError = (ToolError) obj;
        return errorCode == toolError.errorCode &&
                Objects.equals(message, toolError.message) &&
                Objects.equals(toolId, toolError.toolId) &&
                Objects.equals(occurredAt, toolError.occurredAt) &&
                Objects.equals(metadata, toolError.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message, toolId, occurredAt, metadata);
    }

    @Override
    public String toString() {
        return "ToolError{errorCode=" + errorCode + ", message='" + message +
                "', toolId=" + toolId + '}';
    }
}
