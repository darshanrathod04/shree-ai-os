package com.shreeai.os.platform.runtime.vector;

/**
 * <b>VectorStoreProvider</b>
 *
 * <p>SPI that binds a {@link VectorStore} together with its matching
 * {@link VectorSearchEngine}. Introduced so the composition root selects a
 * single coherent vector backend by name (in-memory, pgvector, ...) without
 * any kernel or runtime code hard-coding a provider.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>The returned store and search engine MUST be compatible (same data
 *       set, same embedding dimensionality).</li>
 *   <li>Providers MUST be stateless or internally synchronized.</li>
 *   <li>Provider names are lowercase identifiers ({@code "in-memory"},
 *       {@code "pgvector"}).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface VectorStoreProvider {

    /**
     * Returns the provider identifier.
     *
     * @return lowercase provider name (never null or blank)
     */
    String name();

    /**
     * Returns the vector store owned by this provider.
     *
     * @return the vector store (never null)
     */
    VectorStore vectorStore();

    /**
     * Returns the search engine owned by this provider.
     *
     * @return the search engine (never null)
     */
    VectorSearchEngine searchEngine();
}
