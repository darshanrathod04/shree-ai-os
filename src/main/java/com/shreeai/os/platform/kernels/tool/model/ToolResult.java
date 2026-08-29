package com.shreeai.os.platform.kernels.tool.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ToolResult</b> — Immutable outcome of a tool execution.
 *
 * <p><b>Ownership:</b> Tool Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ToolResult {

    private final String toolId;
    private final ToolType toolType;
    private final String action;
    private final ToolStatus status;
    private final Map<String, Object> output;
    private final ToolExecutionMetrics metrics;
    private final Instant completedAt;

    public ToolResult(
            String toolId, ToolType toolType, String action,
            ToolStatus status, Map<String, Object> output,
            ToolExecutionMetrics metrics, Instant completedAt) {
        if (toolId == null) throw new IllegalArgumentException("ToolResult toolId must not be null");
        if (toolType == null) throw new IllegalArgumentException("ToolResult toolType must not be null");
        if (action == null || action.trim().isEmpty()) throw new IllegalArgumentException("ToolResult action must not be null or empty");
        if (status == null) throw new IllegalArgumentException("ToolResult status must not be null");
        if (output == null) throw new IllegalArgumentException("ToolResult output must not be null");
        if (metrics == null) throw new IllegalArgumentException("ToolResult metrics must not be null");
        if (completedAt == null) throw new IllegalArgumentException("ToolResult completedAt must not be null");

        this.toolId = toolId;
        this.toolType = toolType;
        this.action = action;
        this.status = status;
        this.output = Map.copyOf(output);
        this.metrics = metrics;
        this.completedAt = completedAt;
    }

    public String toolId() { return toolId; }
    public ToolType toolType() { return toolType; }
    public String action() { return action; }
    public ToolStatus status() { return status; }
    public Map<String, Object> output() { return output; }
    public ToolExecutionMetrics metrics() { return metrics; }
        public Instant completedAt() { return completedAt; }

    @SuppressWarnings("unchecked")
    public <T> T getOutput(String key) {
        Object value = output.get(key);
        return value == null ? null : (T) value;
    }

    public String errorMessage() {
        return status == ToolStatus.FAILED
                ? String.valueOf(output.getOrDefault("error", "unknown"))
                : null;
    }

    public static ToolResult success(
            String toolId, ToolType toolType, String action,
            Map<String, Object> output, ToolExecutionMetrics metrics) {
        return new ToolResult(toolId, toolType, action, ToolStatus.COMPLETED,
                output, metrics, Instant.now());
    }

    public static ToolResult failure(
            String toolId, ToolType toolType, String action,
            String error, ToolExecutionMetrics metrics) {
        Map<String, Object> errorOutput = new HashMap<>();
        errorOutput.put("error", error);
        return new ToolResult(toolId, toolType, action, ToolStatus.FAILED,
                Collections.unmodifiableMap(errorOutput), metrics, Instant.now());
    }

    public static Map<String, Object> emptyOutput() {
        return Collections.emptyMap();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ToolResult that = (ToolResult) obj;
        return toolId.equals(that.toolId) && toolType == that.toolType &&
                action.equals(that.action) && status == that.status &&
                output.equals(that.output) && Objects.equals(metrics, that.metrics) &&
                completedAt.equals(that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolId, toolType, action, status, output, metrics, completedAt);
    }

    @Override
    public String toString() {
        return "ToolResult{toolId='" + toolId + "', toolType=" + toolType +
                ", action='" + action + "', status=" + status + '}';
    }
}
