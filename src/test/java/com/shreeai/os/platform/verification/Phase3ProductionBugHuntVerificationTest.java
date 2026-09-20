package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolType;
import com.shreeai.os.platform.kernels.tool.service.DefaultToolService;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.gemini.GeminiProvider;
import com.shreeai.os.platform.llm.ollama.OllamaProvider;
import com.shreeai.os.platform.llm.openai.OpenAiProvider;
import com.shreeai.os.platform.runtime.reflection.InMemoryReflectionRepository;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import com.shreeai.os.platform.runtime.vector.InMemoryVectorSearchEngine;
import com.shreeai.os.platform.runtime.vector.InMemoryVectorStore;
import com.shreeai.os.platform.runtime.vector.PgVectors;
import com.shreeai.os.platform.runtime.vector.VectorRuntimeException;
import com.shreeai.os.platform.sdk.IdentitySDK;
import com.shreeai.os.platform.sdk.ProjectSDK;
import com.shreeai.os.platform.sdk.ReflectionSDK;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <b>Phase3ProductionBugHuntVerificationTest</b>
 *
 * <p>Deterministic, evidence-first reproducing test suite for Phase 3 production defects.</p>
 */
public class Phase3ProductionBugHuntVerificationTest {

    // =========================================================================
    // Target 1: Memory Leaks & Lock Contention in InMemoryReflectionRepository
    // =========================================================================

    @Test
    @DisplayName("Target 1: InMemoryReflectionRepository bounds retention preventing heap growth")
    void testBug1_InMemoryReflectionRepository_BoundsRetention() {
        InMemoryReflectionRepository repository = new InMemoryReflectionRepository();
        String tenantId = "tenant-leak-test";

        for (int i = 0; i < 1500; i++) {
            ReflectionHistory history = new ReflectionHistory(
                    tenantId,
                    tenantId,
                    "exec-" + i,
                    "req-" + i,
                    "SUCCESS",
                    0.95,
                    80,
                    List.of("Lesson " + i),
                    null,
                    false,
                    Instant.now().plusMillis(i)
            );
            repository.save(history);
        }

        // On unpatched HEAD: repository retains all 1,500 records unboundedly.
        // Bounded repository must evict eldest and cap recent records at MAX_CAPACITY (<= 1000).
        List<ReflectionHistory> recent = repository.findRecent(2000);
        assertTrue(recent.size() <= 1000,
                "InMemoryReflectionRepository must bound retention to <= 1000 records, but found: " + recent.size());
        assertEquals(recent.size(), repository.countByTenantId(tenantId),
                "Tenant store count must also be bounded to match retention policy");
    }

    // =========================================================================
    // Target 2: Unbounded Tool Execution Result Cache in DefaultToolService
    // =========================================================================

    @Test
    @DisplayName("Target 2: DefaultToolService bounds resultStore retention")
    void testBug2_DefaultToolService_BoundsResultStore() {
        DefaultToolService toolService = new DefaultToolService();

        for (int i = 0; i < 1500; i++) {
            ToolRequest request = new ToolRequest(
                    "tool-" + i,
                    ToolType.FILES,
                    "read",
                    Map.of("path", "file" + i + ".txt")
            );
            toolService.executeTool(request);
        }

        // On unpatched HEAD: tool-0 is retained forever.
        // Bounded result store must evict eldest entries so tool-0 is evicted when count exceeds bound (e.g. 1000).
        assertNull(toolService.getToolResult("tool-0"),
                "Eldest tool execution results must be evicted when resultStore capacity is exceeded to prevent heap leak");
    }

    // =========================================================================
    // Target 3: LLM Timeout/Cancellation Handling & Thread Interruption
    // =========================================================================

