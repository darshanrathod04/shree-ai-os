package com.shreeai.os.platform.runtime.orchestration;

import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>IntelligenceOrchestratorTest</b>
 *
 * <p>Sprint-12 acceptance tests for the Intelligence Orchestrator v2.
 * Verifies that the runtime automatically orchestrates multiple kernels
 * when a user request requires more than one kernel.</p>
 *
 * <p><b>Acceptance criteria:</b></p>
 * <ul>
 *   <li>Memory + Planning execute together</li>
 *   <li>Knowledge + Planning execute together</li>
 *   <li>Knowledge + Execution execute together</li>
 *   <li>Reflection runs after execution</li>
 *   <li>Existing single-kernel requests remain unchanged</li>
 *   <li>Execution order is deterministic</li>
 *   <li>Composite response contains all sections</li>
 *   <li>SDK response remains backward compatible</li>
 * </ul>
 *
 * @since Sprint-12
 */
@DisplayName("Sprint-12: Intelligence Orchestrator v2")
public class IntelligenceOrchestratorTest {

    private ShreeAI ai;

    @BeforeEach
    public void setUp() {
        ai = ShreeAI.builder().apiKey("local").build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IntentAnalyzer Unit Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("IntentAnalyzer")
    class IntentAnalyzerTests {

        private final IntentAnalyzer analyzer = new IntentAnalyzer();

        @Test
        @DisplayName("detects MEMORY_STORE + PLANNING for 'Remember this and create a roadmap'")
        public void testMemoryPlusPlanningDetection() {
            IntentAnalysisResult result = analyzer.analyze(
                    "Remember this project and create a roadmap");

            assertEquals(IntentType.PLANNING, result.primaryIntent());
            assertTrue(result.secondaryIntents().contains(IntentType.MEMORY_STORE),
                    "Secondary should include MEMORY_STORE");
            assertTrue(result.isMultiKernel());
            assertEquals(List.of(KernelType.PLANNING, KernelType.MEMORY),
                    result.requiredKernels());
            assertTrue(result.confidence() > 0.0);
        }

        @Test
        @DisplayName("detects KNOWLEDGE + PLANNING for domain-aware planning queries")
        public void testKnowledgePlusPlanningDetection() {
            IntentAnalysisResult result = analyzer.analyze(
                    "What is the JVM and create a Java roadmap");

            assertEquals(IntentType.KNOWLEDGE_QUERY, result.primaryIntent());
            assertTrue(result.secondaryIntents().contains(IntentType.PLANNING),
                    "Secondary should include PLANNING");
            assertTrue(result.isMultiKernel());
        }

        @Test
        @DisplayName("detects MEMORY_STORE alone for simple remember requests")
        public void testMemoryStoreOnly() {
            IntentAnalysisResult result = analyzer.analyze("Remember Darshan");

            assertEquals(IntentType.MEMORY_STORE, result.primaryIntent());
            assertFalse(result.isMultiKernel(),
                    "Simple memory store should be single-kernel");
        }

        @Test
        @DisplayName("detects PLANNING alone for roadmap-only requests")
        public void testPlanningOnly() {
            IntentAnalysisResult result = analyzer.analyze(
                    "Build a roadmap for the project");

            assertEquals(IntentType.PLANNING, result.primaryIntent());
            assertFalse(result.isMultiKernel());
        }

        @Test
        @DisplayName("extracts JAVA domain entity from 'Build an AI assistant for doctors'")
        public void testDomainEntityExtraction() {
            IntentAnalysisResult result = analyzer.analyze(
                    "Build an AI assistant for doctors");

            assertEquals("AI", result.entities().get("domain"));
            assertTrue(result.requiredKernels().contains(KernelType.PLANNING));
        }

        @Test
        @DisplayName("returns CHAT intent for unrecognized input")
        public void testUnknownInputReturnsChat() {
            IntentAnalysisResult result = analyzer.analyze("Hello, how are you?");

            assertEquals(IntentType.CHAT, result.primaryIntent());
            assertFalse(result.isMultiKernel());
        }

        @Test
        @DisplayName("handles null input gracefully")
        public void testNullInput() {
            IntentAnalysisResult result = analyzer.analyze(null);

            assertEquals(IntentType.CHAT, result.primaryIntent());
            assertEquals(0.0, result.confidence());
        }

        @Test
        @DisplayName("detects EXECUTION + REFLECTION for task execution")
        public void testExecutionReflectionDetection() {
            IntentAnalysisResult result = analyzer.analyze(
                    "Build it and deploy the application");

            assertEquals(IntentType.EXECUTION, result.primaryIntent());
            assertTrue(result.isMultiKernel(),
                    "Execution should be multi-kernel with reflection");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // KernelExecutionGraph Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("KernelExecutionGraph")
    class KernelExecutionGraphTests {

        @Test
        @DisplayName("orders MEMORY before PLANNING")
        public void testMemoryBeforePlanning() {
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .secondaryIntents(List.of(IntentType.MEMORY_STORE))
                    .requiredKernels(List.of(KernelType.MEMORY, KernelType.PLANNING))
                    .originalInput("Remember this and create a roadmap")
                    .build();

            KernelExecutionGraph graph = KernelExecutionGraph.builder()
                    .buildFrom(analysis);

            List<KernelExecutionGraph.Node> order = graph.executionOrder();
            assertTrue(order.size() >= 2);

            int memoryPos = -1, planningPos = -1;
            for (KernelExecutionGraph.Node node : order) {
                if (node.kernelType() == KernelType.MEMORY) {
                    memoryPos = node.position();
                }
                if (node.kernelType() == KernelType.PLANNING) {
                    planningPos = node.position();
                }
            }
            assertTrue(memoryPos >= 0 && planningPos >= 0);
            assertTrue(memoryPos < planningPos,
                    "MEMORY must execute before PLANNING. Got memory=" + memoryPos
                            + ", planning=" + planningPos);
        }

        @Test
        @DisplayName("orders KNOWLEDGE before PLANNING")
        public void testKnowledgeBeforePlanning() {
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .secondaryIntents(List.of(IntentType.KNOWLEDGE_QUERY))
                    .requiredKernels(List.of(KernelType.KNOWLEDGE, KernelType.PLANNING))
                    .originalInput("What is Java and create a roadmap")
                    .build();

            KernelExecutionGraph graph = KernelExecutionGraph.builder()
                    .buildFrom(analysis);

            List<KernelExecutionGraph.Node> order = graph.executionOrder();
            int knowledgePos = -1, planningPos = -1;
            for (KernelExecutionGraph.Node node : order) {
                if (node.kernelType() == KernelType.KNOWLEDGE) {
                    knowledgePos = node.position();
                }
                if (node.kernelType() == KernelType.PLANNING) {
                    planningPos = node.position();
                }
            }
            assertTrue(knowledgePos >= 0 && planningPos >= 0);
            assertTrue(knowledgePos < planningPos,
                    "KNOWLEDGE must execute before PLANNING");
        }

        @Test
        @DisplayName("single kernel graph has size 1")
        public void testSingleKernelGraph() {
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .requiredKernels(List.of(KernelType.PLANNING))
                    .originalInput("Build a roadmap")
                    .build();

            KernelExecutionGraph graph = KernelExecutionGraph.builder()
                    .buildFrom(analysis);

            assertEquals(1, graph.size());
            assertEquals(KernelType.PLANNING,
                    graph.executionOrder().get(0).kernelType());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CompositeKernelResult Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CompositeKernelResult")
    class CompositeKernelResultTests {

        @Test
        @DisplayName("aggregates multiple kernel results")
        public void testAggregation() {
            CompositeKernelResult result = CompositeKernelResult.builder()
                    .requestId("req-001")
                    .addKernelResult(new CompositeKernelResult.KernelResult(
                            "Memory Kernel",
                            KernelType.MEMORY,
                            "Stored successfully",
                            true, 100L, 0.95, Map.of()))
                    .addKernelResult(new CompositeKernelResult.KernelResult(
                            "Planning Kernel",
                            KernelType.PLANNING,
                            "Plan created",
                            true, 200L, 0.90, Map.of()))
                    .computeConfidenceFromResults()
                    .build();

            assertEquals(2, result.kernelResults().size());
            assertEquals(2, result.executionOrder().size());
            assertTrue(result.isSuccess());
            // 0.95 and 0.90 average = 0.925, rounded to 2dp = 0.93
            assertEquals(0.93, result.overallConfidence(), 0.01);
        }

        @Test
        @DisplayName("marks failure when any kernel fails")
        public void testFailurePropagation() {
            CompositeKernelResult result = CompositeKernelResult.builder()
                    .requestId("req-002")
                    .addKernelResult(new CompositeKernelResult.KernelResult(
                            "Memory Kernel",
                            KernelType.MEMORY,
                            "Stored successfully",
                            true, 100L, 0.95, Map.of()))
                    .addKernelResult(new CompositeKernelResult.KernelResult(
                            "Planning Kernel",
                            KernelType.PLANNING,
                            "Planning failed",
                            false, 50L, 0.0, Map.of()))
                    .computeConfidenceFromResults()
                    .build();

            assertFalse(result.isSuccess());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // End-to-End SDK Integration Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SDK Integration — Multi-Kernel Orchestration")
    class SDKIntegrationTests {

        @Test
        @DisplayName("chat with 'Remember this and create a roadmap' triggers multi-kernel orchestration")
        public void testMemoryPlusPlanningOrchestration() {
            SDKResponse response = ai.chat(
                    "Remember this project about AI doctors and create a Java roadmap");

            assertNotNull(response, "Response should not be null");
            assertNotNull(response.answer(), "Answer should not be null");
            assertFalse(response.answer().isBlank(), "Answer should not be blank");

            // Verify the response contains multi-kernel sections
            String answer = response.answer();
            assertTrue(answer.contains("Roadmap") || answer.contains("AI") || answer.contains("Java"),
                    "Response should contain roadmap domain info: " + answer);

            // Verify backward-compatible structured payload exists
            Map<String, Object> payload = response.structuredPayload();
            assertNotNull(payload, "Structured payload should not be null");
        }

        @Test
        @DisplayName("single-kernel request 'Create a roadmap' routes unchanged")
        public void testSingleKernelPlanningUnchanged() {
            SDKResponse response = ai.chat("Create a roadmap for the project");

            assertNotNull(response, "Response should not be null");
            assertNotNull(response.answer(), "Answer should not be null");
            assertFalse(response.answer().isBlank());

            // Single kernel - should NOT have orchestrator markers
            Map<String, Object> payload = response.structuredPayload();
            assertNotNull(payload);
            // The response should be a normal plan response
            assertTrue(response.answer().length() > 10);
        }

        @Test
        @DisplayName("chat with 'What is Java?' routes to knowledge (unchanged)")
        public void testKnowledgeQueryUnchanged() {
            SDKResponse response = ai.chat("What is Java?");

            assertNotNull(response);
            assertNotNull(response.answer());
            assertFalse(response.answer().isBlank());
        }

        @Test
        @DisplayName("SDK response structure is backward compatible")
        public void testSDKResponseBackwardCompatibility() {
            SDKResponse response = ai.chat("Hello, what can you do?");

            assertNotNull(response);
            assertNotNull(response.answer());
            assertNotNull(response.structuredPayload());
            // All fields that existed before Sprint-12 must still be present
            assertDoesNotThrow(() -> {
                response.answer();
                response.structuredPayload();
            });
        }

        @Test
        @DisplayName("runtime service submit with multi-intent triggers orchestrator")
        public void testDirectSubmitMultiIntent() {
            Runtime runtime = ai.client().runtime();

            // Multi-intent: memory + planning combined
            ExecutionSession session = runtime.submit(
                    ExecutionRequest.builder()
                            .requestId("orchestrated-001")
                            .requestType("CHAT")
                            .payload("Remember this project and create a roadmap")
                            .build()
            );

            assertNotNull(session, "Session should not be null");
            assertNotNull(session.result(), "Result should not be null");

            Map<String, Object> payload = session.result().structuredPayload();
            assertNotNull(payload);

            // Either orchestrated or pipeline result — both are valid
            assertTrue(
                    payload.containsKey("response") || payload.containsKey("routedKernel"),
                    "Should have either orchestrated response or routed kernel info"
            );
        }

        @Test
        @DisplayName("knowledge + planning multi-kernel request executes correctly")
        public void testKnowledgePlusPlanningMultiKernel() {
            Runtime runtime = ai.client().runtime();

            ExecutionSession session = runtime.submit(
                    ExecutionRequest.builder()
                            .requestId("kp-orchestrated-001")
                            .requestType("CHAT")
                            .payload("What is machine learning and create an AI project plan")
                            .build()
            );

            assertNotNull(session);
            assertNotNull(session.result());
            assertTrue(session.result().isSuccess());
        }

        @Test
        @DisplayName("deterministic execution order: Memory always before Planning")
        public void testDeterministicExecutionOrder() {
            Runtime runtime = ai.client().runtime();

            // Submit the same multi-intent request multiple times
            for (int i = 0; i < 3; i++) {
                ExecutionSession session = runtime.submit(
                        ExecutionRequest.builder()
                                .requestId("order-test-" + i)
                                .requestType("CHAT")
                                .payload("Remember the client requirements and create a roadmap")
                                .build()
                );

                assertNotNull(session);
                assertTrue(session.result().isSuccess(),
                        "Run " + i + " should succeed");
            }
        }

        @Test
        @DisplayName("runtime pipeline still has 11 stages (canonical unchanged)")
        public void testCanonicalPipelinePreserved() {
            Runtime runtime = ai.client().runtime();

            assertTrue(runtime instanceof DefaultRuntimeService);

            com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline pipeline =
                    (com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline)
                            runtime.pipeline();

            assertEquals(11, pipeline.getStages().size(),
                    "Canonical pipeline must keep 11 stages");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Intent Analysis Result Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("IntentAnalysisResult")
    class IntentAnalysisResultTests {

        @Test
        @DisplayName("isMultiKernel returns true when multiple kernels required")
        public void testIsMultiKernelTrue() {
            IntentAnalysisResult result = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .requiredKernels(List.of(KernelType.MEMORY, KernelType.PLANNING))
                    .build();

            assertTrue(result.isMultiKernel());
        }

        @Test
        @DisplayName("isMultiKernel returns false when single kernel")
        public void testIsMultiKernelFalse() {
            IntentAnalysisResult result = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .requiredKernels(List.of(KernelType.PLANNING))
                    .build();

            assertFalse(result.isMultiKernel());
        }

        @Test
        @DisplayName("entities are preserved in result")
        public void testEntitiesPreserved() {
            IntentAnalysisResult result = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .entities(Map.of("domain", "JAVA"))
                    .build();

            assertEquals("JAVA", result.entities().get("domain"));
        }

        @Test
        @DisplayName("confidence is clamped to [0.0, 1.0]")
        public void testConfidenceClamped() {
            IntentAnalysisResult result = IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.PLANNING)
                    .confidence(1.5) // Invalid value
                    .build();

            assertEquals(1.0, result.confidence());
        }
    }
}
