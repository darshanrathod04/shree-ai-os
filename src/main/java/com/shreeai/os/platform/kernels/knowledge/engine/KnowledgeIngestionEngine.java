package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.List;
import java.util.Map;

/**
 * <b>KnowledgeIngestionEngine</b>
 *
 * <p>Pure processing contract for document ingestion — no persistence, no
 * orchestration. Splits documents into retrieval-stable chunks and builds the
 * immutable knowledge node representing each chunk. State transitions and
 * persistence are the responsibility of the service layer
 * ({@code DefaultKnowledgeService}); the engine stays stateless and
 * deterministic per the kernel engine principles.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-106, PHASE-1-ARCH-001</p>
 */
public interface KnowledgeIngestionEngine {

    /**
     * Splits document content into retrieval-stable chunks.
     *
     * @param content the document content (must not be null)
     * @return an immutable list of chunks (never null; empty only when the
     *         content is blank)
     */
    List<KnowledgeChunk> chunk(String content);

    /**
     * Builds the knowledge node representing a single chunk.
     *
     * @param documentId       the ingestion document id (must not be null or blank)
     * @param title            the document title (must not be null or blank)
     * @param tenantId         the tenant identifier (must not be null or blank)
     * @param chunk            the chunk to represent (must not be null)
     * @param embeddingVersion the embedding version used (may be null)
     * @param callerMetadata   caller-supplied metadata (may be null)
     * @param nodeId           the node id to assign (must not be null)
     * @return a new immutable knowledge node
     */
    KnowledgeNode toNode(
            String documentId,
            String title,
            String tenantId,
            KnowledgeChunk chunk,
            String embeddingVersion,
            Map<String, Object> callerMetadata,
            KnowledgeId nodeId);
}
