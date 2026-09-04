/**
 * <b>Runtime — Vector Subsystem</b>
 *
 * <p>Provider-neutral vector storage and semantic search infrastructure.
 * The knowledge and memory kernels depend only on the SPIs
 * ({@link com.shreeai.os.platform.runtime.vector.VectorStore},
 * {@link com.shreeai.os.platform.runtime.vector.VectorSearchEngine},
 * {@link com.shreeai.os.platform.runtime.vector.VectorStoreProvider});
 * concrete backends (in-JVM, PostgreSQL + pgvector) plug in via configuration.
 * Embedding production lives in the separate {@code runtime.embedding}
 * subsystem.</p>
 *
 * <p><b>Components:</b></p>
 * <pre>
 * ├── VectorStore / VectorSearchEngine / VectorStoreProvider — SPIs
 * ├── VectorRecord / VectorSearchResult  — immutable value objects
 * ├── CosineSimilarity                   — the single canonical similarity impl
 * ├── InMemoryVectorStoreProvider        — default backend (zero infrastructure)
 * ├── PgVectorMemoryStore                — PostgreSQL + pgvector persistence
 * ├── PgVectorSearchEngine               — pgvector KNN (&lt;=&gt; operator)
 * ├── PgVectorStoreProvider / VectorStoreProviders — provider binding + config
 * └── PgConnections / PgVectors / PgVectorsJson / SqlConnectionSupplier
 * </pre>
 *
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
package com.shreeai.os.platform.runtime.vector;
