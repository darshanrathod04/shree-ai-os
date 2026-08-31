package com.shreeai.os.platform.runtime.vector;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the thread-safe {@link InMemoryVectorStore}.
 */
class InMemoryVectorStoreTest {

    private final InMemoryVectorStore store = new InMemoryVectorStore();

    private static VectorRecord record(String id, String content) {
        return VectorRecord.of(id, content, new double[]{1.0, 0.5, -0.25}, Map.of(
                "documentId", "doc-1",
                "tenantId", "default",
                "embeddingVersion", "local-deterministic-v1"));
    }

    @Test
    void storeIsAnUpsert() {
        store.store(record("id-1", "first"));
        store.store(record("id-1", "second"));
        assertEquals(1, store.all().size());
        assertEquals("second", store.findById("id-1").orElseThrow().content());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(store.findById("missing").isEmpty());
    }

    @Test
    void deleteRemovesTheRecord() {
        store.store(record("id-1", "first"));
        assertTrue(store.delete("id-1"));
        assertFalse(store.delete("id-1"));
        assertTrue(store.all().isEmpty());
    }

    @Test
    void immutabilityDefensiveCopies() {
        VectorRecord stored = record("id-1", "first");
        store.store(stored);
        VectorRecord loaded = store.findById("id-1").orElseThrow();

        assertSame(loaded.metadata().getClass(), stored.metadata().getClass());
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> loaded.metadata().put("x", "y"));
    }

    @Test
    void concurrentWritesDoNotCorruptState() throws InterruptedException {
        int threads = 8;
        int writesPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < writesPerThread; i++) {
                        store.store(record("thread-" + threadId + "-" + i, "content"));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdownNow();

        List<VectorRecord> all = store.all();
        assertEquals(threads * writesPerThread, all.size());
    }
}
