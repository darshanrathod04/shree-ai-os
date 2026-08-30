package com.shreeai.os.platform.kernels.tool.engine;

import com.shreeai.os.platform.kernels.tool.model.ToolExecutionMetrics;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>DefaultToolProcessingEngine</b> — Canonical engine implementation for
 * the Tool Kernel. Computes deterministic tool execution outcomes.
 *
 * <p><b>Ownership:</b> Tool Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultToolProcessingEngine implements ToolProcessingEngine {

    public DefaultToolProcessingEngine() {
        // Stateless engine
    }

    @Override
    public ToolResult processToolExecution(ToolRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ToolRequest must not be null");
        }

        Instant startedAt = Instant.now();

        try {
            ToolResult result = dispatch(request);
            Instant completedAt = Instant.now();
            return withMetrics(result, new ToolExecutionMetrics(
                    completedAt.toEpochMilli() - startedAt.toEpochMilli(),
                    startedAt, completedAt, Map.of("engine", "DefaultToolProcessingEngine")
            ));
        } catch (Exception e) {
            Instant completedAt = Instant.now();
            Map<String, Object> errorOutput = new HashMap<>();
            errorOutput.put("error", e.getMessage());
            errorOutput.put("exceptionType", e.getClass().getSimpleName());
            return new ToolResult(
                    request.toolId(), request.toolType(), request.action(),
                    ToolStatus.FAILED, errorOutput,
                    new ToolExecutionMetrics(
                            completedAt.toEpochMilli() - startedAt.toEpochMilli(),
                            startedAt, completedAt, Map.of()
                    ),
                    completedAt
            );
        }
    }

    private ToolResult dispatch(ToolRequest request) {
        ToolType type = request.toolType();
        String action = request.action().toLowerCase();
        Map<String, Object> params = request.parameters();

        switch (type) {
            case GIT -> { return processGitAction(request, action, params); }
            case FILES -> { return processFilesAction(request, action, params); }
            case BROWSER -> { return processBrowserAction(request, action, params); }
            case DB -> { return processDbAction(request, action, params); }
            default -> throw new IllegalArgumentException("Unsupported tool type: " + type);
        }
    }

    private ToolResult processGitAction(ToolRequest request, String action, Map<String, Object> params) {
        Map<String, Object> output = new HashMap<>();
        switch (action) {
            case "clone" -> output.put("repoUrl", params.get("repoUrl"));
            case "commit" -> output.put("message", params.get("message"));
            case "push" -> output.put("branch", params.getOrDefault("branch", "main"));
            case "pull" -> output.put("branch", params.getOrDefault("branch", "main"));
            case "branch" -> output.put("name", params.get("name"));
            case "status" -> output.put("clean", true);
            case "log" -> output.put("entries", List.of());
            case "diff" -> output.put("changes", Map.of());
            default -> throw new IllegalArgumentException("Unknown Git action: " + action);
        }
        output.put("simulated", true);
        return ToolResult.success(request.toolId(), request.toolType(),
                request.action(), output, ToolExecutionMetrics.empty());
    }

    private ToolResult processFilesAction(ToolRequest request, String action, Map<String, Object> params) {
        Map<String, Object> output = new HashMap<>();
        switch (action) {
            case "read" -> output.put("content", params.getOrDefault("content", ""));
            case "write" -> output.put("bytesWritten", params.getOrDefault("content", "").toString().length());
            case "delete" -> output.put("deleted", params.get("path"));
            case "list" -> output.put("entries", List.of());
            case "move" -> output.put("from", params.get("from"));
            case "copy" -> output.put("from", params.get("from"));
            case "exists" -> output.put("exists", true);
            default -> throw new IllegalArgumentException("Unknown Files action: " + action);
        }
        output.put("simulated", true);
        return ToolResult.success(request.toolId(), request.toolType(),
                request.action(), output, ToolExecutionMetrics.empty());
    }

    private ToolResult processBrowserAction(ToolRequest request, String action, Map<String, Object> params) {
        Map<String, Object> output = new HashMap<>();
        switch (action) {
            case "navigate" -> output.put("url", params.get("url"));
            case "click" -> output.put("selector", params.get("selector"));
            case "type" -> output.put("selector", params.get("selector"));
            case "screenshot" -> output.put("base64", params.getOrDefault("base64", ""));
            case "extract" -> output.put("content", params.getOrDefault("content", ""));
            case "scroll" -> output.put("direction", params.getOrDefault("direction", "down"));
            default -> throw new IllegalArgumentException("Unknown Browser action: " + action);
        }
        output.put("simulated", true);
        return ToolResult.success(request.toolId(), request.toolType(),
                request.action(), output, ToolExecutionMetrics.empty());
    }

    private ToolResult processDbAction(ToolRequest request, String action, Map<String, Object> params) {
        Map<String, Object> output = new HashMap<>();
        switch (action) {
            case "query" -> output.put("rows", List.of());
            case "insert" -> output.put("insertedId", params.getOrDefault("id", ""));
            case "update" -> output.put("rowsAffected", 0);
            case "delete" -> output.put("rowsAffected", 0);
            case "schema" -> output.put("tables", List.of());
            case "tables" -> output.put("tables", List.of());
            default -> throw new IllegalArgumentException("Unknown DB action: " + action);
        }
        output.put("simulated", true);
        return ToolResult.success(request.toolId(), request.toolType(),
                request.action(), output, ToolExecutionMetrics.empty());
    }

    private ToolResult withMetrics(ToolResult result, ToolExecutionMetrics metrics) {
        return new ToolResult(
                result.toolId(), result.toolType(), result.action(),
                result.status(), result.output(),
                metrics, result.completedAt()
        );
    }
}
