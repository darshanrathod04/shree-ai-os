package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;

import java.time.Instant;
import java.util.ArrayList;
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
 * {@value #MAX_CHUNK_LENGTH} characters, preferring paragraph boundaries
 * ({@code \n\n}), then sentence boundaries, then hard splits. Consecutive
 * chunks overlap by {@value #CHUNK_OVERLAP} characters so statements near a
 * boundary remain retrievable from both chunks.</p>
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

    @Override
    public List<KnowledgeChunk> chunk(String content) {
        Objects.requireNonNull(content, "content must not be null");
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        List<String> rawSegments = splitToSegments(trimmed);
        List<KnowledgeChunk> chunks = new ArrayList<>();
        int index = 0;

        for (String segment : rawSegments) {
            if (chunks.isEmpty()) {
                chunks.add(KnowledgeChunk.of(index++, segment));
                continue;
            }

            KnowledgeChunk previous = chunks.get(chunks.size() - 1);
            String combined = previous.text() + "\n" + segment;
            if (combined.length() <= MAX_CHUNK_LENGTH) {
                // Grow the current chunk rather than replacing it.
                chunks.set(chunks.size() - 1, KnowledgeChunk.of(previous.index(), combined));
            } else {
                chunks.add(KnowledgeChunk.of(index++, segment));
            }
        }

        return List.copyOf(chunks);
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

    private List<String> splitToSegments(String content) {
        List<String> segments = new ArrayList<>();

        for (String paragraph : content.split("\\n\\n+")) {
            String paragraphTrimmed = paragraph.trim();
            if (paragraphTrimmed.isEmpty()) {
                continue;
            }
            if (paragraphTrimmed.length() <= MAX_CHUNK_LENGTH) {
                segments.add(paragraphTrimmed);
                continue;
            }
            // Oversized paragraph: split on sentence boundaries, then hard split.
            for (String sentence : paragraphTrimmed.split("(?<=\\.)\\s+")) {
                String sentenceTrimmed = sentence.trim();
                if (sentenceTrimmed.isEmpty()) {
                    continue;
                }
                if (sentenceTrimmed.length() <= MAX_CHUNK_LENGTH) {
                    segments.add(sentenceTrimmed);
                } else {
                    hardSplit(segments, sentenceTrimmed);
                }
            }
        }

        return segments;
    }

    private void hardSplit(List<String> segments, String text) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
            segments.add(text.substring(start, end));
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - CHUNK_OVERLAP, start + 1);
        }
    }
}
