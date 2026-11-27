package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.services.McpController;
import com.shreeai.os.platform.tools.engine.AgentTeam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMilestone5Test {

    @Test
    @DisplayName("AgentTeam orchestrates specialist handoffs with full hop audit trail")
    void testMultiAgentHandoff() {
        AtomicInteger turn = new AtomicInteger(0);
        LlmProvider mockLlm = new LlmProvider() {
            @Override public String providerName() { return "team-llm"; }
            @Override public Stream<String> stream(LlmRequest request) {
                if (turn.getAndIncrement() == 0) {
                    return Stream.of("{\"handoff\": \"reviewer\", \"task\": \"Verify architecture plan\"}");
                }
                return Stream.of("Architecture verified and approved by reviewer.");
            }
        };

        AgentTeam team = new AgentTeam(mockLlm)
                .register(new AgentTeam.SpecialistAgent("planner", "Architect", "Design system plan"))
                .register(new AgentTeam.SpecialistAgent("reviewer", "Security Reviewer", "Review and approve"));

        AgentTeam.TeamResult result = team.run("planner", "default", "Build zero-trust gateway");
        assertTrue(result.completed());
        assertEquals(2, result.hops().size());
        assertEquals("planner", result.hops().get(0).fromAgent());
        assertEquals("reviewer", result.hops().get(0).toAgent());
        assertEquals("Architecture verified and approved by reviewer.", result.finalAnswer());
    }

    @Test
    @DisplayName("McpController serves HTTP JSON-RPC 2.0 initialize, tools/list, and tools/call")
    void testHttpMcpController() {
        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            McpController mcp = new McpController(ai);

            ResponseEntity<String> init = mcp.handleJsonRpc(
                    "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}");
            assertEquals(200, init.getStatusCode().value());
            assertTrue(init.getBody().contains("shree-ai-os-mcp"));

            ResponseEntity<String> list = mcp.handleJsonRpc(
                    "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");
            assertEquals(200, list.getStatusCode().value());
            assertTrue(list.getBody().contains("\"echo\""));

            ResponseEntity<String> call = mcp.handleJsonRpc(
                    "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"message\":\"remote-mcp\"}}}");
            assertEquals(200, call.getStatusCode().value());
            assertTrue(call.getBody().contains("remote-mcp"));
        }
    }
}
