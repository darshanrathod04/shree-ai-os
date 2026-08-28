package com.shreeai.os.platform.core.lifecycle;

import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;
import com.shreeai.os.platform.core.lifecycle.model.KernelState;
import com.shreeai.os.platform.core.lifecycle.service.DefaultLifecycleService;
import com.shreeai.os.platform.core.lifecycle.validator.LifecycleValidator;
import com.shreeai.os.platform.core.registry.api.KernelRegistry;
import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.model.KernelMetadata;
import com.shreeai.os.platform.core.registry.model.KernelVersion;
import com.shreeai.os.platform.core.registry.model.RegisteredKernel;
import com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry;
import com.shreeai.os.platform.core.registry.validator.KernelRegistrationValidator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>LifecycleConcurrencyTests</b>
 *
 * <p>Verifies the thread-safety of the {@link DefaultLifecycleService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that concurrent transitions do not cause data corruption.</li>
 *   <li>Validates that concurrent reads return correct state.</li>
 *   <li>Validates that mixed transition/read workloads work correctly.</li>
 *   <li>Validates no race conditions under concurrent load.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultLifecycleService
 */
public class LifecycleConcurrencyTests {

    private DefaultLifecycleService createReadyService() {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId("test-kernel");
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel", "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register("test-kernel", kernel);

        LifecycleValidator validator = new LifecycleValidator();
        LifecycleTransitionEngine engine = new LifecycleTransitionEngine(validator);
        DefaultLifecycleService service = new DefaultLifecycleService(registry, validator, engine);
        service.initialize(kernelId);
        service.start(kernelId);
        return service;
    }

    /**
     * Verifies that 100 concurrent transitions do not cause data corruption.
     */
    public void testConcurrentTransitionsNoDataCorruption() throws Exception {
        // Arrange
        int threadCount = 100;
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act - all threads try to suspend
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean result = service.suspend(kernelId);
                    if (result) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert successCount.get() >= 1 : "At least one suspend should succeed";
        assert service.state(kernelId) == KernelState.SUSPENDED : "Final state should be SUSPENDED";
    }

    /**
     * Verifies that 100 concurrent reads return correct state.
     */
    public void testConcurrentReadsReturnCorrectState() throws Exception {
        // Arrange
        int threadCount = 100;
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger correctStateCount = new AtomicInteger(0);

        // Act - all threads read state
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    KernelState state = service.state(kernelId);
                    if (state == KernelState.RUNNING) {
                        correctStateCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert correctStateCount.get() == threadCount : "All reads should return RUNNING";
    }

    /**
     * Verifies that mixed transition/read workloads work correctly.
     */
    public void testMixedTransitionAndReadWorkload() throws Exception {
        // Arrange
        int threadCount = 50;
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - half do transitions, half do reads
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 2) {
                        // Transition: suspend and resume
                        service.suspend(kernelId);
                        service.resume(kernelId);
                    } else {
                        // Read
                        service.state(kernelId);
                        service.health(kernelId);
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert - final state should be RUNNING (resume was called)
        assert service.state(kernelId) == KernelState.RUNNING : "Final state should be RUNNING";
    }

    /**
     * Verifies that concurrent initialize/start/stop sequence works correctly.
     */
    public void testConcurrentLifecycleSequence() throws Exception {
        // Arrange
        int threadCount = 30;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId("test-kernel");
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel", "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register("test-kernel", kernel);

        LifecycleValidator validator = new LifecycleValidator();
        LifecycleTransitionEngine engine = new LifecycleTransitionEngine(validator);
        DefaultLifecycleService service = new DefaultLifecycleService(registry, validator, engine);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - concurrent lifecycle operations
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 3) {
                        service.initialize(kernelId);
                    } else if (index < 2 * threadCount / 3) {
                        service.start(kernelId);
                    } else {
                        service.state(kernelId);
                    }
                } catch (Exception e) {
                    // Ignore expected exceptions (e.g., not initialized)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert - final state should be RUNNING
        assert service.state(kernelId) == KernelState.RUNNING : "Final state should be RUNNING";
    }

    /**
     * Verifies that concurrent health reads work correctly.
     */
    public void testConcurrentHealthReads() throws Exception {
        // Arrange
        int threadCount = 50;
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger healthyCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    var health = service.health(kernelId);
                    if ("HEALTHY".equals(health.status())) {
                        healthyCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert healthyCount.get() == threadCount : "All health reads should return HEALTHY";
    }
}