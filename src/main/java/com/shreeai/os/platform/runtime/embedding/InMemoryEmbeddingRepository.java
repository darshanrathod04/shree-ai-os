package com.shreeai.os.platform.runtime.embedding;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>InMemoryEmbeddingRepository</b>
 *
 * <p>Thread-safe default {@link EmbeddingRepository}. Suitable for local
 * development, unit tests, and as the graceful-degradation fallback when no
 * database is configured. Data is not durable across restarts — PostgreSQL
 * persistence arrives with {@code PgEmbeddingRepository}.</p>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class InMemoryEmbeddingRepository implements EmbeddingRepository {

    private final Map<String, EmbeddedVector> store = new ConcurrentHashMap<>();

    @Override
    public void save(String ownerId, double[] embedding, String embeddingVersion) {
        validateOwnerId(ownerId);
        Objects.requireNonNull(embedding, "embedding must not be null");
        Objects.requireNonNull(embeddingVersion, "embeddingVersion must not be null");
        store.put(ownerId, EmbeddedVector.of(embedding, embeddingVersion));
    }

    @Override
    public Optional<EmbeddedVector> load(String ownerId) {
        validateOwnerId(ownerId);
        return Optional.ofNullable(store.get(ownerId));
    }

    @Override
    public boolean exists(String ownerId) {
        validateOwnerId(ownerId);
        return store.containsKey(ownerId);
    }

    private void validateOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new EmbeddingRuntimeException("ownerId must not be null or blank");
        }
    }
}
