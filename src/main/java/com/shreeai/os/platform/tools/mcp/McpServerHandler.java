package com.shreeai.os.platform.tools.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.engine.ToolExecutor;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.ToolRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>McpServerHandler</b>
 *
 * <p>Exposes Shree AI OS's {@link ToolRegistry} and {@link ToolExecutor} as an
 * MCP JSON-RPC 2.0 server ({@code initialize}, {@code tools/list}, {@code tools/call})
 * so external MCP-compatible IDEs and agents can invoke Shree tools safely.</p>
 */
public final class McpServerHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROTOCOL_VERSION = "2024-11-05";

    private final ToolRegistry registry;
    private final ToolExecutor executor;

    public McpServerHandler(ToolRegistry registry, ToolExecutor executor) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * Handles a single JSON-RPC 2.0 line and returns the serialized response line,
     * or {@code null} for JSON-RPC notifications (no {@code id}).
     */
    public String handleLine(String jsonRpcLine) {
        try {
            JsonNode req = MAPPER.readTree(jsonRpcLine);
            if (!req.has("id") || req.get("id").isNull()) {
                return null;
            }
            Object id = req.get("id").isNumber() ? req.get("id").asLong() : req.get("id").asText();
            String method = req.path("method").asText("");
            JsonNode params = req.path("params");

            Object result = switch (method) {
                case "initialize" -> Map.of(
                        "protocolVersion", PROTOCOL_VERSION,
                        "capabilities", Map.of("tools", Map.of()),
                        "serverInfo", Map.of("name", "shree-ai-os-mcp", "version", "1.1.0")
                );
                case "tools/list" -> handleListTools();
                case "tools/call" -> handleCallTool(params);
                default -> null;
            };

            if (result == null) {
                return MAPPER.writeValueAsString(Map.of(
                        "jsonrpc", "2.0",
                        "id", id,
                        "error", Map.of("code", -32601, "message", "Method not found: " + method)
                ));
            }
            return MAPPER.writeValueAsString(Map.of(
                    "jsonrpc", "2.0",
                    "id", id,
                    "result", result
            ));
        } catch (Exception e) {
            return "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32700,\"message\":\"Parse error\"}}";
        }
    }

    private Map<String, Object> handleListTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        for (Tool t : registry.getAll()) {
            tools.add(Map.of(
                    "name", t.id(),
                    "description", t.description(),
                    "inputSchema", Map.of("type", "object")
            ));
        }
        return Map.of("tools", tools);
    }

    private Map<String, Object> handleCallTool(JsonNode params) {
        String name = params.path("name").asText("");
        Map<String, Object> args = params.has("arguments") && params.get("arguments").isObject()
                ? MAPPER.convertValue(params.get("arguments"), new TypeReference<>() {})
                : Map.of();

        ToolResponse response = executor.execute(new ToolRequest(name, args));
        String text = response.message() + (response.data().isEmpty() ? "" : " " + response.data());
        return Map.of(
                "isError", !response.success(),
                "content", List.of(Map.of("type", "text", "text", text))
        );
    }
}
