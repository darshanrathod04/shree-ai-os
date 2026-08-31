package com.shreeai.os.platform.runtime.vector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>InMemoryVectorStore</b>
 *
 * <p>Thread-safe default {@link VectorStore}. Suitable for local development,
 * unit tests, and as the graceful-degradation backend when no database is
 * configured. Data is not durable across restarts — PostgreSQL persistence
 * arrives with {@code PgVectorMemoryStore}.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class InMemoryVectorStore implements VectorStore {

    private final Map<String, VectorRecord> records = new ConcurrentHashMap<>();

    @Override
    public void store(VectorRecord record) {
        if (record == null) {
            throw new VectorRuntimeException("record must not be null");
        }
        records.put(record.id(), record);
    }

    @Override
    public Optional<VectorRecord> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new VectorRuntimeException("id must not be null or blank");
        }
        return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<VectorRecord> all() {
        return List.copyOf(records.values());
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.isBlank()) {
            throw new VectorRuntimeException("id must not be null or blank");
        }
        return records.remove(id) != null;
    }
}
