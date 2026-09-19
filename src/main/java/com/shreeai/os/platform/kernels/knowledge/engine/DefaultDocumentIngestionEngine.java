package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.engine.parser.DocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.HtmlDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.MarkdownDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.ParsedSection;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.PdfDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.engine.parser.TextDocumentParser;
import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultDocumentIngestionEngine</b>
 *
 * <p>The default {@link DocumentIngestionEngine}: a stateless, thread-safe,
 * deterministic transformer of raw content into the canonical
 * {@code KnowledgeDocument} representation. It holds no database, no global
 * state and no caches - every ingestion is a pure function of its input plus
 * the {@code KnowledgeSourceRegistry} used to resolve and validate the
 * source.</p>
 *
 * <p><b>Deterministic id strategy:</b></p>
 * <ul>
 *   <li>documentId = {@code SHA-256(sourceId + title + parserType)}</li>
 *   <li>chunkId = {@code SHA-256(documentId + chunkIndex + content)}</li>
 * </ul>
 * Both ids are lowercase hex digests: identical input always produces
 * identical ids; there is no UUID and no randomness.</p>
 *
 * <p><b>Chunking strategy:</b> Markdown splits at ATX headings; HTML and web
 * pages split at the semantic tags {@code h1}/{@code h2}/{@code h3}/{@code p}
 * after script/style/navigation removal; plain text uses paragraph-aware
 * windows of at most {@value TextDocumentParser#MAX_CHUNK_LENGTH} characters
 * that never split words. PDF ingestion is rejected loudly until the PDFBox
 * parser integration arrives.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultDocumentIngestionEngine implements DocumentIngestionEngine {

    private final KnowledgeSourceRegistry registry;
    private final Map<ParserType, DocumentParser> parsers;
    private final Clock clock;

    /**
     * Creates an engine bound to the registry that owns the knowledge sources.
     * Uses {@link Clock#systemUTC()} for observability timestamps.
     *
     * @param registry the source registry used to resolve and validate sources
     *                 (must not be null)
     */
    public DefaultDocumentIngestionEngine(KnowledgeSourceRegistry registry) {
        this(registry, Clock.systemUTC());
    }

    /**
     * Creates an engine bound to the registry with an injectable {@link Clock}.
     *
     * <p>Production code uses {@code Clock.systemUTC()}; deterministic tests
     * may inject {@code Clock.fixed(...)} so that the same input always
     * produces byte-identical canonical output, including {@code ingestedAt}.</p>
     *
     * @param registry the source registry used to resolve and validate sources
     *                 (must not be null)
     * @param clock    the clock supplying the {@code ingestedAt} timestamp
     *                 (must not be null)
     */
    public DefaultDocumentIngestionEngine(KnowledgeSourceRegistry registry, Clock clock) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        DocumentParser html = new HtmlDocumentParser();
        Map<ParserType, DocumentParser> map = new EnumMap<>(ParserType.class);
        map.put(ParserType.MARKDOWN, new MarkdownDocumentParser());
        map.put(ParserType.TEXT, new TextDocumentParser());
        map.put(ParserType.HTML, html);
        map.put(ParserType.WEB, html);
        map.put(ParserType.PDF, new PdfDocumentParser());
        this.parsers = Map.copyOf(map);
    }

    @Override
    public IngestionResult ingest(String sourceId,
                                  String title,
                                  ParserType parserType,
                                  String content) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(parserType, "parserType must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        KnowledgeSource source = registry.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown knowledge source: " + sourceId));

        long startNanos = System.nanoTime();
        DocumentParser parser = parsers.get(parserType);
        List<ParsedSection> sections = parser.parse(content);

        String documentId = documentIdFor(sourceId, title, parserType);
        String language = languageOf(parserType, content);

        List<KnowledgeDocumentChunk> chunks = buildChunks(
                documentId, title, sourceId, language, sections);

        KnowledgeDocument document = new KnowledgeDocument(
                documentId, sourceId, title, parserType, chunks, clock.instant());
        Duration processingTime = Duration.ofNanos(System.nanoTime() - startNanos);
        return new IngestionResult(document, chunks.size(), totalCharacters(chunks), processingTime);
    }

    /** Builds the ordered, deterministically identified chunk list. */
    private static List<KnowledgeDocumentChunk> buildChunks(String documentId,
                                                            String title,
                                                            String sourceId,
                                                            String language,
                                                            List<ParsedSection> sections) {
        List<KnowledgeDocumentChunk> chunks = new java.util.ArrayList<>(sections.size());
        for (int index = 0; index < sections.size(); index++) {
            ParsedSection section = sections.get(index);
            if (section.content().isBlank()) {
                continue;
            }
            String content = section.content();
            String chunkId = chunkIdFor(documentId, chunks.size(), content);
            ChunkMetadata metadata = new ChunkMetadata(
                    title, section.section(), language, chunks.size(), sourceId);
            chunks.add(new KnowledgeDocumentChunk(chunkId, content, metadata));
        }
        return List.copyOf(chunks);
    }

    /** Resolves the declared document language when the parser type carries one. */
    private static String languageOf(ParserType parserType, String content) {
        if (parserType == ParserType.HTML || parserType == ParserType.WEB) {
            return HtmlDocumentParser.documentLanguage(content);
        }
        return "";
    }

    private static int totalCharacters(List<KnowledgeDocumentChunk> chunks) {
        int total = 0;
        for (KnowledgeDocumentChunk chunk : chunks) {
            total += chunk.content().length();
        }
        return total;
    }

    /**
     * Computes the deterministic document id for the given ingestion input.
     *
     * @param sourceId   the registered source id (must not be null)
     * @param title      the document title (must not be null)
     * @param parserType the parser type (must not be null)
     * @return the lowercase hex SHA-256 document id (never null)
     */
    public static String documentIdFor(String sourceId, String title, ParserType parserType) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(parserType, "parserType must not be null");
        String seed = sourceId + title + parserType.name();
        return sha256Hex(seed);
    }

    /**
     * Computes the deterministic chunk id for the given chunk position.
     *
     * @param documentId the parent document id (must not be null)
     * @param chunkIndex the zero-based chunk index (must be &gt;= 0)
     * @param content    the chunk content (must not be null)
     * @return the lowercase hex SHA-256 chunk id (never null)
     */
    public static String chunkIdFor(String documentId, int chunkIndex, String content) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be >= 0: " + chunkIndex);
        }
        String seed = documentId + chunkIndex + content;
        return sha256Hex(seed);
    }

    /** Lowercase hex SHA-256 of the UTF-8 encoded seed. */
    private static String sha256Hex(String seed) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
        byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
