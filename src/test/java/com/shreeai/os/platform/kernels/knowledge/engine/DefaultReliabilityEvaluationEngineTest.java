package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.FreshnessLevel;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;
import com.shreeai.os.platform.kernels.knowledge.model.SourceAuthority;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultReliabilityEvaluationEngine}
 * and the K5 trust models.
 */
public class DefaultReliabilityEvaluationEngineTest {

    private static final Instant NOW = Instant.parse("2026-01-10T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private DefaultKnowledgeSourceRegistry registry;
    private ConceptGraph graph;
    private List<KnowledgeDocument> corpus;
    private DefaultReliabilityEvaluationEngine engine;

    private KnowledgeDocument officialDoc;
    private KnowledgeDocument oldOfficialDoc;
    private KnowledgeDocument communityDoc;
    private KnowledgeDocument untaggedDoc;

    @BeforeEach
    void setUp() {
        registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource official = registry.register(KnowledgeSourceType.MARKDOWN,
                "Official Java Docs", "https://docs.java", null,
                Map.of("authority", "official"));
        KnowledgeSource community = registry.register(KnowledgeSourceType.TEXT,
                "Community Wiki", "wiki.example", null,
                Map.of("authority", "community"));
        registry.register(KnowledgeSourceType.TEXT,
                "Untagged Notes", "notes.example", null, Map.of());

        officialDoc = document("doc-official", official.sourceId(), "Java Guide",
                NOW.minus(Duration.ofDays(1)),
                chunk("doc-official", 0, "Intro", "Java collections overview."),
                chunk("doc-official", 1, "Advanced", "Advanced java collections usage."));
        oldOfficialDoc = document("doc-official-old", official.sourceId(), "Old Guide",
                NOW.minus(Duration.ofDays(400)),
                chunk("doc-official-old", 0, "Legacy", "Legacy java material."));
        communityDoc = document("doc-community", community.sourceId(), "Wiki Notes",
                NOW.minus(Duration.ofDays(400)),
                chunk("doc-community", 0, "Notes", "Random community remarks."));
        untaggedDoc = document("doc-untagged", findSourceId("Untagged Notes"), "Fresh Notes",
                NOW.minus(Duration.ofDays(10)),
                chunk("doc-untagged", 0, "Fresh", "Fresh notes."));

        corpus = List.of(officialDoc, oldOfficialDoc, communityDoc, untaggedDoc);

        GraphConcept java = new GraphConcept(
                DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.LANGUAGE, "Java"),
                "Java", ConceptType.LANGUAGE);
        GraphConcept collections = new GraphConcept(
                DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.LIBRARY, "Collections"),
                "Collections", ConceptType.LIBRARY);
        graph = new ConceptGraph(
                List.of(java, collections),
                List.of(new ConceptRelationship(
                        DefaultKnowledgeGraphBuilder.relationshipIdFor(
                                java.conceptId(), RelationshipType.RELATED_TO, collections.conceptId()),
                        java.conceptId(), collections.conceptId(),
                        RelationshipType.RELATED_TO, 1.00)));

