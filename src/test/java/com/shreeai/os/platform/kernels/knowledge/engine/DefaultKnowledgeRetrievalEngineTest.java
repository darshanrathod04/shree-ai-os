package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalEvidence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalQuery;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultKnowledgeRetrievalEngine} and
 * the K4 retrieval models.
 */
public class DefaultKnowledgeRetrievalEngineTest {

    private static final String DOC_JAVA = "doc-java";
    private static final String DOC_SPRING = "doc-spring";

    private KnowledgeDocument javaDoc;
    private KnowledgeDocument springDoc;
    private ConceptGraph graph;
    private DefaultKnowledgeRetrievalEngine engine;

    @BeforeEach
    void setUp() {
        javaDoc = new KnowledgeDocument(DOC_JAVA, "src-java", "Java Collections Guide",
                ParserType.MARKDOWN,
                List.of(
                        chunk(DOC_JAVA, 0, "Collections",
                                "The collections framework organizes objects into lists and sets."),
                        chunk(DOC_JAVA, 1, "Streams",
                                "Streams process elements with functional operations.")),
                Instant.EPOCH);
        springDoc = new KnowledgeDocument(DOC_SPRING, "src-spring", "Spring Boot Deployment",
                ParserType.HTML,
                List.of(chunk(DOC_SPRING, 0, "Bootstrapping",
                        "Spring Boot auto-configuration boots applications quickly.")),
                Instant.EPOCH);

        GraphConcept java = concept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = concept("Collections", ConceptType.LIBRARY);
        GraphConcept streams = concept("Streams", ConceptType.LIBRARY);
        GraphConcept springBoot = concept("Spring Boot", ConceptType.FRAMEWORK);
        GraphConcept list = concept("List", ConceptType.TOPIC);
        graph = new ConceptGraph(
                List.of(java, collections, streams, springBoot, list),
                List.of(
                        relationship(springBoot, RelationshipType.DEPENDS_ON, java),
                        relationship(list, RelationshipType.PART_OF, collections),
                        relationship(collections, RelationshipType.PREREQUISITE, streams),
                        relationship(java, RelationshipType.RELATED_TO, collections)));

        engine = new DefaultKnowledgeRetrievalEngine(List.of(javaDoc, springDoc), graph);
    }

    private static GraphConcept concept(String name, ConceptType type) {
        return new GraphConcept(
                DefaultKnowledgeGraphBuilder.conceptIdFor(type, name), name, type);
    }

    private static ConceptRelationship relationship(GraphConcept from,
                                                    RelationshipType type,
                                                    GraphConcept to) {
        return new ConceptRelationship(
                DefaultKnowledgeGraphBuilder.relationshipIdFor(
                        from.conceptId(), type, to.conceptId()),
                from.conceptId(), to.conceptId(), type, 0.90);
    }

    private static KnowledgeDocumentChunk chunk(String docId, int index,
                                                String section, String content) {
        return new KnowledgeDocumentChunk(docId + "-chunk-" + index, content,
                new ChunkMetadata("Doc Title", section, "", index, docId));
    }

    private static RetrievalQuery query(String... keywords) {
        return new RetrievalQuery(intent(), domain(), goalStructure("Learn the platform"),
                UserConstraints.empty(), List.of(keywords));
    }

    private static IntentProfile intent() {
        return IntentProfile.of(PrimaryIntent.LEARN, 0.9, List.of(), Instant.EPOCH, "test");
    }

    private static DomainProfile domain() {
        return DomainProfile.of(PrimaryDomain.JAVA, 0.9, List.of(), Instant.EPOCH, "test");
    }

    private static GoalStructure goalStructure(String title) {
        GoalNode goal = new GoalNode(title, 1.0, new GoalEvidence("test evidence", 1, 1));
        return GoalStructure.of(goal, List.of(), GoalComplexity.SIMPLE, Instant.EPOCH, "test");
    }

    @Test
    @DisplayName("Test 1: Query is built deterministically from context artifacts")
    void testQueryBuiltFromContext() {
        RetrievalQuery built = RetrievalQuery.fromContext(
                intent(), domain(), goalStructure("Become Java Developer"), UserConstraints.empty());

        assertEquals(List.of("learn", "java", "become", "developer"), built.keywords());
        assertEquals(4, built.keywordCount());
    }

