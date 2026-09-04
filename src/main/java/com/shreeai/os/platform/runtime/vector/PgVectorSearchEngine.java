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
 * <p><b>Hybrid search:</b> in addition to pure vector KNN, this engine
 * implements {@link #hybridSearch(double[], String, int)} using Reciprocal
 * Rank Fusion (RRF) over a vector CTE and a {@code tsvector} full-text CTE
 * combined with a {@code FULL OUTER JOIN}. This combines semantic and
 * lexical relevance so keyword-heavy queries still return useful results
 * even when the embedding disagrees with the exact wording.</p>
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

    /**
     * Hybrid RRF query: combines KNN over embeddings (HNSW-accelerated) with
     * full-text search over {@code content_tsv} (GIN-accelerated) and ranks
     * the union with Reciprocal Rank Fusion. Per-relation {@code LIMIT 20}
     * is a tunable candidate set — large enough that top-K is unlikely to be
     * missed, small enough that the join stays cheap.
     */
    private static final String RRF_SQL = """
            WITH vector_matches AS (
                SELECT id, content, metadata::text,
                       ROW_NUMBER() OVER (ORDER BY embedding <=> ?::vector) AS rank_vec
                FROM shree_vector_memory
                ORDER BY embedding <=> ?::vector
                LIMIT 20
            ),
            text_matches AS (
                SELECT id, content, metadata::text,
                       ROW_NUMBER() OVER (
                           ORDER BY ts_rank_cd(content_tsv, plainto_tsquery('english', ?)) DESC
                       ) AS rank_text
                FROM shree_vector_memory
                WHERE content_tsv @@ plainto_tsquery('english', ?)
                ORDER BY ts_rank_cd(content_tsv, plainto_tsquery('english', ?)) DESC
                LIMIT 20
            )
            SELECT
                COALESCE(v.id, t.id)                                              AS id,
                COALESCE(v.content, t.content)                                    AS content,
                COALESCE(v.metadata, t.metadata)                                  AS metadata,
                COALESCE(1.0 / (60 + v.rank_vec), 0.0)
                  + COALESCE(1.0 / (60 + t.rank_text), 0.0)                        AS rrf_score
            FROM vector_matches v
            FULL OUTER JOIN text_matches t ON v.id = t.id
            ORDER BY rrf_score DESC
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

    /**
     * Hybrid search: combines vector KNN and full-text search via RRF.
     * Falls back to pure vector search when {@code textQuery} is blank.
     */
    @Override
    public List<VectorSearchResult> hybridSearch(
            double[] queryEmbedding,
            String textQuery,
            int topK) {

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new VectorRuntimeException("queryEmbedding must not be null or empty");
        }
        if (topK <= 0) {
            throw new VectorRuntimeException("topK must be positive");
        }

        // Fall back to pure vector search when no text query is available.
        if (textQuery == null || textQuery.isBlank()) {
            return search(queryEmbedding, topK);
        }

        String literal = PgVectors.toPgVectorLiteral(queryEmbedding);
        List<VectorSearchResult> results = new ArrayList<>();

        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(RRF_SQL)) {
            statement.setString(1, literal);    // CTE 1: ORDER BY embedding <=> ?::vector
            statement.setString(2, literal);    // CTE 1: LIMIT 20 ORDER BY
            statement.setString(3, textQuery);  // CTE 2: ts_rank_cd ORDER BY
            statement.setString(4, textQuery);  // CTE 2: WHERE content_tsv @@ plainto_tsquery
            statement.setString(5, textQuery);  // CTE 2: LIMIT 20 ORDER BY
            statement.setInt(6, topK);          // Outer LIMIT

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> metadata =
                            PgVectorsJson.fromJson(resultSet.getString("metadata"));
                    if (metadata == null) {
                        metadata = new HashMap<>();
                    }
                    results.add(VectorSearchResult.of(
                            resultSet.getString("id"),
                            resultSet.getDouble("rrf_score"),
                            resultSet.getString("content"),
                            metadata));
                }
            }
            return List.copyOf(results);

        } catch (SQLException e) {
            throw new VectorRuntimeException(
                    "Failed to hybrid-search vectors: " + e.getMessage(), e);
        }
    }
}
