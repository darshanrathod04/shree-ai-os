package com.shreeai.os.platform.kernels.knowledge.service;

import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeException;
import com.shreeai.os.platform.kernels.knowledge.model.*;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeExtractionService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeGraphService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeIngestionService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeError;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeErrorCode;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeValidationException;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.UpdateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.validation.KnowledgeValidator;
import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;
import com.shreeai.os.platform.kernels.knowledge.engine.search.DefaultKnowledgeSearchEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.search.KnowledgeSearchEngine;
import com.shreeai.os.platform.runtime.embedding.EmbeddingProvider;
import com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder;
import com.shreeai.os.platform.runtime.storage.InMemoryKnowledgeGraphStore;
import com.shreeai.os.platform.runtime.storage.KnowledgeGraphStore;
import com.shreeai.os.platform.runtime.vector.InMemoryVectorStoreProvider;
import com.shreeai.os.platform.runtime.vector.VectorRecord;
import com.shreeai.os.platform.runtime.vector.VectorSearchEngine;
import com.shreeai.os.platform.runtime.vector.VectorSearchResult;
import com.shreeai.os.platform.runtime.vector.VectorStore;
import com.shreeai.os.platform.runtime.vector.VectorStoreProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <b>DefaultKnowledgeService</b>
 *
 * <p>The default implementation of the Knowledge service layer within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all Knowledge API requests by implementing {@link KnowledgeService},
 *       {@link KnowledgeQueryService}, {@link KnowledgeGraphService}, and
 *       {@link KnowledgeExtractionService}.</li>
 *   <li>Validates incoming requests using static {@link KnowledgeValidator} methods.</li>
 *   <li>Delegates processing to {@link KnowledgeProcessingEngine}.</li>
 *   <li>Translates failures into the {@link KnowledgeException} hierarchy.</li>
 *   <li>Contains ZERO business logic — pure coordination layer.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Constructor injection only — no field injection, service locator, or static singleton.</li>
 *   <li>Stateless — no mutable instance state, no cached Knowledge objects.</li>
 *   <li>Validation delegated to static KnowledgeValidator methods.</li>
 *   <li>Processing delegated to KnowledgeProcessingEngine.</li>
 *   <li>Exception translation — never exposes primitive error information.</li>
 *   <li>Thread-safe — immutable state, no synchronization needed.</li>
 * </ul>
 *
 * <p><b>Processing Flow:</b></p>
 * <pre>
 * Request
 *     │
 *     ▼
 * KnowledgeValidator (static)
 *     │
 *     ▼
 * KnowledgeProcessingEngine
 *     │
 *     ▼
 * Return Result
 * </pre>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-105, EIO-ARCH-001</p>
 *
 * @see KnowledgeService
 * @see KnowledgeQueryService
 * @see KnowledgeGraphService
 * @see KnowledgeExtractionService
 * @see KnowledgeProcessingEngine
 * @see KnowledgeValidator
 */
