package com.shreeai.os.platform.kernels.tool.engine;

import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;

/**
 * <b>ToolProcessingEngine</b> — Engine-layer contract for deterministic
 * tool execution computation.
 *
 * <p><b>Ownership:</b> Tool Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ToolProcessingEngine {

    /**
     * Processes a tool execution request deterministically.
     *
     * <p>This method performs the deterministic computation of a tool result
     * from a validated {@link ToolRequest}. It contains no orchestration,
     * validation, or exception-translation logic.</p>
     *
     * @param request the tool execution request (must not be {@code null})
     * @return the tool execution result
     * @throws IllegalArgumentException if request is {@code null}
     */
    ToolResult processToolExecution(ToolRequest request);
}
