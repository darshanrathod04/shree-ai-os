package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>KnowledgeChunk</b>
 *
 * <p>Immutable unit of a chunked ingestion document: one chunk becomes exactly
 * one knowledge node and one vector record, so retrieval granularity is stable
 * and predictable.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, PHASE-1-ARCH-001</p>
 */
public final class KnowledgeChunk {

    private final int index;
    private final String text;

    private KnowledgeChunk(int index, String text) {
        this.index = index;
        this.text = text;
    }

    /**
     * Creates a new immutable chunk.
     *
     * @param index zero-based position inside the document (must be &gt;= 0)
     * @param text  chunk text (must not be null or blank)
     * @return a new immutable chunk
     */
    public static KnowledgeChunk of(int index, String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("chunk text must not be blank");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        return new KnowledgeChunk(index, text);
    }

    /** Returns the zero-based chunk position inside the document. */
    public int index() {
        return index;
    }

    /** Returns the chunk text (never null). */
    public String text() {
        return text;
    }
}
