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
     * ({@code IF NOT EXISTS}). The HNSW and GIN indexes are created
     * best-effort: they require the {@code vector} extension and a compatible
     * PostgreSQL version, but their absence only degrades performance, not
     * correctness.
     *
     * <p>Also provisions the {@code content_tsv} tsvector column, GIN index,
     * and the trigger that keeps it in sync with {@code content} so the
     * hybrid (vector + full-text) search can run end-to-end.</p>
     *
     * @throws VectorRuntimeException if the table cannot be created
     */
    public void ensureSchema() {
        String tableSql = """
                CREATE TABLE IF NOT EXISTS shree_vector_memory (
                    id TEXT PRIMARY KEY,
                    content TEXT NOT NULL,
                    embedding vector(%d),
                    content_tsv tsvector,
                    document_id TEXT,
                    tenant_id TEXT,
                    embedding_version TEXT,
                    metadata JSONB,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                )
                """.formatted(dimensions);
        String addColumnSql = """
                ALTER TABLE shree_vector_memory
                ADD COLUMN IF NOT EXISTS content_tsv tsvector
                """;
        String hnswIndexSql = """
                CREATE INDEX IF NOT EXISTS shree_vector_memory_embedding_idx
                ON shree_vector_memory USING hnsw (embedding vector_cosine_ops)
                """;
        String ginIndexSql = """
                CREATE INDEX IF NOT EXISTS shree_vector_memory_tsv_idx
                ON shree_vector_memory USING gin (content_tsv)
                """;
        String tsvFunctionSql = """
                CREATE OR REPLACE FUNCTION shree_vector_memory_tsv_update()
                RETURNS trigger AS $$
                BEGIN
                    NEW.content_tsv := to_tsvector('english', coalesce(NEW.content, ''));
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """;
        String tsvTriggerSql = """
                DROP TRIGGER IF EXISTS shree_vector_memory_tsv_trigger
                    ON shree_vector_memory
                """;
        String tsvTriggerCreateSql = """
                CREATE TRIGGER shree_vector_memory_tsv_trigger
                    BEFORE INSERT OR UPDATE OF content
                    ON shree_vector_memory
                    FOR EACH ROW EXECUTE FUNCTION shree_vector_memory_tsv_update()
                """;
        String tsvBackfillSql = """
                UPDATE shree_vector_memory
                   SET content_tsv = to_tsvector('english', content)
                 WHERE content_tsv IS NULL
                """;

        try (Connection connection = connections.get();
             Statement statement = connection.createStatement()) {

            // 1. Core table — idempotent.
            statement.execute(tableSql);

            // 2. content_tsv column — add if missing (handles pre-existing tables).
            try {
                statement.execute(addColumnSql);
            } catch (Exception tolerated) {
                // Column may already exist; ignore.
            }

            // 3. HNSW index for cosine KNN.
            try {
                statement.execute(hnswIndexSql);
            } catch (Exception tolerated) {
                // HNSW index is an optimization; correctness never depends on it.
            }

            // 4. GIN index over content_tsv for full-text search.
            try {
                statement.execute(ginIndexSql);
            } catch (Exception tolerated) {
                // GIN index is an optimization; ignore.
            }

            // 5. Trigger function + trigger that auto-populate content_tsv.
            try {
                statement.execute(tsvFunctionSql);
            } catch (Exception tolerated) {
                // Function may already exist with different body; ignore.
            }
            try {
                statement.execute(tsvTriggerSql);
                statement.execute(tsvTriggerCreateSql);
            } catch (Exception tolerated) {
                // Trigger may already exist; ignore.
            }

            // 6. Backfill NULL content_tsv for rows inserted before the trigger.
            try {
                statement.execute(tsvBackfillSql);
            } catch (Exception tolerated) {
                // No rows to backfill, or table empty; ignore.
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
