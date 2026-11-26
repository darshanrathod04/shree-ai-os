package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.services.SdkDiagnosticsService;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.engine.ToolCallingAgent;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeRouterPillarTest {

    @Test
    @DisplayName("LlmRouter opens circuit breaker after threshold failures and records telemetry")
    void testCircuitBreakerAndTelemetry() {
        AtomicInteger primaryCalls = new AtomicInteger(0);
        LlmProvider flakyPrimary = new LlmProvider() {
            @Override public String providerName() { return "flaky"; }
            @Override public Stream<String> stream(LlmRequest request) {
                primaryCalls.incrementAndGet();
                throw new IllegalStateException("503 upstream down");
            }
        };
        LlmProvider healthyFallback = new LlmProvider() {
            @Override public String providerName() { return "healthy"; }
            @Override public Stream<String> stream(LlmRequest request) {
                return Stream.of("Hello world ", "from fallback!");
            }
        };

        LlmRouter router = new LlmRouter(List.of(flakyPrimary, healthyFallback), 2, 60_000L);
        LlmRequest req = LlmRequest.builder().model("default").prompt("hi").build();

        // Call 1 & 2 hit flakyPrimary (2 failures -> circuit opens)
        for (int i = 0; i < 2; i++) {
            try (Stream<String> s = router.stream(req)) {
                assertEquals("Hello world from fallback!", s.collect(Collectors.joining()));
            }
        }
        assertEquals(2, primaryCalls.get());

        // Call 3 skips flakyPrimary completely because circuit is open
        try (Stream<String> s = router.stream(req)) {
            assertEquals("Hello world from fallback!", s.collect(Collectors.joining()));
        }
        assertEquals(2, primaryCalls.get());

        Map<String, LlmRouter.ProviderTelemetry> stats = router.telemetry();
        assertTrue(stats.get("flaky").circuitOpen());
        assertEquals(2, stats.get("flaky").failureCount());
        assertEquals("503 upstream down", stats.get("flaky").lastError());
        assertEquals(3, stats.get("healthy").successCount());
        assertTrue(stats.get("healthy").estimatedOutputTokens() > 0);
    }

    @Test
    @DisplayName("ToolCallingAgent repairs malformed tool JSON via retry and updates diagnostics")
    void testToolCallingJsonRepairAndDiagnostics() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        registry.register(new EchoTool());
        DefaultToolExecutor executor = new DefaultToolExecutor(registry);

        AtomicInteger turn = new AtomicInteger(0);
        LlmProvider repairingLlm = new LlmProvider() {
            @Override public String providerName() { return "repair-llm"; }
            @Override public Stream<String> stream(LlmRequest request) {
                return switch (turn.getAndIncrement()) {
                    case 0 -> Stream.of("{\"tool\": \"echo\", \"arguments\": {broken}");
                    case 1 -> Stream.of("{\"tool\": \"echo\", \"arguments\": {\"message\": \"fixed\"}}");
                    default -> Stream.of("{\"final\": \"Done with repaired JSON\"}");
                };
            }
        };

        ToolCallingAgent agent = new ToolCallingAgent(repairingLlm, executor, registry, 4);
        ToolCallingAgent.Result result = agent.run("default", "Echo fixed");
        assertTrue(result.completed());
        assertEquals("Done with repaired JSON", result.finalAnswer());
        assertEquals(1, result.steps().size());

        SdkDiagnosticsService diagnostics = new SdkDiagnosticsService();
        diagnostics.recordLlmCall("ollama", "llama3.2:1b", 42L, 128L);
        Map<String, Object> report = diagnostics.report();
        assertEquals(1, report.get("totalRequests"));
        assertEquals(128L, report.get("totalEstimatedTokens"));
    }
}
