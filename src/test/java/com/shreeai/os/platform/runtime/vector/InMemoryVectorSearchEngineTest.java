package com.shreeai.os.platform.runtime.vector;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the brute-force cosine {@link InMemoryVectorSearchEngine}.
 */
class InMemoryVectorSearchEngineTest {

    @Test
    void resultsAreOrderedByDescendingScore() {
        InMemoryVectorStore store = new InMemoryVectorStore();

        store.store(VectorRecord.of("near", "near", new double[]{1, 0}, Map.of()));
        store.store(VectorRecord.of("far", "far", new double[]{-1, 0}, Map.of()));
        store.store(VectorRecord.of("middle", "middle", new double[]{0.7071067811865476, 0.7071067811865476}, Map.of()));

        InMemoryVectorSearchEngine engine = new InMemoryVectorSearchEngine(store);
        List<VectorSearchResult> results = engine.search(new double[]{1, 0}, 3);

        // "far" is negatively similar and filtered out by the score > 0 rule.
        assertEquals(2, results.size());
        assertEquals("near", results.get(0).recordId());
        assertEquals("middle", results.get(1).recordId());
        assertTrue(results.get(0).score() > results.get(1).score());
    }

    @Test
    void topKLimitsResultCount() {
        InMemoryVectorStore store = new InMemoryVectorStore();
        for (int i = 0; i < 20; i++) {
            store.store(VectorRecord.of("id-" + i, "content-" + i,
                    new double[]{Math.cos(i), Math.sin(i)}, Map.of()));
        }

        InMemoryVectorSearchEngine engine = new InMemoryVectorSearchEngine(store);
        assertEquals(5, engine.search(new double[]{1, 0}, 5).size());
    }

    @Test
    void metadataSurvivesTheRoundTrip() {
        InMemoryVectorStore store = new InMemoryVectorStore();
        store.store(VectorRecord.of("id-1", "content", new double[]{1, 0}, Map.of(
                "documentId", "doc-9",
                "tenantId", "tenant-7",
                "embeddingVersion", "local-deterministic-v1")));

        VectorSearchResult result = new InMemoryVectorSearchEngine(store).search(new double[]{1, 0}, 1).getFirst();

        assertEquals("doc-9", result.metadata().get("documentId"));
        assertEquals("tenant-7", result.metadata().get("tenantId"));
        assertEquals("local-deterministic-v1", result.metadata().get("embeddingVersion"));
    }
}
