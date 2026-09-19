package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultKnowledgeGraphBuilder} and the
 * K3 graph models.
 */
public class DefaultKnowledgeGraphBuilderTest {

    private DefaultKnowledgeGraphBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DefaultKnowledgeGraphBuilder();
    }

    private static KnowledgeDocumentChunk chunk(int index, String section, String content) {
        return new KnowledgeDocumentChunk("chunk-" + index, content,
                new ChunkMetadata("Doc Title", section, "", index, "src-fixed"));
    }

    private static KnowledgeDocument document(String title, KnowledgeDocumentChunk... chunks) {
        return new KnowledgeDocument("doc-fixed", "src-fixed", title,
                ParserType.MARKDOWN, List.of(chunks), Instant.EPOCH);
    }

    private static Map<String, GraphConcept> conceptsByName(ConceptGraph graph) {
        Map<String, GraphConcept> byName = new java.util.HashMap<>();
        for (GraphConcept concept : graph.concepts()) {
            byName.put(concept.name(), concept);
        }
        return byName;
    }

    private static Optional<ConceptRelationship> findEdge(ConceptGraph graph,
                                                          String fromName,
                                                          RelationshipType type,
                                                          String toName) {
        Map<String, GraphConcept> byId = new java.util.HashMap<>();
        for (GraphConcept concept : graph.concepts()) {
            byId.put(concept.conceptId(), concept);
        }
        return graph.relationships().stream()
                .filter(edge -> edge.type() == type)
                .filter(edge -> byId.containsKey(edge.fromConcept())
                        && byId.containsKey(edge.toConcept()))
                .filter(edge -> byId.get(edge.fromConcept()).name().equals(fromName)
                        && byId.get(edge.toConcept()).name().equals(toName))
                .findFirst();
    }

    private static boolean hasRelatedToBetween(ConceptGraph graph, String nameA, String nameB) {
        Map<String, GraphConcept> byId = new java.util.HashMap<>();
        for (GraphConcept concept : graph.concepts()) {
            byId.put(concept.conceptId(), concept);
        }
        return graph.relationships().stream()
                .filter(edge -> edge.type() == RelationshipType.RELATED_TO)
                .anyMatch(edge -> {
                    String a = byId.get(edge.fromConcept()).name();
                    String b = byId.get(edge.toConcept()).name();
                    return (a.equals(nameA) && b.equals(nameB)) || (a.equals(nameB) && b.equals(nameA));
                });
    }

    /** RELATED_TO edges are direction-canonicalized by concept id, so match unordered. */
    private static Optional<ConceptRelationship> findRelatedTo(ConceptGraph graph, String nameA, String nameB) {
        return graph.relationships().stream()
                .filter(edge -> edge.type() == RelationshipType.RELATED_TO)
                .filter(edge -> hasRelatedToBetween(graph, nameA, nameB))
                .findFirst();
    }

    @Test
    @DisplayName("Test 1: Extracts the Java language concept from the title")
    void testExtractJavaConcept() {
        ConceptGraph graph = builder.build(document(
                "Java Collections Guide",
                chunk(0, "Intro", "The platform ships with rich examples.")));

        GraphConcept java = conceptsByName(graph).get("Java");
        assertNotNull(java, "Java must be discovered from the title");
        assertEquals(ConceptType.LANGUAGE, java.type());
        assertNotNull(java.conceptId());
    }

    @Test
    @DisplayName("Test 2: Extracts Spring Boot from its compact alias")
    void testExtractSpringBoot() {
        ConceptGraph graph = builder.build(document(
                "Platform Notes",
                chunk(0, "Setup", "springboot simplifies configuration dramatically.")));

        GraphConcept springBoot = conceptsByName(graph).get("Spring Boot");
        assertNotNull(springBoot, "the springboot alias must canonicalize to Spring Boot");
        assertEquals(ConceptType.FRAMEWORK, springBoot.type());
        assertFalse(graph.concepts().stream().anyMatch(c -> c.name().equals("springboot")));
    }

    @Test
    @DisplayName("Test 3: Canonicalization collapses case and alias variants")
    void testCanonicalization() {
        ConceptGraph graph = builder.build(document(
                "JAVA AND POSTGRES GUIDE",
                chunk(0, "Storage", "jdk tools integrate with postgresql databases.")));

        Map<String, GraphConcept> byName = conceptsByName(graph);
        assertTrue(byName.containsKey("Java"), "JAVA must canonicalize to Java");
        assertTrue(byName.containsKey("PostgreSQL"), "postgres must canonicalize to PostgreSQL");
        assertFalse(byName.containsKey("JAVA"));
        assertFalse(byName.containsKey("postgres"));
        assertFalse(byName.containsKey("jdk"), "jdk must collapse into Java");
    }

    @Test
    @DisplayName("Test 4: Longest-match wins over shorter aliases")
    void testLongestMatchPrecedence() {
        ConceptGraph graph = builder.build(document(
                "Platform Notes",
                chunk(0, "Data", "spring data repositories simplify persistence.")));

        Map<String, GraphConcept> byName = conceptsByName(graph);
        assertTrue(byName.containsKey("Spring Data"),
                "the longest alias spring data must win over spring");
        assertFalse(byName.containsKey("Spring"),
                "the shorter overlapping alias must not also be emitted");
    }

    @Test
    @DisplayName("Test 5: Explicit prerequisite rule produces a confident edge")
    void testPrerequisiteRelation() {
        ConceptGraph graph = builder.build(document(
                "Learning Path",
                chunk(0, "Roadmap", "collections come before streams in the curriculum.")));

        Optional<ConceptRelationship> edge =
                findEdge(graph, "Collections", RelationshipType.PREREQUISITE, "Streams");
        assertTrue(edge.isPresent(), "Collections must be a prerequisite of Streams");
        assertEquals(DefaultKnowledgeGraphBuilder.PREREQUISITE_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 6: Explicit part-of rule structures the hierarchy")
    void testPartOfRelation() {
        ConceptGraph graph = builder.build(document(
                "Framework Tour",
                chunk(0, "Structure", "the list interface is a core member of collections.")));

        Optional<ConceptRelationship> edge =
                findEdge(graph, "List", RelationshipType.PART_OF, "Collections");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.SECTION_CO_OCCURRENCE_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 7: Explicit depends-on rule links framework to language")
    void testDependsOnRelation() {
        ConceptGraph graph = builder.build(document(
                "Stack Overview",
                chunk(0, "Architecture", "spring boot is built on top of java.")));

        Optional<ConceptRelationship> edge =
                findEdge(graph, "Spring Boot", RelationshipType.DEPENDS_ON, "Java");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.SECTION_CO_OCCURRENCE_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 8: Explicit rules stay silent when an endpoint is absent")
    void testDependsOnSkippedWhenEndpointAbsent() {
        ConceptGraph graph = builder.build(document(
                "Stack Overview",
                chunk(0, "Architecture", "spring boot supports externalized configuration.")));

        assertTrue(findEdge(graph, "Spring Boot", RelationshipType.DEPENDS_ON, "Java").isEmpty(),
                "no edge may be emitted when Java is absent");
    }

    @Test
    @DisplayName("Test 9: Repeated section co-occurrence yields 0.80")
    void testRelatedToSectionTier() {
        ConceptGraph graph = builder.build(document(
                "Data Tooling",
                chunk(0, "Overview", "postgresql pairs with maven in this setup."),
                chunk(1, "Details", "maven packages the postgresql driver.")));

        Optional<ConceptRelationship> edge =
                findRelatedTo(graph, "Maven", "PostgreSQL");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.SECTION_CO_OCCURRENCE_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 10: Single paragraph co-occurrence yields 0.70")
    void testRelatedToParagraphTier() {
        ConceptGraph graph = builder.build(document(
                "Data Tooling",
                chunk(0, "Notes", "postgresql and maven work well together.")));

        Optional<ConceptRelationship> edge =
                findRelatedTo(graph, "Maven", "PostgreSQL");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.PARAGRAPH_RELATION_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 11: Heading co-occurrence yields 0.95")
    void testRelatedToHeadingTier() {
        ConceptGraph graph = builder.build(document(
                "Platform Notes",
                chunk(0, "Java Overview", "Design details are documented here."),
                chunk(1, "PostgreSQL Storage", "Operational notes are documented too.")));

        Optional<ConceptRelationship> edge =
                findRelatedTo(graph, "Java", "PostgreSQL");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.HEADING_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 12: Title co-occurrence yields 1.00")
    void testRelatedToTitleTier() {
        ConceptGraph graph = builder.build(document(
                "Java and PostgreSQL",
                chunk(0, "Guide", "See the official documentation.")));

        Optional<ConceptRelationship> edge =
                findRelatedTo(graph, "Java", "PostgreSQL");
        assertTrue(edge.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.TITLE_CONFIDENCE,
                edge.orElseThrow().confidence());
    }

    @Test
    @DisplayName("Test 13: Repeated mentions deduplicate into one concept")
    void testDeduplicateConcepts() {
        ConceptGraph graph = builder.build(document(
                "Java Guide",
                chunk(0, "Java Basics", "Java is versatile. Java is fast."),
                chunk(1, "Java Advanced", "JAVA idioms improve JAVA code.")));

        long javaCount = graph.concepts().stream()
                .filter(concept -> concept.name().equals("Java"))
                .count();
        assertEquals(1, javaCount, "all Java mentions must collapse into one concept");
    }

    @Test
    @DisplayName("Test 14: Repeated co-occurrences deduplicate into one edge")
    void testDeduplicateRelationships() {
        ConceptGraph graph = builder.build(document(
                "Data Tooling",
                chunk(0, "Setup", "postgresql and maven are installed."),
                chunk(1, "Build", "maven drives the postgresql migrations."),
                chunk(2, "Verify", "postgresql backups run under maven.")));

        long relatedToCount = graph.relationships().stream()
                .filter(edge -> edge.type() == RelationshipType.RELATED_TO)
                .filter(edge -> {
                    Map<String, GraphConcept> byId = new java.util.HashMap<>();
                    graph.concepts().forEach(c -> byId.put(c.conceptId(), c));
                    String a = byId.get(edge.fromConcept()).name();
                    String b = byId.get(edge.toConcept()).name();
                    return (a.equals("PostgreSQL") && b.equals("Maven"))
                            || (a.equals("Maven") && b.equals("PostgreSQL"));
                })
                .count();
        assertEquals(1, relatedToCount, "repeated co-occurrence must yield exactly one edge");
    }

    @Test
    @DisplayName("Test 15: Explicit rules suppress co-occurrence edges")
    void testExplicitRuleOverridesRelatedTo() {
        ConceptGraph graph = builder.build(document(
                "Framework Tour",
                chunk(0, "Structure", "the list interface is a core member of collections.")));

        assertTrue(findEdge(graph, "List", RelationshipType.PART_OF, "Collections").isPresent());
        assertFalse(hasRelatedToBetween(graph, "List", "Collections"),
                "no RELATED_TO edge may coexist with the explicit part-of edge");
    }

    @Test
    @DisplayName("Test 16: Concept ids are stable and type-sensitive")
    void testStableConceptIds() {
        String javaId = DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.LANGUAGE, "Java");
        String again = DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.LANGUAGE, "Java");
        assertEquals(javaId, again);
        assertEquals(64, javaId.length());
        assertNotEquals(javaId,
                DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.FRAMEWORK, "Java"));

        ConceptGraph graph = builder.build(document(
                "Java Guide", chunk(0, "Intro", "Java powers the platform.")));
        assertEquals(javaId, graph.concepts().get(0).conceptId());
    }

    @Test
    @DisplayName("Test 17: Relationship ids are stable and match the locked seed")
    void testStableRelationshipIds() {
        String javaId = DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.LANGUAGE, "Java");
        String postgresId = DefaultKnowledgeGraphBuilder.conceptIdFor(ConceptType.DATABASE, "PostgreSQL");
        String expected = DefaultKnowledgeGraphBuilder.relationshipIdFor(
                javaId, RelationshipType.RELATED_TO, postgresId);
        assertEquals(expected,
                DefaultKnowledgeGraphBuilder.relationshipIdFor(javaId, RelationshipType.RELATED_TO, postgresId));
        assertEquals(64, expected.length());

        ConceptGraph graph = builder.build(document(
                "Java and PostgreSQL",
                chunk(0, "Guide", "Both are covered.")));
        ConceptRelationship edge = graph.relationships().get(0);
        assertEquals(expected, edge.relationshipId());
    }

    @Test
    @DisplayName("Test 18: Same input produces a structurally identical graph")
    void testSameInputIdenticalGraph() {
        KnowledgeDocument document = document(
                "Java Collections Guide",
                chunk(0, "Collections", "collections and streams organize objects."),
                chunk(1, "List", "the list interface belongs to collections."));
        ConceptGraph first = builder.build(document);
        ConceptGraph second = builder.build(document);

        assertEquals(first, second);
        assertEquals(first.conceptCount(), second.conceptCount());
        assertEquals(first.relationshipCount(), second.relationshipCount());
    }

    @Test
    @DisplayName("Test 19: Canonical ordering is stable by name")
    void testDeterministicOrdering() {
        ConceptGraph graph = builder.build(document(
                "Java and PostgreSQL",
                chunk(0, "Guide", "postgresql, maven and java together.")));

        List<String> names = graph.concepts().stream().map(GraphConcept::name).toList();
        List<String> sorted = names.stream().sorted().toList();
        assertEquals(sorted, names, "concepts must be ordered by canonical name");
    }

    @Test
    @DisplayName("Test 20: Graph lists are deeply immutable")
    void testGraphImmutability() {
        ConceptGraph graph = builder.build(document(
                "Java Guide", chunk(0, "Intro", "Java powers the platform.")));

        assertThrows(UnsupportedOperationException.class, () -> graph.concepts().add(null));
        assertThrows(UnsupportedOperationException.class, () -> graph.relationships().add(null));
    }

    @Test
    @DisplayName("Test 21: Unknown text yields an empty graph without failure")
    void testEmptyGraphForUnrelatedText() {
        ConceptGraph graph = builder.build(document(
                "The Quick Brown Fox",
                chunk(0, "Story", "the quick brown fox jumps over the lazy dog.")));

        assertEquals(0, graph.conceptCount());
        assertEquals(0, graph.relationshipCount());
    }

    @Test
    @DisplayName("Test 22: Null documents are rejected")
    void testNullDocumentRejected() {
        NullPointerException npe =
                assertThrows(NullPointerException.class, () -> builder.build(null));
        assertTrue(npe.getMessage().contains("document"));
    }

    @Test
    @DisplayName("Test 23: Locked confidence constants are exactly as ordered")
    void testConfidenceConstantsLocked() {
        assertEquals(1.00, DefaultKnowledgeGraphBuilder.TITLE_CONFIDENCE);
        assertEquals(0.95, DefaultKnowledgeGraphBuilder.HEADING_CONFIDENCE);
        assertEquals(0.90, DefaultKnowledgeGraphBuilder.PREREQUISITE_CONFIDENCE);
        assertEquals(0.80, DefaultKnowledgeGraphBuilder.SECTION_CO_OCCURRENCE_CONFIDENCE);
        assertEquals(0.70, DefaultKnowledgeGraphBuilder.PARAGRAPH_RELATION_CONFIDENCE);
    }

    @Test
    @DisplayName("Test 24: End-to-end K1 + K2 + K3 pipeline builds the full graph")
    void testIntegrationPipeline() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        DefaultDocumentIngestionEngine ingestion = new DefaultDocumentIngestionEngine(registry);
        KnowledgeSource source = registry.register(KnowledgeSourceType.MARKDOWN,
                "Java Collections Guide", "docs/collections.md", null, Map.of());
        String markdown = "# Java Collections\n\n"
                + "Java provides the collections framework for storing objects.\n\n"
                + "## List\n\n"
                + "The List interface belongs to collections, and streams extend collections.\n";
        IngestionResult result = ingestion.ingest(
                source.sourceId(), "Java Collections Guide", ParserType.MARKDOWN, markdown);

        ConceptGraph graph = builder.build(result.document());
        Map<String, GraphConcept> byName = conceptsByName(graph);
        assertEquals(4, graph.conceptCount());
        assertTrue(byName.containsKey("Java"));
        assertTrue(byName.containsKey("Collections"));
        assertTrue(byName.containsKey("List"));
        assertTrue(byName.containsKey("Streams"));

        assertTrue(findEdge(graph, "List", RelationshipType.PART_OF, "Collections").isPresent());
        assertTrue(findEdge(graph, "Collections", RelationshipType.PREREQUISITE, "Streams").isPresent());
        Optional<ConceptRelationship> javaCollections =
                findRelatedTo(graph, "Java", "Collections");
        assertTrue(javaCollections.isPresent());
        assertEquals(DefaultKnowledgeGraphBuilder.TITLE_CONFIDENCE, javaCollections.orElseThrow().confidence());
        assertFalse(hasRelatedToBetween(graph, "Collections", "Streams"),
                "the explicit prerequisite must suppress the co-occurrence edge");
    }
}
