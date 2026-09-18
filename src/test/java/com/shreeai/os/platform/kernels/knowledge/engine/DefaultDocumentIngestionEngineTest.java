package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.engine.parser.HtmlDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

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
 * Deterministic unit tests for {@link DefaultDocumentIngestionEngine} and the
 * K2 canonical ingestion models.
 */
public class DefaultDocumentIngestionEngineTest {

    private static final String MARKDOWN_DOC =
            "# Collections\n"
            + "\n"
            + "The collections framework stores and manipulates groups of objects.\n"
            + "\n"
            + "## List\n"
            + "\n"
            + "A list is an ordered collection that allows duplicate elements.\n";

    private static final String HTML_DOC =
            "<html lang=\"en\">\n"
            + "<head><style>body { color: red; }</style></head>\n"
            + "<body>\n"
            + "<nav><a href=\"/home\">Home</a></nav>\n"
            + "<script>alert(&quot;ignored&quot;);</script>\n"
            + "<h1>Java Guide</h1>\n"
            + "<p>Java is a class-based language with &amp; strong typing.</p>\n"
            + "<h2>Concurrency</h2>\n"
            + "<p>Executors manage worker threads &amp; futures.</p>\n"
            + "<footer>copyright noise</footer>\n"
            + "</body>\n"
            + "</html>";

    private DefaultKnowledgeSourceRegistry registry;
    private DefaultDocumentIngestionEngine engine;
    private String sourceId;

    @BeforeEach
    void setUp() {
        registry = new DefaultKnowledgeSourceRegistry();
        engine = new DefaultDocumentIngestionEngine(registry);
        KnowledgeSource source = registry.register(KnowledgeSourceType.MARKDOWN,
                "Java Guide", "docs/java-guide.md", null, Map.of());
        sourceId = source.sourceId();
    }

    @Test
    @DisplayName("Test 1: Markdown ingestion produces a canonical document")
    void testMarkdownIngestionProducesDocument() {
        IngestionResult result = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        KnowledgeDocument document = result.document();
        assertEquals(sourceId, document.sourceId());
        assertEquals("Java Guide", document.title());
        assertEquals(ParserType.MARKDOWN, document.parserType());
        assertEquals(2, document.chunkCount());
        assertEquals(sourceId, document.chunks().get(0).metadata().sourceId());
    }

    @Test
    @DisplayName("Test 2: Markdown heading chunking matches the locked example")
    void testMarkdownHeadingChunking() {
        IngestionResult result = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        List<KnowledgeDocumentChunk> chunks = result.document().chunks();
        assertEquals("Collections", chunks.get(0).metadata().section());
        assertEquals("List", chunks.get(1).metadata().section());
        assertTrue(chunks.get(0).content().startsWith("# Collections"));
        assertTrue(chunks.get(0).content().contains("collections framework"));
        assertEquals("## List\n\nA list is an ordered collection that allows duplicate elements.\n",
                chunks.get(1).content());
    }

    @Test
    @DisplayName("Test 3: Fenced code blocks never produce heading splits")
    void testMarkdownCodeFences() {
        String content = "# Guide\n\n```\n# not a heading\n```\n\nreal body\n";
        IngestionResult result = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, content);