    @Test
    @DisplayName("Test 2: Unknown and general context markers never become keywords")
    void testNoiseMarkersSkipped() {
        IntentProfile unknown = IntentProfile.of(
                PrimaryIntent.UNKNOWN, 0.0, List.of(), Instant.EPOCH, "test");
        DomainProfile general = DomainProfile.of(
                PrimaryDomain.GENERAL, 0.0, List.of(), Instant.EPOCH, "test");
        RetrievalQuery built = RetrievalQuery.fromContext(
                unknown, general, goalStructure("Study streams"), UserConstraints.empty());

        assertEquals(List.of("study", "streams"), built.keywords());
    }

    @Test
    @DisplayName("Test 3: Query keywords are normalized to lowercase and deduplicated")
    void testKeywordNormalization() {
        RetrievalQuery raw = query("JAVA", "Java", "collections");
        assertEquals(List.of("java", "collections"), raw.keywords());
    }

    @Test
    @DisplayName("Test 4: Title matches retrieve chunks with the title signal")
    void testTitleMatchRetrieval() {
        RetrievalResult result = engine.retrieve(query("deployment"));

        assertEquals(1, result.evidenceCount());
        assertEquals(2, result.searchedDocuments());
        EvidenceItem item = result.evidence().get(0);
        assertEquals(DOC_SPRING, item.documentId());
        assertEquals(DefaultKnowledgeRetrievalEngine.TITLE_WEIGHT, item.relevanceScore());
    }

    @Test
    @DisplayName("Test 5: Section matches retrieve chunks with the section signal")
    void testSectionMatchRetrieval() {
        RetrievalResult result = engine.retrieve(query("bootstrapping"));

        assertEquals(1, result.evidenceCount());
        EvidenceItem item = result.evidence().get(0);
        assertEquals(DOC_SPRING, item.documentId());
        assertEquals(DefaultKnowledgeRetrievalEngine.SECTION_WEIGHT, item.relevanceScore());
    }

    @Test
    @DisplayName("Test 6: Keyword matches in content score with the keyword weight")
    void testKeywordMatchRetrieval() {
        RetrievalResult result = engine.retrieve(query("streams"));

        EvidenceItem item = result.evidence().get(0);
        assertEquals(DOC_JAVA + "-chunk-1", item.chunkId());
        assertEquals(0.85, item.relevanceScore());
    }

    @Test
    @DisplayName("Test 7: The locked weighted formula produces exact scores")
    void testRelevanceScoringFormula() {
        RetrievalResult result = engine.retrieve(query("collections"));

        assertEquals(2, result.evidenceCount());
        EvidenceItem best = result.evidence().get(0);
        assertEquals(DOC_JAVA + "-chunk-0", best.chunkId());
        assertEquals(0.85, best.relevanceScore());
        assertEquals(0.375, result.evidence().get(1).relevanceScore());
    }

    @Test
    @DisplayName("Test 8: One-hop graph expansion retrieves related chunks")
    void testGraphExpansion() {
        RetrievalResult result = engine.retrieve(query("java"));

        EvidenceItem expanded = result.evidence().stream()
                .filter(item -> item.documentId().equals(DOC_SPRING))
                .findFirst()
                .orElseThrow();
        assertEquals(0.10, expanded.relevanceScore());
        assertTrue(expanded.matchedConcepts().contains("Spring Boot"),
                "Spring Boot must be matched through one-hop expansion from Java");
    }

    @Test
    @DisplayName("Test 9: Expansion never traverses beyond one hop")
    void testNoRecursiveExpansion() {
        KnowledgeDocument listDoc = new KnowledgeDocument("doc-list", "src-list", "Misc Notes",
                ParserType.TEXT,
                List.of(chunk("doc-list", 0, "Misc", "The list interface is simple.")),
                Instant.EPOCH);
        DefaultKnowledgeRetrievalEngine scoped = new DefaultKnowledgeRetrievalEngine(
                List.of(listDoc), graph);

        assertTrue(scoped.retrieve(query("java")).evidence().isEmpty(),
                "List is two hops from Java and must not be expanded");
    }

