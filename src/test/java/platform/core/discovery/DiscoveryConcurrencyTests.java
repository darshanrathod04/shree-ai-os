package platform.core.discovery;

import platform.core.discovery.model.CapabilityId;
import platform.core.discovery.model.DiscoveryResult;
import platform.core.discovery.model.ResolutionStatus;
import platform.core.discovery.service.DefaultDiscoveryService;
import platform.core.discovery.validator.DiscoveryValidator;
import platform.core.registry.api.KernelRegistry;
import platform.core.registry.model.KernelId;
import platform.core.registry.model.KernelMetadata;
import platform.core.registry.model.KernelVersion;
import platform.core.registry.model.RegisteredKernel;
import platform.core.registry.service.DefaultKernelRegistry;
import platform.core.registry.validator.KernelRegistrationValidator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>DiscoveryConcurrencyTests</b>
 *
 * <p>Verifies the thread-safety of the {@link DefaultDiscoveryService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that concurrent capability lookups do not cause data corruption.</li>
 *   <li>Validates that concurrent operations return correct DiscoveryResult.</li>
 *   <li>Validates that thread safety is preserved under concurrent load.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see KernelRegistry
 */
public class DiscoveryConcurrencyTests {

    private DefaultDiscoveryService createDiscoveryService() {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        DiscoveryValidator discoveryValidator = new DiscoveryValidator();
        return new DefaultDiscoveryService(registry, discoveryValidator);
    }

    private RegisteredKernel createKernelWithCapability(String kernelId, String capability) {
        KernelId id = new KernelId(kernelId);
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add(capability);
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel " + kernelId,
                "Test description",
                "Test Author",
                tags,
                "test-category",
                Instant.now()
        );
        return new RegisteredKernel(id, version, metadata);
    }

    /**
     * Verifies that 100 concurrent capability lookups do not cause data corruption.
     */
    public void testConcurrentCapabilityLookupsNoDataCorruption() throws Exception {
        // Arrange
        int threadCount = 100;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    CapabilityId capabilityId = new CapabilityId("text-generation");
                    DiscoveryResult result = discoveryService.resolveByCapability(capabilityId).orElseThrow();
                    if (result.status() == ResolutionStatus.FOUND) {
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
                : "All lookups should succeed, but got " + successCount.get() + " successes and "
                + failureCount.get() + " failures";
    }

    /**
     * Verifies that concurrent lookups return correct DiscoveryResult.
     */
    public void testConcurrentLookupsReturnCorrectResult() throws Exception {
        // Arrange
        int threadCount = 100;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger correctResultCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    CapabilityId capabilityId = new CapabilityId("text-generation");
                    DiscoveryResult result = discoveryService.resolveByCapability(capabilityId).orElseThrow();
                    if (result.kernelId().value().equals("kernel-1")
                            && result.capabilityId().value().equals("text-generation")
                            && result.status() == ResolutionStatus.FOUND) {
                        correctResultCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore failures
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert correctResultCount.get() == threadCount
                : "All results should be correct, but got " + correctResultCount.get() + " correct results";
    }

    /**
     * Verifies that concurrent capability lookups and supports() calls work correctly.
     */
    public void testConcurrentLookupsAndSupports() throws Exception {
        // Arrange
        int threadCount = 50;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - half do lookups, half do supports
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 2) {
                        // Lookup
                        discoveryService.resolveByCapability(new CapabilityId("text-generation"));
                    } else {
                        // Supports
                        discoveryService.supports(new CapabilityId("text-generation"));
                    }
                } catch (Exception e) {
                    // Ignore failures
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert - verify final state is correct
        assert discoveryService.supports(new CapabilityId("text-generation"))
                : "Capability should still be supported after concurrent operations";
    }

    /**
     * Verifies that concurrent availableCapabilities() calls work correctly.
     */
    public void testConcurrentAvailableCapabilities() throws Exception {
        // Arrange
        int threadCount = 50;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithCapability("kernel-1", "text-generation");
        RegisteredKernel kernel2 = createKernelWithCapability("kernel-2", "image-generation");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    discoveryService.availableCapabilities();
                } catch (Exception e) {
                    // Ignore failures
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        var capabilities = discoveryService.availableCapabilities();
        assert capabilities.size() == 2 : "Should have 2 capabilities after concurrent operations";
    }

    /**
     * Verifies that concurrent registrations and discovery work correctly.
     */
    public void testConcurrentRegistrationsAndDiscovery() throws Exception {
        // Arrange
        int threadCount = 50;
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - register half, discover all
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index < threadCount / 2) {
                        // Register
                        RegisteredKernel kernel = createKernelWithCapability(
                                "concurrent-kernel-" + index,
                                "capability-" + index
                        );
                        registry.register("concurrent-kernel-" + index, kernel);
                    } else {
                        // Discover
                        try {
                            discoveryService.resolveByCapability(new CapabilityId("capability-" + (index - threadCount / 2)));
                        } catch (CapabilityNotFoundException e) {
                            // Expected for unregistered capabilities
                        }
                    }
                } catch (Exception e) {
                    // Ignore failures
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Assert
        assert discoveryService.availableCapabilities().size() == threadCount / 2
                : "Should have " + (threadCount / 2) + " capabilities after concurrent operations";
    }
}