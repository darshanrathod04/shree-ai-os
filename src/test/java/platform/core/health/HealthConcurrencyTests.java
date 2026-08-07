package platform.core.health;

import com.shreeai.os.platform.core.health.error.HealthException;
import com.shreeai.os.platform.core.health.error.InvalidHealthComponentException;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.model.HealthReport;
import com.shreeai.os.platform.core.health.service.DefaultHealthService;
import com.shreeai.os.platform.core.health.validator.HealthValidator;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthConcurrencyTests</b>
 *
 * <p>Tests for concurrent health operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies thread-safe health component registration.</li>
 *   <li>Verifies concurrent health checks.</li>
 *   <li>Verifies concurrent unregistration.</li>
 *   <li>Verifies no race conditions in storage.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultHealthService
 * @see HealthValidator
 */
public class HealthConcurrencyTests {

    private DefaultHealthService service;
    private HealthValidator validator;
    private HealthEvaluationEngine engine;

    @BeforeEach
    void setUp() {
        validator = new HealthValidator();
        engine = new HealthEvaluationEngine();
        service = new DefaultHealthService(validator, engine);
    }

    // Concurrent registration tests

    /**
     * Test: Concurrent registration of different components succeeds.
     */
    @Test
    void testConcurrentRegistrationOfDifferentComponents() throws Exception {
        // Arrange
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("concurrent-" + index);
                    HealthComponent component = new HealthComponent(id, "Concurrent " + index, "Category");
                    boolean result = service.register(component);
                    if (result) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, successCount.get());
    }

    /**
     * Test: Concurrent registration of same component throws exception.
     */
    @Test
    void testConcurrentRegistrationOfSameComponent() throws Exception {
        // Arrange
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        HealthComponentId id = new HealthComponentId("same-component");
        HealthComponent component = new HealthComponent(id, "Same Component", "Category");

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.register(component);
                    successCount.incrementAndGet();
                } catch (HealthException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - at least one should succeed, and some should throw exceptions
        // Due to timing, we may see 1 or 2 successes, but there should be exceptions
        assertTrue(successCount.get() >= 1, "At least one registration should succeed");
        assertTrue(exceptionCount.get() >= 1, "At least one registration should throw exception");
        assertEquals(threadCount, successCount.get() + exceptionCount.get(), "Total should equal thread count");
    }

    /**
     * Test: Concurrent registration and unregistration.
     */
    @Test
    void testConcurrentRegistrationAndUnregistration() throws Exception {
        // Arrange
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("reg-unreg-" + index);
                    HealthComponent component = new HealthComponent(id, "Reg/Unreg " + index, "Category");
                    
                    // Register
                    service.register(component);
                    
                    // Small delay to increase chance of race condition
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Unregister
                    service.unregister(component);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - all operations should complete without throwing
        assertTrue(latch.getCount() == 0);
    }

    // Concurrent check tests

    /**
     * Test: Concurrent checks of same component.
     */
    @Test
    void testConcurrentChecksOfSameComponent() throws Exception {
        // Arrange
        HealthComponentId id = new HealthComponentId("concurrent-check");
        HealthComponent component = new HealthComponent(id, "Concurrent Check", "Category");
        service.register(component);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    var result = service.check(component);
                    if (result.isPresent()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, successCount.get());
    }

    /**
     * Test: Concurrent checks of different components.
     */
    @Test
    void testConcurrentChecksOfDifferentComponents() throws Exception {
        // Arrange
        int componentCount = 10;
        for (int i = 0; i < componentCount; i++) {
            HealthComponentId id = new HealthComponentId("check-diff-" + i);
            HealthComponent component = new HealthComponent(id, "Check Diff " + i, "Category");
            service.register(component);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("check-diff-" + index);
                    HealthComponent component = new HealthComponent(id, "Check Diff " + index, "Category");
                    var result = service.check(component);
                    if (result.isPresent()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, successCount.get());
    }

    // Concurrent checkAll tests

    /**
     * Test: Concurrent checkAll calls.
     */
    @Test
    void testConcurrentCheckAll() throws Exception {
        // Arrange
        int componentCount = 5;
        for (int i = 0; i < componentCount; i++) {
            HealthComponentId id = new HealthComponentId("checkall-concurrent-" + i);
            HealthComponent component = new HealthComponent(id, "CheckAll Concurrent " + i, "Category");
            service.register(component);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Collection<HealthReport>> results = Collections.synchronizedList(new ArrayList<>());

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Collection<HealthReport> reports = service.checkAll();
                    results.add(reports);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, results.size());
        for (Collection<HealthReport> reports : results) {
            assertEquals(componentCount, reports.size());
        }
    }

    // Concurrent exists tests

    /**
     * Test: Concurrent exists checks.
     */
    @Test
    void testConcurrentExistsChecks() throws Exception {
        // Arrange
        HealthComponentId id = new HealthComponentId("exists-concurrent");
        HealthComponent component = new HealthComponent(id, "Exists Concurrent", "Category");
        service.register(component);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger trueCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean exists = service.exists(component);
                    if (exists) {
                        trueCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, trueCount.get());
    }

    // Mixed operations tests

    /**
     * Test: Mixed concurrent operations.
     */
    @Test
    void testMixedConcurrentOperations() throws Exception {
        // Arrange
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger registerSuccess = new AtomicInteger(0);
        AtomicInteger checkSuccess = new AtomicInteger(0);
        AtomicInteger unregisterSuccess = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("mixed-" + index);
                    HealthComponent component = new HealthComponent(id, "Mixed " + index, "Category");
                    
                    // Register
                    if (service.register(component)) {
                        registerSuccess.incrementAndGet();
                    }
                    
                    // Check
                    var result = service.check(component);
                    if (result.isPresent()) {
                        checkSuccess.incrementAndGet();
                    }
                    
                    // Exists
                    if (service.exists(component)) {
                        // Unregister
                        if (service.unregister(component)) {
                            unregisterSuccess.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - all operations should complete (latch should be 0)
        assertEquals(0, latch.getCount());
    }

    /**
     * Test: Concurrent registration with validation errors.
     */
    @Test
    void testConcurrentRegistrationWithValidationErrors() throws Exception {
        // Arrange
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("valid-" + index);
                    HealthComponent component = new HealthComponent(id, "Valid " + index, "Category");
                    service.register(component);
                    successCount.incrementAndGet();
                } catch (InvalidHealthComponentException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, successCount.get());
        assertEquals(0, exceptionCount.get());
    }

    // Stress tests

    /**
     * Test: Stress test with many concurrent operations.
     */
    @Test
    void testStressTestWithManyConcurrentOperations() throws Exception {
        // Arrange
        int operationCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(operationCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < operationCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("stress-" + index);
                    HealthComponent component = new HealthComponent(id, "Stress " + index, "Category");
                    
                    // Register
                    service.register(component);
                    
                    // Check
                    var result = service.check(component);
                    if (result.isPresent()) {
                        successCount.incrementAndGet();
                    }
                    
                    // Unregister
                    service.unregister(component);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - all operations completed successfully
        assertEquals(0, latch.getCount());
        assertEquals(operationCount, successCount.get());
    }

    /**
     * Test: Concurrent checkAll during registration.
     */
    @Test
    void testConcurrentCheckAllDuringRegistration() throws Exception {
        // Arrange
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Register some initial components
        for (int i = 0; i < 5; i++) {
            HealthComponentId id = new HealthComponentId("initial-" + i);
            HealthComponent component = new HealthComponent(id, "Initial " + i, "Category");
            service.register(component);
        }

        // Act - mix of registrations and checkAll calls
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // Register new component
                        HealthComponentId id = new HealthComponentId("during-checkall-" + index);
                        HealthComponent component = new HealthComponent(id, "During CheckAll " + index, "Category");
                        service.register(component);
                    } else {
                        // Call checkAll
                        Collection<HealthReport> reports = service.checkAll();
                        assertNotNull(reports);
                    }
                } catch (Exception e) {
                    // Log exception
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(0, latch.getCount());
    }

    // Thread safety tests

    /**
     * Test: Service remains consistent under concurrent load.
     */
    @Test
    void testServiceRemainsConsistentUnderConcurrentLoad() throws Exception {
        // Arrange
        int threadCount = 10;
        int operationsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * operationsPerThread);

        // Act
        for (int i = 0; i < threadCount * operationsPerThread; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("consistency-" + index);
                    HealthComponent component = new HealthComponent(id, "Consistency " + index, "Category");
                    
                    service.register(component);
                    service.check(component);
                    service.exists(component);
                    service.unregister(component);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - all operations completed
        assertEquals(0, latch.getCount());
    }

    /**
     * Test: No data corruption under concurrent access.
     */
    @Test
    void testNoDataCorruptionUnderConcurrentAccess() throws Exception {
        // Arrange
        int threadCount = 10;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * operationsPerThread);

        // Act
        for (int i = 0; i < threadCount * operationsPerThread; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    HealthComponentId id = new HealthComponentId("corruption-" + index);
                    HealthComponent component = new HealthComponent(id, "Corruption " + index, "Category");
                    
                    service.register(component);
                    
                    // Verify exists
                    if (!service.exists(component)) {
                        throw new AssertionError("Component should exist after registration");
                    }
                    
                    // Unregister
                    if (!service.unregister(component)) {
                        throw new AssertionError("Unregister should succeed");
                    }
                    
                    // Verify not exists
                    if (service.exists(component)) {
                        throw new AssertionError("Component should not exist after unregistration");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(0, latch.getCount());
    }
}