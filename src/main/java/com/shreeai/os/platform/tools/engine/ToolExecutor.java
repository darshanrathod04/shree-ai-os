package com.shreeai.os.platform.tools.engine;

import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

/**
 * Constitutional Tool Executor.
 *
 * Runtime executes every tool through this interface.
 */
public interface ToolExecutor {

    ToolResponse execute(ToolRequest request);

}