package platform.core.discovery;

import com.shreeai.os.platform.core.discovery.error.ContractNotFoundException;
import com.shreeai.os.platform.core.discovery.error.DiscoveryErrorCode;
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
 * <b>DiscoveryContractTests</b>
 *
 * <p>Verifies the contract resolution behavior of the {@link DefaultDiscoveryService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that existing contracts can be resolved.</li>
 *   <li>Validates that missing contracts throw ContractNotFoundException.</li>
 *   <li>Validates that DiscoveryResult contains correct information.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see KernelRegistry
 */
public class DiscoveryContractTests {

    private DefaultDiscoveryService createDiscoveryService() {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        DiscoveryValidator discoveryValidator = new DiscoveryValidator();
        return new DefaultDiscoveryService(registry, discoveryValidator);
    }

    private RegisteredKernel createKernelWithContract(String kernelId, String category) {
        KernelId id = new KernelId(kernelId);
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test-tag");
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
     * Verifies that an existing contract can be resolved.
     */
    public void testResolveExistingContract() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithContract("kernel-1", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        ContractId contractId = new ContractId("llm-service");
        DiscoveryResult result = discoveryService.resolveByContract(contractId).orElseThrow();

        // Assert
        assert result.contractId().value().equals("llm-service") : "Contract should match";
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should match";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
    }

    /**
     * Verifies that resolving a missing contract throws ContractNotFoundException.
     */
    public void testResolveMissingContract() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            ContractId contractId = new ContractId("nonexistent-contract");
            service.resolveByContract(contractId);
            throw new AssertionError("Should have thrown ContractNotFoundException");
        } catch (ContractNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CONTRACT_NOT_FOUND
                    : "Error code should be DISCOVERY_CONTRACT_NOT_FOUND";
        }
    }

    /**
     * Verifies that DiscoveryResult contains correct information for contract resolution.
     */
    public void testDiscoveryResultContainsCorrectInformation() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel = createKernelWithContract("kernel-1", "llm-service");
        registry.register("kernel-1", kernel);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        ContractId contractId = new ContractId("llm-service");
        DiscoveryResult result = discoveryService.resolveByContract(contractId).orElseThrow();

        // Assert
        assert result.contractId() != null : "ContractId should not be null";
        assert result.kernelId() != null : "KernelId should not be null";
        assert result.capabilityId() != null : "CapabilityId should not be null";
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
        assert result.contractId().value().equals("llm-service") : "Contract should match";
        assert result.kernelId().value().equals("kernel-1") : "Kernel ID should match";
    }

    /**
     * Verifies that resolving a contract returns the correct kernel.
     */
    public void testResolveContractReturnsCorrectKernel() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithContract("kernel-1", "llm-service");
        RegisteredKernel kernel2 = createKernelWithContract("kernel-2", "embedding-service");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByContract(new ContractId("embedding-service"))
                .orElseThrow();

        // Assert
        assert result.kernelId().value().equals("kernel-2") : "Should return kernel-2";
        assert result.contractId().value().equals("embedding-service") : "Contract should match";
    }

    /**
     * Verifies that multiple kernels with the same contract can be resolved (returns first match).
     */
    public void testResolveContractWithMultipleMatches() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        RegisteredKernel kernel1 = createKernelWithContract("kernel-1", "llm-service");
        RegisteredKernel kernel2 = createKernelWithContract("kernel-2", "llm-service");
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);

        DiscoveryValidator validator = new DiscoveryValidator();
        DefaultDiscoveryService discoveryService = new DefaultDiscoveryService(registry, validator);

        // Act
        DiscoveryResult result = discoveryService.resolveByContract(new ContractId("llm-service"))
                .orElseThrow();

        // Assert
        assert result.status() == ResolutionStatus.FOUND : "Status should be FOUND";
        assert result.contractId().value().equals("llm-service") : "Contract should match";
    }
}