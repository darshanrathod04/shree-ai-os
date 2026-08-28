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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the {@link MemoryVersionLedger}. */
class MemoryVersionLedgerTest {

    private final MemoryVersionLedger ledger = new MemoryVersionLedger();

    @Test
    void newMemoryHasVersionOne() {
        Memory memory = memory("v1");

        assertEquals(1, ledger.versionOf(memory.id()));
        assertTrue(ledger.history(memory.id()).isEmpty());
        assertTrue(ledger.previousVersion(memory.id()).isEmpty());
    }

    @Test
    void snapshotsBuildVersionChain() {
        Memory v1 = memory("v1");
        Memory v2 = memory("v2", v1.id());
        Memory v3 = memory("v3", v1.id());

        ledger.snapshot(v1);
        ledger.snapshot(v2);

        assertEquals(3, ledger.versionOf(v3.id()));
        assertEquals(2, ledger.history(v3.id()).size());
        assertEquals("v2", ledger.previousVersion(v3.id()).orElseThrow().content().text());
    }

    @Test
    void purgeClearsHistory() {
        Memory memory = memory("v1");
        ledger.snapshot(memory);
        ledger.purge(memory.id());

        assertEquals(1, ledger.versionOf(memory.id()));
        assertTrue(ledger.history(memory.id()).isEmpty());
    }

    private Memory memory(String text) {
        return memory(text, new MemoryId("mem-" + System.nanoTime()));
    }

    private Memory memory(String text, MemoryId id) {
        MemoryContent content = new MemoryContent(text, null, Map.of(), Instant.now());
        MemoryMetadata metadata = new MemoryMetadata(
                id,
                MemoryType.SEMANTIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("test-owner"),
                Set.of("test"),
                0.5,
                0.9,
                "test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L);
        return new Memory(id, content, metadata, Instant.now(), Instant.now());
    }
}