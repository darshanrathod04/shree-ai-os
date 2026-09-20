package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.HtmlDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.MarkdownDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.TextDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryVersionLedger;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryResult;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.memory.model.UpdateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.ollama.OllamaProvider;
import com.shreeai.os.platform.llm.openai.OpenAiProvider;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression and verification test suite asserting that the 10 production defects
 * discovered during the Shree AI OS V1 Bug Hunting Mission are cleanly and robustly fixed.
 */
public class ProductionBugSweepVerificationTest {

    // =========================================================================
    // BUG 1: OpenAiProvider ChunkSpliterator contract violation
    // =========================================================================
    @Test
    @DisplayName("Bug 1 Fix: OpenAiProvider ChunkSpliterator handles ping/keep-alive and does not violate Spliterator contract")
    void testBug1_openAiProviderSpliteratorContract() {
        String ssePayload =
                ": ping - keep-alive comment line\n\n" +
                "data: {\"choices\":[{\"delta\":{}}]}\n\n" +
                "\n\n" +
                "data: {\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}\n\n" +
                ": keepalive again\n\n" +
                "data: {\"choices\":[{\"delta\":{\"content\":\" World\"}}]}\n\n" +
                "data: [DONE]\n\n";

        OkHttpClient mockClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> new Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(ResponseBody.create(MediaType.parse("text/event-stream"), ssePayload))
                        .build())
                .build();

        OpenAiProvider provider = new OpenAiProvider("http://fake-openai-url/v1/chat/completions", "test-key", mockClient);
        LlmRequest request = LlmRequest.builder().prompt("test").build();

