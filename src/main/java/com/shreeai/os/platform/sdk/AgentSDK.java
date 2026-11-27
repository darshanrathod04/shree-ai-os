package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.llm.inmemory.InMemoryLlmProvider;
import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.engine.ToolCallingAgent;
import com.shreeai.os.platform.tools.impl.BrowserTool;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.impl.FileSystemTool;
import com.shreeai.os.platform.tools.impl.GitTool;
import com.shreeai.os.platform.tools.impl.TerminalTool;
import com.shreeai.os.platform.tools.mcp.McpClient;
import com.shreeai.os.platform.tools.mcp.McpToolAdapter;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentSDK</b>
 *
 * <p>First-class autonomous tool-calling SDK on {@link ShreeAI#agent()}.
 * Connects the active {@link LlmRouter} with {@link DefaultToolRegistry}
 * and {@link DefaultToolExecutor} (enforcing {@code PermissionManager}
 * and {@code ApprovalService} gates on every tool call).</p>
 */
public final class AgentSDK {

    private final Runtime runtime;
    private final DefaultToolRegistry toolRegistry;
    private final DefaultToolExecutor toolExecutor;

    AgentSDK(Runtime runtime) {
        this.runtime = runtime;
        this.toolRegistry = new DefaultToolRegistry();
        this.toolRegistry.register(new EchoTool());
        this.toolRegistry.register(new FileSystemTool());
        this.toolRegistry.register(new GitTool());
        this.toolRegistry.register(new BrowserTool());
        this.toolRegistry.register(new TerminalTool());
        this.toolExecutor = new DefaultToolExecutor(this.toolRegistry);
    }

    public AgentSDK registerTool(Tool tool) {
        Objects.requireNonNull(tool, "tool must not be null");
        toolRegistry.register(tool);
        return this;
    }

    /**
     * Connects to an MCP server via {@link McpClient}, discovers all advertised
     * tools, and registers them as {@link McpToolAdapter}s in the tool registry.
     */
    public List<String> registerMcpClient(McpClient client) throws java.io.IOException {
        Objects.requireNonNull(client, "client must not be null");
        client.initialize();
        List<McpClient.McpToolDefinition> defs = client.listTools();
        List<String> registeredIds = new java.util.ArrayList<>();
        for (McpClient.McpToolDefinition def : defs) {
            McpToolAdapter adapter = new McpToolAdapter(client, def);
            toolRegistry.register(adapter);
            registeredIds.add(adapter.id());
        }
        return List.copyOf(registeredIds);
    }

    public List<String> registeredTools() {
        return toolRegistry.getAll().stream().map(Tool::id).sorted().toList();
    }

    public SDKResponse run(String goal) {
        return run("default", goal, 5);
    }

    public SDKResponse run(String model, String goal, int maxSteps) {
        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("goal must not be null or blank");
        }
        var provider = runtime != null
                ? runtime.llmRouter().map(r -> (com.shreeai.os.platform.llm.LlmProvider) r).orElseGet(InMemoryLlmProvider::new)
                : new InMemoryLlmProvider();

        ToolCallingAgent agent = new ToolCallingAgent(provider, toolExecutor, toolRegistry, maxSteps);
        ToolCallingAgent.Result result = agent.run(model == null || model.isBlank() ? "default" : model, goal);

        List<Map<String, Object>> stepMaps = result.steps().stream()
                .map(s -> Map.<String, Object>of(
                        "step", s.step(),
                        "toolId", s.toolId(),
                        "arguments", s.arguments(),
                        "success", s.response().success(),
                        "message", s.response().message(),
                        "data", s.response().data()
                ))
                .toList();

        return SDKResponse.builder()
                .answer(result.finalAnswer())
                .confidence(result.completed() ? 0.9 : 0.4)
                .reasoningAvailable(true)
                .metadata("agent-tool-loop")
                .structuredPayload(Map.of(
                        "completed", result.completed(),
                        "iterationsUsed", result.iterationsUsed(),
                        "steps", stepMaps,
                        "registeredTools", registeredTools()
                ))
                .timestamp(Instant.now())
                .build();
    }

    public Map<String, LlmRouter.ProviderTelemetry> routerTelemetry() {
        if (runtime == null) {
            return Map.of();
        }
        return runtime.llmRouter().map(LlmRouter::telemetry).orElse(Map.of());
    }
}
