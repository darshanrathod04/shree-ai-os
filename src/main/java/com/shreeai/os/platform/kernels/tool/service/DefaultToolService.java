package com.shreeai.os.platform.kernels.tool.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.kernels.tool.engine.ToolProcessingEngine;
import com.shreeai.os.platform.kernels.tool.error.ToolError;
import com.shreeai.os.platform.kernels.tool.error.ToolErrorCode;
import com.shreeai.os.platform.kernels.tool.error.ToolException;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;
import com.shreeai.os.platform.kernels.tool.validation.ToolValidationResult;
import com.shreeai.os.platform.kernels.tool.validation.ToolValidator;

/**
 * <b>DefaultToolService</b> — Canonical service implementation for the Tool Kernel.
 * Orchestrates tool execution by delegating to the Validation Layer and
 * Processing Engine, and translates failures into the canonical
 * {@link ToolException} hierarchy.
 *
 * <p><b>Ownership:</b> Tool Kernel — Service Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since 1.0
 */
public final class DefaultToolService implements ToolService {

    private final ToolValidator validator;
    private final ToolProcessingEngine processingEngine;
    private final Map<String, ToolResult> resultStore;

    /**
     * Constructs a {@code DefaultToolService} with the specified dependencies.
     *
     * @param validator        the tool validator (must not be {@code null})
     * @param processingEngine the tool processing engine (must not be {@code null})
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public DefaultToolService(
            ToolValidator validator,
            ToolProcessingEngine processingEngine) {
        this.validator = Objects.requireNonNull(
                validator, "ToolValidator must not be null");
        this.processingEngine = Objects.requireNonNull(
                processingEngine, "ToolProcessingEngine must not be null");
        this.resultStore = new ConcurrentHashMap<>();
    }

    /**
     * Constructs a {@code DefaultToolService} with default validator and engine.
     */
    public DefaultToolService() {
        this(new ToolValidator(),
                new com.shreeai.os.platform.kernels.tool.engine.DefaultToolProcessingEngine());
    }

    @Override
    public ToolResult executeTool(ToolRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ToolRequest must not be null");
        }

        ToolValidationResult validationResult = validator.validate(request);
        if (!validationResult.valid()) {
            throw new ToolException(createValidationError(request, validationResult));
        }

        try {
            ToolResult result = processingEngine.processToolExecution(request);
            resultStore.put(request.toolId(), result);
            return result;
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException(createExecutionError(request, e), e);
        }
    }

    @Override
    public ToolStatus getToolStatus(String toolId) {
        if (toolId == null || toolId.trim().isEmpty()) {
            throw new IllegalArgumentException("toolId must not be null or empty");
        }
        ToolResult result = resultStore.get(toolId);
        return result == null ? ToolStatus.PENDING : result.status();
    }

    @Override
    public ToolResult getToolResult(String toolId) {
        if (toolId == null || toolId.trim().isEmpty()) {
            throw new IllegalArgumentException("toolId must not be null or empty");
        }
        return resultStore.get(toolId);
    }

    @Override
    public List<ToolResult> executeTools(List<ToolRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("requests must not be null");
        }
        List<ToolResult> results = new ArrayList<>();
        for (ToolRequest request : requests) {
            results.add(executeTool(request));
        }
        return List.copyOf(results);
    }

    @Override
    public boolean isToolSupported(ToolType toolType, String action) {
        if (toolType == null || action == null) {
            return false;
        }
        return validator.getSupportedActions(toolType)
                .contains(action.toLowerCase());
    }

    /**
     * Creates a validation ToolError from a validation result.
     */
    private ToolError createValidationError(
            ToolRequest request, ToolValidationResult validationResult) {
        return new ToolError(
                ToolErrorCode.VALIDATION_FAILURE,
                "Validation failed: " + String.join(", ", validationResult.violations()),
                request.toolId(),
                Instant.now(),
                validationResult.metadata()
        );
    }

    /**
     * Creates an execution ToolError from a generic exception.
     */
    private ToolError createExecutionError(ToolRequest request, Exception cause) {
        return new ToolError(
                ToolErrorCode.EXECUTION_FAILURE,
                "Tool execution failed: " + cause.getMessage(),
                request.toolId(),
                Instant.now(),
                Map.of("exceptionType", cause.getClass().getName())
        );
    }
}
