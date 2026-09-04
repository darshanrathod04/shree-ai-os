package com.shreeai.os.platform.runtime.vector;

/**
 * <b>InMemoryVectorStoreProvider</b>
 *
 * <p>Default {@link VectorStoreProvider}: thread-safe in-JVM store plus
 * brute-force cosine search. Used whenever no database is configured —
 * guarantees the platform runs with zero infrastructure.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class InMemoryVectorStoreProvider implements VectorStoreProvider {

    /** Provider identifier used by configuration. */
    public static final String NAME = "in-memory";

    private final InMemoryVectorStore store = new InMemoryVectorStore();
    private final InMemoryVectorSearchEngine searchEngine = new InMemoryVectorSearchEngine(store);

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public VectorStore vectorStore() {
        return store;
    }

    @Override
    public VectorSearchEngine searchEngine() {
        return searchEngine;
    }
}
