package com.shreeai.os.platform.tools.mcp;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.util.Map;
import java.util.Objects;

/**
 * <b>McpToolAdapter</b>
 *
 * <p>Adapts an external MCP tool discovered via {@link McpClient} into a
 * Shree AI OS {@link Tool}, so it runs through {@code DefaultToolExecutor}
 * with full {@code PermissionManager} and {@code ApprovalService} governance.</p>
 */
public final class McpToolAdapter implements Tool {

    private final McpClient client;
    private final String toolId;
    private final String remoteName;
    private final String description;

    public McpToolAdapter(McpClient client, McpClient.McpToolDefinition def) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        Objects.requireNonNull(def, "def must not be null");
        this.remoteName = def.name();
        this.toolId = "mcp:" + def.name();
        this.description = def.description() == null || def.description().isBlank()
                ? "External MCP tool: " + def.name()
                : def.description();
    }

    @Override
    public String id() {
        return toolId;
    }

    @Override
    public String name() {
        return remoteName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {
        try {
            McpClient.McpCallResult result = client.callTool(remoteName, request.arguments());
            return result.success()
                    ? ToolResponse.success(result.content(), Map.of("mcpTool", remoteName))
                    : ToolResponse.failure(result.content());
        } catch (Exception e) {
            return ToolResponse.failure("MCP execution failed: " + e.getMessage());
        }
    }
}
