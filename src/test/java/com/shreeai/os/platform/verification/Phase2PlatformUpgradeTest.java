package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.anthropic.AnthropicProvider;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.services.ByokSettingsService;
import com.shreeai.os.platform.services.DeveloperController;
import com.shreeai.os.platform.services.PlaygroundController;
import com.shreeai.os.platform.services.ProviderSettings;
import com.shreeai.os.platform.services.ProviderType;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.engine.ToolCallingAgent;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Phase2PlatformUpgradeTest {

    @Test
    @DisplayName("PlaygroundController and DeveloperController serve unified endpoints")
    void testUnifiedControllers() {
        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            PlaygroundController playground = new PlaygroundController(ai);
            ResponseEntity<?> chatRes = playground.chat(Map.of("message", "Hello Shree"));
            assertEquals(200, chatRes.getStatusCode().value());

            ResponseEntity<?> memStore = playground.memoryStore(Map.of("title", "lang", "content", "Java 21"));
            assertEquals(200, memStore.getStatusCode().value());

            ResponseEntity<?> ingestRes = playground.knowledgeIngest(Map.of("title", "Arch", "content", "Shree AI OS"));
            assertEquals(200, ingestRes.getStatusCode().value());

            DeveloperController dev = new DeveloperController(ai);
            ResponseEntity<?> openRes = dev.openWorkspace(Map.of("path", "."));
            assertEquals(200, openRes.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> session = (Map<String, Object>) openRes.getBody();
            assertNotNull(session);
            String wsId = (String) session.get("id");
            assertEquals(200, dev.getSummary(wsId).getStatusCode().value());
            assertEquals(204, dev.closeWorkspace(wsId).getStatusCode().value());
        }
    }

    @Test
    @DisplayName("ToolCallingAgent executes bounded tool loop through DefaultToolExecutor")
    void testToolCallingAgentLoop() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        registry.register(new EchoTool());
        DefaultToolExecutor executor = new DefaultToolExecutor(registry);

        AtomicInteger turn = new AtomicInteger(0);
        LlmProvider mockLlm = new LlmProvider() {
            @Override
            public String providerName() {
                return "mock-agent";
            }

            @Override
            public Stream<String> stream(LlmRequest request) {
                if (turn.getAndIncrement() == 0) {
                    return Stream.of("{\"tool\": \"echo\", \"arguments\": {\"message\": \"ping\"}}");
                }
                return Stream.of("{\"final\": \"Echo completed successfully\"}");
            }
        };

        ToolCallingAgent agent = new ToolCallingAgent(mockLlm, executor, registry, 4);
        ToolCallingAgent.Result result = agent.run("default", "Echo ping");

        assertTrue(result.completed());
        assertEquals("Echo completed successfully", result.finalAnswer());
        assertEquals(1, result.steps().size());
        assertEquals("echo", result.steps().get(0).toolId());
        assertEquals(2, result.iterationsUsed());
    }

    @Test
    @DisplayName("AnthropicProvider parses SSE tokens and integrates with BYOK settings")
    void testAnthropicProviderAndByok() {
        ByokSettingsService byok = new ByokSettingsService();
        byok.save(new ProviderSettings(ProviderType.ANTHROPIC, true, "sk-ant-test-key-12345", "", ""));
        LlmProvider materialized = byok.materializeProvider(ProviderType.ANTHROPIC);
        assertNotNull(materialized);
        assertInstanceOf(AnthropicProvider.class, materialized);
        assertEquals("anthropic", materialized.providerName());
    }
}
