package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRegistrySnapshot;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultKnowledgeSourceRegistry}.
 */
public class DefaultKnowledgeSourceRegistryTest {

    private DefaultKnowledgeSourceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultKnowledgeSourceRegistry();
    }

    @Test
    @DisplayName("Test 1: PDF source registered with metadata")
    void testRegisterPdfSource() {
        KnowledgeSource pdf = registry.register(KnowledgeSourceType.PDF,
                "Java.pdf", "docs/java.pdf", "Oracle Java reference",
                Map.of("author", "Oracle", "language", "java"));
        assertNotNull(pdf);
        assertEquals(KnowledgeSourceType.PDF, pdf.type());
        assertEquals(KnowledgeSourceStatus.REGISTERED, pdf.status());
        assertEquals("Java.pdf", pdf.name());
        assertEquals("docs/java.pdf", pdf.location());
        assertEquals("Oracle", pdf.metadata().get("author"));
        assertFalse(pdf.sourceId().isBlank(), "source id must be present");
    }

    @Test
    @DisplayName("Test 2: Web source registered")
    void testRegisterWebSource() {
        KnowledgeSource web = registry.register(KnowledgeSourceType.WEB,
                "Oracle Docs", "https://docs.oracle.com", "Official docs", Map.of());
        assertNotNull(web);
        assertEquals(KnowledgeSourceType.WEB, web.type());
        assertEquals(KnowledgeSourceStatus.REGISTERED, web.status());
    }

    @Test
    @DisplayName("Test 3: Folder source registered without description")
    void testRegisterFolderSource() {
        KnowledgeSource folder = registry.register(KnowledgeSourceType.FOLDER,
                "docs", "docs/", null, Map.of());
        assertNotNull(folder);
        assertEquals(KnowledgeSourceType.FOLDER, folder.type());
        assertNull(folder.description());
    }

    @Test
    @DisplayName("Test 4: Duplicate registration returns the same deterministic id")
    void testDuplicateRegistrationReturnsSameId() {
        KnowledgeSource first = registry.register(KnowledgeSourceType.PDF,
                "Java.pdf", "docs/java.pdf", "desc", Map.of());
        KnowledgeSource second = registry.register(KnowledgeSourceType.PDF,
                "Java.pdf", "docs/java.pdf", "desc", Map.of());
        assertEquals(first.sourceId(), second.sourceId(),
                "identical registration input must produce identical ids");
        assertSame(first, second,
                "duplicate registration must return the originally registered source");
        assertEquals(1, registry.snapshot().size());
    }

    @Test
    @DisplayName("Test 5: Activate source moves life-cycle status to ACTIVE")
    void testActivateSource() {
        KnowledgeSource registered = registry.register(KnowledgeSourceType.MARKDOWN,
                "spring.md", "docs/spring.md", null, Map.of());
        Optional<KnowledgeSource> activated = registry.activate(registered.sourceId());
        assertTrue(activated.isPresent());
        assertEquals(KnowledgeSourceStatus.ACTIVE, activated.get().status());
        assertEquals(registered.sourceId(), activated.get().sourceId());
    }

    @Test
    @DisplayName("Test 6: Disable source moves life-cycle status to DISABLED")
    void testDisableSource() {
        KnowledgeSource registered = registry.register(KnowledgeSourceType.MARKDOWN,
                "spring.md", "docs/spring.md", null, Map.of());
        Optional<KnowledgeSource> disabled = registry.disable(registered.sourceId());
        assertTrue(disabled.isPresent());
        assertEquals(KnowledgeSourceStatus.DISABLED, disabled.get().status());
    }

    @Test
    @DisplayName("Test 7: Find sources by type")
    void testFindByType() {
        registry.register(KnowledgeSourceType.PDF, "Java.pdf", "docs/java.pdf", null, Map.of());
        registry.register(KnowledgeSourceType.PDF, "Spring.pdf", "docs/spring.pdf", null, Map.of());
        registry.register(KnowledgeSourceType.WEB, "Oracle", "https://docs.oracle.com", null, Map.of());

        List<KnowledgeSource> pdfs = registry.findByType(KnowledgeSourceType.PDF);
        assertEquals(2, pdfs.size());
        assertEquals(KnowledgeSourceType.PDF, pdfs.get(0).type());

        List<KnowledgeSource> webs = registry.findByType(KnowledgeSourceType.WEB);
        assertEquals(1, webs.size());
        assertEquals("Oracle", webs.get(0).name());
    }

    @Test
    @DisplayName("Test 8: Snapshot is an immutable point-in-time view")
    void testSnapshotImmutable() {
        registry.register(KnowledgeSourceType.PDF, "A.pdf", "docs/a.pdf", null, Map.of());
        registry.register(KnowledgeSourceType.WEB, "B", "https://b.example", null, Map.of());

        KnowledgeRegistrySnapshot snapshot = registry.snapshot();
        assertEquals(2, snapshot.size());

        registry.register(KnowledgeSourceType.FOLDER, "C", "docs/c/", null, Map.of());
        assertEquals(2, snapshot.size(),
                "a snapshot must not change after the registry mutates");

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.sources().clear(),
                "the snapshot's source list must be unmodifiable");
    }

    @Test
    @DisplayName("Test 9: Same registration input always produces the same source id")
    void testSameInputProducesSameId() {
        String id1 = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.PDF, "Java.pdf", "docs/java.pdf");
        String id2 = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.PDF, "Java.pdf", "docs/java.pdf");
        assertEquals(id1, id2, "same input must always produce the same id");
        assertFalse(id1.isBlank());
    }

    @Test
    @DisplayName("Test 10: Different registration input produces a different id")
    void testDifferentInputProducesDifferentId() {
        String id1 = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.PDF, "Java.pdf", "docs/java.pdf");
        String id2 = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.PDF, "Java.pdf", "OTHER/path.pdf");
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Test 11: Metadata is defensively copied and immutable")
    void testMetadataIsDefensivelyCopied() {
        Map<String, String> mutable = new HashMap<>();
        mutable.put("owner", "team-a");
        KnowledgeSource source = registry.register(KnowledgeSourceType.JSON,
                "knowledge.json", "docs/knowledge.json", null, mutable);

        mutable.put("owner", "intruder");
        assertEquals("team-a", source.metadata().get("owner"),
                "mutating the caller's map must not affect the registered source");

        assertThrows(UnsupportedOperationException.class,
                () -> source.metadata().put("x", "y"),
                "the stored metadata map must be unmodifiable");
    }

    @Test
    @DisplayName("Test 12: Find by id returns the source or empty for unknown ids")
    void testFindById() {
        KnowledgeSource source = registry.register(KnowledgeSourceType.TEXT,
                "notes", "notes.txt", null, Map.of());
        Optional<KnowledgeSource> found = registry.findById(source.sourceId());
        assertEquals(source, found.orElseThrow());

        assertTrue(registry.findById("no-such-id").isEmpty());
    }

    @Test
    @DisplayName("Test 13: Activate and disable on unknown ids return empty")
    void testLifecycleOnUnknownIdReturnsEmpty() {
        assertTrue(registry.activate("no-such-id").isEmpty());
        assertTrue(registry.disable("no-such-id").isEmpty());
    }

    @Test
    @DisplayName("Test 14: Location normalization yields equal ids")
    void testLocationNormalizationProducesSameId() {
        String forwardSlash = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.FOLDER, "docs", "docs/java.md");
        String backSlash = DefaultKnowledgeSourceRegistry.sourceIdFor(
                KnowledgeSourceType.FOLDER, "docs", "Docs\\JAVA.MD");
        assertEquals(forwardSlash, backSlash,
                "case and separator differences must normalize to the same id");
    }

    @Test
    @DisplayName("Test 15: Concurrent registration is thread-safe")
    void testConcurrentRegistrationIsThreadSafe() throws Exception {
        int tasks = 50;
        ExecutorService pool = Executors.newFixedThreadPool(tasks);
        CountDownLatch ready = new CountDownLatch(tasks);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                registry.register(KnowledgeSourceType.PDF,
                        "doc-" + idx + ".pdf", "docs/doc-" + idx + ".pdf", null, Map.of());
            }));
        }
        ready.await();
        go.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();
        assertEquals(tasks, registry.snapshot().size());
    }
}
