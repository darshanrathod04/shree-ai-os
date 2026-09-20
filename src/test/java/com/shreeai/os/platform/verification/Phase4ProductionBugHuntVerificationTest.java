package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.graph.NodeType;
import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.engine.DefaultAgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.*;
import com.shreeai.os.platform.kernels.planning.engine.DefaultTaskDependencyGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.PlanMilestone;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.resolver.CapabilityRequirement;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.cache.InMemoryCacheClient;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.runtime.graph.ExecutionNodeResult;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.execution.KnowledgeIngestionEventConsumer;
import com.shreeai.os.platform.runtime.graph.DefaultGraphRuntimeExecutor;
import com.shreeai.os.platform.runtime.graph.NodeExecutor;
import com.shreeai.os.platform.runtime.storage.InMemoryKnowledgeGraphStore;
import com.shreeai.os.platform.runtime.storage.StorageRuntimeException;
import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeClient;
import com.shreeai.os.platform.sdk.ShreeSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <b>Phase4ProductionBugHuntVerificationTest</b>
 *
 * <p>Constitutional test suite reproducing and verifying fixes for 7 newly
 * discovered production defects in Phase 4:
 * <ul>
 *   <li>Target 1: Race condition / CME in DefaultAgentOrchestrator parallel fail-fast</li>
 *   <li>Target 2: Arithmetic overflow on large TTL in InMemoryCacheClient</li>
 *   <li>Target 3: Missing SPI exception translation on null in InMemoryKnowledgeGraphStore</li>
 *   <li>Target 4: Graph runtime crash on arbitrary node IDs in DefaultGraphRuntimeExecutor</li>
 *   <li>Target 5: Ingestion hang and missing failure event on null metadata in KnowledgeIngestionEventConsumer</li>
 *   <li>Target 6: Concurrency / CME in ShreeSession metadata under concurrent chat and mutation</li>
 *   <li>Target 7: NullPointerException on null milestone element in DefaultTaskDependencyGraphEngine</li>
 * </ul>
 */
public class Phase4ProductionBugHuntVerificationTest {

    // =========================================================================
    // Target 1: Concurrency / Race Condition in DefaultAgentOrchestrator
    // =========================================================================

    @Test
    @DisplayName("Target 1: DefaultAgentOrchestrator parallel fail-fast does not throw ConcurrentModificationException")
    void testBug1_DefaultAgentOrchestrator_ConcurrentFailFastDoesNotThrowCME() {
        MultiAgentService mockService = mock(MultiAgentService.class);

        // Create 20 agents
        List<AgentDescriptor> agents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            agents.add(new AgentDescriptor("agent-" + i, "Worker", List.of(), "HIGH", List.of(), Map.of()));
        }
        when(mockService.discoverAgents(any())).thenReturn(agents);

        // First agent fails immediately; other agents take a small delay
        when(mockService.communicate(any())).thenAnswer(invocation -> {
            AgentCommunication comm = invocation.getArgument(0);
            if ("agent-0".equals(comm.receiverId())) {
                return new AgentResponse(false, "immediate failure", "agent-0", Map.of("error", "fail"));
            }
            Thread.sleep(5);
            return new AgentResponse(true, "success", comm.receiverId(), Map.of());
        });

        DefaultAgentOrchestrator orchestrator = new DefaultAgentOrchestrator(mockService);
        ParallelExecutionPolicy policy = new ParallelExecutionPolicy(16, 5000L, true);

