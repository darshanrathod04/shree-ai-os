package com.shreeai.os.platform.core.discovery;

import com.shreeai.os.platform.core.discovery.model.CapabilityId;
import com.shreeai.os.platform.core.discovery.model.ContractId;
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
 * <b>DiscoveryIntegrationTests</b>
 *
 * <p>Verifies the integration between Discovery Service and Kernel Registry.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that Discovery obtains kernel information ONLY through KernelRegistry.</li>
 *   <li>Validates that Registry remains the only source of truth.</li>
 *   <li>Validates the complete flow: Discovery → Registry → RegisteredKernel → DiscoveryResult.</li>
 *   <li>Validates that Discovery never duplicates Registry data.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see KernelRegistry
 */
public class DiscoveryIntegrationTests {

    private DefaultDiscoveryService createDiscoveryService() {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        DiscoveryValidator discoveryValidator = new DiscoveryValidator();
        return new DefaultDiscoveryService(registry, discoveryValidator);
    }

    private RegisteredKernel createKernelWithCapability(String kernelId, String capability, String category) {
        KernelId id = new KernelId(kernelId);
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add(capability);
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel " + kernelId,
                "Test description",
                "Test Author",
                tags,
                category,
                Instant.now()
        );
        return new RegisteredKernel(id, version, metadata);
    }

    /**
     * Verifies that Discovery obtains kernel information ONLY through KernelRegistry.
     */
    public void testDiscoveryUsesOnlyKernelRegistry() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();

        // Assert
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should come from Registry";
        assert result.capabilityId().value().equals("text-generation") : "Capability should match";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }

    /**
     * Verifies that Registry remains the only source of truth.
     */
    public void testRegistryIsSingleSourceOfTruth() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act - resolve capability
        DiscoveryResult result = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();

        // Assert - verify result reflects Registry state
        assert result.kernelId().value().equals("kernel-1") : "Should reflect Registry state";

        // Unregister from Registry
        registry.unregister("kernel-1");

        // Verify Discovery reflects the change (no cache)
        boolean supports = discoveryService.supports(new CapabilityId("text-generation"));
        assert !supports : "Discovery should reflect Registry state after unregistration";
    }

    /**
     * Verifies the complete flow: Discovery → Registry → RegisteredKernel → DiscoveryResult.
     */
    public void testCompleteDiscoveryFlow() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();

        // Assert - verify complete flow
        assert result != null : "DiscoveryResult should not be null";
        assert result.capabilityId() != null : "CapabilityId should not be null";
        assert result.kernelId() != null : "KernelId should not be null";
        assert result.contractId() != null : "ContractId should not be null";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";

        // Verify kernel information came from Registry
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should match Registry";
    }

    /**
     * Verifies that Discovery never duplicates Registry data.
     */
    public void testDiscoveryDoesNotDuplicateRegistryData() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        RegisteredKernel kernel2 = createKernelWithCapability("kernel-2", "image-generation", "vision-service");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act - resolve multiple capabilities
        DiscoveryResult result1 = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();
        DiscoveryResult result2 = discoveryService.resolveByCapability(new CapabilityId("image-generation"))
                .orElseThrow();

        // Assert - verify results point to Registry kernels, not copies
        assert result1.kernelId().value().equals("kernel-1") : "Should reference Registry kernel";
        assert result2.kernelId().value().equals("kernel-2") : "Should reference Registry kernel";

        // Verify Registry still has original kernels
        assert registry.findAll().size() == 2 : "Registry should still have 2 kernels";
    }

    /**
     * Verifies that contract resolution integrates with Registry.
     */
    public void testContractResolutionIntegratesWithRegistry() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByContract(new ContractId("llm-service"))
                .orElseThrow();

        // Assert
        assert result.contractId().value().equals("llm-service") : "Contract should match";
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should match Registry";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }

    /**
     * Verifies that availableCapabilities() reflects Registry state.
     */
    public void testAvailableCapabilitiesReflectsRegistryState() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        RegisteredKernel kernel2 = createKernelWithCapability("kernel-2", "image-generation", "vision-service");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        var capabilities = discoveryService.availableCapabilities();

        // Assert
        assert capabilities.size() == 2 : "Should have 2 capabilities";
        assert capabilities.contains(new CapabilityId("text-generation")) : "Should contain text-generation";
        assert capabilities.contains(new CapabilityId("image-generation")) : "Should contain image-generation";

        // Unregister one kernel
        registry.unregister("kernel-1");

        // Verify capabilities reflect Registry state
        var updatedCapabilities = discoveryService.availableCapabilities();
        assert updatedCapabilities.size() == 1 : "Should have 1 capability after unregistration";
        assert !updatedCapabilities.contains(new CapabilityId("text-generation")) : "Should not contain text-generation";
    }

    /**
     * Verifies that DiscoveryResult contains data from RegisteredKernel.
     */
    public void testDiscoveryResultContainsDataFromRegisteredKernel() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithCapability("kernel-1", "text-generation", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByCapability(new CapabilityId("text-generation"))
                .orElseThrow();

        // Assert - verify DiscoveryResult contains data from RegisteredKernel
        assert result.kernelId().value().equals(kernel.kernelId().value()) : "KernelId should match RegisteredKernel";
        assert result.capabilityId().value().equals("text-generation") : "Capability should match";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }
}