        List<String> tokens = provider.stream(request).toList();
        assertEquals(List.of("Hello", " World"), tokens, "Stream should skip keep-alive comments and return tokens without contract violation");
    }

    // =========================================================================
    // BUG 2: OllamaProvider ChunkSpliterator terminal token drop
    // =========================================================================
    @Test
    @DisplayName("Bug 2 Fix: OllamaProvider ChunkSpliterator yields terminal token on done:true chunk")
    void testBug2_ollamaProviderSpliteratorTerminalToken() {
        String ndjsonPayload =
                "{\"response\":\"Hello\",\"done\":false}\n" +
                "\n" +
                "{\"done\":false}\n" +
                "{\"response\":\" World\",\"done\":true}\n";

        OkHttpClient mockClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> new Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(ResponseBody.create(MediaType.parse("application/json"), ndjsonPayload))
                        .build())
                .build();

        OllamaProvider provider = new OllamaProvider("http://fake-ollama-url/api/generate", mockClient);
        LlmRequest request = LlmRequest.builder().prompt("test").build();

        List<String> tokens = provider.stream(request).toList();
        assertEquals(List.of("Hello", " World"), tokens, "Terminal token carried on done:true must be preserved and not dropped");
    }

    // =========================================================================
    // BUG 3: DefaultKnowledgeService concurrent graph mutation lost updates
    // =========================================================================
    @Test
    @DisplayName("Bug 3 Fix: DefaultKnowledgeService graph mutations use atomic CAS updateAndGet under concurrency")
    void testBug3_defaultKnowledgeServiceConcurrentMutations() throws InterruptedException {
        DefaultKnowledgeService knowledgeService = new DefaultKnowledgeService(new DefaultKnowledgeProcessingEngine());
        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    CreateKnowledgeRequest request = CreateKnowledgeRequest.of(
                            KnowledgeType.CONCEPT,
                            KnowledgeState.ACTIVE,
                            KnowledgeScope.GLOBAL,
                            "Concept-" + index,
                            "Description of concept " + index,
                            Map.of("topic", "concurrent-test-topic", "authority", 0.9)
                    );
                    knowledgeService.createKnowledge(request);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue(endGate.await(10, TimeUnit.SECONDS), "All concurrent knowledge creation tasks should complete");
        executor.shutdown();

        List<KnowledgeNode> nodes = knowledgeService.searchByTopic("concurrent-test-topic");
        assertEquals(threadCount, nodes.size(), "All concurrent knowledge creations must be preserved via CAS loops without lost updates");
    }

    // =========================================================================
    // BUG 4: DefaultMemoryService concurrent update check-then-act race
    // =========================================================================
    @Test
    @DisplayName("Bug 4 Fix: DefaultMemoryService updateMemory uses atomic computeIfPresent")
    void testBug4_defaultMemoryServiceConcurrentUpdates() throws InterruptedException {
        DefaultMemoryService memoryService = new DefaultMemoryService(new MemoryValidator(), new DefaultMemoryProcessingEngine());

        MemoryContent initialContent = new MemoryContent("Initial content", null, Map.of(), Instant.now());
        MemoryMetadata metadata = new MemoryMetadata(
                new MemoryId("mem-test-1"),
                MemoryType.SEMANTIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("owner-1"),
                Set.of("tag1"),
                0.8,
                0.9,
                "source",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );
        MemoryId id = memoryService.createMemory(new CreateMemoryRequest(initialContent, metadata, Instant.now()));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    MemoryContent updatedContent = new MemoryContent("Updated content " + index, null, Map.of(), Instant.now());
                    MemoryResult result = memoryService.updateMemory(new UpdateMemoryRequest(id, updatedContent, null, Instant.now()));
                    if (result.success()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue(endGate.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threadCount, successCount.get(), "All updates should succeed atomically");
        assertEquals(threadCount + 1, memoryService.versionOf(id), "Version ledger should record exactly all updates");
    }

    // =========================================================================
    // BUG 5: MemoryVersionLedger ConcurrentModificationException
    // =========================================================================
    @Test
    @DisplayName("Bug 5 Fix: MemoryVersionLedger history() synchronizes on versions monitor to prevent ConcurrentModificationException")
    void testBug5_memoryVersionLedgerConcurrentSnapshotAndHistory() throws InterruptedException {
        MemoryVersionLedger ledger = new MemoryVersionLedger();
        MemoryId id = new MemoryId("ledger-test-id");
        int iterations = 1000;
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicBoolean errorDetected = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        MemoryMetadata meta = new MemoryMetadata(
                id, MemoryType.EPISODIC, MemoryStatus.ACTIVE, MemoryVisibility.PRIVATE,
                new IdentityId("user-1"), Set.of(), 0.5, 0.5, "source",
                Instant.now(), Instant.now(), Instant.now(), 0L
        );

        // Writer threads
        for (int w = 0; w < 2; w++) {
            final int writerId = w;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterations && running.get(); i++) {
                        Memory m = new Memory(id, new MemoryContent("content " + writerId + "-" + i, null, Map.of(), Instant.now()), meta, Instant.now(), Instant.now());
                        ledger.snapshot(m);
                    }
                } catch (Throwable t) {
                    errorDetected.set(true);
                }
            });
        }

        // Reader threads
        for (int r = 0; r < 2; r++) {
            executor.submit(() -> {
                try {
                    while (running.get()) {
                        List<Memory> history = ledger.history(id);
                        assertNotNull(history);
                    }
                } catch (Throwable t) {
                    errorDetected.set(true);
                }
            });
        }

        Thread.sleep(300);
        running.set(false);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(errorDetected.get(), "No ConcurrentModificationException should occur during concurrent snapshot and history");
    }

    // =========================================================================
    // BUG 6: DefaultMemoryService blank query leaks all data & NPE on null text
    // =========================================================================
    @Test
    @DisplayName("Bug 6 Fix: DefaultMemoryService search ignores blank queries and handles null content safely")
    void testBug6_memoryServiceBlankAndNullSearch() {
        DefaultMemoryService memoryService = new DefaultMemoryService(new MemoryValidator(), new DefaultMemoryProcessingEngine());

        // Store a memory
        MemoryContent content = new MemoryContent("User profile for Darshan", null, Map.of(), Instant.now());
        MemoryMetadata metadata = new MemoryMetadata(
                new MemoryId("mem-blank-1"),
                MemoryType.EPISODIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("user-1"),
                Set.of("profile"),
                0.5,
                0.5,
                "profile",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );
        memoryService.createMemory(new CreateMemoryRequest(content, metadata, Instant.now()));

        // Blank and whitespace search queries MUST return empty list, not all memories
        assertTrue(memoryService.search("").isEmpty(), "Blank query must return empty list");
        assertTrue(memoryService.search("   ").isEmpty(), "Whitespace query must return empty list");
        assertTrue(memoryService.searchBySimilarity("").isEmpty(), "Blank similarity query must return empty list");
        assertTrue(memoryService.searchBySimilarity("   ").isEmpty(), "Whitespace similarity query must return empty list");

        // Search with non-blank query retrieves match
        assertEquals(1, memoryService.search("Darshan").size());
        assertEquals(1, memoryService.searchBySimilarity("darshan").size());
    }

    // =========================================================================
    // BUG 7: RuntimeEventBus null safety and listener exception isolation
    // =========================================================================
    @Test
    @DisplayName("Bug 7 Fix: RuntimeEventBus isolates listener exceptions and protects against null events")
    void testBug7_runtimeEventBusFaultIsolation() {
        RuntimeEventBus bus = new RuntimeEventBus();
        AtomicBoolean listener2Received = new AtomicBoolean(false);

        // Listener 1 throws an unhandled RuntimeException
        bus.subscribe(EventType.PIPELINE_STARTED, event -> {
            throw new RuntimeException("Simulated listener 1 failure");
        });

        // Listener 2 should still receive the event
        bus.subscribe(EventType.PIPELINE_STARTED, event -> {
            listener2Received.set(true);
        });

        RuntimeEvent event = new RuntimeEvent(
                EventType.PIPELINE_STARTED,
                "req-1",
                "Pipeline",
                Instant.now(),
                Map.of()
        );

        bus.publish(event);
        assertTrue(listener2Received.get(), "Listener 2 must receive event even if Listener 1 throws RuntimeException");

        // Null checks
        bus.publish(null);
        bus.subscribe(null, null);
        bus.unsubscribe(null, null);
    }

    // =========================================================================
    // BUG 8: Knowledge document parsers null safety
    // =========================================================================
    @Test
    @DisplayName("Bug 8 Fix: Html, Text, and Markdown document parsers return empty list on null/blank input without NPE")
    void testBug8_documentParsersNullSafety() {
        HtmlDocumentParser htmlParser = new HtmlDocumentParser();
        assertTrue(htmlParser.parse(null).isEmpty());
        assertTrue(htmlParser.parse("   ").isEmpty());

        TextDocumentParser textParser = new TextDocumentParser();
        assertTrue(textParser.parse(null).isEmpty());
        assertTrue(textParser.parse("   ").isEmpty());

        MarkdownDocumentParser mdParser = new MarkdownDocumentParser();
        assertTrue(mdParser.parse(null).isEmpty());
        assertTrue(mdParser.parse("   ").isEmpty());
    }

    // =========================================================================
    // BUG 9: DefaultMultiHopReasoningEngine missing concept chunk mapping NPE
    // =========================================================================
    @Test
    @DisplayName("Bug 9 Fix: DefaultMultiHopReasoningEngine handles evidence with null content and missing chunk mappings without NPE")
    void testBug9_multiHopReasoningEngineNullSafety() {
        DefaultMultiHopReasoningEngine engine = new DefaultMultiHopReasoningEngine();

        GraphConcept concept1 = new GraphConcept("id-java", "Java", ConceptType.TECHNOLOGY);
        GraphConcept concept2 = new GraphConcept("id-kotlin", "Kotlin", ConceptType.TECHNOLOGY);
        ConceptRelationship rel = new ConceptRelationship("rel-1", "Java", "Kotlin", RelationshipType.DEPENDS_ON, 0.9);
        ConceptGraph graph = new ConceptGraph(List.of(concept1, concept2), List.of(rel));

        // Evidence mentioning Java only (Kotlin has no evidence chunks, so evidenceChunkIdsByConcept.get("Kotlin") is null)
        TrustedEvidence te1 = new TrustedEvidence(
                new EvidenceItem("c1", "d1", "Java is a programming language", 0.9, List.of()),
                new TrustScore(0.9, 1.0, 0.9, 0.8, "official")
        );
        TrustedEvidence te2 = new TrustedEvidence(
                new EvidenceItem("c2", "d2", "General architecture overview", 0.5, List.of()),
                new TrustScore(0.5, 0.5, 0.5, 0.5, "unverified")
        );

        ReliabilityResult reliability = new ReliabilityResult(List.of(te1, te2), 0.7);
        ReasoningGraph result = engine.reason(reliability, graph);
        assertNotNull(result, "Reasoning graph must be produced cleanly without NullPointerException");
    }

    // =========================================================================
    // BUG 10: DefaultReflectionEngine locale-independent score formatting
    // =========================================================================
    @Test
    @DisplayName("Bug 10 Fix: DefaultReflectionEngine formats scores using Locale.ROOT to prevent comma decimal separators in European locales")
    void testBug10_reflectionEngineLocaleIndependence() {
        Locale originalDefault = Locale.getDefault();
        try {
            // In Germany, decimal separator is a comma: "0,85"
            Locale.setDefault(Locale.GERMANY);

            DefaultReflectionEngine engine = new DefaultReflectionEngine();
            ReflectionInput input = new ReflectionInput("req-test-1", "test prompt", 2, "COMPLETED", true, "response summary", 0.40);
            ReflectionAnalysis analysis = engine.reflect(input);

            // Summary must contain dot decimal separator "score 0.82", NOT comma "score 0,82"
            assertTrue(analysis.summary().contains("score 0.82"), "Summary must format score with dot decimal separator: " + analysis.summary());

            // Low confidence lesson must also contain dot decimal separator "0.40"
            boolean foundLesson = analysis.lessons().stream().anyMatch(l -> l.contains("0.40"));
            assertTrue(foundLesson, "Lessons must format confidence with dot decimal separator: " + analysis.lessons());
        } finally {
            Locale.setDefault(originalDefault);
        }
    }
}
