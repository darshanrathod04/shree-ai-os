package com.shreeai.os.platform.runtime.vector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>PgVectorMemoryStore</b>
 *
 * <p>PostgreSQL + pgvector adapter of {@link VectorStore}. Persists vector
 * records in table {@code shree_vector_memory} with the metadata-first
 * document schema (documentId, tenantId, embeddingVersion). Plain JDBC with
 * the {@code ?::vector} cast — no ORM.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgVectorMemoryStore implements VectorStore {

    private static final String UPSERT_SQL = """
            INSERT INTO shree_vector_memory
                (id, content, embedding, document_id, tenant_id, embedding_version, metadata)
            VALUES (?, ?, ?::vector, ?, ?, ?, ?::jsonb)
            ON CONFLICT (id)
            DO UPDATE SET content = EXCLUDED.content,
                          embedding = EXCLUDED.embedding,
                          document_id = EXCLUDED.document_id,
                          tenant_id = EXCLUDED.tenant_id,
                          embedding_version = EXCLUDED.embedding_version,
                          metadata = EXCLUDED.metadata
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, content, embedding::text, document_id, tenant_id,
                   embedding_version, metadata::text, created_at
            FROM shree_vector_memory WHERE id = ?
            """;

    private static final String SELECT_ALL = """
            SELECT id, content, embedding::text, document_id, tenant_id,
                   embedding_version, metadata::text, created_at
            FROM shree_vector_memory
            """;

    private static final String DELETE_BY_ID = "DELETE FROM shree_vector_memory WHERE id = ?";

    private final SqlConnectionSupplier connections;

    /**
     * Creates a PgVector-backed store.
     *
     * @param jdbcUrl  PostgreSQL JDBC URL (must not be null or blank)
     * @param user     database user (must not be null)
     * @param password database password (must not be null)
     */
    public PgVectorMemoryStore(String jdbcUrl, String user, String password) {
        this(PgConnections.from(jdbcUrl, user, password));
    }

    /**
     * Creates a PgVector-backed store over a connection supplier.
     *
     * @param connections supplier of JDBC connections (must not be null)
     */
    public PgVectorMemoryStore(SqlConnectionSupplier connections) {
        this.connections = java.util.Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public void store(VectorRecord record) {
        if (record == null) {
            throw new VectorRuntimeException("record must not be null");
        }
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, record.id());
            statement.setString(2, record.content());
            statement.setString(3, PgVectors.toPgVectorLiteral(record.embedding()));
            statement.setString(4, string(record.metadata().get("documentId")));
            statement.setString(5, string(record.metadata().get("tenantId")));
            statement.setString(6, string(record.metadata().get("embeddingVersion")));
            statement.setString(7, PgVectorsJson.toJson(record.metadata()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new VectorRuntimeException("Failed to store vector record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<VectorRecord> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new VectorRuntimeException("id must not be null or blank");
        }
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new VectorRuntimeException("Failed to find vector record: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VectorRecord> all() {
        List<VectorRecord> results = new ArrayList<>();
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
            return List.copyOf(results);
        } catch (SQLException e) {
            throw new VectorRuntimeException("Failed to list vector records: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.isBlank()) {
            throw new VectorRuntimeException("id must not be null or blank");
        }
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new VectorRuntimeException("Failed to delete vector record: " + e.getMessage(), e);
        }
    }

    private VectorRecord mapRow(ResultSet resultSet) throws SQLException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", resultSet.getString("document_id"));
        metadata.put("tenantId", resultSet.getString("tenant_id"));
        metadata.put("embeddingVersion", resultSet.getString("embedding_version"));
        Map<String, Object> stored = PgVectorsJson.fromJson(resultSet.getString("metadata"));
        if (stored != null) {
            metadata.putAll(stored);
        }

        java.time.Instant createdAt = resultSet.getTimestamp("created_at") != null
                ? resultSet.getTimestamp("created_at").toInstant()
                : java.time.Instant.now();

        return VectorRecord.of(
                resultSet.getString("id"),
                resultSet.getString("content"),
                PgVectors.fromPgVectorLiteral(resultSet.getString("embedding")),
                java.util.Collections.unmodifiableMap(metadata),
                createdAt);
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }
}
