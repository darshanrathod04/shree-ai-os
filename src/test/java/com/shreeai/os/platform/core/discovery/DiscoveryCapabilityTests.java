package com.shreeai.os.platform.core.discovery;

import com.shreeai.os.platform.core.discovery.error.CapabilityNotFoundException;
import com.shreeai.os.platform.core.discovery.error.DiscoveryErrorCode;
import com.shreeai.os.platform.core.discovery.model.CapabilityId;
import com.shreeai.os.platform.core.discovery.model.DiscoveryResult;
import com.shreeai.os.platform.core.discovery.model.ResolutionStatus;
import com.shreeai.os.platform.core.discovery.service.DefaultDiscoveryService;
import com.shreeai.os.platform.core.discovery.validator.DiscoveryValidator;
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

/**
 * <b>DiscoveryCapabilityTests</b>
 *
 * <p>Verifies the capability resolution behavior of the {@link DefaultDiscoveryService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that existing capabilities can be resolved.</li>
 *   <li>Validates that missing capabilities throw CapabilityNotFoundException.</li>
 *   <li>Validates that supports() correctly reports capability existence.</li>
 *   <li>Validates that availableCapabilities() returns all capabilities.</li>
 *   <li>Validates that ResolutionStatus is correct in DiscoveryResult.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see KernelRegistry
 */
public class DiscoveryCapabilityTests {

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
     * Verifies that an existing capability can be resolved.
     */
    public void testResolveExistingCapability() {
        // Arrange
        var service = createDiscoveryService();
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        CapabilityId capabilityId = new CapabilityId("text-generation");
        DiscoveryResult result = discoveryService.resolveByCapability(capabilityId).orElseThrow();

        // Assert
        assert result.capabilityId().value().equals("text-generation") : "Capability should match";
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should match";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }

    /**
     * Verifies that resolving a missing capability throws CapabilityNotFoundException.
     */
    public void testResolveMissingCapability() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            CapabilityId capabilityId = new CapabilityId("nonexistent-capability");
            service.resolveByCapability(capabilityId);
            throw new AssertionError("Should have thrown CapabilityNotFoundException");
        } catch (CapabilityNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CAPABILITY_NOT_FOUND
                    : "Error code should be DISCOVERY_CAPABILITY_NOT_FOUND";
        }
    }

    /**
     * Verifies that supports() returns true for existing capabilities.
     */
    public void testSupportsReturnsTrueForExistingCapability() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        boolean supports = discoveryService.supports(new CapabilityId("text-generation"));

        // Assert
        assert supports : "supports() should return true for existing capability";
    }

    /**
     * Verifies that supports() returns false for missing capabilities.
     */
    public void testSupportsReturnsFalseForMissingCapability() {
        // Arrange
        var service = createDiscoveryService();

        // Act
        boolean supports = service.supports(new CapabilityId("nonexistent-capability"));

        // Assert
        assert !supports : "supports() should return false for missing capability";
    }

    /**
     * Verifies that availableCapabilities() returns all registered capabilities.
     */
    public void testAvailableCapabilitiesReturnsAllCapabilities() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithCapability("kernel-1", "text-generation");
        RegisteredKernel kernel2 = createKernelWithCapability("kernel-2", "image-generation");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        var capabilities = discoveryService.availableCapabilities();

        // Assert
        assert capabilities.size() == 2 : "Should return 2 capabilities";
        assert capabilities.contains(new CapabilityId("text-generation")) : "Should contain text-generation";
        assert capabilities.contains(new CapabilityId("image-generation")) : "Should contain image-generation";
    }

    /**
     * Verifies that availableCapabilities() returns empty collection when no kernels registered.
     */
    public void testAvailableCapabilitiesReturnsEmptyWhenNoKernels() {
        // Arrange
        var service = createDiscoveryService();

        // Act
        var capabilities = service.availableCapabilities();

        // Assert
        assert capabilities.isEmpty() : "Should return empty collection when no kernels";
    }

    /**
     * Verifies that ResolutionStatus.FOUND is returned for existing capabilities.
     */
    public void testResolutionStatusIsFoundForExistingCapability() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();

        // Assert
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }
}