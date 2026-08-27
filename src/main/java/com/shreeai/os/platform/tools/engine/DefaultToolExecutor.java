package com.shreeai.os.platform.tools.engine;

import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.engine.DefaultPermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.ToolRegistry;

import java.util.Objects;

/**
 * Constitutional Tool Executor.
 *
 * Every execution passes through the Permission Kernel.
 */
public final class DefaultToolExecutor implements ToolExecutor {

    private final ToolRegistry registry;
    private final PermissionManager permissionManager;

    public DefaultToolExecutor(ToolRegistry registry) {
        this(
                registry,
                new DefaultPermissionManager()
        );
    }

    public DefaultToolExecutor(
            ToolRegistry registry,
            PermissionManager permissionManager
    ) {
        this.registry = Objects.requireNonNull(registry);
        this.permissionManager = Objects.requireNonNull(permissionManager);
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

        String operation = String.valueOf(
                request.arguments()
                        .getOrDefault("operation", "")
        );

        PermissionDecision decision =
                permissionManager.check(
                        new PermissionRequest(
                                request.toolId(),
                                operation,
                                request.arguments()
                        )
                );

        return switch (decision) {

            case ALLOW -> tool.execute(request);

            case ASK_USER -> ToolResponse.failure(
                    "Permission required for tool: " + request.toolId()
            );

            case DENY -> ToolResponse.failure(
                    "Permission denied for tool: " + request.toolId()
            );
        };
    }
}