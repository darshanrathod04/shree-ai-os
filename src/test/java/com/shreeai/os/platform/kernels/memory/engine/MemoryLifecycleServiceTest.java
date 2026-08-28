package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the {@link MemoryLifecycleService}. */
class MemoryLifecycleServiceTest {

    private final MemoryLifecycleService lifecycle = new MemoryLifecycleService();

    @Test
    void importanceRisesWithAccessCountAndRecency() {
        Memory fresh = memory(MemoryType.WORKING, 0.3, 0L, Instant.now());
        Memory stale = memory(MemoryType.WORKING, 0.3, 0L, Instant.now().minusSeconds(60L * 60 * 24 * 30));

        assertTrue(lifecycle.scoreImportance(fresh) > lifecycle.scoreImportance(stale),
                "fresh memory should outscore a stale one");

        Memory touched = lifecycle.touch(lifecycle.touch(lifecycle.touch(fresh)));
        assertTrue(touched.metadata().accessCount() == 3L);
        assertTrue(lifecycle.scoreImportance(touched) > lifecycle.scoreImportance(stale));
    }

    @Test
    void workingMemoryPromotesAfterEnoughAccesses() {
        Memory working = memory(MemoryType.WORKING, 0.3, 5L, Instant.now());

        var promoted = lifecycle.promoteIfEligible(working);

        assertTrue(promoted.isPresent(), "5 accesses should trigger promotion");
        assertEquals(LifecyclePolicy.DEFAULT.promoteTarget(), promoted.get().metadata().type());
        assertEquals(MemoryStatus.ACTIVE, promoted.get().metadata().status());
    }

    @Test
    void nonWorkingMemoryNeverPromotes() {
        Memory semantic = memory(MemoryType.SEMANTIC, 0.9, 100L, Instant.now());

        assertTrue(lifecycle.promoteIfEligible(semantic).isEmpty());
    }

    @Test
    void lowAccessWorkingMemoryStays() {
        Memory working = memory(MemoryType.WORKING, 0.1, 0L, Instant.now());

        assertTrue(lifecycle.promoteIfEligible(working).isEmpty());
    }

    @Test
    void staleLowImportanceMemoryArchives() {
        LifecyclePolicy policy = new LifecyclePolicy(3, 0.7, 30, 7, MemoryType.SEMANTIC);
        MemoryLifecycleService service = new MemoryLifecycleService(policy);

        Memory stale = memory(MemoryType.EPISODIC, 0.1, 0L, Instant.now().minusSeconds(60L * 60 * 24 * 60));

        var archived = service.archiveIfStale(stale);

        assertTrue(archived.isPresent(), "60-day idle low-importance memory should archive");
        assertEquals(MemoryStatus.ARCHIVED, archived.get().metadata().status());
    }

    @Test
    void factMemoriesAreNeverArchived() {
        LifecyclePolicy policy = new LifecyclePolicy(3, 0.7, 30, 7, MemoryType.SEMANTIC);
        MemoryLifecycleService service = new MemoryLifecycleService(policy);

        Memory fact = memory(MemoryType.FACT, 0.1, 0L, Instant.now().minusSeconds(60L * 60 * 24 * 365));

        assertTrue(service.archiveIfStale(fact).isEmpty());
    }

    @Test
    void consolidateReturnsOnlyChangedMemories() {
        LifecyclePolicy policy = new LifecyclePolicy(3, 0.7, 30, 7, MemoryType.SEMANTIC);
        MemoryLifecycleService service = new MemoryLifecycleService(policy);

        Memory promotable = memory(MemoryType.WORKING, 0.3, 10L, Instant.now());
        Memory active = memory(MemoryType.EPISODIC, 0.5, 1L, Instant.now());
        Memory stale = memory(MemoryType.EPISODIC, 0.05, 0L, Instant.now().minusSeconds(60L * 60 * 24 * 90));

        List<Memory> changed = service.consolidate(List.of(promotable, active, stale));

        assertEquals(2, changed.size(), "expected one promotion and one archival");
    }

    private Memory memory(MemoryType type, double importance, long accessCount, Instant accessedAt) {
        MemoryId id = new MemoryId("mem-" + System.nanoTime());
        MemoryContent content = new MemoryContent("test content", null, Map.of(), Instant.now());
        MemoryMetadata metadata = new MemoryMetadata(
                id,
                type,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("test-owner"),
                Set.of("test"),
                importance,
                0.9,
                "test",
                accessedAt,
                accessedAt,
                accessedAt,
                accessCount);
        return new Memory(id, content, metadata, accessedAt, accessedAt);
    }
}