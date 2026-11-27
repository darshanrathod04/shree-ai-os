package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.mcp.McpClient;
import com.shreeai.os.platform.tools.mcp.McpServerHandler;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMilestone2McpTest {

    @Test
    @DisplayName("McpClient and McpServerHandler complete full JSON-RPC 2.0 initialize, listTools, and callTool")
    void testMcpStdioRoundTripAndAgentRegistration() throws Exception {
        DefaultToolRegistry serverRegistry = new DefaultToolRegistry();
        serverRegistry.register(new EchoTool());
        McpServerHandler server = new McpServerHandler(serverRegistry, new DefaultToolExecutor(serverRegistry));

        PipedOutputStream clientOut = new PipedOutputStream();
        PipedInputStream serverIn = new PipedInputStream(clientOut);
        PipedOutputStream serverOut = new PipedOutputStream();
        PipedInputStream clientIn = new PipedInputStream(serverOut);

        Thread serverThread = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(serverIn, StandardCharsets.UTF_8));
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(serverOut, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String resp = server.handleLine(line);
                    if (resp != null) {
                        w.write(resp);
                        w.newLine();
                        w.flush();
                    }
                }
            } catch (Exception ignored) {
                // Stream closed on test teardown
            }
        }, "mcp-test-server");
        serverThread.setDaemon(true);
        serverThread.start();

        try (McpClient mcpClient = new McpClient(clientIn, clientOut);
             ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {

            List<String> mounted = ai.agent().registerMcpClient(mcpClient);
            assertEquals(List.of("mcp:echo"), mounted);
            assertTrue(ai.agent().registeredTools().contains("mcp:echo"));

            McpClient.McpCallResult result = mcpClient.callTool("echo", Map.of("message", "hello-mcp"));
            assertTrue(result.success());
            assertTrue(result.content().contains("hello-mcp"));
        }
    }
}
