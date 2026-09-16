package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecision;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionRecord;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionStatus;
import com.shreeai.os.platform.kernels.knowledge.engine.DocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultKnowledgeAcquisitionOrchestrator</b>
 *
 * <p>The K0.6.5 implementation of {@link KnowledgeAcquisitionOrchestrator}.
 * Performs a five-stage, fully deterministic execution of the immutable
 * {@link AcquisitionDecisionPlan} produced by the K0.6.4 Freshness &amp; Cache
 * Policy Engine:</p>
 *
 * <ol>
 *   <li><b>Read Decision Plan</b> - the plan is the single input; the prompt is
 *       never inspected again and no decision is ever re-made here.</li>
 *   <li><b>Execute Target</b> - every target is executed with the locked
 *       decision semantics: {@code USE_CACHE} reuses the existing document
 *       ({@code SKIPPED}), {@code ACQUIRE} ingests once ({@code ACQUIRED}),
 *       {@code REFRESH} re-ingests the same source and replaces the cached
 *       document ({@code ACQUIRED}). No other behavior exists.</li>
 *   <li><b>Failure Isolation</b> - a failing source is recorded as
 *       {@code FAILED} and never stops the pipeline; every remaining target is
 *       still executed.</li>
 *   <li><b>Stable Ordering</b> - records are ordered by decision priority
 *       (ACQUIRE, REFRESH, USE_CACHE), then topic name, then source id;
 *       documents are ordered by source id, then document id.</li>
 *   <li><b>Build Result</b> - one immutable {@link AcquisitionResult}; no
 *       metadata, no timestamps on records, no mutable state escapes.</li>
 * </ol>
 *
 * <p><b>Determinism:</b> the orchestrator is stateless and performs no network
 * calls and no LLM invocations. Raw content and cached documents are supplied
 * read-only per execution; both inputs are defensively copied and never
 * mutated. Document identity is inherited from the deterministic K2 ingestion
 * engine ({@code SHA-256(sourceId + title + parserType)}).</p>
 *
 * <p><b>Locked parser resolution</b> (for {@code ACQUIRE}/{@code REFRESH}
 * ingestions), evaluated in this order:</p>
 * <ol>
 *   <li>the source {@value #PARSER_TYPE_METADATA_KEY} metadata declaration,</li>
 *   <li>the source location file extension ({@code .md}, {@code .txt},
 *       {@code .html}, {@code .htm}, {@code .pdf}),</li>
 *   <li>the locked {@link KnowledgeSourceType} family mapping,</li>
 *   <li>{@link ParserType#TEXT} as the deterministic fallback.</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.5 Acquisition Orchestrator</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultKnowledgeAcquisitionOrchestrator
        implements KnowledgeAcquisitionOrchestrator {

    /** Source metadata key that explicitly declares the ingestion parser type. */
    public static final String PARSER_TYPE_METADATA_KEY = "parserType";

    private final KnowledgeSourceRegistry registry;
    private final DocumentIngestionEngine ingestionEngine;

    /**
     * Creates a stateless orchestrator bound to the K1 registry that owns the
     * sources and the K2 ingestion engine that produces canonical documents.
     *
     * @param registry        the knowledge source registry (must not be null)
     * @param ingestionEngine the document ingestion engine (must not be null)
     */
    public DefaultKnowledgeAcquisitionOrchestrator(KnowledgeSourceRegistry registry,
                                                   DocumentIngestionEngine ingestionEngine) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.ingestionEngine = Objects.requireNonNull(ingestionEngine,
                "ingestionEngine must not be null");
    }

    @Override
    public AcquisitionResult execute(AcquisitionDecisionPlan plan) {
        return execute(plan, List.of(), Map.of());
    }

    @Override
    public AcquisitionResult execute(AcquisitionDecisionPlan plan,
                                     List<KnowledgeDocument> cachedDocuments,
                                     Map<String, String> rawContentBySourceId) {
        Objects.requireNonNull(plan, "plan must not be null");
        List<KnowledgeDocument> cache = cachedDocuments == null
                ? List.of()
                : List.copyOf(cachedDocuments);
        Map<String, String> contentSupply = rawContentBySourceId == null
                ? Map.of()
                : Map.copyOf(rawContentBySourceId);

        // Stage 1 + 2 + 3: read the plan, execute every target with failure
        // isolation. Execution follows the canonical plan order; the result is
        // stably re-ordered in stage 4.
        Map<String, KnowledgeDocument> cacheBySource = cacheBySource(cache);
        Map<String, KnowledgeDocument> documentsBySource = new LinkedHashMap<>();
        List<AcquisitionRecord> records = new ArrayList<>(plan.size());
        for (AcquisitionDecisionTarget target : plan.targets()) {
            records.add(executeTarget(target, cacheBySource, contentSupply, documentsBySource));
        }

        // Stage 4: stable, deterministic ordering.
        List<AcquisitionRecord> orderedRecords = records.stream()
                .sorted(Comparator
                        .comparingInt((AcquisitionRecord record) -> decisionPriority(record.decision()))
                        .thenComparing(AcquisitionRecord::topicId)
                        .thenComparing(AcquisitionRecord::sourceId))
                .toList();

        // Stage 5: build the immutable canonical result.
        return AcquisitionResult.of(List.copyOf(documentsBySource.values()), orderedRecords);
    }

    /** Executes one decision target under the locked decision semantics. */
    private AcquisitionRecord executeTarget(AcquisitionDecisionTarget target,
                                            Map<String, KnowledgeDocument> cacheBySource,
                                            Map<String, String> contentSupply,
                                            Map<String, KnowledgeDocument> documentsBySource) {
        return switch (target.decision()) {
            case USE_CACHE -> executeCacheReuse(target, cacheBySource, documentsBySource);
            case ACQUIRE, REFRESH -> executeIngestion(target, cacheBySource, contentSupply, documentsBySource);
        };
    }

    /** USE_CACHE: reuse the existing document; never ingest. */
    private AcquisitionRecord executeCacheReuse(AcquisitionDecisionTarget target,
                                                Map<String, KnowledgeDocument> cacheBySource,
                                                Map<String, KnowledgeDocument> documentsBySource) {
        KnowledgeDocument cached = cacheBySource.get(target.sourceId());
        if (cached == null) {
            return failedRecord(target);
        }
        documentsBySource.put(target.sourceId(), cached);
        return new AcquisitionRecord(target.topicId(), target.sourceId(),
                target.decision(), AcquisitionStatus.SKIPPED, cached.documentId());
    }

    /** ACQUIRE / REFRESH: ingest the source; REFRESH replaces the cached document. */
    private AcquisitionRecord executeIngestion(AcquisitionDecisionTarget target,
                                               Map<String, KnowledgeDocument> cacheBySource,
                                               Map<String, String> contentSupply,
                                               Map<String, KnowledgeDocument> documentsBySource) {
        try {
            String rawContent = contentSupply.get(target.sourceId());
            if (rawContent == null || rawContent.isBlank()) {
                return ingestionFailed(target, cacheBySource, documentsBySource);
            }
            KnowledgeSource source = registry.findById(target.sourceId()).orElse(null);
            if (source == null) {
                return ingestionFailed(target, cacheBySource, documentsBySource);
            }
            ParserType parserType = resolveParserType(source);
            IngestionResult result = ingestionEngine.ingest(
                    target.sourceId(), source.name(), parserType, rawContent);
            KnowledgeDocument document = result.document();
            // REFRESH replaces the cached document as the authoritative knowledge.
            documentsBySource.put(target.sourceId(), document);
            return new AcquisitionRecord(target.topicId(), target.sourceId(),
                    target.decision(), AcquisitionStatus.ACQUIRED, document.documentId());
        } catch (RuntimeException e) {
            return ingestionFailed(target, cacheBySource, documentsBySource);
        }
    }

    /**
     * Records an ingestion failure. Failure isolation: only this record fails;
     * the remaining targets continue. A failed REFRESH never destroys the
     * cached document - the existing knowledge remains authoritative.
     */
    private AcquisitionRecord ingestionFailed(AcquisitionDecisionTarget target,
                                              Map<String, KnowledgeDocument> cacheBySource,
                                              Map<String, KnowledgeDocument> documentsBySource) {
        if (target.decision() == AcquisitionDecision.REFRESH) {
            KnowledgeDocument cached = cacheBySource.get(target.sourceId());
            if (cached != null) {
                documentsBySource.putIfAbsent(target.sourceId(), cached);
            }
        }
        return failedRecord(target);
    }

    /** Builds a FAILED record without a document id. */
    private static AcquisitionRecord failedRecord(AcquisitionDecisionTarget target) {
        return new AcquisitionRecord(target.topicId(), target.sourceId(),
                target.decision(), AcquisitionStatus.FAILED, null);
    }

    /**
     * Indexes the cache by source id, deterministically selecting the most
     * recently ingested document per source (ties broken by the smallest
     * document id).
     */
    private static Map<String, KnowledgeDocument> cacheBySource(List<KnowledgeDocument> cache) {
        Map<String, KnowledgeDocument> index = new LinkedHashMap<>();
        for (KnowledgeDocument document : cache) {
            KnowledgeDocument current = index.get(document.sourceId());
            if (current == null || preferCached(document, current)) {
                index.put(document.sourceId(), document);
            }
        }
        return index;
    }

    /** True when {@code candidate} is the better cached document for a source. */
    private static boolean preferCached(KnowledgeDocument candidate, KnowledgeDocument current) {
        int byFreshness = candidate.ingestedAt().compareTo(current.ingestedAt());
        if (byFreshness != 0) {
            return byFreshness > 0;
        }
        return candidate.documentId().compareTo(current.documentId()) < 0;
    }

    /**
     * Locked decision priority for stable record ordering: acquisitions first,
     * refreshes second, cache reuses last.
     */
    private static int decisionPriority(AcquisitionDecision decision) {
        return switch (decision) {
            case ACQUIRE -> 0;
            case REFRESH -> 1;
            case USE_CACHE -> 2;
        };
    }

    /**
     * Resolves the deterministic parser type of a source:
     * explicit metadata declaration, then location extension, then the locked
     * source family mapping, then the TEXT fallback.
     */
    private static ParserType resolveParserType(KnowledgeSource source) {
        String declared = source.metadata().get(PARSER_TYPE_METADATA_KEY);
        if (declared != null && !declared.isBlank()) {
            try {
                return ParserType.valueOf(declared.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Fall through to the location extension.
            }
        }
        ParserType byExtension = parserTypeByExtension(source.location());
        if (byExtension != null) {
            return byExtension;
        }
        return parserTypeByFamily(source.type());
    }

    /** Maps a location file extension to its parser type (null when unknown). */
    private static ParserType parserTypeByExtension(String location) {
        if (location == null) {
            return null;
        }
        String lower = location.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".md")) {
            return ParserType.MARKDOWN;
        }
        if (lower.endsWith(".txt")) {
            return ParserType.TEXT;
        }
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return ParserType.HTML;
        }
        if (lower.endsWith(".pdf")) {
            return ParserType.PDF;
        }
        return null;
    }

    /** Locked source family to parser type mapping. */
    private static ParserType parserTypeByFamily(KnowledgeSourceType type) {
        return switch (type) {
            case PDF -> ParserType.PDF;
            case MARKDOWN -> ParserType.MARKDOWN;
            case TEXT -> ParserType.TEXT;
            case WEB -> ParserType.WEB;
            case JSON, FOLDER, DATABASE, API -> ParserType.TEXT;
        };
    }
}
