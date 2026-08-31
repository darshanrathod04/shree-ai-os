package com.shreeai.os.platform.runtime.embedding;

import java.util.Optional;

/**
 * <b>EmbeddingRepository</b>
 *
 * <p>SPI for persisting and retrieving embeddings keyed by an owner identifier
 * (document id, knowledge node id, memory id, ...). Part of the
 * metadata-first document schema: every stored embedding carries the
 * {@code embeddingVersion} of the provider that produced it.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Implementations MUST be thread-safe.</li>
 *   <li>Implementations MUST NOT hard-code a storage provider — adapters
 *       (in-memory, PostgreSQL/PgVector) plug in via configuration.</li>
 *   <li>Failures MUST be translated into {@link EmbeddingRuntimeException}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface EmbeddingRepository {

    /**
     * Persists (upserts) the embedding of the given owner.
     *
     * @param ownerId          unique owner identifier (must not be null or blank)
     * @param embedding        the embedding vector (must not be null)
     * @param embeddingVersion the provider version that produced the embedding
     *                         (must not be null or blank)
     * @throws EmbeddingRuntimeException if persistence fails
     */
    void save(String ownerId, double[] embedding, String embeddingVersion);

    /**
     * Loads the embedding stored for the given owner.
     *
     * @param ownerId unique owner identifier (must not be null or blank)
     * @return the stored embedding, or empty when the owner is unknown
     * @throws EmbeddingRuntimeException if loading fails
     */
    Optional<EmbeddedVector> load(String ownerId);

    /**
     * Indicates whether an embedding exists for the given owner.
     *
     * @param ownerId unique owner identifier (must not be null or blank)
     * @return {@code true} when an embedding is stored for the owner
     * @throws EmbeddingRuntimeException if the check fails
     */
    boolean exists(String ownerId);
}