    @Test
    @DisplayName("Target 3: GeminiProvider does not retry on InterruptedIOException and preserves interrupt flag")
    void testBug3_GeminiProvider_CancellationAndTimeoutPreservesInterruptWithoutRetry() throws IOException {
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        AtomicInteger executeAttempts = new AtomicInteger(0);
        when(mockCall.execute()).thenAnswer(inv -> {
            executeAttempts.incrementAndGet();
            throw new InterruptedIOException("Execution interrupted or timed out");
        });

        GeminiProvider provider = new GeminiProvider(
                "https://generativelanguage.googleapis.com/v1beta/models/",
                "test-api-key",
                mockClient,
                2,
                10L
        );

        LlmRequest request = LlmRequest.builder()
                .model("gemini-2.5-flash")
                .prompt("Hello")
                .build();

        // Ensure current thread is not interrupted initially
        Thread.interrupted();

        assertThrows(IllegalStateException.class, () -> provider.stream(request));

        // It must NOT retry on timeout/interruption
        assertEquals(1, executeAttempts.get(),
                "GeminiProvider must not retry when request is interrupted or times out");
        assertTrue(Thread.currentThread().isInterrupted(),
                "Thread interrupt status must be preserved upon InterruptedIOException");

        // Clean up interrupt status
        Thread.interrupted();
    }

    @Test
    @DisplayName("Target 3b: OpenAiProvider and OllamaProvider preserve interrupt flag on InterruptedIOException")
    void testBug3b_OpenAiAndOllama_PreserveInterruptOnInterruptedIOException() throws IOException {
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new InterruptedIOException("Network call interrupted"));

        OpenAiProvider openAi = new OpenAiProvider("https://api.openai.com/v1/chat/completions", "test-key", mockClient);
        OllamaProvider ollama = new OllamaProvider("http://localhost:11434/api/generate", mockClient);
        LlmRequest req = LlmRequest.builder().model("default").prompt("hi").build();

        Thread.interrupted();
        assertThrows(IllegalStateException.class, () -> openAi.stream(req));
        assertTrue(Thread.currentThread().isInterrupted(), "OpenAiProvider must preserve thread interrupt flag");

