package com.shreeai.os.platform.kernels.knowledge;

import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeIngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.runtime.vector.VectorSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test: ingested documents become permanently searchable through
 * both the lexical {@code search} contract and the semantic
 * {@code searchBySimilarity} contract.
 */
class KnowledgeIngestionRetrievalTest {

    private static final String DOCUMENT =
            "Postgres database replication streams the write-ahead log to a hot standby. "
                    + "Streaming replication keeps the standby ready for failover at any time.";

    @Test
    void ingestedDocumentIsLexicallySearchable() {
        DefaultKnowledgeService service = DefaultKnowledgeService.withInMemoryDefaults(
                new DefaultKnowledgeProcessingEngine());

        KnowledgeIngestionResult result = service.ingest(
                "Postgres Replication", DOCUMENT, Map.of("tenantId", "acme"));

        assertEquals(1, result.getChunkCount());
        assertFalse(result.getNodeIds().isEmpty());

        List<KnowledgeNode> hits = service.search("Postgres");
        assertFalse(hits.isEmpty(), "ingested document must be searchable");
        assertEquals(result.getNodeIds().getFirst(), hits.getFirst().getId().value());
    }

    @Test
    void ingestedDocumentIsSemanticallySearchable() {
        DefaultKnowledgeService service = DefaultKnowledgeService.withInMemoryDefaults(
                new DefaultKnowledgeProcessingEngine());

        KnowledgeIngestionResult result = service.ingest(
                "Postgres Replication", DOCUMENT, null);

        List<KnowledgeNode> hits = service.searchBySimilarity("database replication failover");
        assertFalse(hits.isEmpty(), "semantic retrieval must return the ingested chunk");
        assertEquals(result.getNodeIds().getFirst(), hits.getFirst().getId().value());
    }

    @Test
    void vectorRecordCarriesMetadataFirstSchema() {
        com.shreeai.os.platform.runtime.vector.InMemoryVectorStore store =
                new com.shreeai.os.platform.runtime.vector.InMemoryVectorStore();
        com.shreeai.os.platform.runtime.vector.InMemoryVectorSearchEngine searchEngine =
                new com.shreeai.os.platform.runtime.vector.InMemoryVectorSearchEngine(store);
        com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder embedder =
                new com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder();

        DefaultKnowledgeService service = new DefaultKnowledgeService(
                new DefaultKnowledgeProcessingEngine(),
                new com.shreeai.os.platform.runtime.storage.InMemoryKnowledgeGraphStore(),
                store,
                searchEngine,
                embedder);

        KnowledgeIngestionResult result = service.ingest(
                "Postgres Replication", DOCUMENT, Map.of("tenantId", "acme"));

        List<VectorSearchResult> vectorHits =
                searchEngine.search(embedder.embed("replication"), 10);

        assertFalse(vectorHits.isEmpty());
        VectorSearchResult hit = vectorHits.getFirst();
        assertEquals(result.getDocumentId(), hit.metadata().get("documentId"));
        assertEquals("acme", hit.metadata().get("tenantId"));
        assertEquals(com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder.VERSION,
                hit.metadata().get("embeddingVersion"));
        assertEquals(result.getNodeIds().getFirst(), hit.recordId());
    }
}