    @Test
    @DisplayName("Test 10: Equal scores order by document title")
    void testStableOrderingByDocumentTitle() {
        KnowledgeDocument docA = new KnowledgeDocument("doc-a", "src-a", "A Guide",
                ParserType.TEXT,
                List.of(chunk("doc-a", 0, "Shared", "shared topic here.")), Instant.EPOCH);
        KnowledgeDocument docB = new KnowledgeDocument("doc-b", "src-b", "B Guide",
                ParserType.TEXT,
                List.of(chunk("doc-b", 0, "Shared", "shared topic here.")), Instant.EPOCH);
        DefaultKnowledgeRetrievalEngine scoped = new DefaultKnowledgeRetrievalEngine(
                List.of(docB, docA));

        RetrievalResult result = scoped.retrieve(query("shared"));
        assertEquals(2, result.evidenceCount());
        assertEquals("doc-a", result.evidence().get(0).documentId());
        assertEquals("doc-b", result.evidence().get(1).documentId());
    }

    @Test
    @DisplayName("Test 11: Remaining ties order by section then chunk index")
    void testStableOrderingBySectionAndIndex() {
        KnowledgeDocument doc = new KnowledgeDocument("doc-ties", "src-ties", "Tie Guide",
                ParserType.TEXT,
                List.of(
                        chunk("doc-ties", 1, "Beta", "ties break deterministically."),
                        chunk("doc-ties", 0, "Alpha", "ties break deterministically.")),
                Instant.EPOCH);
        DefaultKnowledgeRetrievalEngine scoped = new DefaultKnowledgeRetrievalEngine(
                List.of(doc));

        RetrievalResult result = scoped.retrieve(query("ties"));
        assertEquals(2, result.evidenceCount());
        assertEquals("Alpha", sectionOfTitleCase(result.evidence().get(0).chunkId(), doc));
        assertEquals(0, result.evidence().get(0).relevanceScore() - result.evidence().get(1).relevanceScore(), 1e-9);
    }

    private static String sectionOfTitleCase(String chunkId, KnowledgeDocument document) {
        return document.chunks().stream()
                .filter(c -> c.chunkId().equals(chunkId))
                .findFirst()
                .orElseThrow()
                .metadata()
                .section();
    }

    @Test
    @DisplayName("Test 12: Higher scores always rank first")
    void testScoreOrderingDescending() {
        RetrievalResult result = engine.retrieve(query("collections"));

        for (int i = 1; i < result.evidenceCount(); i++) {
            assertTrue(result.evidence().get(i - 1).relevanceScore()
                    >= result.evidence().get(i).relevanceScore());
        }
    }

    @Test
    @DisplayName("Test 13: Same input produces identical output")
    void testSameInputIdenticalOutput() {
        RetrievalQuery q = query("collections");
        assertEquals(engine.retrieve(q), engine.retrieve(q));
    }

    @Test
    @DisplayName("Test 14: Original chunk content is preserved verbatim")
    void testOriginalContentPreserved() {
        RetrievalResult result = engine.retrieve(query("collections"));

        EvidenceItem item = result.evidence().get(0);
        assertEquals("The collections framework organizes objects into lists and sets.",
                item.content());
    }

    @Test
    @DisplayName("Test 15: Matched concepts are canonically sorted")
    void testMatchedConceptsSorted() {
        RetrievalResult result = engine.retrieve(query("collections"));

        List<String> concepts = result.evidence().get(0).matchedConcepts();
        assertEquals(concepts.stream().sorted().toList(), concepts);
        assertTrue(concepts.contains("Collections"));
        assertTrue(concepts.contains("Java"), "title mention must count as concept match");
    }

    @Test
    @DisplayName("Test 16: No match yields an empty result without failure")
    void testNoMatchYieldsEmptyResult() {
        RetrievalResult result = engine.retrieve(query("quantum"));

        assertEquals(0, result.evidenceCount());
        assertEquals(0, result.matchedChunks());
        assertEquals(2, result.searchedDocuments());
        assertTrue(result.best().isEmpty());
    }

    @Test
    @DisplayName("Test 17: Whole-word matching rejects substring noise")
    void testWholeWordMatching() {
        KnowledgeDocument jsDoc = new KnowledgeDocument("doc-js", "src-js", "Script Notes",
                ParserType.TEXT,
                List.of(chunk("doc-js", 0, "Script", "JavaScript everywhere and nothing else.")),
                Instant.EPOCH);
        DefaultKnowledgeRetrievalEngine scoped = new DefaultKnowledgeRetrievalEngine(
                List.of(jsDoc), graph);

        assertTrue(scoped.retrieve(query("java")).evidence().isEmpty(),
                "javascript must never match the whole-word keyword java");
    }

    @Test
    @DisplayName("Test 18: Empty keyword queries produce empty results")
    void testEmptyKeywordQuery() {
        RetrievalResult result = engine.retrieve(query());

        assertEquals(0, result.evidenceCount());
        assertEquals(2, result.searchedDocuments());
    }

