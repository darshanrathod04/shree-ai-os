package com.shreeai.os.platform.services;

import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.impl.BrowserTool;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.impl.FileSystemTool;
import com.shreeai.os.platform.tools.impl.GitTool;
import com.shreeai.os.platform.tools.impl.TerminalTool;
import com.shreeai.os.platform.tools.mcp.McpServerHandler;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>McpController</b> — Remote HTTP JSON-RPC 2.0 MCP Server endpoint ({@code POST /api/mcp}).
 *
 * <p>Allows external MCP-compatible IDEs and agent frameworks to discover and invoke
 * Shree AI OS tools over HTTP through {@link McpServerHandler} and {@link DefaultToolExecutor}.</p>
 */
@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpServerHandler handler;

    public McpController(ShreeAI ai) {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        registry.register(new EchoTool());
        registry.register(new FileSystemTool());
        registry.register(new GitTool());
        registry.register(new BrowserTool());
        registry.register(new TerminalTool());
        this.handler = new McpServerHandler(registry, new DefaultToolExecutor(registry));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleJsonRpc(@RequestBody String jsonRpcBody) {
        if (jsonRpcBody == null || jsonRpcBody.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32600,\"message\":\"Empty request\"}}");
        }
        String response = handler.handleLine(jsonRpcBody);
        if (response == null) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(response);
    }
}
