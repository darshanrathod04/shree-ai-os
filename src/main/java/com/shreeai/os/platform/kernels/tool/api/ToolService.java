package com.shreeai.os.platform.kernels.tool.api;

import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;

import java.util.List;

/**
 * <b>ToolService</b> — Primary façade for the Tool Kernel.
 *
 * <p><b>Ownership:</b> Tool Kernel — API</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ToolService {

    /**
     * Executes a single tool operation based on the provided request.
     *
     * @param request the tool execution request (must not be {@code null})
     * @return the immutable tool result
     * @throws IllegalArgumentException if request is {@code null}
     */
    ToolResult executeTool(ToolRequest request);

    /**
     * Retrieves the current status of a tool execution.
     *
     * @param toolId the tool instance identifier
     * @return the current execution status
     * @throws IllegalArgumentException if toolId is {@code null} or empty
     */
    ToolStatus getToolStatus(String toolId);

    /**
     * Retrieves the result of a completed tool execution.
     *
     * @param toolId the tool instance identifier
     * @return the tool result, or {@code null} if not yet available
     * @throws IllegalArgumentException if toolId is {@code null} or empty
     */
    ToolResult getToolResult(String toolId);

    /**
     * Executes multiple tool operations.
     *
     * @param requests the list of tool execution requests (must not be {@code null})
     * @return the list of tool results
     * @throws IllegalArgumentException if requests is {@code null}
     */
    List<ToolResult> executeTools(List<ToolRequest> requests);

    /**
     * Returns whether a given tool type and action combination is supported.
     *
     * @param toolType the tool type
     * @param action   the action
     * @return {@code true} if supported
     */
    boolean isToolSupported(com.shreeai.os.platform.kernels.tool.model.ToolType toolType, String action);
}
