package com.shreeai.os.platform.runtime.vector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>PgVectorSearchEngine</b>
 *
 * <p>PostgreSQL + pgvector adapter of {@link VectorSearchEngine}. Delegates
 * KNN to the database using the cosine distance operator ({@code &lt;=&gt;}),
 * letting pgvector's HNSW index scale semantic retrieval to production
 * volumes. Similarity is returned as {@code 1 - cosine_distance}.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgVectorSearchEngine implements VectorSearchEngine {

    private static final String KNN_SQL = """
            SELECT id, content, 1 - (embedding <=> ?::vector) AS score, metadata::text
            FROM shree_vector_memory
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

    private final SqlConnectionSupplier connections;

    /**
     * Creates a PgVector-backed search engine.
     *
     * @param jdbcUrl  PostgreSQL JDBC URL (must not be null or blank)
     * @param user     database user (must not be null)
     * @param password database password (must not be null)
     */
    public PgVectorSearchEngine(String jdbcUrl, String user, String password) {
        this(PgConnections.from(jdbcUrl, user, password));
    }

    /**
     * Creates a PgVector-backed search engine over a connection supplier.
     *
     * @param connections supplier of JDBC connections (must not be null)
     */
    public PgVectorSearchEngine(SqlConnectionSupplier connections) {
        this.connections = java.util.Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public List<VectorSearchResult> search(double[] queryEmbedding, int topK) {
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new VectorRuntimeException("queryEmbedding must not be null or empty");
        }
        if (topK <= 0) {
            throw new VectorRuntimeException("topK must be positive");
        }

        String literal = PgVectors.toPgVectorLiteral(queryEmbedding);
        List<VectorSearchResult> results = new ArrayList<>();

        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(KNN_SQL)) {
            statement.setString(1, literal);
            statement.setString(2, literal);
            statement.setInt(3, topK);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> metadata =
                            PgVectorsJson.fromJson(resultSet.getString("metadata"));
                    if (metadata == null) {
                        metadata = new HashMap<>();
                    }
                    results.add(VectorSearchResult.of(
                            resultSet.getString("id"),
                            resultSet.getDouble("score"),
                            resultSet.getString("content"),
                            metadata));
                }
            }
            return List.copyOf(results);

        } catch (SQLException e) {
            throw new VectorRuntimeException("Failed to search vectors: " + e.getMessage(), e);
        }
    }
}
