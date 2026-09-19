package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>KnowledgeDocumentChunk</b>
 *
 * <p>One immutable, deterministically identified fragment of a canonical
 * {@link KnowledgeDocument}. This is the K2 canonical representation of a
 * document fragment; every future kernel consumes documents and their chunks
 * and never touches raw files.</p>
 *
 * <p><b>Content preservation:</b> {@code content} carries the original chunk
 * text exactly as extracted - ingestion never summarizes and never rewrites
 * content.</p>
 *
 * <p><b>Deterministic identity:</b> {@code chunkId} is derived by the
 * ingestion engine as {@code SHA-256(documentId + chunkIndex + content)} and
 * is therefore stable across identical ingestion runs.</p>
 *
 * <p><b>Note:</b> the class name deliberately differs from the legacy
 * retrieval-oriented {@code KnowledgeChunk} (EIO-KNW-101), which remains
 * untouched for backward compatibility.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param chunkId  the deterministic chunk id (never null)
 * @param content  the original chunk text, preserved verbatim (never null)
 * @param metadata the immutable provenance metadata (never null)
 */
public record KnowledgeDocumentChunk(String chunkId, String content, ChunkMetadata metadata) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException if chunkId, content or metadata is null
     */
    public KnowledgeDocumentChunk {
        Objects.requireNonNull(chunkId, "chunkId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
    }

    /**
     * Creates a validated, immutable document chunk.
     *
     * @param chunkId  deterministic chunk id (must not be null)
     * @param content  original chunk text (must not be null)
     * @param metadata provenance metadata (must not be null)
     * @return a new immutable KnowledgeDocumentChunk (never null)
     */
    public static KnowledgeDocumentChunk of(String chunkId, String content, ChunkMetadata metadata) {
        return new KnowledgeDocumentChunk(chunkId, content, metadata);
    }

    @Override
    public String toString() {
        return String.format("KnowledgeDocumentChunk{chunkId=%s, section=%s, contentLength=%d}",
                chunkId, metadata.section(), content.length());
    }
}