        assertEquals(1, result.document().chunkCount());
        assertTrue(result.document().chunks().get(0).content().contains("# not a heading"));
    }

    @Test
    @DisplayName("Test 4: HTML ingestion builds semantic sections in order")
    void testHtmlIngestion() {
        String webSourceId = registry.register(KnowledgeSourceType.WEB,
                "Java Guide Web", "https://docs.example/java", null, Map.of()).sourceId();
        IngestionResult result = engine.ingest(
                webSourceId, "Java Guide Web", ParserType.HTML, HTML_DOC);

        List<KnowledgeDocumentChunk> chunks = result.document().chunks();
        assertEquals(2, chunks.size());
        assertEquals("Java Guide", chunks.get(0).metadata().section());
        assertEquals("Concurrency", chunks.get(1).metadata().section());
        assertEquals("Java Guide\n\nJava is a class-based language with & strong typing.",
                chunks.get(0).content());
        assertTrue(chunks.get(1).content().contains("Executors manage worker threads & futures."));
    }

    @Test
    @DisplayName("Test 5: Script, style and navigation content is removed")
    void testHtmlScriptStyleRemoval() {
        String webSourceId = registry.register(KnowledgeSourceType.WEB,
                "Java Guide Web", "https://docs.example/java", null, Map.of()).sourceId();
        IngestionResult result = engine.ingest(
                webSourceId, "Java Guide Web", ParserType.HTML, HTML_DOC);

        for (KnowledgeDocumentChunk chunk : result.document().chunks()) {
            String content = chunk.content();
            assertFalse(content.contains("alert"), "script content must be removed");
            assertFalse(content.contains("color: red"), "style content must be removed");
            assertFalse(content.contains("Home"), "navigation content must be removed");
            assertFalse(content.contains("copyright noise"), "footer content must be removed");
        }
    }

    @Test
    @DisplayName("Test 6: Declared HTML language propagates into chunk metadata")
    void testHtmlLanguageExtraction() {
        String webSourceId = registry.register(KnowledgeSourceType.WEB,
                "Java Guide Web", "https://docs.example/java", null, Map.of()).sourceId();
        IngestionResult result = engine.ingest(
                webSourceId, "Java Guide Web", ParserType.HTML, HTML_DOC);

        for (KnowledgeDocumentChunk chunk : result.document().chunks()) {
            assertEquals("en", chunk.metadata().language());
        }
        assertEquals("en", HtmlDocumentParser.documentLanguage(HTML_DOC));
    }

    @Test
    @DisplayName("Test 7: WEB parser type behaves like HTML for the same content")
    void testWebParserTypeMatchesHtml() {
        String webSourceId = registry.register(KnowledgeSourceType.WEB,
                "Java Guide Web", "https://docs.example/java", null, Map.of()).sourceId();
        IngestionResult html = engine.ingest(
                webSourceId, "Java Guide Web", ParserType.HTML, HTML_DOC);
        IngestionResult web = engine.ingest(
                webSourceId, "Java Guide Web", ParserType.WEB, HTML_DOC);

        assertEquals(html.document().chunkCount(), web.document().chunkCount());
        assertEquals(html.document().chunks().get(0).content(),
                web.document().chunks().get(0).content());
        assertNotEquals(html.document().documentId(), web.document().documentId(),
                "parser type is part of the deterministic document identity");
    }

    @Test
    @DisplayName("Test 8: Text chunking stays within the 800 character window")
    void testTextChunking() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            content.append("Paragraph ").append(i)
                    .append(" explains another deterministic aspect of the platform engine. ")
                    .append("It keeps every sentence readable and intact inside the chunk.\n\n");
        }
        IngestionResult result = engine.ingest(
                sourceId, "Long Text", ParserType.TEXT, content.toString());

        List<KnowledgeDocumentChunk> chunks = result.document().chunks();
        assertTrue(chunks.size() >= 3, "long text must produce several chunks");
        for (KnowledgeDocumentChunk chunk : chunks) {
            assertTrue(chunk.content().length() <= 800,
                    "no chunk may exceed the 800 character window");
            assertFalse(chunk.content().isBlank());
        }
    }

    @Test
    @DisplayName("Test 9: Text chunk boundaries never split words")
    void testTextChunkingNeverSplitsWords() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            content.append("word").append(i).append(" ");
        }
        IngestionResult result = engine.ingest(
                sourceId, "Words", ParserType.TEXT, content.toString());

        List<KnowledgeDocumentChunk> chunks = result.document().chunks();
        assertTrue(chunks.size() >= 2);
        for (int i = 0; i < chunks.size() - 1; i++) {
            String ending = chunks.get(i).content();
            assertTrue(ending.endsWith(" ") || ending.endsWith("\n"),
                    "chunk boundaries must fall on whitespace, never inside a word");
        }
        String whole = String.join("", chunks.stream().map(KnowledgeDocumentChunk::content).toList());
        assertTrue(whole.contains("word0"));
        assertTrue(whole.contains("word199"));
    }

    @Test
    @DisplayName("Test 10: Document id is stable and derived from the locked seed")
    void testStableDocumentId() {
        IngestionResult first = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);
        IngestionResult second = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        assertEquals(first.document().documentId(), second.document().documentId());
        assertEquals(DefaultDocumentIngestionEngine.documentIdFor(sourceId, "Java Guide", ParserType.MARKDOWN),
                first.document().documentId());
        assertNotEquals(DefaultDocumentIngestionEngine.documentIdFor(sourceId, "Other", ParserType.MARKDOWN),
                first.document().documentId());
        assertEquals(64, first.document().documentId().length());
    }

    @Test
    @DisplayName("Test 11: Chunk ids are stable and derived from the locked seed")
    void testStableChunkId() {
        IngestionResult first = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);
        IngestionResult second = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        List<KnowledgeDocumentChunk> a = first.document().chunks();
        List<KnowledgeDocumentChunk> b = second.document().chunks();
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).chunkId(), b.get(i).chunkId());
            assertEquals(DefaultDocumentIngestionEngine.chunkIdFor(
                            a.get(i).metadata().chunkIndex() == i
                                    ? first.document().documentId() : "x", i, a.get(i).content()),
                    a.get(i).chunkId(),
                    "chunk id must equal SHA-256(documentId + chunkIndex + content)");
        }
    }

    @Test
    @DisplayName("Test 12: Empty and blank content is rejected")
    void testEmptyContentRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.ingest(sourceId, "Java Guide", ParserType.MARKDOWN, ""));
        assertThrows(IllegalArgumentException.class,
                () -> engine.ingest(sourceId, "Java Guide", ParserType.MARKDOWN, "   \n\t  "));
    }

    @Test
    @DisplayName("Test 13: Null input is rejected with a meaningful message")
    void testNullInputRejected() {
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> engine.ingest(null, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC));
        assertTrue(npe.getMessage().contains("sourceId"));
        assertThrows(NullPointerException.class,
                () -> engine.ingest(sourceId, null, ParserType.MARKDOWN, MARKDOWN_DOC));
        assertThrows(NullPointerException.class,
                () -> engine.ingest(sourceId, "Java Guide", null, MARKDOWN_DOC));
        assertThrows(NullPointerException.class,
                () -> engine.ingest(sourceId, "Java Guide", ParserType.MARKDOWN, null));
    }

    @Test
    @DisplayName("Test 14: Unknown source id is rejected")
    void testUnknownSourceRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> engine.ingest("no-such-source", "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC));
        assertTrue(ex.getMessage().contains("unknown knowledge source"));
    }

    @Test
    @DisplayName("Test 15: Ingestion is snapshot compatible and never mutates the registry")
    void testSnapshotCompatibility() {
        assertEquals(1, registry.snapshot().size());
        engine.ingest(sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);
        assertEquals(1, registry.snapshot().size(),
                "ingestion must not add or alter registry sources");
        assertTrue(registry.findById(sourceId).isPresent());
        assertEquals(KnowledgeSourceType.MARKDOWN,
                registry.findById(sourceId).orElseThrow().type());
    }

    @Test
    @DisplayName("Test 16: Same input produces byte-identical canonical output")
    void testSameInputIdenticalOutput() {
        // Fixed clock makes the engine fully deterministic, including ingestedAt.
        DefaultDocumentIngestionEngine fixedClockEngine =
                new DefaultDocumentIngestionEngine(registry,
                        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        IngestionResult first = fixedClockEngine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);
        IngestionResult second = fixedClockEngine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        assertEquals(first.document().documentId(), second.document().documentId());
        assertEquals(first.document().chunks(), second.document().chunks());
        assertEquals(first.document().ingestedAt(), second.document().ingestedAt());
        assertEquals(first.document(), second.document());
    }

    @Test
    @DisplayName("Test 17: Ingestion result carries correct observability statistics")
    void testIngestionResultStatistics() {
        IngestionResult result = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);

        assertEquals(result.document().chunkCount(), result.totalChunks());
        int expected = result.document().chunks().stream()
                .mapToInt(chunk -> chunk.content().length()).sum();
        assertEquals(expected, result.totalCharacters());
        assertNotNull(result.processingTime());
        assertFalse(result.processingTime().isNegative());
        assertTrue(result.totalCharacters() > 0);
    }

    @Test
    @DisplayName("Test 18: PDF stub fails loudly instead of silently")
    void testPdfStubRejectedLoudly() {
        String pdfSourceId = registry.register(KnowledgeSourceType.PDF,
                "Spec PDF", "docs/spec.pdf", null, Map.of()).sourceId();
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> engine.ingest(pdfSourceId, "Spec PDF", ParserType.PDF, "raw pdf bytes"));
        assertTrue(ex.getMessage().contains("PDF"));
    }

    @Test
    @DisplayName("Test 19: Chunk metadata is defensively validated")
    void testChunkMetadataValidation() {
        assertThrows(NullPointerException.class,
                () -> new ChunkMetadata(null, "s", "en", 0, "src"));
        assertThrows(NullPointerException.class,
                () -> new ChunkMetadata("t", "s", "en", 0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ChunkMetadata("t", "s", "en", -1, "src"));
        ChunkMetadata metadata = new ChunkMetadata("t", "s", "", 3, "src");
        assertEquals(3, metadata.chunkIndex());
        assertEquals("", metadata.language(), "empty language means unknown");
    }

    @Test
    @DisplayName("Test 20: Canonical documents are deeply immutable")
    void testDocumentImmutability() {
        IngestionResult result = engine.ingest(
                sourceId, "Java Guide", ParserType.MARKDOWN, MARKDOWN_DOC);
        KnowledgeDocument document = result.document();

        assertThrows(UnsupportedOperationException.class,
                () -> document.chunks().add(null));
        KnowledgeDocumentChunk chunk = document.chunks().get(0);
        assertNotNull(chunk.chunkId());
        assertNotNull(chunk.metadata());
        assertEquals(0, chunk.metadata().chunkIndex());
        assertTrue(Duration.ZERO.compareTo(result.processingTime()) <= 0);
    }
}
