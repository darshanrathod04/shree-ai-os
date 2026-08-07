package platform.core.registry;

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
 * <b>KernelRegistryConcurrencyTests</b>
 *
 * <p>Verifies the thread-safety of the {@link DefaultKernelRegistry}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that concurrent registrations do not cause data corruption.</li>
 *   <li>Validates that the final registry size is correct after concurrent operations.</li>
 *   <li>Validates that no duplicate entries exist after concurrent registrations.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKernelRegistry
 * @see KernelRegistrationValidator
 */
public class KernelRegistryConcurrencyTests {

    private final DefaultKernelRegistry registry;
    private final KernelRegistrationValidator validator;

    public KernelRegistryConcurrencyTests() {
        this.validator = new KernelRegistrationValidator();
        this.registry = new DefaultKernelRegistry(validator);
    }

    private RegisteredKernel createValidKernel(String id, String name, String category) {
        KernelId kernelId = new KernelId(id);
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                name,
                "Test kernel description",
                "Test Author",
                tags,
                category,
                Instant.now()
        );
        return new RegisteredKernel(kernelId, version, metadata);
    }

    /**
     * Verifies that 100 concurrent registrations do not cause data corruption.
     */
    public void testConcurrentRegistrationsNoDataCorruption() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    RegisteredKernel kernel = createValidKernel(
                            "concurrent-kernel-" + index,
                            "Concurrent Kernel " + index,
                            "test"
                    );
                    boolean result = registry.register("concurrent-kernel-" + index, kernel);
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
        assert successCount.get() == threadCount
                : "All registrations should succeed, but got " + successCount.get() + " successes and "
                + failureCount.get() + " failures";
    }

    /**
     * Verifies that the final registry size is correct after concurrent registrations.
     */
    public void testConcurrentRegistrationsCorrectSize() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    RegisteredKernel kernel = createValidKernel(
                            "size-kernel-" + index,
                            "Size Kernel " + index,
                            "test"
                    );
                    registry.register("size-kernel-" + index, kernel);
                } catch (Exception e) {
                    // Ignore failures for this test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert registry.findAll().size() == threadCount
                : "Registry should contain " + threadCount + " kernels, but found "
                + registry.findAll().size();
    }

    /**
     * Verifies that no duplicate entries exist after concurrent registrations.
     */
    public void testConcurrentRegistrationsNoDuplicates() throws Exception {
        // Arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    RegisteredKernel kernel = createValidKernel(
                            "unique-kernel-" + index,
                            "Unique Kernel " + index,
                            "test"
                    );
                    registry.register("unique-kernel-" + index, kernel);
                } catch (Exception e) {
                    // Ignore failures for this test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert - verify no duplicates by checking all kernel IDs are unique
        Set<KernelId> kernelIds = new HashSet<>();
        for (RegisteredKernel kernel : registry.findAll()) {
            boolean added = kernelIds.add(kernel.kernelId());
            assert added : "Duplicate kernel found: " + kernel.kernelId().value();
        }
    }

    /**
     * Verifies that concurrent registrations and lookups work correctly.
     */
    public void testConcurrentRegistrationsAndLookups() throws Exception {
        // Arrange
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - register half, then lookup all
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 2) {
                        // Register
                        RegisteredKernel kernel = createValidKernel(
                                "mixed-kernel-" + index,
                                "Mixed Kernel " + index,
                                "test"
                        );
                        registry.register("mixed-kernel-" + index, kernel);
                    } else {
                        // Lookup
                        registry.find("mixed-kernel-" + (index - threadCount / 2));
                    }
                } catch (Exception e) {
                    // Ignore failures for this test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert registry.findAll().size() == threadCount / 2
                : "Registry should contain " + (threadCount / 2) + " kernels";
    }

    /**
     * Verifies that concurrent unregistrations work correctly.
     */
    public void testConcurrentUnregistrations() throws Exception {
        // Arrange
        int kernelCount = 50;
        for (int i = 0; i < kernelCount; i++) {
            RegisteredKernel kernel = createValidKernel(
                    "unregister-kernel-" + i,
                    "Unregister Kernel " + i,
                    "test"
            );
            registry.register("unregister-kernel-" + i, kernel);
        }

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    registry.unregister("unregister-kernel-" + index);
                } catch (Exception e) {
                    // Ignore failures for this test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert registry.findAll().isEmpty()
                : "Registry should be empty after unregistering all kernels";
    }
}