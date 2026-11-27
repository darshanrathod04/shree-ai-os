package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.engine.DefaultDocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.services.PlaygroundController;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompetitorParityBenchmarkTest {

    @Test
    @DisplayName("Knowledge ingestion supports structured JSON and symbol-aware CODE parsing")
    void testJsonAndCodeParsers() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        String jsonSource = registry.register(
                KnowledgeSourceType.TEXT, "Config JSON", "config.json", null, Map.of()).sourceId();
        String codeSource = registry.register(
                KnowledgeSourceType.TEXT, "Sample Java", "Sample.java", null, Map.of()).sourceId();

        DefaultDocumentIngestionEngine engine = new DefaultDocumentIngestionEngine(registry);

        IngestionResult jsonRes = engine.ingest(
                jsonSource,
                "Config JSON",
                ParserType.JSON,
                "{\"runtime\":{\"port\":8080},\"llm\":{\"chain\":\"ollama,in-memory\"}}"
        );
        assertEquals(2, jsonRes.totalChunks());
        assertEquals("runtime", jsonRes.document().chunks().get(0).metadata().section());
        assertEquals("llm", jsonRes.document().chunks().get(1).metadata().section());

        String javaCode = """
                package com.example;

                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                    public int multiply(int a, int b) {
                        return a * b;
                    }
                }
                """;
        IngestionResult codeRes = engine.ingest(codeSource, "Sample Java", ParserType.CODE, javaCode);
        assertTrue(codeRes.totalChunks() >= 3);
        assertEquals("Calculator", codeRes.document().chunks().get(1).metadata().section());
        assertEquals("add", codeRes.document().chunks().get(2).metadata().section());
    }

    @Test
    @DisplayName("ShreeAI.agent() and PlaygroundController expose AgentSDK, custom tools, and router telemetry")
    @SuppressWarnings("unchecked")
    void testAgentSdkAndTelemetryEndpoints() {
        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            List<String> tools = ai.agent().registeredTools();
            assertTrue(tools.containsAll(List.of("browser", "echo", "filesystem", "git", "terminal")));

            ai.agent().registerTool(new Tool() {
                @Override public String id() { return "custom-ping"; }
                @Override public String name() { return "Custom Ping"; }
                @Override public String description() { return "Returns pong"; }
                @Override public ToolResponse execute(ToolRequest request) {
                    return ToolResponse.success("pong", Map.of());
                }
            });
            assertTrue(ai.agent().registeredTools().contains("custom-ping"));

            SDKResponse agentRes = ai.agent().run("Summarize system readiness");
            assertNotNull(agentRes.answer());
            assertEquals("agent-tool-loop", agentRes.metadata());
            assertTrue(agentRes.structuredPayload().containsKey("steps"));

            PlaygroundController controller = new PlaygroundController(ai);
            ResponseEntity<?> agentHttp = controller.runAgent(Map.of("goal", "Check readiness", "maxSteps", 3));
            assertEquals(200, agentHttp.getStatusCode().value());

            ResponseEntity<?> telemetryHttp = controller.telemetry();
            assertEquals(200, telemetryHttp.getStatusCode().value());
            Map<String, Object> body = (Map<String, Object>) telemetryHttp.getBody();
            assertNotNull(body);
            assertTrue(body.containsKey("providers"));
            assertTrue(body.containsKey("registeredTools"));
        }
    }
}
