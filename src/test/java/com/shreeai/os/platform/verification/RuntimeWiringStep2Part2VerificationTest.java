package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.runtime.agents.NaturalResponseAgent;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.model.VerificationReport;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Phase 2 Steps 2.3 & 2.4:
 * - Step 2.3: DefaultRuntimeService.streamText() routes through pipeline context & guardrails.
 * - Step 2.4: NaturalResponseAgent answers general queries via LLM even when KB evidence is empty,
 *             and respects shree.llm.model configuration.
 */
class RuntimeWiringStep2Part2VerificationTest {

    @Test
    @DisplayName("Step 2.4a: NaturalResponseAgent uses General Assistance LLM when evidence is INSUFFICIENT")
    void testGeneralAssistanceOnInsufficientEvidence() {
        AtomicReference<LlmRequest> captured = new AtomicReference<>();
        LlmProvider mockProvider = new LlmProvider() {
            @Override
            public String providerName() { return "mock-llm"; }

            @Override
            public Stream<String> stream(LlmRequest request) {
                captured.set(request);
                return Stream.of("Java 21 virtual threads ", "scale lightweight concurrency.");
            }
        };

        System.setProperty("shree.llm.model", "gemini-3.6-flash");
        try {
            NaturalResponseAgent agent = new NaturalResponseAgent(mockProvider);
            VerificationReport emptyReport = VerificationReport.builder()
                    .tier(VerificationReport.ConfidenceTier.INSUFFICIENT)
                    .confidence(0.0)
                    .build();
            ExecutionRequest req = ExecutionRequest.builder()
                    .requestId("req-1")
                    .requestType("CHAT")
                    .payload("Explain Java 21 virtual threads")
                    .build();

            SynthesizedResponse resp = agent.generate(emptyReport, req);
            assertNotNull(captured.get());
            assertEquals("gemini-3.6-flash", captured.get().model());
            assertTrue(captured.get().prompt().contains("<user_query>"));
            assertTrue(resp.answer().contains("Java 21 virtual threads scale lightweight concurrency."));
            assertEquals("GENERAL_ASSISTANCE", resp.structuredData().get("synthesisMode"));
        } finally {
            System.clearProperty("shree.llm.model");
        }
    }

    @Test
    @DisplayName("Step 2.4b: NaturalResponseAgent falls back to Insufficient template when LLM is null")
    void testInsufficientFallbackWhenNoLlm() {
        NaturalResponseAgent agent = new NaturalResponseAgent(null);
        VerificationReport emptyReport = VerificationReport.builder()
                .tier(VerificationReport.ConfidenceTier.INSUFFICIENT)
                .confidence(0.0)
                .build();
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("req-2")
                .requestType("CHAT")
                .payload("Explain Java 21 virtual threads")
                .build();

        SynthesizedResponse resp = agent.generate(emptyReport, req);
        assertTrue(resp.answer().contains("Insufficient Evidence"));
    }

    @Test
    @DisplayName("Step 2.3: streamText() executes pipeline and passes guardrailed prompt to LlmRouter")
    void testStreamTextUsesGroundedGuardrailedPrompt() throws Exception {
        DefaultRuntimeService runtime = new DefaultRuntimeService(
                RuntimeConfiguration.builder().runtimeName("stream-test").build(),
                RuntimeContract.builder().contractVersion("1.0.0").build()
        );
        runtime.initialize();
        runtime.start();

        AtomicReference<LlmRequest> captured = new AtomicReference<>();
        LlmProvider mockProvider = new LlmProvider() {
            @Override
            public String providerName() { return "mock-stream"; }

            @Override
            public Stream<String> stream(LlmRequest request) {
                captured.set(request);
                return Stream.of("Hello ", "from ", "grounded stream!");
            }
        };

        Field f = DefaultRuntimeService.class.getDeclaredField("llmRouter");
        f.setAccessible(true);
        f.set(runtime, new LlmRouter(List.of(mockProvider)));

        String output;
        try (Stream<String> s = runtime.streamText("What is Shree AI OS?")) {
            output = s.collect(Collectors.joining());
        } finally {
            runtime.stop();
        }

        assertEquals("Hello from grounded stream!", output);
        assertNotNull(captured.get());
        assertTrue(Boolean.TRUE.equals(captured.get().stream()));
        assertTrue(captured.get().prompt().contains("<user_query>"));
        assertTrue(captured.get().prompt().contains("What is Shree AI OS?"));
        assertTrue(captured.get().prompt().contains("Security instruction:"));
    }
}
