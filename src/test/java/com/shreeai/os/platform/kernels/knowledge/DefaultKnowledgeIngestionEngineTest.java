package com.shreeai.os.platform.kernels.knowledge;

import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeChunk;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for chunking and node construction of the ingestion engine.
 */
class DefaultKnowledgeIngestionEngineTest {

    private final DefaultKnowledgeIngestionEngine engine = new DefaultKnowledgeIngestionEngine();

    @Test
    void blankContentProducesNoChunks() {
        assertTrue(engine.chunk("   ").isEmpty());
    }

    @Test
    void shortContentIsASingleChunk() {
        List<KnowledgeChunk> chunks = engine.chunk("Postgres replication streams WAL to replicas.");
        assertEquals(1, chunks.size());
        assertEquals(0, chunks.getFirst().index());
    }

    @Test
    void paragraphsArePreservedWhenTheyFit() {
        List<KnowledgeChunk> chunks = engine.chunk("First paragraph.\n\nSecond paragraph.");
        assertEquals(1, chunks.size());
        assertTrue(chunks.getFirst().text().contains("First paragraph."));
        assertTrue(chunks.getFirst().text().contains("Second paragraph."));
    }

    @Test
    void oversizedContentIsChunkedWithinLimit() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            content.append("Sentence number ").append(i)
                    .append(" discusses database replication and failover mechanics.\n\n");
        }

        List<KnowledgeChunk> chunks = engine.chunk(content.toString());
        assertTrue(chunks.size() > 1);
        for (KnowledgeChunk chunk : chunks) {
            assertTrue(chunk.text().length() <= DefaultKnowledgeIngestionEngine.MAX_CHUNK_LENGTH);
        }
    }

    @Test
    void chunkNodeCarriesMetadataFirstSchema() {
        KnowledgeChunk chunk = KnowledgeChunk.of(2, "Replication keeps a hot standby in sync.");
        KnowledgeNode node = engine.toNode(
                "doc-123",
                "Database Replication",
                "tenant-42",
                chunk,
                "local-deterministic-v1",
                Map.of("source", "manual"),
                new KnowledgeId("node-1"));

        Map<String, Object> metadata = node.getMetadata();
        assertEquals("doc-123", metadata.get("documentId"));
        assertEquals("tenant-42", metadata.get("tenantId"));
        assertEquals("Database Replication", metadata.get("title"));
        assertEquals(2, metadata.get("chunkIndex"));
        assertEquals("local-deterministic-v1", metadata.get("embeddingVersion"));
        assertEquals(DefaultKnowledgeIngestionEngine.SOURCE_DOCUMENT_INGESTION, metadata.get("source"));
        assertEquals(1.0, ((Number) metadata.get("confidence")).doubleValue());
        assertEquals(1.0, ((Number) metadata.get("authority")).doubleValue());

        assertEquals(chunk.text(), node.getDescription());
        assertTrue(node.getLabel().contains("chunk 3"));
    }
}
