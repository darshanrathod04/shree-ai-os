package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeIngestionResult</b>
 *
 * <p>Immutable result of a document ingestion. Follows the metadata-first
 * document schema: the returned document id, tenant id, and embedding version
 * are the primary retrieval coordinates of everything persisted by the
 * ingestion.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, PHASE-1-ARCH-001</p>
 */
public final class KnowledgeIngestionResult {

    private final String documentId;
    private final String title;
    private final String tenantId;
    private final int chunkCount;
    private final List<String> nodeIds;
    private final String embeddingVersion;

    private KnowledgeIngestionResult(
            String documentId,
            String title,
            String tenantId,
            int chunkCount,
            List<String> nodeIds,
            String embeddingVersion) {
        this.documentId = documentId;
        this.title = title;
        this.tenantId = tenantId;
        this.chunkCount = chunkCount;
        this.nodeIds = nodeIds;
        this.embeddingVersion = embeddingVersion;
    }

    /**
     * Creates a new immutable ingestion result.
     *
     * @param documentId       unique document identifier (must not be null or blank)
     * @param title            document title (must not be null or blank)
     * @param tenantId         tenant identifier (must not be null or blank)
     * @param chunkCount       number of persisted chunks (must be &gt;= 0)
     * @param nodeIds          ids of the created knowledge nodes (must not be null)
     * @param embeddingVersion embedding provider version used (may be null when
     *                         no embedding provider is configured)
     * @return a new immutable result
     */
    public static KnowledgeIngestionResult of(
            String documentId,
            String title,
            String tenantId,
            int chunkCount,
            List<String> nodeIds,
            String embeddingVersion) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        if (documentId.isBlank()) {
            throw new IllegalArgumentException("documentId must not be blank");
        }
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(nodeIds, "nodeIds must not be null");

        return new KnowledgeIngestionResult(
                documentId,
                title,
                tenantId,
                chunkCount,
                List.copyOf(nodeIds),
                embeddingVersion);
    }

    /** Returns the unique document identifier (never null). */
    public String getDocumentId() {
        return documentId;
    }

    /** Returns the document title (never null). */
    public String getTitle() {
        return title;
    }

    /** Returns the tenant identifier (never null). */
    public String getTenantId() {
        return tenantId;
    }

    /** Returns the number of persisted chunks. */
    public int getChunkCount() {
        return chunkCount;
    }

    /** Returns the ids of the created knowledge nodes (never null). */
    public List<String> getNodeIds() {
        return nodeIds;
    }

    /** Returns the embedding version used, or null when embeddings are disabled. */
    public String getEmbeddingVersion() {
        return embeddingVersion;
    }
}
