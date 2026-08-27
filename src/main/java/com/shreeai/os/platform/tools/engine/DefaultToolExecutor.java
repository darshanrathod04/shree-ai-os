package com.shreeai.os.platform.tools.engine;

import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.engine.DefaultPermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.ToolRegistry;
import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.engine.InMemoryApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;

import java.util.Map;
import java.util.Objects;

/**
 * Constitutional Tool Executor.
 *
 * Every execution passes through the Permission Kernel.
 */
public final class DefaultToolExecutor implements ToolExecutor {

    private final ToolRegistry registry;
    private final PermissionManager permissionManager;
    private final ApprovalService approvalService;

    public DefaultToolExecutor(ToolRegistry registry) {
        this(
                registry,
                new DefaultPermissionManager(),
                new InMemoryApprovalService()
        );
    }

    public DefaultToolExecutor(
            ToolRegistry registry,
            PermissionManager permissionManager,
            ApprovalService approvalService
    ) {
        this.registry = Objects.requireNonNull(registry);
        this.permissionManager = Objects.requireNonNull(permissionManager);
        this.approvalService = Objects.requireNonNull(approvalService);
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

            case ASK_USER -> {

                ApprovalRequest approval =
                        approvalService.create(
                                ApprovalRequest.pending(
                                        request.toolId(),
                                        operation,
                                        request.arguments()
                                )
                        );

                yield ToolResponse.success(
                        "User approval required",
                        Map.of(
                                "approvalId", approval.requestId(),
                                "status", approval.status().name(),
                                "tool", approval.toolId(),
                                "operation", approval.operation()
                        )
                );
            }

            case DENY -> ToolResponse.failure(
                    "Permission denied for tool: " + request.toolId()
            );
        };
    }
}