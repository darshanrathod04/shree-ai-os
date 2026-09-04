package com.shreeai.os.platform.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>PersistenceStoreTest</b>
 *
 * <p>10 test cases for the PersistenceStore interface implementations.</p>
 *
 * @since v1.0
 */
public class PersistenceStoreTest {

    @Test
    void inMemoryStore_putAndGet() {
        InMemoryStore store = new InMemoryStore();
        store.put("memory:1", "Hello");
        Optional<String> v = store.get("memory:1");
        assertTrue(v.isPresent());
        assertEquals("Hello", v.get());
    }

    @Test
    void inMemoryStore_delete() {
        InMemoryStore store = new InMemoryStore();
        store.put("key", "value");
        store.delete("key");
        assertFalse(store.exists("key"));
    }

    @Test
    void inMemoryStore_keysByPrefix() {
        InMemoryStore store = new InMemoryStore();
        store.put("memory:1", "A");
        store.put("memory:2", "B");
        store.put("knowledge:1", "C");

        List<String> memKeys = store.keys("memory:");
        assertEquals(2, memKeys.size());
        assertTrue(memKeys.contains("memory:1"));
        assertTrue(memKeys.contains("memory:2"));
    }

    @Test
    void inMemoryStore_entriesByPrefix() {
        InMemoryStore store = new InMemoryStore();
        store.put("memory:a", "1");
        store.put("memory:b", "2");
        store.put("other:c", "3");

        Map<String, String> entries = store.entries("memory:");
        assertEquals(2, entries.size());
        assertEquals("1", entries.get("memory:a"));
        assertEquals("2", entries.get("memory:b"));
    }

    @Test
    void inMemoryStore_sizeByPrefix() {
        InMemoryStore store = new InMemoryStore();
        store.put("a:1", "1");
        store.put("a:2", "2");
        store.put("b:3", "3");
        assertEquals(2, store.size("a:"));
        assertEquals(3, store.size(""));
    }

    @Test
    void inMemoryStore_clearByPrefix() {
        InMemoryStore store = new InMemoryStore();
        store.put("a:1", "1");
        store.put("b:2", "2");
        store.clear("a:");
        assertFalse(store.exists("a:1"));
        assertTrue(store.exists("b:2"));
    }

    @Test
    void inMemoryStore_clearAll() {
        InMemoryStore store = new InMemoryStore();
        store.put("a", "1");
        store.put("b", "2");
        store.clear("");
        assertEquals(0, store.size(""));
    }

    @Test
    void inMemoryStore_name() {
        InMemoryStore store = new InMemoryStore();
        assertEquals("in-memory", store.name());
    }

    @Test
    void inMemoryStore_isHealthy() {
        InMemoryStore store = new InMemoryStore();
        assertTrue(store.isHealthy());
    }

    @Test
    void inMemoryStore_concurrentAccess() throws InterruptedException {
        InMemoryStore store = new InMemoryStore();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) store.put("k:" + i, "v" + i);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) store.put("k:" + (i + 100), "v" + i);
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        assertEquals(200, store.size("k:"));
    }
}