        Thread.interrupted();
        assertThrows(IllegalStateException.class, () -> ollama.stream(req));
        assertTrue(Thread.currentThread().isInterrupted(), "OllamaProvider must preserve thread interrupt flag");
        Thread.interrupted();
    }

    // =========================================================================
    // Target 4: Vector Consistency & SPI Contract Validation
    // =========================================================================

    @Test
    @DisplayName("Target 4: VectorSearchEngine and PgVectors translate failures into VectorRuntimeException")
    void testBug4_VectorSearchEngine_ContractValidationAndConsistency() {
        InMemoryVectorStore store = new InMemoryVectorStore();
        InMemoryVectorSearchEngine engine = new InMemoryVectorSearchEngine(store);

        // 4a: InMemoryVectorSearchEngine must reject empty query embedding with VectorRuntimeException (same contract as PgVectorSearchEngine)
        assertThrows(VectorRuntimeException.class, () -> engine.search(new double[0], 5),
                "InMemoryVectorSearchEngine must throw VectorRuntimeException on empty query embedding");

        // 4b: VectorSearchEngine.hybridSearch must throw VectorRuntimeException (not raw UnsupportedOperationException)
        assertThrows(VectorRuntimeException.class, () -> engine.hybridSearch(new double[]{0.1, 0.2}, "query text", 5),
                "VectorSearchEngine.hybridSearch must throw VectorRuntimeException per SPI failure translation contract");

        // 4c: PgVectors.fromPgVectorLiteral must translate NumberFormatException into VectorRuntimeException
        assertThrows(VectorRuntimeException.class, () -> PgVectors.fromPgVectorLiteral("[1.0, invalid_float, 3.0]"),
                "PgVectors.fromPgVectorLiteral must throw VectorRuntimeException on malformed vector literal");
    }

    // =========================================================================
    // Target 5: JVM Crash / NPE on Dangling Relationships in Multi-Hop Reasoning
    // =========================================================================

    @Test
    @DisplayName("Target 5: MultiHopReasoningEngine handles dangling concept relationships without NullPointerException")
    void testBug5_MultiHopReasoning_DanglingConceptRelationshipDoesNotCrash() {
        DefaultMultiHopReasoningEngine engine = new DefaultMultiHopReasoningEngine();

        // Trusted evidence referencing "Spring Boot"
        EvidenceItem item = new EvidenceItem(
                "chunk-1",
                "doc-1",
                "We use Spring Boot for our microservices.",
                0.95,
                List.of("Spring Boot")
        );
        TrustScore trust = new TrustScore(0.9, 0.9, 0.9, 0.9, "high trust");
        ReliabilityResult reliability = new ReliabilityResult(List.of(new TrustedEvidence(item, trust)), 0.9);

        // Concept graph with "Spring Boot" concept, but a relationship pointing to an undefined concept "missing-target-concept"
        GraphConcept c1 = GraphConcept.of("c-springboot", "Spring Boot", ConceptType.FRAMEWORK);
        ConceptRelationship danglingRel = ConceptRelationship.of(
                "rel-1",
                "c-springboot",
                "missing-target-concept",
                RelationshipType.DEPENDS_ON,
                0.9
        );
        ConceptGraph conceptGraph = new ConceptGraph(List.of(c1), List.of(danglingRel));

        // On unpatched HEAD: engine crashes with NullPointerException when building ReasoningNode with null neighborName
        assertDoesNotThrow(() -> {
            ReasoningGraph result = engine.reason(reliability, conceptGraph);
            assertNotNull(result, "ReasoningGraph must be produced successfully without crashing on dangling relationships");
        });
    }

    // =========================================================================
    // Target 6: SDK Contract Violations — Raw Exceptions vs ValidationException
    // =========================================================================

    @Test
    @DisplayName("Target 6: IdentitySDK, ReflectionSDK, and ProjectSDK throw ValidationException on invalid arguments")
    void testBug6_SdkContractViolations_ThrowsValidationException() {
        ShreeAI ai = ShreeAI.builder().apiKey("test-key").build();
        IdentitySDK identitySDK = ai.identity();
        ReflectionSDK reflectionSDK = ai.reflection();
        ProjectSDK projectSDK = ai.project();

        // 6a: IdentitySDK.resolve must throw ValidationException, not IllegalArgumentException
        assertThrows(ValidationException.class, () -> identitySDK.resolve("", null, null, null),
                "IdentitySDK.resolve must throw ValidationException on blank identityId");

        // 6b: ReflectionSDK methods must throw ValidationException, not IllegalArgumentException
        assertThrows(ValidationException.class, () -> reflectionSDK.reflect(""),
                "ReflectionSDK.reflect must throw ValidationException on blank executionId");
        assertThrows(ValidationException.class, () -> reflectionSDK.getHistory("", 10),
                "ReflectionSDK.getHistory must throw ValidationException on blank tenantId");
        assertThrows(ValidationException.class, () -> reflectionSDK.getAnalytics("tenant", 0),
                "ReflectionSDK.getAnalytics must throw ValidationException on non-positive window");

        // 6c: ProjectSDK methods must throw ValidationException, not NullPointerException
        assertThrows(ValidationException.class, () -> projectSDK.analyze((String) null),
                "ProjectSDK.analyze must throw ValidationException on null path");
        assertThrows(ValidationException.class, () -> projectSDK.findClass(null),
                "ProjectSDK.findClass must throw ValidationException on null simpleName");
        assertThrows(ValidationException.class, () -> projectSDK.impact(null),
                "ProjectSDK.impact must throw ValidationException on null simpleName");
    }

    // =========================================================================
    // Target 7: NullPointerException in Project Intelligence Impact Analysis
    // =========================================================================

    @Test
    @DisplayName("Target 7: DefaultProjectIntelligenceEngine handles null target gracefully in impact()")
    void testBug7_ProjectIntelligence_ImpactAnalysisWithNullTarget() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        // Analyze current directory to populate lastGraph and lastClasses
        engine.analyze(Path.of("").toAbsolutePath());

        // On unpatched HEAD: simpleName.equals(c.name()) throws NullPointerException when simpleName is null
        assertDoesNotThrow(() -> {
            ProjectImpact impact = engine.impact(null);
            assertNotNull(impact, "ProjectImpact must be returned without throwing NullPointerException");
            assertEquals("", impact.target(), "Target should default to empty string on null target");
        });
    }
}
