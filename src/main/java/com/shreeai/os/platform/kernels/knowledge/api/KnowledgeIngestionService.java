package com.shreeai.os.platform.kernels.knowledge.api;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeIngestionResult;

import java.util.Map;

/**
 * <b>KnowledgeIngestionService</b>
 *
 * <p>Additive port of the Knowledge Kernel for permanent document ingestion.
 * Ingested documents are chunked, embedded (when an embedding provider is
 * configured), and persisted so they become <b>permanently searchable</b>
 * through the existing {@link KnowledgeSearchService} and
 * {@link KnowledgeQueryService} contracts.</p>
 *
 * <p><b>Metadata-first document schema.</b> The caller MAY supply a metadata
 * map carrying at least:</p>
 * <ul>
 *   <li>{@code tenantId} — tenant isolation key (defaults to {@code "default"};</li>
 *       full enforcement arrives with Phase 2 multi-tenancy)</li>
 *   <li>{@code source} — free-form provenance marker</li>
 * </ul>
 * The kernel enriches every persisted artifact with {@code documentId},
 * {@code tenantId}, {@code chunkIndex}, and {@code embeddingVersion}.</p>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, PHASE-1-ARCH-001</p>
 *
 * @see KnowledgeSearchService
 * @see KnowledgeService
 */
public interface KnowledgeIngestionService {

    /**
     * Ingests a document permanently.
     *
     * @param title    document title (must not be null or blank)
     * @param content  document content (must not be null or blank)
     * @param metadata optional metadata map ({@code tenantId}, {@code source}, ...);
     *                 may be null or empty
     * @return an immutable ingestion result (never null)
     * @throws IllegalArgumentException if title or content is null or blank
     */
    KnowledgeIngestionResult ingest(String title, String content, Map<String, Object> metadata);
}
