package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.services.DeveloperController;
import com.shreeai.os.platform.services.PlaygroundController;
import com.shreeai.os.platform.services.ShreeApiSecurityFilter;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.engine.ToolCallingAgent;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.impl.FileSystemTool;
import com.shreeai.os.platform.tools.impl.TerminalTool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SecurityHardeningTest {

    @Test
    @DisplayName("ShreeApiSecurityFilter enforces API key, allowlist CORS, headers, and rate limit")
    void testApiSecurityFilter() throws Exception {
        ShreeApiSecurityFilter filter = new ShreeApiSecurityFilter(
                "secret-token-123",
                Set.of("http://localhost:3000"),
                2
        );

        // 1. Missing API key -> 401
        MockHttpServletRequest unauthReq = new MockHttpServletRequest("POST", "/api/playground/chat");
        unauthReq.addHeader("Origin", "http://localhost:3000");
        MockHttpServletResponse unauthRes = new MockHttpServletResponse();
        filter.doFilter(unauthReq, unauthRes, new MockFilterChain());
        assertEquals(401, unauthRes.getStatus());
        assertEquals("nosniff", unauthRes.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", unauthRes.getHeader("X-Frame-Options"));
        assertEquals("http://localhost:3000", unauthRes.getHeader("Access-Control-Allow-Origin"));

        // 2. Untrusted Origin -> no ACAO header
        MockHttpServletRequest evilOriginReq = new MockHttpServletRequest("GET", "/api/developer/workspace/sessions");
        evilOriginReq.addHeader("Origin", "https://evil.example");
        evilOriginReq.addHeader("X-API-Key", "secret-token-123");
        MockHttpServletResponse evilOriginRes = new MockHttpServletResponse();
        filter.doFilter(evilOriginReq, evilOriginRes, new MockFilterChain());
        assertEquals(200, evilOriginRes.getStatus());
        assertNull(evilOriginRes.getHeader("Access-Control-Allow-Origin"));

        // 3. Second allowed request passes, third hits 429 rate limit
        MockHttpServletRequest okReq = new MockHttpServletRequest("GET", "/api/developer/workspace/sessions");
        okReq.addHeader("Authorization", "Bearer secret-token-123");
        MockHttpServletResponse okRes = new MockHttpServletResponse();
        filter.doFilter(okReq, okRes, new MockFilterChain());
        assertEquals(200, okRes.getStatus());

        MockHttpServletResponse rateLimitedRes = new MockHttpServletResponse();
        filter.doFilter(okReq, rateLimitedRes, new MockFilterChain());
        assertEquals(429, rateLimitedRes.getStatus());
        assertEquals("60", rateLimitedRes.getHeader("Retry-After"));
    }

    @Test
    @DisplayName("FileSystemTool and TerminalTool block path traversal and chained destructive commands")
    void testToolSandboxing(@TempDir Path sandbox) {
        FileSystemTool fs = new FileSystemTool(sandbox);
        ToolResponse traversal = fs.execute(new ToolRequest("filesystem", Map.of(
                "operation", "read",
                "path", "../../etc/passwd"
        )));
        assertFalse(traversal.success());
        assertTrue(traversal.message().contains("Security violation"));

        TerminalTool terminal = new TerminalTool();
        assertFalse(terminal.execute(new ToolRequest("terminal", Map.of("command", "echo hi && rm -rf /"))).success());
        assertFalse(terminal.execute(new ToolRequest("terminal", Map.of("command", "powershell Remove-Item C:\\temp"))).success());
        assertFalse(terminal.execute(new ToolRequest("terminal", Map.of("command", "  DEL secret.txt"))).success());
    }

    @Test
    @DisplayName("DeveloperController enforces allowed workspace root and input size limits")
    void testControllerValidation(@TempDir Path allowedRoot) {
        String prev = System.getProperty("shree.workspace.allowed-root");
        System.setProperty("shree.workspace.allowed-root", allowedRoot.toString());
        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            DeveloperController dev = new DeveloperController(ai);
            ResponseEntity<?> escapeRes = dev.openWorkspace(Map.of("path", allowedRoot.getParent().toString()));
            assertEquals(400, escapeRes.getStatusCode().value());

            String oversized = "x".repeat(100_005);
            ResponseEntity<?> bigChat = dev.chatAsk(Map.of("sessionId", "s1", "question", oversized));
            assertEquals(400, bigChat.getStatusCode().value());

            PlaygroundController playground = new PlaygroundController(ai);
            assertNotNull(playground.chatStream(Map.of("message", "hi")));
        } finally {
            if (prev == null) {
                System.clearProperty("shree.workspace.allowed-root");
            } else {
                System.setProperty("shree.workspace.allowed-root", prev);
            }
        }
    }

    @Test
    @DisplayName("ToolCallingAgent aborts consecutive identical tool calls to prevent infinite loops")
    void testToolCallingAgentLoopGuard() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        registry.register(new EchoTool());
        DefaultToolExecutor executor = new DefaultToolExecutor(registry);

        LlmProvider loopingLlm = new LlmProvider() {
            @Override
            public String providerName() {
                return "looping";
            }

            @Override
            public Stream<String> stream(LlmRequest request) {
                return Stream.of("{\"tool\": \"echo\", \"arguments\": {\"message\": \"same\"}}");
            }
        };

        ToolCallingAgent agent = new ToolCallingAgent(loopingLlm, executor, registry, 5);
        ToolCallingAgent.Result result = agent.run("default", "Test loop guard");
        assertFalse(result.completed());
        assertEquals(1, result.steps().size());
        assertTrue(result.finalAnswer().contains("infinite loop"));
    }
}