    @Test
    @DisplayName("Test 19: Null queries are rejected")
    void testNullQueryRejected() {
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> engine.retrieve(null));
        assertTrue(npe.getMessage().contains("query"));
    }

    @Test
    @DisplayName("Test 20: Evidence items are deeply immutable")
    void testEvidenceImmutability() {
        RetrievalResult result = engine.retrieve(query("collections"));

        assertThrows(UnsupportedOperationException.class,
                () -> result.evidence().add(null));
        assertThrows(UnsupportedOperationException.class,
                () -> result.evidence().get(0).matchedConcepts().add("x"));
    }

    @Test
    @DisplayName("Test 21: Evidence item validation is defensive")
    void testEvidenceItemValidation() {
        assertThrows(NullPointerException.class,
                () -> new EvidenceItem(null, "doc", "c", 0.5, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new EvidenceItem(" ", "doc", "c", 0.5, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new EvidenceItem("c1", "doc", "c", 1.5, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new EvidenceItem("c1", "doc", "c", -0.1, List.of()));
        EvidenceItem valid = new EvidenceItem("c1", "doc", "content", 0.5, List.of("b", "a"));
        assertEquals(List.of("a", "b"), valid.matchedConcepts());
        assertEquals("content", valid.content());
    }

    @Test
    @DisplayName("Test 22: Retrieval result counters must stay consistent")
    void testRetrievalResultValidation() {
        EvidenceItem item = new EvidenceItem("c1", "doc", "content", 0.5, List.of());
        assertThrows(IllegalArgumentException.class,
                () -> new RetrievalResult(List.of(item), 2, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new RetrievalResult(List.of(item), -1, 1));
        RetrievalResult valid = new RetrievalResult(List.of(item), 2, 1);
        assertEquals(1, valid.evidenceCount());
        assertEquals(0.5, valid.best().orElseThrow().relevanceScore());
    }

    @Test
    @DisplayName("Test 23: Weight constants are exactly as locked")
    void testWeightConstantsLocked() {
        assertEquals(0.35, DefaultKnowledgeRetrievalEngine.KEYWORD_WEIGHT);
        assertEquals(0.30, DefaultKnowledgeRetrievalEngine.CONCEPT_WEIGHT);
        assertEquals(0.20, DefaultKnowledgeRetrievalEngine.SECTION_WEIGHT);
        assertEquals(0.15, DefaultKnowledgeRetrievalEngine.TITLE_WEIGHT);
    }

    @Test
    @DisplayName("Test 24: End-to-end K1 + K2 + K3 + K4 pipeline retrieves ranked evidence")
    void testIntegrationPipeline() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        DefaultDocumentIngestionEngine ingestion = new DefaultDocumentIngestionEngine(registry);
        DefaultKnowledgeGraphBuilder graphBuilder = new DefaultKnowledgeGraphBuilder();
        KnowledgeSource source = registry.register(KnowledgeSourceType.MARKDOWN,
                "Java Collections Guide", "docs/collections.md", null, Map.of());
        String markdown = "# Java Collections\n\n"
                + "Java provides the collections framework for storing objects.\n\n"
                + "## List\n\n"
                + "The List interface belongs to collections, and streams extend collections.\n";
        IngestionResult ingested = ingestion.ingest(
                source.sourceId(), "Java Collections Guide", ParserType.MARKDOWN, markdown);
        ConceptGraph built = graphBuilder.build(ingested.document());

        DefaultKnowledgeRetrievalEngine retrieval = new DefaultKnowledgeRetrievalEngine(
                List.of(ingested.document()), built);
        RetrievalQuery contextQuery = RetrievalQuery.fromContext(
                intent(), domain(), goalStructure("Learn Java Collections"), UserConstraints.empty());
        RetrievalResult result = retrieval.retrieve(contextQuery);

        assertTrue(result.evidenceCount() >= 1);
        assertEquals(1, result.searchedDocuments());
        assertEquals(result.evidenceCount(), result.matchedChunks());
        EvidenceItem best = result.evidence().get(0);
        assertEquals(ingested.document().documentId(), best.documentId());
        assertTrue(best.relevanceScore() > 0.0);
        assertTrue(best.matchedConcepts().contains("Collections"));
        assertTrue(best.content().contains("collections framework"));
        assertEquals(retrieval.retrieve(contextQuery), result);
    }
}
