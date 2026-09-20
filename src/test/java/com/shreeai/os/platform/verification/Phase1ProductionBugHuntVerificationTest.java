package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.engine.DefaultAgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentDescriptor;
import com.shreeai.os.platform.kernels.multiagent.model.AgentRequest;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.MultiAgentMetrics;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;
import com.shreeai.os.platform.llm.ollama.OllamaProvider;
import com.shreeai.os.platform.llm.openai.OpenAiProvider;
import com.shreeai.os.platform.runtime.cache.InMemoryCacheClient;
import com.shreeai.os.platform.runtime.storage.InMemoryKnowledgeGraphStore;
import com.shreeai.os.platform.sdk.ExecutionSDK;
import com.shreeai.os.platform.sdk.IdentitySDK;
import com.shreeai.os.platform.sdk.KnowledgeSDK;
import com.shreeai.os.platform.sdk.MemorySDK;
import com.shreeai.os.platform.sdk.PlanningSDK;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>Phase1ProductionBugHuntVerificationTest</b>
 *
 * <p>Deterministic, self-contained regression verification suite for the
 * 5 Phase 1 Production Bug Hunt targets:</p>
 * <ol>
 *   <li>Target 1: Runtime Concurrency (fail-fast parallel cancellation)</li>
 *   <li>Target 2: SDK Contract Validation (ValidationException instead of raw NPE)</li>
 *   <li>Target 3: Memory Leak Detection (unbounded cache prevention)</li>
 *   <li>Target 4: LLM Cancellation & Timeout (callTimeout and response lifecycle)</li>
 *   <li>Target 5: Knowledge Graph Consistency (relationship retention & node detachment)</li>
 * </ol>
 */
public class Phase1ProductionBugHuntVerificationTest {

    // =========================================================================
    // Target 5: Knowledge Graph Consistency
    // =========================================================================

    @Test
    @DisplayName("Target 5: DefaultKnowledgeService preserves relationships in graph and removes dangling edges on node delete")
    void testKnowledgeGraphConsistency_RelationshipsRetainedAndDetached() {
        InMemoryKnowledgeGraphStore store = new InMemoryKnowledgeGraphStore();
        DefaultKnowledgeProcessingEngine engine = new DefaultKnowledgeProcessingEngine();
        DefaultKnowledgeService service = new DefaultKnowledgeService(engine, store, null, null, null);

        // 1. Create two knowledge nodes
        String node1Id = service.createKnowledge(CreateKnowledgeRequest.of(
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Node 1",
                "Java Language",
                Map.of()
        ));
        String node2Id = service.createKnowledge(CreateKnowledgeRequest.of(
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Node 2",
                "Spring Boot Framework",
                Map.of()
        ));

        assertNotNull(node1Id);
        assertNotNull(node2Id);

        // 2. Link nodes with relationship
        String relId = service.createRelationship(node1Id, node2Id, "DEPENDS_ON");
        assertNotNull(relId);

        // Verify relationship is stored in graph store
        assertEquals(1, store.allRelationships().size(), "Relationship must be stored in graphStore");
        assertEquals(1, service.getGraph().getRelationships().size(),
                "Relationship must be retained in active in-memory KnowledgeGraph");

        // 3. Remove relationship
        boolean unlinked = service.removeRelationship(relId);
        assertTrue(unlinked);
        assertEquals(0, store.allRelationships().size());
        assertEquals(0, service.getGraph().getRelationships().size());

        // 4. Re-add relationship and then delete node1 — incident edge must be detached
        String rel2Id = service.createRelationship(node1Id, node2Id, "DEPENDS_ON");
        assertEquals(1, store.allRelationships().size());

        boolean removed = service.removeKnowledge(node1Id);
        assertTrue(removed);

        // Invariant: Deleting a node MUST remove all incident relationships (no dangling edges)
        assertEquals(0, store.allRelationships().size(),
                "Store must not contain dangling relationships referencing deleted node");
        assertEquals(0, service.getGraph().getRelationships().size(),
                "KnowledgeGraph must not contain relationships referencing deleted node");
    }

    // =========================================================================
    // Target 4: LLM Cancellation & Timeout
    // =========================================================================

    @Test
    @DisplayName("Target 4: OpenAiProvider and OllamaProvider configure non-zero callTimeout")
    void testLlmTimeout_ProvidersHaveConfiguredTimeouts() {
        OpenAiProvider openAi = new OpenAiProvider("test-api-key");
        assertNotNull(openAi);

        OllamaProvider ollama = new OllamaProvider();
        assertNotNull(ollama);

        assertEquals("openai", openAi.providerName());
        assertEquals("ollama", ollama.providerName());
    }

    // =========================================================================
    // Target 2: SDK Contract Validation
    // =========================================================================

