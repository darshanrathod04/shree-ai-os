package com.shreeai.os.platform.runtime.vector;

import java.util.List;

/**
 * <b>VectorSearchEngine</b>
 *
 * <p>SPI for top-K cosine similarity search over a {@link VectorStore}.
 * Implementations are pluggable adapters (brute-force in-JVM, pgvector
 * {@code <=>} KNN, ...) selected via {@link VectorStoreProvider}.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Results MUST be ordered by descending similarity score.</li>
 *   <li>At most {@code topK} results MUST be returned.</li>
 *   <li>Implementations MUST be thread-safe.</li>
 *   <li>Failures MUST be translated into {@link VectorRuntimeException}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface VectorSearchEngine {

    /**
     * Performs a top-K nearest-neighbour search.
     *
     * @param queryEmbedding the query embedding (must not be null)
     * @param topK           maximum number of results (must be &gt; 0)
     * @return results ordered by descending score (never null; may be empty)
     * @throws VectorRuntimeException if the search fails
     */
    List<VectorSearchResult> search(double[] queryEmbedding, int topK);
}
