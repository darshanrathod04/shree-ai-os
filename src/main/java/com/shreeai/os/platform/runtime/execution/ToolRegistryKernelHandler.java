package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link KernelHandler} that bridges the Tool Kernel's {@link ToolService}
 * to the Runtime execution dispatch layer. Registered for the
 * {@link ExecutionCapability#TASK_EXECUTION} capability, it translates
 * dispatch input + context into a {@link ToolRequest}, executes it, and
 * converts the {@link ToolResult} into a {@link RichExecutionResult}.
 *
 * @since 2.1
 */
public final class ToolRegistryKernelHandler implements KernelHandler {

    private final ToolService toolService;

    public ToolRegistryKernelHandler(ToolService toolService) {
        this.toolService = Objects.requireNonNull(
                toolService, "toolService must not be null");
    }

    @Override
    public RichExecutionResult handle(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {

        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(context, "context must not be null");

        try {
            ToolRequest request = buildToolRequest(input, context);
            ToolResult result = toolService.executeTool(request);

            Map<String, Object> metadata = Map.of(
                    "toolId", result.toolId(),
                    "toolType", result.toolType().value(),
                    "action", result.action(),
                    "toolStatus", result.status().value(),
                    "toolOutput", result.output());

            String output = result.status() == ToolStatus.COMPLETED
                    ? formatSuccessOutput(result)
                    : formatFailureOutput(result);

            double confidence = result.status() == ToolStatus.COMPLETED ? 0.9 : 0.0;

            return RichExecutionResult.builder()
                    .capability(capability)
                    .status(result.status() == ToolStatus.COMPLETED
                            ? ExecutionStatus.SUCCESS
                            : ExecutionStatus.FAILED)
                    .output(output)
                    .confidence(confidence)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            return RichExecutionResult.failure(
                    capability,
                    "Tool execution failed: " + e.getMessage());
        }
    }

    private ToolRequest buildToolRequest(String input, Map<String, Object> context) {
        String toolId = (String) context.getOrDefault("toolId", "default");
        String toolTypeStr = (String) context.getOrDefault("toolType", "files");
        ToolType toolType = safeToolType(toolTypeStr);
        String action = (String) context.getOrDefault("action", "execute");

        Map<String, Object> parameters = extractParameters(input, context);

        return new ToolRequest(toolId, toolType, action, parameters);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParameters(String input, Map<String, Object> context) {
        Map<String, Object> params = new java.util.HashMap<>();
        if (input != null && !input.isBlank()) {
            params.put("input", input);
        }
        Object ctxParams = context.get("parameters");
        if (ctxParams instanceof Map) {
            params.putAll((Map<String, Object>) ctxParams);
        }
        return params;
    }

    private ToolType safeToolType(String value) {
        try {
            return ToolType.fromValue(value);
        } catch (IllegalArgumentException e) {
            return ToolType.FILES;
        }
    }

    private String formatSuccessOutput(ToolResult result) {
        Object answer = result.getOutput("answer");
        if (answer instanceof String s && !s.isBlank()) {
            return s;
        }
        Object resultData = result.output().get("result");
        if (resultData instanceof String s && !s.isBlank()) {
            return s;
        }
        return "Tool '" + result.toolId() + "' executed successfully";
    }

    private String formatFailureOutput(ToolResult result) {
        String error = result.errorMessage();
        return error != null ? error : "Tool '" + result.toolId() + "' execution failed";
    }
}
