package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.FreshnessLevel;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;
import com.shreeai.os.platform.kernels.knowledge.model.SourceAuthority;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultReliabilityEvaluationEngine</b>
 *
 * <p>The default {@link ReliabilityEvaluationEngine}: a stateless,
 * thread-safe, deterministic trust evaluator over the K1 source registry, a
 * K2 document corpus and a K3 concept graph. No ML, no LLM, no embeddings and
 * no web calls - every component score comes from a locked mapping.</p>
 *
 * <p><b>Locked pipeline:</b></p>
 * <ol>
 *   <li><b>Authority:</b> the {@code authority} entry of the registered
 *       source metadata maps to a {@link SourceAuthority} family and its
 *       locked score ({@code OFFICIAL 1.00}, {@code ENTERPRISE 0.95},
 *       {@code VERIFIED 0.90}, {@code COMMUNITY 0.70}, {@code UNKNOWN 0.40}).</li>
 *   <li><b>Freshness:</b> the document ingestion timestamp bucketed against
 *       the evaluation clock into a {@link FreshnessLevel} ({@code LATEST
 *       1.00}, {@code CURRENT 0.90}, {@code RECENT 0.75},
 *       {@code OUTDATED 0.50}, {@code ARCHIVED 0.20}).</li>
 *   <li><b>Provenance:</b> four equal quarters - document known in the
 *       corpus, chunk located in that document, source registered in the
 *       registry, graph lineage resolving every matched concept.</li>
 *   <li><b>Overall:</b> {@code clamp(authority * 0.45 + freshness * 0.35 +
 *       provenance * 0.20)}, rounded to four decimals.</li>
 *   <li><b>Stable ordering:</b> overall trust descending, then relevance
 *       score descending, then document title, then chunk index.</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultReliabilityEvaluationEngine implements ReliabilityEvaluationEngine {

    /** Locked weight of the authority component. */
    public static final double AUTHORITY_WEIGHT = 0.45;

    /** Locked weight of the freshness component. */
    public static final double FRESHNESS_WEIGHT = 0.35;

    /** Locked weight of the provenance component. */
    public static final double PROVENANCE_WEIGHT = 0.20;

    /** Metadata key of the registered source declaring its authority. */
    public static final String AUTHORITY_METADATA_KEY = "authority";

    private final KnowledgeSourceRegistry registry;
    private final Map<String, KnowledgeDocument> documentById;
    private final Map<String, Integer> chunkIndexById;
    private final Map<String, String> documentTitleById;
    private final Set<String> graphConceptNames;
    private final Clock clock;

    /**
     * Creates an evaluation engine over registry, corpus and graph, using the
     * system UTC clock.
     *
     * @param registry the K1 source registry (must not be null)
     * @param corpus   the K2 document corpus (must not be null)
     * @param graph    the K3 concept graph used for lineage checks (must not
     *                 be null; pass an empty graph when unavailable)
     */
    public DefaultReliabilityEvaluationEngine(KnowledgeSourceRegistry registry,
                                              List<KnowledgeDocument> corpus,
                                              ConceptGraph graph) {
        this(registry, corpus, graph, Clock.systemUTC());
    }

    /**
     * Creates an evaluation engine over the corpus without graph lineage
     * checks, using the system UTC clock.
     *
     * @param registry the K1 source registry (must not be null)
     * @param corpus   the K2 document corpus (must not be null)
     */
    public DefaultReliabilityEvaluationEngine(KnowledgeSourceRegistry registry,
                                              List<KnowledgeDocument> corpus) {
        this(registry, corpus, new ConceptGraph(List.of(), List.of()), Clock.systemUTC());
    }

    /**
     * Creates an evaluation engine with an explicit clock, keeping the
     * evaluation fully deterministic for tests.
     *
     * @param registry the K1 source registry (must not be null)
     * @param corpus   the K2 document corpus (must not be null)
     * @param graph    the K3 concept graph (must not be null)
     * @param clock    the evaluation clock (must not be null)
     */
    public DefaultReliabilityEvaluationEngine(KnowledgeSourceRegistry registry,
                                              List<KnowledgeDocument> corpus,
                                              ConceptGraph graph,
                                              Clock clock) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(corpus, "corpus must not be null");
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(clock, "clock must not be null");
        this.registry = registry;
        this.clock = clock;
        Map<String, KnowledgeDocument> docs = new HashMap<>();
        Map<String, Integer> chunkIndexes = new HashMap<>();
        Map<String, String> titles = new HashMap<>();
        for (KnowledgeDocument document : corpus) {
            docs.put(document.documentId(), document);
            titles.put(document.documentId(), document.title());
            for (KnowledgeDocumentChunk chunk : document.chunks()) {
                chunkIndexes.put(chunk.chunkId(), chunk.metadata().chunkIndex());
            }
        }
        this.documentById = Map.copyOf(docs);
        this.chunkIndexById = Map.copyOf(chunkIndexes);
        this.documentTitleById = Map.copyOf(titles);
        Set<String> names = new HashSet<>();
        graph.concepts().forEach(concept -> names.add(concept.name().toLowerCase(Locale.ROOT)));
        this.graphConceptNames = Set.copyOf(names);
    }

    @Override
    public ReliabilityResult evaluate(RetrievalResult retrieval) {
        Objects.requireNonNull(retrieval, "retrieval must not be null");
        List<Annotated> annotated = new ArrayList<>(retrieval.evidenceCount());
        for (EvidenceItem item : retrieval.evidence()) {
            annotated.add(evaluateItem(item));
        }
        annotated.sort(Comparator.comparingDouble((Annotated a) -> a.trust().overall()).reversed()
                .thenComparing(a -> a.evidence().relevanceScore(), Comparator.reverseOrder())
                .thenComparing(a -> a.documentTitle())
                .thenComparingInt(a -> a.chunkIndex()));

        List<TrustedEvidence> trusted = new ArrayList<>(annotated.size());
        for (Annotated entry : annotated) {
            trusted.add(new TrustedEvidence(entry.evidence(), entry.trust()));
        }
        double overallTrust = round4(annotated.stream()
                .mapToDouble(entry -> entry.trust().overall())
                .average()
                .orElse(0.0));
        return new ReliabilityResult(trusted, overallTrust);
    }

    /** Evaluates authority, freshness and provenance for one evidence item. */
    private Annotated evaluateItem(EvidenceItem item) {
        KnowledgeDocument document = documentById.get(item.documentId());
        KnowledgeSource source = document != null
                ? registry.findById(document.sourceId()).orElse(null)
                : null;

        SourceAuthority authority = source == null
                ? SourceAuthority.UNKNOWN
                : SourceAuthority.fromLabel(
                        source.metadata().get(AUTHORITY_METADATA_KEY));

        FreshnessLevel freshness = document == null
                ? FreshnessLevel.ARCHIVED
                : FreshnessLevel.ofAge(
                        Duration.between(document.ingestedAt(), clock.instant()));

        double provenance = provenanceScore(item, document, source);
        double authorityScore = authority.authorityScore();
        double freshnessScore = freshness.freshnessScore();
        double overall = round4(clamp(AUTHORITY_WEIGHT * authorityScore
                + FRESHNESS_WEIGHT * freshnessScore
                + PROVENANCE_WEIGHT * provenance));

        String explanation = String.format(Locale.ROOT,
                "authority=%s(%.2f), freshness=%s(%.2f), provenance=%.2f, overall=%.4f",
                authority, authorityScore, freshness, freshnessScore, provenance, overall);
        return new Annotated(item, new TrustScore(overall, authorityScore, freshnessScore,
                provenance, explanation),
                documentTitleById.getOrDefault(item.documentId(), ""),
                chunkIndexById.getOrDefault(item.chunkId(), Integer.MAX_VALUE));
    }

    /**
     * Four equal provenance quarters: document known in the corpus, chunk
     * located inside that document, source registered in the registry and
     * graph lineage resolving every matched concept.
     */
    private double provenanceScore(EvidenceItem item,
                                   KnowledgeDocument document,
                                   KnowledgeSource source) {
        double provenance = 0.0;
        if (document != null) {
            provenance += 0.25;
        }
        if (document != null && document.chunks().stream()
                .anyMatch(chunk -> chunk.chunkId().equals(item.chunkId()))) {
            provenance += 0.25;
        }
        if (source != null) {
            provenance += 0.25;
        }
        if (resolvesGraphLineage(item.matchedConcepts())) {
            provenance += 0.25;
        }
        return provenance;
    }

    private boolean resolvesGraphLineage(List<String> matchedConcepts) {
        for (String conceptName : matchedConcepts) {
            if (!graphConceptNames.contains(conceptName.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /** Internal annotation carrying tie-break fields for stable ordering. */
    private record Annotated(
            EvidenceItem evidence,
            TrustScore trust,
            String documentTitle,
            int chunkIndex) {
    }
}