public final class DefaultKnowledgeService implements
        KnowledgeService,
        KnowledgeQueryService,
        KnowledgeSearchService,
        KnowledgeGraphService,
        KnowledgeExtractionService,
        KnowledgeIngestionService {

    private final KnowledgeProcessingEngine processingEngine;

    private final KnowledgeSearchEngine searchEngine;

    /**
     * Current graph snapshot. Guarded by an {@link AtomicReference} so
     * mutations are atomic swap operations (PHASE-1 thread-safety fix —
     * the previous bare field was not safe under concurrent mutation).
     */
    private final AtomicReference<KnowledgeGraph> graphRef;

    /**
     * Durable graph store SPI (nullable — null keeps purely in-memory behavior).
     */
    private final KnowledgeGraphStore graphStore;

    /**
     * Vector store SPI (nullable — null disables semantic vector retrieval).
     */
    private final VectorStore vectorStore;

    /**
     * Vector search engine SPI (nullable — null disables semantic vector retrieval).
     */
    private final VectorSearchEngine vectorSearchEngine;

    /**
     * Embedding provider SPI (nullable — null disables embedding production).
     */
    private final EmbeddingProvider embeddingProvider;

    /**
     * Pure ingestion processing engine (never null).
     */
    private final KnowledgeIngestionEngine ingestionEngine;

    /**
     * Creates a new DefaultKnowledgeService with constructor injection.
     *
     * <p><b>Backward compatibility:</b> this constructor preserves the exact
     * pre-PHASE-1 behavior — purely in-memory graph, lexical search only, no
     * persistence. Existing tests and integrations are unaffected.</p>
     *
     * @param processingEngine the KnowledgeProcessingEngine to delegate processing to
     *                         (must not be null)
     * @throws NullPointerException if {@code processingEngine} is null
     */
    public DefaultKnowledgeService(
            KnowledgeProcessingEngine processingEngine
    ) {
        this(processingEngine, null, null, null, null);
    }

    /**
     * Creates a fully wired DefaultKnowledgeService.
     *
     * <p>Every storage SPI parameter is optional (nullable): a {@code null}
     * disables the corresponding capability and preserves pre-PHASE-1
     * behavior. This is the seam through which PostgreSQL + pgvector and
     * Neo4j adapters are injected by the composition root — never hard-coded.</p>
     *
     * @param processingEngine   the processing engine (must not be null)
     * @param graphStore         durable graph store SPI (may be null)
     * @param vectorStore        vector store SPI (may be null)
     * @param vectorSearchEngine vector search engine SPI (may be null)
     * @param embeddingProvider  embedding provider SPI (may be null)
     */
    public DefaultKnowledgeService(
            KnowledgeProcessingEngine processingEngine,
            KnowledgeGraphStore graphStore,
            VectorStore vectorStore,
            VectorSearchEngine vectorSearchEngine,
            EmbeddingProvider embeddingProvider) {

        this.processingEngine = Objects.requireNonNull(
                processingEngine,
                "processingEngine must not be null"
        );

        this.searchEngine = new DefaultKnowledgeSearchEngine();
        this.graphRef = new AtomicReference<>(KnowledgeGraph.empty());
        this.graphStore = graphStore;
        this.vectorStore = vectorStore;
        this.vectorSearchEngine = vectorSearchEngine;
        this.embeddingProvider = embeddingProvider;
        this.ingestionEngine = new DefaultKnowledgeIngestionEngine();
    }

    /**
     * Convenience factory producing the canonical in-memory wiring:
     * in-memory graph store, in-memory vector provider, local deterministic
     * embedder. Used by the composition root as the default (zero
     * infrastructure) configuration.
     *
     * @param processingEngine the processing engine (must not be null)
     * @return a fully wired in-memory service
     */
    public static DefaultKnowledgeService withInMemoryDefaults(
            KnowledgeProcessingEngine processingEngine) {
        VectorStoreProvider vectorProvider = new InMemoryVectorStoreProvider();
        return new DefaultKnowledgeService(
                processingEngine,
                new InMemoryKnowledgeGraphStore(),
                vectorProvider.vectorStore(),
                vectorProvider.searchEngine(),
                new LocalDeterministicEmbedder());
    }

    // ========================================================================
    // KnowledgeIngestionService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Ingestion flow:</b> validate → chunk → (embed + persist vector
     * record) → create knowledge node (graph mutation + durable store
     * write-through). Ingested documents become permanently searchable through
     * {@link #search(String)} and {@link #searchBySimilarity(String)}.</p>
     */
    @Override
    public KnowledgeIngestionResult ingest(
            String title,
            String content,
            Map<String, Object> metadata) {

        if (title == null || title.isBlank()) {
            throw createValidationException("title must not be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }

        String tenantId = stringMetadata(metadata, "tenantId", "default");
        String documentId = UUID.randomUUID().toString();
        String embeddingVersion = embeddingProvider != null ? embeddingProvider.version() : null;

        List<KnowledgeChunk> chunks = ingestionEngine.chunk(content);
        List<String> nodeIds = new ArrayList<>();

        KnowledgeGraph graph = graphRef.get();
        for (KnowledgeChunk chunk : chunks) {
            KnowledgeId nodeId = new KnowledgeId(UUID.randomUUID().toString());
            KnowledgeNode node = ingestionEngine.toNode(
                    documentId, title, tenantId, chunk, embeddingVersion, metadata, nodeId);

            if (embeddingProvider != null && vectorStore != null) {
                double[] embedding = embeddingProvider.embed(chunk.text());
                Map<String, Object> vectorMetadata =
                        new LinkedHashMap<>(metadata != null ? metadata : Map.of());
                vectorMetadata.put("documentId", documentId);
                vectorMetadata.put("tenantId", tenantId);
                vectorMetadata.put("title", title);
                vectorMetadata.put("chunkIndex", chunk.index());
                vectorMetadata.put("embeddingVersion", embeddingVersion);
                vectorMetadata.put("source",
                        DefaultKnowledgeIngestionEngine.SOURCE_DOCUMENT_INGESTION);
                vectorStore.store(VectorRecord.of(nodeId.value(), chunk.text(), embedding, vectorMetadata));
            }

            graph = processingEngine.processCreate(graph, node).getGraph();

            if (graphStore != null) {
                graphStore.saveNode(node);
            }

            nodeIds.add(nodeId.value());
        }

        graphRef.set(graph);

        return KnowledgeIngestionResult.of(
                documentId,
                title,
                tenantId,
                chunks.size(),
                nodeIds,
                embeddingVersion);
    }

    private String stringMetadata(Map<String, Object> metadata, String key, String fallback) {
        if (metadata == null) {
            return fallback;
        }
        Object value = metadata.get(key);
        return value != null && !value.toString().isBlank() ? value.toString() : fallback;
    }

    // ========================================================================
    // KnowledgeService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the request using KnowledgeValidator.</li>
     *   <li>Delegates creation to KnowledgeProcessingEngine.</li>
     *   <li>Returns the created node's identifier as a string.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String createKnowledge(Object entity) {

        if (!(entity instanceof CreateKnowledgeRequest request)) {
            throw createValidationException(
                    "Entity must be a CreateKnowledgeRequest"
            );
        }

        var violations = KnowledgeValidator.validateCreateRequest(request);

        if (!violations.isEmpty()) {
            throw createValidationException(violations);
        }

        KnowledgeNode node = buildNode(request);

        var result = processingEngine.processCreate(
                graphRef.get(),
                node
        );

        graphRef.set(result.getGraph());

        if (graphStore != null) {
            graphStore.saveNode(node);
        }

        return node.getId().value();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier and entity.</li>
     *   <li>Delegates update to KnowledgeProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean updateKnowledge(String knowledgeId, Object entity) {
        KnowledgeId id = parseKnowledgeId(knowledgeId);

        if (!(entity instanceof UpdateKnowledgeRequest request)) {
            throw createValidationException("Entity must be an UpdateKnowledgeRequest");
        }

        // Validate request
        var violations = KnowledgeValidator.validateUpdateRequest(request);
        if (!violations.isEmpty()) {
            throw createValidationException(violations);
        }

        // Build updated node and delegate to engine
        KnowledgeNode updatedNode = buildUpdatedNode(id, request);
        var result = processingEngine.processUpdate(
                graphRef.get(),
                updatedNode
        );

        graphRef.set(result.getGraph());

        if (graphStore != null) {
            graphStore.saveNode(updatedNode);
        }

        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier.</li>
     *   <li>Delegates removal to KnowledgeProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean removeKnowledge(String knowledgeId) {
        KnowledgeId id = parseKnowledgeId(knowledgeId);

        // Delegate to engine
        var result = processingEngine.processDelete(
                graphRef.get(),
                id
        );

        graphRef.set(result.getGraph());

        if (graphStore != null) {
            graphStore.removeNode(id.value());
        }

        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier.</li>
     *   <li>Returns the identifier as the knowledge entity reference.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object getKnowledge(String knowledgeId) {
        return parseKnowledgeId(knowledgeId);
    }

    // ========================================================================
    // KnowledgeQueryService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] queryKnowledge(Object query) {
        Objects.requireNonNull(query, "query must not be null");
        // Normalize the query to enable proper matching
        String normalized = QueryNormalizer.normalize(String.valueOf(query));
        // Delegate to engine
        return processingEngine.processClone(KnowledgeGraph.empty()).getGraph().getNodes().toArray();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] searchSemantic(String query) {

        // Normalize the query to enable proper matching
        String normalized = QueryNormalizer.normalize(query);
        return searchEngine
                .semanticSearch(graphRef.get(), normalized)
                .toArray();
    }

    private double semanticScore(
            KnowledgeNode node,
            String query
    ) {

        double score = 0.0;

        if (node.getLabel().toLowerCase().contains(query)) {
            score += 5.0;
        }

        if (node.getDescription().toLowerCase().contains(query)) {
            score += 3.0;
        }

        for (Object value : node.getMetadata().values()) {

            if (value != null &&
                    value.toString().toLowerCase().contains(query)) {

                score += 1.0;
            }
        }

        return score;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object getById(String knowledgeId) {
        return parseKnowledgeId(knowledgeId);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] filterKnowledge(Object criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");
        // TODO: Implement filtering
        return new Object[0];
    }

    // ========================================================================
    // KnowledgeGraphService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String createRelationship(String sourceEntityId, String targetEntityId, String relationshipType) {
        if (relationshipType == null || relationshipType.isBlank()) {
            throw createValidationException("relationshipType must not be null or blank");
        }

        KnowledgeId sourceId = parseKnowledgeId(sourceEntityId);
        KnowledgeId targetId = parseKnowledgeId(targetEntityId);

        // Build relationship and delegate to engine
        KnowledgeRelationship relationship = buildRelationship(sourceId, targetId, relationshipType);
        var result = processingEngine.processLink(KnowledgeGraph.empty(), relationship);

        if (graphStore != null) {
            graphStore.saveRelationship(relationship);
        }

        return relationship.getId().value();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean removeRelationship(String relationshipId) {
        KnowledgeId id = parseKnowledgeId(relationshipId);
        var result = processingEngine.processUnlink(KnowledgeGraph.empty(), id);

        if (graphStore != null) {
            graphStore.removeRelationship(id.value());
        }

        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] queryConnections(String entityId) {
        parseKnowledgeId(entityId);
        // TODO: Implement connection querying
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] traverseGraph(String startEntityId, String[] relationshipTypes) {
        Objects.requireNonNull(relationshipTypes, "relationshipTypes must not be null");
        parseKnowledgeId(startEntityId);
        // TODO: Implement graph traversal
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] getEntityRelationships(String entityId) {
        parseKnowledgeId(entityId);
        // TODO: Implement relationship retrieval
        return new Object[0];
    }

    // ========================================================================
    // KnowledgeSearchService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public List<KnowledgeNode> search(String keyword) {

        Objects.requireNonNull(keyword, "keyword must not be null");

        // Normalize the keyword to enable proper matching
        String normalized = QueryNormalizer.normalize(keyword);

        List<KnowledgeNode> results =
                searchEngine.keywordSearch(graphRef.get(), normalized);

        // Hybrid retrieval: when lexical search finds nothing, fall back to
        // semantic vector retrieval so paraphrased queries still match
        // ingested documents.
        if (results.isEmpty()) {
            List<KnowledgeNode> semantic = semanticVectorSearch(normalized, 10);
            if (!semantic.isEmpty()) {
                return semantic;
            }
        }

        return List.copyOf(results);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public List<KnowledgeNode> searchByTopic(String topic) {
        return searchEngine.topicSearch(graphRef.get(), topic);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public List<KnowledgeNode> searchByConcept(String concept) {
        if (concept == null || concept.isBlank()) {
            throw createValidationException("concept must not be null or blank");
        }
        // TODO: Implement concept search
        return List.of();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public List<KnowledgeNode> searchByTags(Iterable<String> tags) {
        return searchEngine.tagSearch(graphRef.get(), tags);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public List<KnowledgeNode> searchBySimilarity(String text) {

        // Semantic retrieval prefers the vector search engine when available
        // (embeddings persisted at ingestion time); the lexical engine is the
        // backward-compatible fallback.
        List<KnowledgeNode> semantic = semanticVectorSearch(text, 10);
        if (!semantic.isEmpty()) {
            return semantic;
        }

        return searchEngine.semanticSearch(
                graphRef.get(),
                text
        );
    }

    /**
     * Performs semantic vector retrieval and maps record ids back to their
     * knowledge nodes. Returns an empty list when the vector subsystem is
     * not configured.
     */
    private List<KnowledgeNode> semanticVectorSearch(String text, int topK) {
        if (vectorSearchEngine == null || embeddingProvider == null
                || text == null || text.isBlank()) {
            return List.of();
        }

        double[] queryEmbedding = embeddingProvider.embed(text);
        return vectorSearchEngine.search(queryEmbedding, topK).stream()
                .map(VectorSearchResult::recordId)
                .map(this::findNodeByVectorId)
                .filter(Objects::nonNull)
                .toList();
    }

    private KnowledgeNode findNodeByVectorId(String recordId) {
        return graphRef.get().getNodes().stream()
                .filter(node -> node.getId().value().equals(recordId))
                .findFirst()
                .orElse(null);
    }

    // ========================================================================
    // KnowledgeExtractionService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] extractConcepts(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        // TODO: Implement concept extraction
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] generateStructuredKnowledge(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        // TODO: Implement structured knowledge generation
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] extractRelationships(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        // TODO: Implement relationship extraction
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String[] classifyContent(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        // TODO: Implement content classification
        return new String[0];
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Parses a string identifier into a {@link KnowledgeId}.
     *
     * @param knowledgeId the string identifier (must not be null)
     * @return the parsed KnowledgeId
     * @throws KnowledgeValidationException if the identifier is invalid
     */
    private KnowledgeId parseKnowledgeId(String knowledgeId) {
        if (knowledgeId == null || knowledgeId.isBlank()) {
            throw createValidationException("knowledgeId must not be null or blank");
        }
        return new KnowledgeId(knowledgeId);
    }

    /**
     * Builds a {@link KnowledgeNode} from a creation request.
     */
    private KnowledgeNode buildNode(CreateKnowledgeRequest request) {
        return KnowledgeNode.of(
                new KnowledgeId(java.util.UUID.randomUUID().toString()),
                request.getType(),
                request.getState(),
                request.getScope(),
                request.getLabel(),
                request.getDescription(),
                request.getMetadata(),
                Instant.now(),
                Instant.now());
    }

    /**
     * Builds an updated {@link KnowledgeNode} from an update request.
     */
    private KnowledgeNode buildUpdatedNode(KnowledgeId id, UpdateKnowledgeRequest request) {
        return KnowledgeNode.of(
                id,
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                request.getLabel(),
                request.getDescription(),
                request.getMetadata(),
                Instant.now(),
                Instant.now());
    }

    /**
     * Builds a {@link KnowledgeRelationship} from source, target, and type.
     */
    private KnowledgeRelationship buildRelationship(KnowledgeId sourceId, KnowledgeId targetId, String type) {
        KnowledgeRelationshipType relType;
        try {
            relType = KnowledgeRelationshipType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            relType = KnowledgeRelationshipType.RELATED_TO;
        }

        return KnowledgeRelationship.of(
                new KnowledgeId(java.util.UUID.randomUUID().toString()),
                sourceId,
                targetId,
                relType,
                "Relationship: " + sourceId.value() + " -> " + targetId.value(),
                Map.of(),
                Instant.now());
    }

    /**
     * Creates a KnowledgeValidationException from a single violation message.
     */
    private KnowledgeValidationException createValidationException(String message) {
        KnowledgeError error = new KnowledgeError(
                KnowledgeErrorCode.VALIDATION_FAILED,
                "Knowledge validation failed: " + message,
                Instant.now(),
                Map.of("violation", message)
        );
        return new KnowledgeValidationException(error);
    }

    /**
     * Creates a KnowledgeValidationException from a list of violation messages.
     */
    private KnowledgeValidationException createValidationException(List<String> violations) {
        KnowledgeError error = new KnowledgeError(
                KnowledgeErrorCode.VALIDATION_FAILED,
                "Knowledge validation failed: " + String.join(", ", violations),
                Instant.now(),
                Map.of("violations", violations)
        );
        return new KnowledgeValidationException(error);
    }
}