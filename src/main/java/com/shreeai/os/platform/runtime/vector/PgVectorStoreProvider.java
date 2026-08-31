package com.shreeai.os.platform.runtime.vector;

import java.sql.Connection;
import java.sql.Statement;

/**
 * <b>PgVectorStoreProvider</b>
 *
 * <p>PostgreSQL + pgvector {@link VectorStoreProvider}. Binds
 * {@link PgVectorMemoryStore} with {@link PgVectorSearchEngine} over a shared
 * connection supplier and owns schema self-provisioning
 * ({@code CREATE TABLE IF NOT EXISTS ... vector(dim)} + HNSW index).</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgVectorStoreProvider implements VectorStoreProvider {

    /** Provider identifier used by configuration. */
    public static final String NAME = "pgvector";

    private final PgVectorMemoryStore store;
    private final PgVectorSearchEngine searchEngine;
    private final SqlConnectionSupplier connections;
    private final int dimensions;

    /**
     * Creates a PgVector provider.
     *
     * @param jdbcUrl    PostgreSQL JDBC URL (must not be null or blank)
     * @param user       database user (must not be null)
     * @param password   database password (must not be null)
     * @param dimensions embedding dimension; must match the vector column type
     */
    public PgVectorStoreProvider(String jdbcUrl, String user, String password, int dimensions) {
        this(PgConnections.from(jdbcUrl, user, password), dimensions);
    }

    /**
     * Creates a PgVector provider over a connection supplier.
     *
     * @param connections supplier of JDBC connections (must not be null)
     * @param dimensions  embedding dimension; must match the vector column type
     */
    public PgVectorStoreProvider(SqlConnectionSupplier connections, int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        this.connections = java.util.Objects.requireNonNull(connections, "connections must not be null");
        this.dimensions = dimensions;
        this.store = new PgVectorMemoryStore(connections);
        this.searchEngine = new PgVectorSearchEngine(connections);
    }

    /**
     * Self-provisions the vector schema. Safe to call repeatedly
     * ({@code IF NOT EXISTS}). The HNSW index is created best-effort: it
     * requires the {@code vector} extension and a compatible PostgreSQL
     * version, but its absence only degrades performance, not correctness.
     *
     * @throws VectorRuntimeException if the table cannot be created
     */
    public void ensureSchema() {
        String tableSql = """
                CREATE TABLE IF NOT EXISTS shree_vector_memory (
                    id TEXT PRIMARY KEY,
                    content TEXT NOT NULL,
                    embedding vector(%d),
                    document_id TEXT,
                    tenant_id TEXT,
                    embedding_version TEXT,
                    metadata JSONB,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                )
                """.formatted(dimensions);
        String indexSql = """
                CREATE INDEX IF NOT EXISTS shree_vector_memory_embedding_idx
                ON shree_vector_memory USING hnsw (embedding vector_cosine_ops)
                """;

        try (Connection connection = connections.get();
             Statement statement = connection.createStatement()) {
            statement.execute(tableSql);
            try {
                statement.execute(indexSql);
            } catch (Exception tolerated) {
                // HNSW index is an optimization; correctness never depends on it.
            }
        } catch (Exception e) {
            throw new VectorRuntimeException(
                    "Failed to provision pgvector schema: " + e.getMessage(), e);
        }
    }

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
