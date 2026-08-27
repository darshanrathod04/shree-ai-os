package com.shreeai.os.platform.tools.engine;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.ToolRegistry;

import java.util.Objects;

/**
 * Default constitutional implementation.
 */
public final class DefaultToolExecutor implements ToolExecutor {

    private final ToolRegistry registry;

    public DefaultToolExecutor(ToolRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        Tool tool = registry.find(request.toolId())
                .orElse(null);

        if (tool == null) {
            return ToolResponse.failure(
                    "Tool not found: " + request.toolId()
            );
        }

        return tool.execute(request);
    }
}