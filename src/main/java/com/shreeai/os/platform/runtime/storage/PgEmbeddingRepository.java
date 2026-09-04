package com.shreeai.os.platform.runtime.storage;

import com.shreeai.os.platform.runtime.embedding.EmbeddedVector;
import com.shreeai.os.platform.runtime.embedding.EmbeddingRepository;
import com.shreeai.os.platform.runtime.embedding.EmbeddingRuntimeException;
import com.shreeai.os.platform.runtime.vector.PgConnections;
import com.shreeai.os.platform.runtime.vector.PgVectors;
import com.shreeai.os.platform.runtime.vector.SqlConnectionSupplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * <b>PgEmbeddingRepository</b>
 *
 * <p>PostgreSQL + pgvector adapter of {@link EmbeddingRepository}. Stores
 * embeddings in table {@code shree_embedding} together with the
 * {@code embeddingVersion} of the provider that produced them
 * (metadata-first schema). Plain JDBC — no ORM — keeping the kernels
 * free of persistence technology.</p>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgEmbeddingRepository implements EmbeddingRepository {

    private final SqlConnectionSupplier connections;
    private final int dimensions;

    /**
     * Creates a PgVector-backed embedding repository.
     *
     * @param jdbcUrl    PostgreSQL JDBC URL (must not be null or blank)
     * @param user       database user (must not be null)
     * @param password   database password (must not be null)
     * @param dimensions embedding dimension; must match the vector column type
     */
    public PgEmbeddingRepository(String jdbcUrl, String user, String password, int dimensions) {
        this(PgConnections.from(jdbcUrl, user, password), dimensions);
    }

    /**
     * Creates a PgVector-backed embedding repository over a connection supplier.
     *
     * @param connections supplier of JDBC connections (must not be null)
     * @param dimensions  embedding dimension; must match the vector column type
     */
    public PgEmbeddingRepository(SqlConnectionSupplier connections, int dimensions) {
        this.connections = java.util.Objects.requireNonNull(connections, "connections must not be null");
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        this.dimensions = dimensions;
    }

    @Override
    public void save(String ownerId, double[] embedding, String embeddingVersion) {
        validateOwnerId(ownerId);
        if (embedding == null || embedding.length != dimensions) {
            throw new EmbeddingRuntimeException(
                    "embedding must have exactly " + dimensions + " dimensions");
        }
        String sql = """
                INSERT INTO shree_embedding (owner_id, embedding, embedding_version)
                VALUES (?, ?::vector, ?)
                ON CONFLICT (owner_id)
                DO UPDATE SET embedding = EXCLUDED.embedding,
                              embedding_version = EXCLUDED.embedding_version
                """;
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId);
            statement.setString(2, PgVectors.toPgVectorLiteral(embedding));
            statement.setString(3, embeddingVersion);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new EmbeddingRuntimeException("Failed to save embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<EmbeddedVector> load(String ownerId) {
        validateOwnerId(ownerId);
        String sql = "SELECT embedding::text, embedding_version FROM shree_embedding WHERE owner_id = ?";
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                double[] embedding = PgVectors.fromPgVectorLiteral(resultSet.getString(1));
                return Optional.of(EmbeddedVector.of(embedding, resultSet.getString(2)));
            }
        } catch (SQLException e) {
            throw new EmbeddingRuntimeException("Failed to load embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String ownerId) {
        validateOwnerId(ownerId);
        String sql = "SELECT 1 FROM shree_embedding WHERE owner_id = ?";
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new EmbeddingRuntimeException("Failed to check embedding existence: " + e.getMessage(), e);
        }
    }

    private void validateOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new EmbeddingRuntimeException("ownerId must not be null or blank");
        }
    }
}
