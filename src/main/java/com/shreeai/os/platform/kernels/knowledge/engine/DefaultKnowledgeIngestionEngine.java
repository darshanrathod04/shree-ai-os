package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.chunking.DocumentChunker;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultKnowledgeIngestionEngine</b>
 *
 * <p>Default implementation of {@link KnowledgeIngestionEngine}.</p>
 *
 * <p><b>Chunking strategy:</b> content is split into chunks of at most
 * {@value #MAX_CHUNK_LENGTH} characters, respecting sentence boundaries
 * (. ! ? \n). Consecutive chunks overlap by {@value #CHUNK_OVERLAP}
 * characters so statements near a boundary remain retrievable from both
 * chunks. The actual chunking logic is delegated to {@link DocumentChunker},
 * which enforces sentence-boundary awareness and prevents cutting words
 * midway during overlap.</p>
 *
 * <p><b>Node metadata (metadata-first schema):</b> every chunk node carries
 * {@code documentId}, {@code tenantId}, {@code title}, {@code chunkIndex},
 * {@code embeddingVersion}, {@code source}, and full-strength
 * {@code confidence}/{@code authority} (ingested documents are first-class
 * evidence for semantic grounding).</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-106, PHASE-1-ARCH-001</p>
 */
public final class DefaultKnowledgeIngestionEngine implements KnowledgeIngestionEngine {

    /** Maximum characters per chunk. */
    public static final int MAX_CHUNK_LENGTH = 600;

    /** Characters of overlap between consecutive chunks. */
    public static final int CHUNK_OVERLAP = 80;

    /** Confidence assigned to ingested document evidence. */
    public static final double INGESTED_CONFIDENCE = 1.0;

    /** Authority assigned to ingested document evidence. */
    public static final double INGESTED_AUTHORITY = 1.0;

    /** Metadata source marker for ingestion-created nodes. */
    public static final String SOURCE_DOCUMENT_INGESTION = "DOCUMENT_INGESTION";

    private final DocumentChunker chunker = new DocumentChunker(MAX_CHUNK_LENGTH, CHUNK_OVERLAP);

    @Override
    public List<KnowledgeChunk> chunk(String content) {
        Objects.requireNonNull(content, "content must not be null");
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        // DocumentChunker produces TextChunk records; map each to a KnowledgeChunk.
        return chunker.chunk(trimmed).stream()
                .map(textChunk -> KnowledgeChunk.of(textChunk.index(), textChunk.text()))
                .toList();
    }

    @Override
    public KnowledgeNode toNode(
            String documentId,
            String title,
            String tenantId,
            KnowledgeChunk chunk,
            String embeddingVersion,
            Map<String, Object> callerMetadata,
            KnowledgeId nodeId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(chunk, "chunk must not be null");
        Objects.requireNonNull(nodeId, "nodeId must not be null");

        Map<String, Object> metadata = new LinkedHashMap<>();
        if (callerMetadata != null) {
            metadata.putAll(callerMetadata);
        }
        metadata.put("documentId", documentId);
        metadata.put("tenantId", tenantId);
        metadata.put("title", title);
        metadata.put("chunkIndex", chunk.index());
        metadata.put("embeddingVersion", embeddingVersion);
        metadata.put("source", SOURCE_DOCUMENT_INGESTION);
        metadata.put("confidence", INGESTED_CONFIDENCE);
        metadata.put("authority", INGESTED_AUTHORITY);

        String label = chunk.index() == 0
                ? title
                : title + " [chunk " + (chunk.index() + 1) + "]";

        return KnowledgeNode.of(
                nodeId,
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                label,
                chunk.text(),
                metadata,
                Instant.now(),
                Instant.now());
    }
}