        engine = new DefaultReliabilityEvaluationEngine(registry, corpus, graph, CLOCK);
    }

    private String findSourceId(String name) {
        return registry.snapshot().sources().stream()
                .filter(source -> source.name().equals(name))
                .findFirst()
                .orElseThrow()
                .sourceId();
    }

    private static KnowledgeDocument document(String docId, String sourceId, String title,
                                              Instant ingestedAt,
                                              KnowledgeDocumentChunk... chunks) {
        return new KnowledgeDocument(docId, sourceId, title, ParserType.MARKDOWN,
                List.of(chunks), ingestedAt);
    }

    private static KnowledgeDocumentChunk chunk(String docId, int index,
                                                String section, String content) {
        return new KnowledgeDocumentChunk(docId + "-c" + index, content,
                new ChunkMetadata("Doc Title", section, "", index, docId));
    }

    private static RetrievalResult retrieval(EvidenceItem... items) {
        return new RetrievalResult(List.of(items), items.length, items.length);
    }

    @Test
    @DisplayName("Test 1: Authority labels map deterministically to locked scores")
    void testAuthorityMapping() {
        assertEquals(SourceAuthority.OFFICIAL, SourceAuthority.fromLabel("official"));
        assertEquals(SourceAuthority.ENTERPRISE, SourceAuthority.fromLabel("ENTERPRISE"));
        assertEquals(SourceAuthority.VERIFIED, SourceAuthority.fromLabel("Verified"));
        assertEquals(SourceAuthority.COMMUNITY, SourceAuthority.fromLabel("community"));
        assertEquals(SourceAuthority.UNKNOWN, SourceAuthority.fromLabel("mystery"));
        assertEquals(SourceAuthority.UNKNOWN, SourceAuthority.fromLabel(null));
        assertEquals(1.00, SourceAuthority.OFFICIAL.authorityScore());
        assertEquals(0.95, SourceAuthority.ENTERPRISE.authorityScore());
        assertEquals(0.90, SourceAuthority.VERIFIED.authorityScore());
        assertEquals(0.70, SourceAuthority.COMMUNITY.authorityScore());
        assertEquals(0.40, SourceAuthority.UNKNOWN.authorityScore());
    }

    @Test
    @DisplayName("Test 2: Freshness buckets follow the locked age boundaries")
    void testFreshnessBuckets() {
        assertEquals(FreshnessLevel.LATEST, FreshnessLevel.ofAge(Duration.ofDays(5)));
        assertEquals(FreshnessLevel.LATEST, FreshnessLevel.ofAge(Duration.ofDays(30)));
        assertEquals(FreshnessLevel.CURRENT, FreshnessLevel.ofAge(Duration.ofDays(31)));
        assertEquals(FreshnessLevel.CURRENT, FreshnessLevel.ofAge(Duration.ofDays(180)));
        assertEquals(FreshnessLevel.RECENT, FreshnessLevel.ofAge(Duration.ofDays(181)));
        assertEquals(FreshnessLevel.RECENT, FreshnessLevel.ofAge(Duration.ofDays(365)));
        assertEquals(FreshnessLevel.OUTDATED, FreshnessLevel.ofAge(Duration.ofDays(366)));
        assertEquals(FreshnessLevel.OUTDATED, FreshnessLevel.ofAge(Duration.ofDays(1095)));
        assertEquals(FreshnessLevel.ARCHIVED, FreshnessLevel.ofAge(Duration.ofDays(1096)));
        assertEquals(FreshnessLevel.ARCHIVED, FreshnessLevel.ofAge(Duration.ofDays(2000)));
        assertEquals(FreshnessLevel.LATEST, FreshnessLevel.ofAge(Duration.ofDays(-5)));
        assertEquals(1.00, FreshnessLevel.LATEST.freshnessScore());
        assertEquals(0.90, FreshnessLevel.CURRENT.freshnessScore());
        assertEquals(0.75, FreshnessLevel.RECENT.freshnessScore());
        assertEquals(0.50, FreshnessLevel.OUTDATED.freshnessScore());
        assertEquals(0.20, FreshnessLevel.ARCHIVED.freshnessScore());
    }

    @Test
    @DisplayName("Test 3: The official source outranks the community source")
    void testOfficialSourceWins() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections")),
                new EvidenceItem("doc-community-c0", "doc-community",
                        "Random community remarks.", 0.80, List.of())));

        TrustedEvidence first = result.evidence().get(0);
        assertEquals("doc-official-c0", first.evidence().chunkId());
        assertEquals(1.00, first.trust().overall());
        assertEquals(0.69, result.evidence().get(1).trust().overall());
        assertTrue(first.trust().overall() > result.evidence().get(1).trust().overall());
    }

    @Test
    @DisplayName("Test 4: Fresh documents outrank outdated documents of the same authority")
    void testFreshDocumentOutranksOld() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.50, List.of()),
                new EvidenceItem("doc-official-old-c0", "doc-official-old",
                        "Legacy java material.", 0.50, List.of())));

        TrustedEvidence fresh = result.evidence().get(0);
        assertEquals("doc-official-c0", fresh.evidence().chunkId());
        assertEquals(1.00, fresh.trust().overall());
        assertEquals(FreshnessLevel.LATEST.freshnessScore(), fresh.trust().freshness());
        assertEquals(0.825, result.evidence().get(1).trust().overall());
        assertEquals(FreshnessLevel.OUTDATED.freshnessScore(),
                result.evidence().get(1).trust().freshness());
    }

    @Test
    @DisplayName("Test 5: Full lineage yields provenance 1.0")
    void testProvenanceFull() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections"))));

        assertEquals(1.0, result.evidence().get(0).trust().provenance());
    }

    @Test
    @DisplayName("Test 6: Unknown documents lose three provenance quarters")
    void testProvenanceLoweredForUnknownDocument() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("ghost-chunk", "ghost-doc", "Untraceable text.", 0.90, List.of())));

        TrustScore trust = result.evidence().get(0).trust();
        assertEquals(0.25, trust.provenance());
        assertEquals(0.30, trust.overall());
    }

    @Test
    @DisplayName("Test 7: A missing chunk quarter lowers provenance to 0.75")
    void testProvenancePartialWhenChunkMissing() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-ghost", "doc-official",
                        "Content without a chunk.", 0.60, List.of())));

        TrustScore trust = result.evidence().get(0).trust();
        assertEquals(0.75, trust.provenance());
        assertEquals(0.95, trust.overall());
    }

    @Test
    @DisplayName("Test 8: The locked final trust formula holds exactly")
    void testFinalTrustFormula() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections")),
                new EvidenceItem("doc-community-c0", "doc-community",
                        "Random community remarks.", 0.80, List.of()),
                new EvidenceItem("ghost-chunk", "ghost-doc", "Untraceable text.", 0.90, List.of())));

        assertEquals(1.00, result.evidence().get(0).trust().overall());
        assertEquals(0.69, result.evidence().get(1).trust().overall());
        assertEquals(0.30, result.evidence().get(2).trust().overall());
    }

    @Test
    @DisplayName("Test 9: Trust ties break by relevance score descending")
    void testStableOrderingByRelevance() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c1", "doc-official",
                        "Advanced java collections usage.", 0.40, List.of()),
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.90, List.of())));

        assertEquals("doc-official-c0", result.evidence().get(0).evidence().chunkId());
        assertEquals("doc-official-c1", result.evidence().get(1).evidence().chunkId());
    }

    @Test
    @DisplayName("Test 10: Remaining ties order by chunk index")
    void testStableOrderingByChunkIndex() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c1", "doc-official",
                        "Advanced java collections usage.", 0.50, List.of()),
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.50, List.of())));

        assertEquals(2, result.evidenceCount());
        assertEquals("doc-official-c0", result.evidence().get(0).evidence().chunkId());
        assertEquals("doc-official-c1", result.evidence().get(1).evidence().chunkId());
    }

    @Test
    @DisplayName("Test 11: Same input and clock produce identical output")
    void testSameInputIdenticalOutput() {
        RetrievalResult input = retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections")),
                new EvidenceItem("doc-community-c0", "doc-community",
                        "Random community remarks.", 0.80, List.of()));
        assertEquals(engine.evaluate(input), engine.evaluate(input));
    }

    @Test
    @DisplayName("Test 12: Explanations are deterministic and informative")
    void testExplanationDeterministic() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections"))));

        assertEquals("authority=OFFICIAL(1.00), freshness=LATEST(1.00), "
                        + "provenance=1.00, overall=1.0000",
                result.evidence().get(0).trust().explanation());
    }

    @Test
    @DisplayName("Test 13: Empty retrieval evaluates to an empty trusted result")
    void testEmptyRetrieval() {
        ReliabilityResult result = engine.evaluate(
                new RetrievalResult(List.of(), 0, 0));

        assertEquals(0, result.evidenceCount());
        assertEquals(0.0, result.overallTrust());
    }

    @Test
    @DisplayName("Test 14: Null inputs are rejected with meaningful messages")
    void testNullRejections() {
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> engine.evaluate(null));
        assertTrue(npe.getMessage().contains("retrieval"));
        assertThrows(NullPointerException.class,
                () -> new DefaultReliabilityEvaluationEngine(null, corpus, graph));
        assertThrows(NullPointerException.class,
                () -> new DefaultReliabilityEvaluationEngine(registry, null, graph));
        assertThrows(NullPointerException.class,
                () -> new DefaultReliabilityEvaluationEngine(registry, corpus, null));
    }

    @Test
    @DisplayName("Test 15: Sources without authority metadata default to UNKNOWN")
    void testAuthorityDefaultsToUnknown() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-untagged-c0", "doc-untagged",
                        "Fresh notes.", 0.50, List.of())));

        TrustScore trust = result.evidence().get(0).trust();
        assertEquals(0.40, trust.authority());
        assertEquals(0.73, trust.overall());
    }

    @Test
    @DisplayName("Test 16: Overall trust is the rounded mean of item trusts")
    void testOverallTrustIsMean() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections")),
                new EvidenceItem("doc-community-c0", "doc-community",
                        "Random community remarks.", 0.80, List.of()),
                new EvidenceItem("ghost-chunk", "ghost-doc", "Untraceable text.", 0.90, List.of())));

        assertEquals(0.6633, result.overallTrust());
    }

    @Test
    @DisplayName("Test 17: Trusted evidence validates both components")
    void testTrustedEvidenceValidation() {
        EvidenceItem item = new EvidenceItem("c1", "doc", "content", 0.5, List.of());
        assertThrows(NullPointerException.class, () -> new TrustedEvidence(null, null));
        assertThrows(NullPointerException.class,
                () -> new TrustedEvidence(item, null));
        TrustedEvidence valid = new TrustedEvidence(item, new TrustScore(
                1.0, 1.0, 1.0, 1.0, "ok"));
        assertEquals(item, valid.evidence());
    }

    @Test
    @DisplayName("Test 18: Trust scores validate their ranges")
    void testTrustScoreValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrustScore(1.5, 1.0, 1.0, 1.0, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> new TrustScore(0.5, -0.1, 1.0, 1.0, "x"));
        assertThrows(NullPointerException.class,
                () -> new TrustScore(0.5, 0.5, 0.5, 0.5, null));
        TrustScore valid = new TrustScore(0.69, 0.70, 0.50, 1.0, "authority=COMMUNITY");
        assertEquals("authority=COMMUNITY", valid.explanation());
    }

    @Test
    @DisplayName("Test 19: Reliability results validate their trust range and stay immutable")
    void testReliabilityResultValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReliabilityResult(List.of(), 1.5));
        assertThrows(IllegalArgumentException.class,
                () -> new ReliabilityResult(List.of(), -0.1));
        ReliabilityResult result = new ReliabilityResult(List.of(), 0.0);
        assertThrows(UnsupportedOperationException.class,
                () -> result.evidence().add(null));
    }

    @Test
    @DisplayName("Test 20: The locked weights and metadata key stay exact")
    void testWeightsLocked() {
        assertEquals(0.45, DefaultReliabilityEvaluationEngine.AUTHORITY_WEIGHT);
        assertEquals(0.35, DefaultReliabilityEvaluationEngine.FRESHNESS_WEIGHT);
        assertEquals(0.20, DefaultReliabilityEvaluationEngine.PROVENANCE_WEIGHT);
        assertEquals("authority", DefaultReliabilityEvaluationEngine.AUTHORITY_METADATA_KEY);
    }

    @Test
    @DisplayName("Test 21: The trusted evidence list is deeply immutable")
    void testResultImmutability() {
        ReliabilityResult result = engine.evaluate(retrieval(
                new EvidenceItem("doc-official-c0", "doc-official",
                        "Java collections overview.", 0.80, List.of("Java", "Collections"))));

        assertThrows(UnsupportedOperationException.class,
                () -> result.evidence().add(null));
    }

    @Test
    @DisplayName("Test 22: End-to-end K1 to K5 pipeline annotates ranked evidence with trust")
    void testIntegrationPipeline() {
        DefaultKnowledgeSourceRegistry liveRegistry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = liveRegistry.register(KnowledgeSourceType.MARKDOWN,
                "Official Java Docs", "https://docs.java", null,
                Map.of("authority", "official"));
        DefaultDocumentIngestionEngine ingestion =
                new DefaultDocumentIngestionEngine(liveRegistry);
        String markdown = "# Java Collections\n\n"
                + "Java provides the collections framework for storing objects.\n\n"
                + "## List\n\n"
                + "The List interface belongs to collections, and streams extend collections.\n";
        IngestionResult ingested = ingestion.ingest(
                source.sourceId(), "Java Collections Guide", ParserType.MARKDOWN, markdown);
        ConceptGraph built = new DefaultKnowledgeGraphBuilder().build(ingested.document());
        DefaultKnowledgeRetrievalEngine retrieval = new DefaultKnowledgeRetrievalEngine(
                List.of(ingested.document()), built);
        RetrievalResult ranked = retrieval.retrieve(
                new com.shreeai.os.platform.kernels.knowledge.model.RetrievalQuery(
                        com.shreeai.os.platform.kernels.context.model.IntentProfile.unknown("test"),
                        com.shreeai.os.platform.kernels.context.model.DomainProfile.unknown("test"),
                        com.shreeai.os.platform.kernels.context.model.GoalStructure.none("test"),
                        com.shreeai.os.platform.kernels.context.model.UserConstraints.empty(),
                        List.of("collections")));

        DefaultReliabilityEvaluationEngine liveEngine = new DefaultReliabilityEvaluationEngine(
                liveRegistry, List.of(ingested.document()), built);
        ReliabilityResult trusted = liveEngine.evaluate(ranked);

        assertEquals(ranked.matchedChunks(), trusted.evidenceCount());
        assertTrue(trusted.overallTrust() > 0.0);
        for (TrustedEvidence entry : trusted.evidence()) {
            assertEquals(1.0, entry.trust().provenance());
            assertTrue(entry.trust().overall() > 0.0);
            assertEquals(entry.evidence().content(),
                    ingested.document().chunks().stream()
                            .filter(c -> c.chunkId().equals(entry.evidence().chunkId()))
                            .findFirst().orElseThrow().content());
        }
        assertEquals(liveEngine.evaluate(ranked), trusted);
    }
}
