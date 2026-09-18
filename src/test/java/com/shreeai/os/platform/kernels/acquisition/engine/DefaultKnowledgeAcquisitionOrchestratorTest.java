package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecision;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionRecord;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionStatus;
import com.shreeai.os.platform.kernels.acquisition.model.FreshnessReason;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultDocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.engine.DocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic tests for the K0.6.5 {@link DefaultKnowledgeAcquisitionOrchestrator}.
 * Every test is fixed-input, fixed-clock and side-effect free: the recording
 * ingestion engine proves exactly when ingestion happens, and the real K2
 * ingestion engine proves canonical document production.
 */
public class DefaultKnowledgeAcquisitionOrchestratorTest {

    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");
    private static final String MARKDOWN_CONTENT = "# Java\n\nJava is a programming language.";

    private DefaultKnowledgeSourceRegistry registry;
    private RecordingIngestionEngine ingestion;
    private DefaultKnowledgeAcquisitionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        registry = new DefaultKnowledgeSourceRegistry();
        ingestion = new RecordingIngestionEngine();
        orchestrator = new DefaultKnowledgeAcquisitionOrchestrator(registry, ingestion);
    }

    // ------------------------------------------------------------------
    // Test infrastructure
    // ------------------------------------------------------------------

    /** Registers a source and activates it; returns the deterministic id. */
    private String registerActive(KnowledgeSourceType type, String name,
                                  String location, Map<String, String> metadata) {
        KnowledgeSource source = registry.register(type, name, location, "test source", metadata);
        registry.activate(source.sourceId());
        return source.sourceId();
    }

    private static AcquisitionDecisionPlan plan(AcquisitionDecisionTarget... targets) {
        List<AcquisitionDecisionTarget> targetList = List.of(targets);
        List<FreshnessReason> reasons = new ArrayList<>();
        for (AcquisitionDecisionTarget target : targetList) {
            reasons.add(new FreshnessReason(target.sourceId(), target.decision(), "test reason"));
        }
        return new AcquisitionDecisionPlan(targetList, reasons);
    }

    private static AcquisitionDecisionTarget target(String topicId, String topicName,
                                                    String sourceId, AcquisitionDecision decision) {
        return new AcquisitionDecisionTarget(topicId, topicName, sourceId, decision);
    }

    private static KnowledgeDocument doc(String documentId, String sourceId, Instant ingestedAt) {
        return new KnowledgeDocument(documentId, sourceId, "Cached Title",
                ParserType.TEXT, List.of(), ingestedAt);
    }

    /** Deterministic ingestion engine double that records every call. */
    private static final class RecordingIngestionEngine implements DocumentIngestionEngine {
        final List<String> sourceIds = new ArrayList<>();
        final List<String> titles = new ArrayList<>();
        final List<ParserType> parserTypes = new ArrayList<>();
        final List<String> contents = new ArrayList<>();
        final Set<String> failingSourceIds = new HashSet<>();

        int count() {
            return sourceIds.size();
        }

        @Override
        public IngestionResult ingest(String sourceId, String title,
                                      ParserType parserType, String content) {
            sourceIds.add(sourceId);
            titles.add(title);
            parserTypes.add(parserType);
            contents.add(content);
            if (failingSourceIds.contains(sourceId)) {
                throw new IllegalStateException("boom: " + sourceId);
            }
            KnowledgeDocument document = new KnowledgeDocument(
                    "doc-" + sourceId, sourceId, title, parserType, List.of(), BASE);
            return IngestionResult.of(document, 1, content.length(), Duration.ZERO);
        }
    }

    // ------------------------------------------------------------------
    // USE_CACHE - reuse, never ingest
    // ------------------------------------------------------------------

    @Nested
    class UseCacheExecution {

        @Test
        void useCacheSkipsIngestion() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument cached = doc("doc-cached-1", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(cached), Map.of("ignored", "content"));

            assertEquals(0, ingestion.count(), "USE_CACHE must never ingest");
            assertEquals(1, result.records().size());
        }

        @Test
        void useCacheRecordIsSkipped() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(doc("doc-cached-1", sourceId, BASE)), Map.of());

            assertEquals(AcquisitionStatus.SKIPPED, result.records().get(0).status());
        }

        @Test
        void useCacheRecordCarriesCachedDocumentId() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(doc("doc-cached-1", sourceId, BASE)), Map.of());

            assertEquals("doc-cached-1", result.records().get(0).documentId());
        }

        @Test
        void useCacheReusedDocumentIsInResult() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument cached = doc("doc-cached-1", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(cached), Map.of());

            assertEquals(List.of(cached), result.documents());
        }

        @Test
        void useCachePrefersMostRecentlyIngested() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument older = doc("doc-old", sourceId, BASE);
            KnowledgeDocument newer = doc("doc-new", sourceId, BASE.plusSeconds(3600));
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(older, newer), Map.of());

            assertEquals("doc-new", result.records().get(0).documentId());
            assertEquals(List.of(newer), result.documents());
        }

        @Test
        void useCacheTieBreaksBySmallestDocumentId() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument laterId = doc("doc-zzz", sourceId, BASE);
            KnowledgeDocument earlierId = doc("doc-aaa", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(laterId, earlierId), Map.of());

            assertEquals("doc-aaa", result.records().get(0).documentId());
        }

        @Test
        void useCacheWithoutCachedDocumentFails() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(), Map.of());

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertTrue(result.documents().isEmpty());
        }

        @Test
        void useCacheFailureRecordHasNullDocumentId() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(), Map.of());

            assertNull(result.records().get(0).documentId());
        }

        @Test
        void useCacheDoesNotRequireRawContent() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(doc("doc-cached-1", sourceId, BASE)), null);

            assertEquals(AcquisitionStatus.SKIPPED, result.records().get(0).status());
        }

        @Test
        void useCacheNeverMutatesSuppliedCache() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            List<KnowledgeDocument> cache = List.of(doc("doc-cached-1", sourceId, BASE));
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.USE_CACHE)),
                    cache, Map.of());

            assertEquals(1, cache.size());
            assertEquals(cache.get(0), result.documents().get(0));
        }
    }

    // ------------------------------------------------------------------
    // ACQUIRE - first-time ingestion
    // ------------------------------------------------------------------

    @Nested
    class AcquireExecution {

        @Test
        void acquireIngestsExactlyOnce() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(1, ingestion.count(), "ACQUIRE must ingest exactly once");
        }

        @Test
        void acquireRecordIsAcquired() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.ACQUIRED, result.records().get(0).status());
        }

        @Test
        void acquireRecordDocumentIdMatchesIngestedDocument() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals("doc-" + sourceId, result.records().get(0).documentId());
        }

        @Test
        void acquireDocumentIsInResult() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(1, result.documents().size());
            assertEquals(sourceId, result.documents().get(0).sourceId());
        }

        @Test
        void acquirePassesSourceNameAsTitle() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals("Java Guide", ingestion.titles.get(0));
        }

        @Test
        void acquirePassesSuppliedRawContent() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(MARKDOWN_CONTENT, ingestion.contents.get(0));
        }

        @Test
        void acquireResolvesMarkdownParserFromLocationExtension() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(ParserType.MARKDOWN, ingestion.parserTypes.get(0));
        }

        @Test
        void acquireResolvesParserTypeFromMetadataDeclaration() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide",
                    "java.md", Map.of("parserType", "TEXT"));
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(ParserType.TEXT, ingestion.parserTypes.get(0));
        }

        @Test
        void acquireResolvesTextParserForApiSource() {
            String sourceId = registerActive(KnowledgeSourceType.API, "Internal REST",
                    "https://internal/api", Map.of());
            orchestrator.execute(
                    plan(target("t1", "API Knowledge", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "plain content"));

            assertEquals(ParserType.TEXT, ingestion.parserTypes.get(0));
        }

        @Test
        void acquireResolvesHtmlParserFromHtmlExtension() {
            String sourceId = registerActive(KnowledgeSourceType.WEB, "HTML Notes", "page.html", Map.of());
            orchestrator.execute(
                    plan(target("t1", "HTML", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "<html><body><p>Hi</p></body></html>"));

            assertEquals(ParserType.HTML, ingestion.parserTypes.get(0));
        }

        @Test
        void acquireWithoutRawContentFails() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of());

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(0, ingestion.count());
        }

        @Test
        void acquireWithBlankRawContentFails() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "   "));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
        }

        @Test
        void acquireWithUnregisteredSourceFails() {
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", "unknown-source", AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of("unknown-source", MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(0, ingestion.count());
        }

        @Test
        void acquireIngestionFailureIsIsolated() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            ingestion.failingSourceIds.add(sourceId);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertTrue(result.documents().isEmpty());
        }

        @Test
        void acquireIgnoresCacheEvenWhenPresent() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument cached = doc("doc-cached-1", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(cached), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(1, ingestion.count());
            assertEquals("doc-" + sourceId, result.documents().get(0).documentId());
            assertFalse(result.documents().contains(cached));
        }
    }

    // ------------------------------------------------------------------
    // REFRESH - re-ingest and replace
    // ------------------------------------------------------------------

    @Nested
    class RefreshExecution {

        @Test
        void refreshReingestsSourceOnce() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(doc("doc-old", sourceId, BASE)),
                    Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(1, ingestion.count());
        }

        @Test
        void refreshRecordIsAcquired() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(doc("doc-old", sourceId, BASE)),
                    Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.ACQUIRED, result.records().get(0).status());
        }

        @Test
        void refreshReplacesCachedDocumentInResult() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument oldDocument = doc("doc-old", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(oldDocument),
                    Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals("doc-" + sourceId, result.documents().get(0).documentId());
            assertFalse(result.documents().contains(oldDocument),
                    "REFRESH must replace the cached document");
        }

        @Test
        void refreshUsesFreshContent() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            String freshContent = "# Java 21\n\nVirtual threads are here.";
            orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(doc("doc-old", sourceId, BASE)),
                    Map.of(sourceId, freshContent));

            assertEquals(freshContent, ingestion.contents.get(0));
        }

        @Test
        void refreshWithoutRawContentFails() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument oldDocument = doc("doc-old", sourceId, BASE);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(oldDocument), Map.of());

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
        }

        @Test
        void refreshFailurePreservesCachedDocument() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            KnowledgeDocument oldDocument = doc("doc-old", sourceId, BASE);
            ingestion.failingSourceIds.add(sourceId);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(oldDocument),
                    Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(List.of(oldDocument), result.documents(),
                    "A failed REFRESH must keep the cached document");
        }

        @Test
        void refreshFailureRecordHasNullDocumentId() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            ingestion.failingSourceIds.add(sourceId);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH)),
                    List.of(doc("doc-old", sourceId, BASE)),
                    Map.of(sourceId, MARKDOWN_CONTENT));

            assertNull(result.records().get(0).documentId());
        }

        @Test
        void refreshWithUnregisteredSourceFails() {
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Java", "ghost-source", AcquisitionDecision.REFRESH)),
                    List.of(doc("doc-old", "ghost-source", BASE)),
                    Map.of("ghost-source", MARKDOWN_CONTENT));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
        }
    }

    // ------------------------------------------------------------------
    // Failure isolation
    // ------------------------------------------------------------------

    @Nested
    class FailureIsolation {

        @Test
        void failingSourceDoesNotStopRemainingTargets() {
            String failing = registerActive(KnowledgeSourceType.MARKDOWN, "Broken Guide", "broken.md", Map.of());
            String working = registerActive(KnowledgeSourceType.TEXT, "Notes", "notes.txt", Map.of());
            ingestion.failingSourceIds.add(failing);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Broken", failing, AcquisitionDecision.ACQUIRE),
                         target("t2", "Working", working, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(failing, "content", working, "content"));

            assertEquals(2, result.size(), "All targets must be executed");
            assertEquals(2, ingestion.count(),
                    "Both targets must be attempted - the failure must not stop the pipeline");
        }

        @Test
        void failedRecordIsFailedOthersAcquired() {
            String failing = registerActive(KnowledgeSourceType.MARKDOWN, "Broken Guide", "broken.md", Map.of());
            String working = registerActive(KnowledgeSourceType.TEXT, "Notes", "notes.txt", Map.of());
            ingestion.failingSourceIds.add(failing);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Broken", failing, AcquisitionDecision.ACQUIRE),
                         target("t2", "Working", working, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(failing, "content", working, "content"));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(AcquisitionStatus.ACQUIRED, result.records().get(1).status());
        }

        @Test
        void failedRecordDoesNotContributeDocument() {
            String failing = registerActive(KnowledgeSourceType.MARKDOWN, "Broken Guide", "broken.md", Map.of());
            String working = registerActive(KnowledgeSourceType.TEXT, "Notes", "notes.txt", Map.of());
            ingestion.failingSourceIds.add(failing);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Broken", failing, AcquisitionDecision.ACQUIRE),
                         target("t2", "Working", working, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(failing, "content", working, "content"));

            assertEquals(1, result.documents().size());
            assertEquals(working, result.documents().get(0).sourceId());
        }

        @Test
        void ingestionExceptionNeverEscapes() {
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Broken Guide", "broken.md", Map.of());
            ingestion.failingSourceIds.add(sourceId);

            AcquisitionResult result = assertDoesNotThrow(() -> orchestrator.execute(
                    plan(target("t1", "Broken", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT)));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
        }

        @Test
        void allTargetsFailingStillReturnsResult() {
            String first = registerActive(KnowledgeSourceType.MARKDOWN, "Broken A", "a.md", Map.of());
            String second = registerActive(KnowledgeSourceType.MARKDOWN, "Broken B", "b.md", Map.of());
            ingestion.failingSourceIds.add(first);
            ingestion.failingSourceIds.add(second);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "A", first, AcquisitionDecision.ACQUIRE),
                         target("t2", "B", second, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(first, "content", second, "content"));

            assertEquals(2, result.size());
            assertTrue(result.documents().isEmpty());
            assertEquals(2, result.failedCount());
        }

        @Test
        void failedRefreshAndSuccessfulAcquireCoexist() {
            String refreshSource = registerActive(KnowledgeSourceType.MARKDOWN, "Stale Guide", "stale.md", Map.of());
            String acquireSource = registerActive(KnowledgeSourceType.TEXT, "Fresh Notes", "fresh.txt", Map.of());
            ingestion.failingSourceIds.add(refreshSource);
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Stale", refreshSource, AcquisitionDecision.REFRESH),
                         target("t2", "Fresh", acquireSource, AcquisitionDecision.ACQUIRE)),
                    List.of(doc("doc-old", refreshSource, BASE)),
                    Map.of(refreshSource, "content", acquireSource, "content"));

            assertEquals(2, result.size());
            assertEquals(1, result.failedCount());
            assertEquals(1, result.acquiredCount());
            assertEquals(2, result.documents().size(),
                    "Failed refresh keeps cache, successful acquire adds its document");
        }
    }

    // ------------------------------------------------------------------
    // Stable ordering and determinism
    // ------------------------------------------------------------------

    @Nested
    class StableOrdering {

        @Test
        void recordsOrderedAcquireRefreshUseCache() {
            String acquireSource = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            String refreshSource = registerActive(KnowledgeSourceType.TEXT, "B", "b.txt", Map.of());
            String cacheSource = registerActive(KnowledgeSourceType.TEXT, "C", "c.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "C Topic", cacheSource, AcquisitionDecision.USE_CACHE),
                         target("t2", "B Topic", refreshSource, AcquisitionDecision.REFRESH),
                         target("t3", "A Topic", acquireSource, AcquisitionDecision.ACQUIRE)),
                    List.of(doc("doc-c", cacheSource, BASE)),
                    Map.of(acquireSource, "content", refreshSource, "content"));

            assertEquals(AcquisitionDecision.ACQUIRE, result.records().get(0).decision());
            assertEquals(AcquisitionDecision.REFRESH, result.records().get(1).decision());
            assertEquals(AcquisitionDecision.USE_CACHE, result.records().get(2).decision());
        }

        @Test
        void recordsOrderedByTopicIdWithinSameDecision() {
            String first = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            String second = registerActive(KnowledgeSourceType.TEXT, "B", "b.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t-zeta", "Zeta", first, AcquisitionDecision.ACQUIRE),
                         target("t-alpha", "Alpha", second, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(first, "content", second, "content"));

            assertEquals("t-alpha", result.records().get(0).topicId());
            assertEquals("t-zeta", result.records().get(1).topicId());
        }

        @Test
        void recordsOrderedBySourceIdWithinSameTopic() {
            String sourceA = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            String sourceB = registerActive(KnowledgeSourceType.TEXT, "B", "b.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Same Topic", sourceB, AcquisitionDecision.ACQUIRE),
                         target("t1", "Same Topic", sourceA, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceA, "content", sourceB, "content"));

            assertEquals(sourceA, result.records().get(0).sourceId());
            assertEquals(sourceB, result.records().get(1).sourceId());
        }

        @Test
        void documentsOrderedBySourceIdThenDocumentId() {
            String sourceB = registerActive(KnowledgeSourceType.TEXT, "B", "b.txt", Map.of());
            String sourceA = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "B Topic", sourceB, AcquisitionDecision.ACQUIRE),
                         target("t2", "A Topic", sourceA, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceA, "content", sourceB, "content"));

            assertEquals(sourceA, result.documents().get(0).sourceId());
            assertEquals(sourceB, result.documents().get(1).sourceId());
        }

        @Test
        void recordOrderIndependentOfPlanOrder() {
            String source = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionDecisionTarget acquire =
                    target("t1", "Topic", source, AcquisitionDecision.ACQUIRE);
            AcquisitionDecisionTarget cache =
                    target("t2", "Cached", source, AcquisitionDecision.USE_CACHE);

            AcquisitionResult forward = orchestrator.execute(plan(acquire, cache),
                    List.of(doc("doc-1", source, BASE)), Map.of(source, "content"));
            AcquisitionResult reversed = orchestrator.execute(plan(cache, acquire),
                    List.of(doc("doc-1", source, BASE)), Map.of(source, "content"));

            assertEquals(forward.records(), reversed.records(),
                    "Stable ordering must be independent of plan order");
        }

        @Test
        void executeIsDeterministicAcrossRuns() {
            String acquireSource = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            String cacheSource = registerActive(KnowledgeSourceType.TEXT, "B", "b.txt", Map.of());
            AcquisitionDecisionPlan decisionPlan = plan(
                    target("t1", "A Topic", acquireSource, AcquisitionDecision.ACQUIRE),
                    target("t2", "B Topic", cacheSource, AcquisitionDecision.USE_CACHE));
            List<KnowledgeDocument> cache = List.of(doc("doc-1", cacheSource, BASE));
            Map<String, String> content = Map.of(acquireSource, "content");

            AcquisitionResult first = orchestrator.execute(decisionPlan, cache, content);
            AcquisitionResult second = orchestrator.execute(decisionPlan, cache, content);

            assertEquals(first, second, "Identical inputs must produce identical results");
        }

        @Test
        void emptyPlanProducesEmptyResult() {
            AcquisitionResult result = orchestrator.execute(AcquisitionDecisionPlan.empty());

            assertEquals(AcquisitionResult.empty(), result);
            assertTrue(result.documents().isEmpty());
            assertTrue(result.records().isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // Model and contract guarantees
    // ------------------------------------------------------------------

    @Nested
    class ContractGuarantees {

        @Test
        void nullPlanThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> orchestrator.execute(null, List.of(), Map.of()));
        }

        @Test
        void nullPlanThrowsForSingleArgExecute() {
            assertThrows(NullPointerException.class, () -> orchestrator.execute(null));
        }

        @Test
        void nullCacheAndContentTreatedAsEmpty() {
            String sourceId = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Topic", sourceId, AcquisitionDecision.ACQUIRE)),
                    null, null);

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(0, ingestion.count());
        }

        @Test
        void resultListsAreImmutable() {
            String sourceId = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Topic", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "content"));

            assertThrows(UnsupportedOperationException.class,
                    () -> result.documents().add(doc("x", sourceId, BASE)));
            assertThrows(UnsupportedOperationException.class,
                    () -> result.records().add(result.records().get(0)));
        }

        @Test
        void singleArgExecuteUsesEmptySupply() {
            String sourceId = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "Topic", sourceId, AcquisitionDecision.ACQUIRE)));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status());
            assertEquals(0, ingestion.count());
        }

        @Test
        void acquisitionStatusHasLockedValues() {
            assertEquals(4, AcquisitionStatus.values().length);
            assertEquals("PENDING", AcquisitionStatus.values()[0].name());
            assertEquals("ACQUIRED", AcquisitionStatus.values()[1].name());
            assertEquals("SKIPPED", AcquisitionStatus.values()[2].name());
            assertEquals("FAILED", AcquisitionStatus.values()[3].name());
        }

        @Test
        void acquisitionRecordAcceptsNullDocumentId() {
            AcquisitionRecord record = new AcquisitionRecord(
                    "t1", "s1", AcquisitionDecision.ACQUIRE, AcquisitionStatus.FAILED, null);
            assertNull(record.documentId());
        }

        @Test
        void acquisitionRecordRejectsNullStatus() {
            assertThrows(NullPointerException.class,
                    () -> new AcquisitionRecord("t1", "s1", AcquisitionDecision.ACQUIRE, null, "doc"));
        }

        @Test
        void acquisitionRecordRejectsBlankTopicId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AcquisitionRecord(" ", "s1", AcquisitionDecision.ACQUIRE,
                            AcquisitionStatus.ACQUIRED, "doc"));
        }

        @Test
        void acquisitionResultEmptyHasNoArtifacts() {
            AcquisitionResult empty = AcquisitionResult.empty();
            assertEquals(0, empty.size());
            assertEquals(0, empty.acquiredCount());
            assertEquals(0, empty.skippedCount());
            assertEquals(0, empty.failedCount());
        }

        @Test
        void acquisitionResultCountsReflectRecords() {
            String sourceId = registerActive(KnowledgeSourceType.TEXT, "A", "a.txt", Map.of());
            AcquisitionResult result = orchestrator.execute(
                    plan(target("t1", "A", sourceId, AcquisitionDecision.ACQUIRE),
                         target("t2", "B", sourceId, AcquisitionDecision.USE_CACHE)),
                    List.of(doc("doc-1", sourceId, BASE)),
                    Map.of(sourceId, "content"));

            assertEquals(2, result.size());
            assertEquals(1, result.acquiredCount());
            assertEquals(1, result.skippedCount());
            assertEquals(0, result.failedCount());
        }
    }

    // ------------------------------------------------------------------
    // Canonical ingestion through the real K2 engine
    // ------------------------------------------------------------------

    @Nested
    class RealIngestionExecution {

        @Test
        void acquireProducesCanonicalChunksFromMarkdown() {
            DefaultDocumentIngestionEngine realEngine = new DefaultDocumentIngestionEngine(registry);
            DefaultKnowledgeAcquisitionOrchestrator realOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(registry, realEngine);
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());

            AcquisitionResult result = realOrchestrator.execute(
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, MARKDOWN_CONTENT));

            assertEquals(1, result.documents().size());
            assertTrue(result.documents().get(0).chunkCount() >= 1);
            assertEquals(ParserType.MARKDOWN, result.documents().get(0).parserType());
        }

        @Test
        void acquireDocumentIdIsDeterministic() {
            DefaultDocumentIngestionEngine realEngine = new DefaultDocumentIngestionEngine(registry);
            DefaultKnowledgeAcquisitionOrchestrator realOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(registry, realEngine);
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionDecisionPlan decisionPlan =
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.ACQUIRE));
            Map<String, String> content = Map.of(sourceId, MARKDOWN_CONTENT);

            KnowledgeDocument first = realOrchestrator.execute(decisionPlan, List.of(), content)
                    .documents().get(0);
            KnowledgeDocument second = realOrchestrator.execute(decisionPlan, List.of(), content)
                    .documents().get(0);

            assertEquals(first.documentId(), second.documentId(),
                    "Document identity is derived, never random");
        }

        @Test
        void refreshProducesSameIdentityForSameContent() {
            DefaultDocumentIngestionEngine realEngine = new DefaultDocumentIngestionEngine(registry);
            DefaultKnowledgeAcquisitionOrchestrator realOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(registry, realEngine);
            String sourceId = registerActive(KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", Map.of());
            AcquisitionDecisionPlan decisionPlan =
                    plan(target("t1", "Java", sourceId, AcquisitionDecision.REFRESH));
            Map<String, String> content = Map.of(sourceId, MARKDOWN_CONTENT);

            KnowledgeDocument refreshed = realOrchestrator.execute(decisionPlan, List.of(), content)
                    .documents().get(0);

            assertEquals(realEngine.documentIdFor(sourceId, "Java Guide", ParserType.MARKDOWN),
                    refreshed.documentId());
        }

        @Test
        void acquireRejectsPdfContentLoudlyAsFailedRecord() {
            DefaultDocumentIngestionEngine realEngine = new DefaultDocumentIngestionEngine(registry);
            DefaultKnowledgeAcquisitionOrchestrator realOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(registry, realEngine);
            String sourceId = registerActive(KnowledgeSourceType.PDF, "Manual", "manual.pdf", Map.of());

            AcquisitionResult result = realOrchestrator.execute(
                    plan(target("t1", "Manual", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "%PDF-not-really"));

            assertEquals(AcquisitionStatus.FAILED, result.records().get(0).status(),
                    "PDF ingestion is a stub until PDFBox integration - it must fail, not crash");
            assertTrue(result.documents().isEmpty());
        }

        @Test
        void acquireWithRealEngineKeepsRecordAndDocumentAligned() {
            DefaultDocumentIngestionEngine realEngine = new DefaultDocumentIngestionEngine(registry);
            DefaultKnowledgeAcquisitionOrchestrator realOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(registry, realEngine);
            String sourceId = registerActive(KnowledgeSourceType.TEXT, "Notes", "notes.txt", Map.of());

            AcquisitionResult result = realOrchestrator.execute(
                    plan(target("t1", "Notes", sourceId, AcquisitionDecision.ACQUIRE)),
                    List.of(), Map.of(sourceId, "Line one.\n\nLine two."));

            assertEquals(result.documents().get(0).documentId(),
                    result.records().get(0).documentId());
        }
    }
}
