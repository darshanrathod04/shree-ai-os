/**
 * <b>Runtime — Storage Subsystem</b>
 *
 * <p>Persistence ports and pluggable adapters for durable platform state.
 * The knowledge kernel depends only on the
 * {@link com.shreeai.os.platform.runtime.storage.KnowledgeGraphStore} SPI
 * (behind the frozen {@code KnowledgeGraphService} abstraction); concrete
 * stores (in-JVM, Neo4j) are selected via configuration — never hard-coded.
 * PostgreSQL-backed embedding persistence lives here as well
 * ({@code PgEmbeddingRepository}, implementing the {@code runtime.embedding}
 * SPI).</p>
 *
 * <p><b>Components:</b></p>
 * <pre>
 * ├── KnowledgeGraphStore          — SPI: durable knowledge graph state
 * ├── InMemoryKnowledgeGraphStore  — default backend (zero infrastructure)
 * ├── Neo4jKnowledgeGraphAdapter   — Neo4j driver adapter (lazy init)
 * ├── KnowledgeGraphStores         — configuration-driven selection
 * ├── PgEmbeddingRepository        — PostgreSQL + pgvector embedding persistence
 * └── StorageRuntimeException      — failure translation type
 * </pre>
 *
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
package com.shreeai.os.platform.runtime.storage;
