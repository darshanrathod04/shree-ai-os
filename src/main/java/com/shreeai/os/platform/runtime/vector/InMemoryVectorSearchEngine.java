package com.shreeai.os.platform.runtime.vector;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>InMemoryVectorSearchEngine</b>
 *
 * <p>Brute-force cosine top-K {@link VectorSearchEngine} over any
 * {@link VectorStore}. Correct and fast enough for local and test profiles;
 * PostgreSQL + pgvector takes over KNN for production scale.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class InMemoryVectorSearchEngine implements VectorSearchEngine {

    private final VectorStore store;

    /**
     * Creates an engine scanning the given store.
     *
     * @param store the vector store to scan (must not be null)
     */
    public InMemoryVectorSearchEngine(VectorStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public List<VectorSearchResult> search(double[] queryEmbedding, int topK) {
        if (queryEmbedding == null) {
            throw new VectorRuntimeException("queryEmbedding must not be null");
        }
        if (topK <= 0) {
            throw new VectorRuntimeException("topK must be positive");
        }

        return store.all().stream()
                .map(record -> VectorSearchResult.of(
                        record.id(),
                        CosineSimilarity.of(record.embedding(), queryEmbedding),
                        record.content(),
                        record.metadata()))
                .filter(result -> result.score() > 0.0)
                .sorted(Comparator.comparingDouble(VectorSearchResult::score).reversed())
                .limit(topK)
                .toList();
    }
}
