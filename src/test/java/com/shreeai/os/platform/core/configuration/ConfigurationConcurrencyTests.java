package com.shreeai.os.platform.core.configuration;

import com.shreeai.os.platform.core.configuration.error.DuplicateConfigurationException;
import com.shreeai.os.platform.core.configuration.model.ConfigurationEntry;
import com.shreeai.os.platform.core.configuration.model.ConfigurationKey;
import com.shreeai.os.platform.core.configuration.model.ConfigurationNamespace;
import com.shreeai.os.platform.core.configuration.model.ConfigurationType;
import com.shreeai.os.platform.core.configuration.service.DefaultConfigurationService;
import com.shreeai.os.platform.core.configuration.validator.ConfigurationValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>ConfigurationConcurrencyTests</b>
 *
 * <p>Tests for Configuration Service concurrency within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies thread-safe registration under concurrent load.</li>
 *   <li>Verifies thread-safe lookup under concurrent load.</li>
 *   <li>Verifies mixed workload handling.</li>
 *   <li>Verifies concurrent removal.</li>
 *   <li>Verifies no data corruption under concurrency.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultConfigurationService
 * @see ConfigurationValidator
 */
public class ConfigurationConcurrencyTests {

    private final DefaultConfigurationService service = new DefaultConfigurationService();

    // 100 concurrent registrations

    /**
     * Test: 100 concurrent registrations with unique keys.
     */
    @org.junit.jupiter.api.Test
    void test100ConcurrentRegistrations() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ConfigurationEntry entry = new ConfigurationEntry(
                            new ConfigurationKey("concurrent.key" + index),
                            new ConfigurationNamespace("test.namespace"),
                            ConfigurationType.STRING,
                            "value" + index,
                            "Description " + index,
                            false,
                            Instant.now()
                    );
                    service.register(entry);
                    successCount.incrementAndGet();
                } catch (DuplicateConfigurationException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, successCount.get());
        org.junit.jupiter.api.Assertions.assertEquals(0, failureCount.get());
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, service.list().size());
    }

    /**
     * Test: 100 concurrent registrations with duplicate keys.
     */
    @org.junit.jupiter.api.Test
    void test100ConcurrentRegistrationsWithDuplicates() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ConfigurationEntry entry = new ConfigurationEntry(
                            new ConfigurationKey("duplicate.key"),
                            new ConfigurationNamespace("test.namespace"),
                            ConfigurationType.STRING,
                            "value",
                            "Description",
                            false,
                            Instant.now()
                    );
                    service.register(entry);
                    successCount.incrementAndGet();
                } catch (DuplicateConfigurationException e) {
                    duplicateCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(1, successCount.get());
        org.junit.jupiter.api.Assertions.assertEquals(threadCount - 1, duplicateCount.get());
    }

    // 100 concurrent lookups

    /**
     * Test: 100 concurrent lookups.
     */
    @org.junit.jupiter.api.Test
    void test100ConcurrentLookups() throws Exception {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("lookup.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (service.get(new ConfigurationKey("lookup.key")).isPresent()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, successCount.get());
    }

    /**
     * Test: 100 concurrent lookups for missing keys.
     */
    @org.junit.jupiter.api.Test
    void test100ConcurrentLookupsForMissingKeys() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger emptyCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (service.get(new ConfigurationKey("missing.key" + index)).isEmpty()) {
                        emptyCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, emptyCount.get());
    }

    // Mixed workload

    /**
     * Test: Mixed workload of registrations and lookups.
     */
    @org.junit.jupiter.api.Test
    void testMixedWorkload() throws Exception {
        // Arrange
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);
        AtomicInteger registrationSuccess = new AtomicInteger(0);
        AtomicInteger lookupSuccess = new AtomicInteger(0);

        // Pre-register some entries
        for (int i = 0; i < 10; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("mixed.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            // Registration thread
            executor.submit(() -> {
                try {
                    ConfigurationEntry entry = new ConfigurationEntry(
                            new ConfigurationKey("mixed.new" + index),
                            new ConfigurationNamespace("test.namespace"),
                            ConfigurationType.STRING,
                            "newValue" + index,
                            "New Description " + index,
                            false,
                            Instant.now()
                    );
                    service.register(entry);
                    registrationSuccess.incrementAndGet();
                } catch (DuplicateConfigurationException e) {
                    // Ignore duplicates
                } finally {
                    latch.countDown();
                }
            });

            // Lookup thread
            executor.submit(() -> {
                try {
                    if (service.get(new ConfigurationKey("mixed.key" + (index % 10))).isPresent()) {
                        lookupSuccess.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, registrationSuccess.get());
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, lookupSuccess.get());
    }

    // Concurrent removal

    /**
     * Test: Concurrent removal of configurations.
     */
    @org.junit.jupiter.api.Test
    void testConcurrentRemoval() throws Exception {
        // Arrange
        for (int i = 0; i < 50; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("remove.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger removalSuccess = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (service.remove(new ConfigurationKey("remove.key" + index))) {
                        removalSuccess.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, removalSuccess.get());
        org.junit.jupiter.api.Assertions.assertEquals(0, service.list().size());
    }

    /**
     * Test: Concurrent removal of read-only configurations throws exception.
     */
    @org.junit.jupiter.api.Test
    void testConcurrentRemovalOfReadOnlyThrowsException() throws Exception {
        // Arrange
        ConfigurationEntry readOnlyEntry = new ConfigurationEntry(
                new ConfigurationKey("readonly.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                true,
                Instant.now()
        );
        service.register(readOnlyEntry);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.remove(new ConfigurationKey("readonly.key"));
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(exceptionCount.get() > 0);
    }

    // No corruption

    /**
     * Test: No data corruption under concurrent load.
     */
    @org.junit.jupiter.api.Test
    void testNoDataCorruptionUnderConcurrentLoad() throws Exception {
        // Arrange
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Pre-register entries
        for (int i = 0; i < 25; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("corruption.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        // Act - concurrent registrations and lookups
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // Register new entry
                        ConfigurationEntry entry = new ConfigurationEntry(
                                new ConfigurationKey("corruption.new" + index),
                                new ConfigurationNamespace("test.namespace"),
                                ConfigurationType.STRING,
                                "newValue" + index,
                                "New Description " + index,
                                false,
                                Instant.now()
                        );
                        service.register(entry);
                    } else {
                        // Lookup existing entry
                        service.get(new ConfigurationKey("corruption.key" + (index % 25)));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert - verify no corruption
        Collection<ConfigurationEntry> all = service.list();
        org.junit.jupiter.api.Assertions.assertTrue(all.size() >= 25);
        org.junit.jupiter.api.Assertions.assertTrue(all.size() <= 50);
    }

    /**
     * Test: Concurrent exists() calls are consistent.
     */
    @org.junit.jupiter.api.Test
    void testConcurrentExistsConsistency() throws Exception {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("exists.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger trueCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (service.exists(new ConfigurationKey("exists.key"))) {
                        trueCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, trueCount.get());
    }

    /**
     * Test: Concurrent list() calls return consistent results.
     */
    @org.junit.jupiter.api.Test
    void testConcurrentListConsistency() throws Exception {
        // Arrange
        for (int i = 0; i < 10; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("list.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger correctSizeCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (service.list().size() >= 10) {
                        correctSizeCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(threadCount, correctSizeCount.get());
    }
}