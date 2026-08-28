package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>MemoryVersionLedger</b>
 *
 * <p>Versioning for the memory kernel: every superseded version of a memory
 * (content updates, status transitions) is retained as an immutable snapshot
 * instead of being destroyed by overwrite.</p>
 *
 * <p><b>Versioning model:</b></p>
 * <ul>
 *   <li>The live memory is always the newest version.</li>
 *   <li>{@link #versionOf(MemoryId)} returns the current version number
 *       (1 for a memory with no history).</li>
 *   <li>{@link #history(MemoryId)} returns all snapshots oldest-first,
 *       excluding the live version.</li>
 *   <li>{@link #previousVersion(MemoryId)} returns the immediately
 *       superseded version, if any.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Per-memory synchronized lists guarded by the
 * ledger; safe for concurrent kernel access.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Constitutional Authority:</b> ADD-201, MEMORY-RUNTIME-001</p>
 */
public final class MemoryVersionLedger {

    private final Map<MemoryId, List<Memory>> history = new ConcurrentHashMap<>();

    /**
     * Records {@code memory} as a superseded version of its id.
     *
     * @param memory the memory being replaced (must not be null)
     */
    public void snapshot(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");
        history.computeIfAbsent(memory.id(), id -> Collections.synchronizedList(new ArrayList<>()))
                .add(memory);
    }

    /**
     * Returns the current version number of a memory.
     *
     * @param id the memory id (must not be null)
     * @return 1 + number of retained prior versions
     */
    public int versionOf(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Memory> versions = history.get(id);
        return versions == null ? 1 : versions.size() + 1;
    }

    /**
     * Returns all retained prior versions of a memory, oldest first.
     *
     * @param id the memory id (must not be null)
     * @return an unmodifiable snapshot list (never null, may be empty)
     */
    public List<Memory> history(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Memory> versions = history.get(id);
        return versions == null ? List.of() : List.copyOf(versions);
    }

    /**
     * Returns the immediately superseded version of a memory.
     *
     * @param id the memory id (must not be null)
     * @return the previous version, or empty when the memory has no history
     */
    public Optional<Memory> previousVersion(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Memory> versions = history.get(id);
        if (versions == null || versions.isEmpty()) {
            return Optional.empty();
        }
        synchronized (versions) {
            return versions.isEmpty()
                    ? Optional.empty()
                    : Optional.of(versions.get(versions.size() - 1));
        }
    }

    /**
     * Drops all retained history for a memory (e.g. on hard delete).
     *
     * @param id the memory id (must not be null)
     */
    public void purge(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        history.remove(id);
    }
}