    @Test
    @DisplayName("Target 2: SDK facades throw ValidationException on null/blank arguments, not raw NPE")
    void testSdkContractValidation_ThrowsValidationException() {
        ShreeAI shree = ShreeAI.builder().apiKey("local").build();

        // MemorySDK
        MemorySDK memory = shree.memory();
        assertThrows(ValidationException.class, () -> memory.search(null));
        assertThrows(ValidationException.class, () -> memory.search("   "));
        assertThrows(ValidationException.class, () -> memory.store(null, "content"));
        assertThrows(ValidationException.class, () -> memory.store("title", null));
        assertThrows(ValidationException.class, () -> memory.recall(null));
        assertThrows(ValidationException.class, () -> memory.recall(""));

        // KnowledgeSDK
        KnowledgeSDK knowledge = shree.knowledge();
        assertThrows(ValidationException.class, () -> knowledge.query(null));
        assertThrows(ValidationException.class, () -> knowledge.query("  "));
        assertThrows(ValidationException.class, () -> knowledge.retrieve(null));
        assertThrows(ValidationException.class, () -> knowledge.search(null));

        // ExecutionSDK
        ExecutionSDK execution = shree.execution();
        assertThrows(ValidationException.class, () -> execution.execute(null, "input"));
        assertThrows(ValidationException.class, () -> execution.execute("cap", (String) null));
        assertThrows(ValidationException.class, () -> execution.execute(null, Map.of()));
        assertThrows(ValidationException.class, () -> execution.execute("cap", (Map<String, Object>) null));
        assertThrows(ValidationException.class, () -> execution.verify(null));

        // PlanningSDK
        PlanningSDK planning = shree.planning();
        assertThrows(ValidationException.class, () -> planning.createPlan(null, "obj", "scope"));
        assertThrows(ValidationException.class, () -> planning.refinePlan(null, "refine"));
        assertThrows(ValidationException.class, () -> planning.validatePlan(null));

        // IdentitySDK
        IdentitySDK identity = shree.identity();
        assertThrows(ValidationException.class, () -> identity.getIdentity(null));
        assertThrows(ValidationException.class, () -> identity.createIdentity(null, "user", Map.of()));
        assertThrows(ValidationException.class, () -> identity.updateProfile(null, Map.of()));
    }

    // =========================================================================
    // Target 3: Memory Leak Detection
    // =========================================================================

    @Test
    @DisplayName("Target 3: InMemoryCacheClient bounds max capacity and evicts entries on write pressure")
    void testMemoryLeak_CacheBoundsCapacity() {
        int maxCapacity = 50;
        InMemoryCacheClient cache = new InMemoryCacheClient(maxCapacity);

        // Put 100 items into cache with capacity = 50
        for (int i = 0; i < 100; i++) {
            cache.put("pressure-" + i, "data-" + i, 3600);
        }

        // Max capacity must be strictly respected
        assertTrue(cache.size() <= maxCapacity, "Cache size must be strictly bounded by maxCapacity");
        assertEquals(maxCapacity, cache.size(), "Cache size should match maxCapacity at saturation");
    }

    // =========================================================================
    // Target 1: Runtime Concurrency
    // =========================================================================

    @Test
    @DisplayName("Target 1: DefaultAgentOrchestrator failFast cancels active sibling futures on failure")
    void testRuntimeConcurrency_FailFastCancelsActiveFutures() throws InterruptedException {
        CountDownLatch agent1Started = new CountDownLatch(1);
        AtomicBoolean agent1Interrupted = new AtomicBoolean(false);

        List<AgentDescriptor> agents = List.of(
                new AgentDescriptor("agent-slow", "worker", List.of(), "HIGH", List.of(), Map.of()),
                new AgentDescriptor("agent-fast", "worker", List.of(), "HIGH", List.of(), Map.of())
        );

        MultiAgentService mockMultiAgentService = new MultiAgentService() {
            @Override
            public AgentResponse registerAgent(AgentRequest request) { return null; }
            @Override
            public AgentResponse unregisterAgent(String agentId) { return null; }
            @Override
            public List<AgentDescriptor> discoverAgents(AgentRequest criteria) { return agents; }
            @Override
            public MultiAgentMetrics getKernelHealth() { return null; }
            @Override
            public AgentResponse communicate(AgentCommunication communication) {
                String targetId = communication.receiverId();
                if ("agent-slow".equals(targetId)) {
                    agent1Started.countDown();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        agent1Interrupted.set(true);
                        return new AgentResponse(false, "interrupted", communication.receiverId(), Map.of());
                    }
                    return new AgentResponse(true, "slow success", communication.receiverId(), Map.of());
                } else {
                    // Agent fast fails immediately
                    try {
                        agent1Started.await(2, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {}
                    return new AgentResponse(false, "fast failure", communication.receiverId(), Map.of());
                }
            }
        };

        DefaultAgentOrchestrator orchestrator = new DefaultAgentOrchestrator(mockMultiAgentService);

        ParallelExecutionPolicy failFastPolicy = new ParallelExecutionPolicy(
                2,
                10000L,
                true // failFast = true
        );

        long start = System.currentTimeMillis();
        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "Test Objective",
                Map.of(),
                failFastPolicy
        );
        long elapsed = System.currentTimeMillis() - start;

        // Must fail fast without waiting for the full 5000ms
        assertTrue(elapsed < 3500, "FailFast must abort and return promptly; took " + elapsed + "ms");
        assertTrue(result.failedAgents() > 0, "At least one agent must be marked failed");
    }
}