        // On unpatched HEAD: futures is ArrayList; fail-fast iteration causes CME or data race
        assertDoesNotThrow(() -> {
            for (int run = 0; run < 15; run++) {
                ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                        "test objective", Map.of(), policy);
                assertNotNull(result);
                assertTrue(result.failedAgents() > 0, "Fail-fast should report failed tasks");
            }
        });
    }

    // =========================================================================
    // Target 2: Arithmetic Overflow on Large TTL in InMemoryCacheClient
    // =========================================================================

    @Test
    @DisplayName("Target 2: InMemoryCacheClient prevents integer overflow on large TTL")
    void testBug2_InMemoryCacheClient_LargeTtlDoesNotOverflow() {
        InMemoryCacheClient cache = new InMemoryCacheClient(100);

        // Putting entry with Long.MAX_VALUE TTL seconds
        cache.put("overflow-key", "valid-value", Long.MAX_VALUE);

        // On unpatched HEAD: ttlSeconds * 1000 overflows to negative number, so expiry is negative
        // and item is expired immediately at insertion time.
        assertTrue(cache.get("overflow-key").isPresent(),
                "Cache entry with Long.MAX_VALUE TTL must not be expired due to integer overflow");
        assertEquals("valid-value", cache.get("overflow-key").get());
        assertTrue(cache.contains("overflow-key"));
    }

    // =========================================================================
    // Target 3: SPI Exception Translation in InMemoryKnowledgeGraphStore
    // =========================================================================

    @Test
    @DisplayName("Target 3: InMemoryKnowledgeGraphStore translates null inputs into StorageRuntimeException")
    void testBug3_InMemoryKnowledgeGraphStore_ThrowsStorageRuntimeExceptionOnNull() {
        InMemoryKnowledgeGraphStore store = new InMemoryKnowledgeGraphStore();

        // SPI contract requires: "Failures MUST be translated into StorageRuntimeException."
        // On unpatched HEAD: Objects.requireNonNull throws raw NullPointerException
        assertThrows(StorageRuntimeException.class, () -> store.saveNode(null),
                "saveNode(null) must throw StorageRuntimeException per SPI contract");

        assertThrows(StorageRuntimeException.class, () -> store.saveRelationship(null),
                "saveRelationship(null) must throw StorageRuntimeException per SPI contract");
    }

    // =========================================================================
    // Target 4: Graph Runtime Executor with Arbitrary Node IDs
    // =========================================================================

    @Test
    @DisplayName("Target 4: DefaultGraphRuntimeExecutor resolves dependencies by node ID without IllegalArgumentException")
    void testBug4_DefaultGraphRuntimeExecutor_ResolvesArbitraryNodeIds() {
        Map<CapabilityType, NodeExecutor> executors = new EnumMap<>(CapabilityType.class);
        executors.put(CapabilityType.IDENTITY, (node, req, ctx) ->
                ExecutionNodeResult.completed(node.nodeId(), node.capability(), "identity-ok", 1.0, 10L));
        executors.put(CapabilityType.PLANNING, (node, req, ctx) ->
                ExecutionNodeResult.completed(node.nodeId(), node.capability(), "planning-ok", 1.0, 10L));

        DefaultGraphRuntimeExecutor executor = new DefaultGraphRuntimeExecutor(executors);

        // Node with custom ID "node-identity"
        ExecutionNode identityNode = ExecutionNode.builder()
                .nodeId("node-identity")
                .capability(CapabilityType.IDENTITY)
                .nodeType(NodeType.forCapability(CapabilityType.IDENTITY))
                .priority(CapabilityRequirement.Priority.REQUIRED)
                .build();

        // Node with custom ID "node-planning" depending on "node-identity"
        ExecutionNode planningNode = ExecutionNode.builder()
                .nodeId("node-planning")
                .capability(CapabilityType.PLANNING)
                .nodeType(NodeType.forCapability(CapabilityType.PLANNING))
                .priority(CapabilityRequirement.Priority.REQUIRED)
                .addDependency("node-identity")
                .build();

        ExecutionGraph graph = ExecutionGraph.builder()
                .graphId("g-test")
                .requestId("req-test")
                .rootNode(identityNode)
                .nodes(List.of(identityNode, planningNode))
                .edges(List.of())
                .build();

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("req-test")
                .requestType("CHAT")
                .payload("test")
                .build();

        // On unpatched HEAD: CapabilityType.valueOf("node-identity") throws IllegalArgumentException
        assertDoesNotThrow(() -> {
            ExecutionSession session = executor.execute(graph, request);
            assertNotNull(session);
            assertEquals(ExecutionSession.SessionStatus.COMPLETED, session.status());
        });
    }

    // =========================================================================
    // Target 5: KnowledgeIngestionEventConsumer Null Metadata Handling
    // =========================================================================

    @Test
    @DisplayName("Target 5: KnowledgeIngestionEventConsumer handles null metadata and faulty service without hanging")
    void testBug5_KnowledgeIngestionEventConsumer_HandlesNullMetadataWithoutHanging() {
        RuntimeEventBus bus = new RuntimeEventBus();
        KnowledgeIngestionEventConsumer consumer = new KnowledgeIngestionEventConsumer(
                () -> { throw new IllegalStateException("Service unavailable"); },
                bus
        );

        AtomicReference<RuntimeEvent> completedEvent = new AtomicReference<>();
        bus.subscribe(EventType.KNOWLEDGE_INGEST_COMPLETED, completedEvent::set);

        // Event with empty/null metadata triggering unhandled exception on unpatched HEAD
        RuntimeEvent requestEvent = mock(RuntimeEvent.class);
        when(requestEvent.type()).thenReturn(EventType.KNOWLEDGE_INGEST_REQUESTED);
        when(requestEvent.requestId()).thenReturn("req-null-meta");
        when(requestEvent.metadata()).thenReturn(null);

        consumer.onEvent(requestEvent);

        // On unpatched HEAD: throws exception in process() and event is never published, causing caller hang
        assertNotNull(completedEvent.get(), "KNOWLEDGE_INGEST_COMPLETED must be published even on invalid metadata/service error");
        assertEquals("FAILED", completedEvent.get().metadata().get("status"),
                "Event status must be FAILED on null metadata/error to unblock caller");
    }

    // =========================================================================
    // Target 6: ShreeSession Concurrency / CME on Metadata Access
    // =========================================================================

    @Test
    @DisplayName("Target 6: ShreeSession supports concurrent metadata mutations and chat requests without CME")
    void testBug6_ShreeSession_ConcurrentMetadataAccess() throws Exception {
        ShreeClient mockClient = mock(ShreeClient.class);
        when(mockClient.chat((SDKRequest) any())).thenReturn(SDKResponse.builder().answer("ok").build());

        ShreeSession session = ShreeSession.create(mockClient);

        int threadCount = 8;
        int iterations = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        if (threadId % 2 == 0) {
                            session.metadata("key-" + threadId + "-" + i, "val-" + i);
                        } else {
                            session.chat("message-" + i);
                        }
                    }
                } catch (Throwable ex) {
                    errors.add(ex);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        // On unpatched HEAD: HashMap throws ConcurrentModificationException during chat() -> new HashMap<>(metadata)
        assertTrue(errors.isEmpty(), "Concurrent session operations must not produce errors: " + errors);
    }

    // =========================================================================
    // Target 7: DefaultTaskDependencyGraphEngine Null Milestone Element
    // =========================================================================

    @Test
    @DisplayName("Target 7: DefaultTaskDependencyGraphEngine gracefully handles null milestone elements")
    void testBug7_DefaultTaskDependencyGraphEngine_HandlesNullMilestoneElements() {
        DefaultTaskDependencyGraphEngine engine = new DefaultTaskDependencyGraphEngine();

        PlanMilestone m1 = new PlanMilestone("Foundation", List.of(), 1, "output", Map.of());
        List<PlanMilestone> milestonesWithNull = Arrays.asList(m1, null);

        // On unpatched HEAD: List.copyOf(milestones) throws NullPointerException
        assertDoesNotThrow(() -> {
            TaskGraph graph = engine.buildTaskGraph(milestonesWithNull);
            assertNotNull(graph, "TaskGraph should be built successfully ignoring null milestone elements");
            assertFalse(graph.tasks().isEmpty(), "Tasks from valid milestone should be present");
        });
    }
}
