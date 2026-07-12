package platform.core.discovery;

import platform.core.discovery.error.DiscoveryErrorCode;
import platform.core.discovery.error.InvalidDiscoveryRequestException;
import platform.core.discovery.model.CapabilityId;
import platform.core.discovery.model.ContractId;
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

/**
 * <b>DiscoveryValidationTests</b>
 *
 * <p>Verifies the validation behavior of the {@link DefaultDiscoveryService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that invalid CapabilityId requests are rejected.</li>
 *   <li>Validates that invalid ContractId requests are rejected.</li>
 *   <li>Validates that invalid DiscoveryResult is rejected.</li>
 *   <li>Validates that validator returns ValidationResult.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see DiscoveryValidator
 */
public class DiscoveryValidationTests {

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
     * Verifies that invalid CapabilityId (null) is rejected.
     */
    public void testInvalidCapabilityIdRejected() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(null);
            throw new AssertionError("Should have thrown InvalidDiscoveryRequestException");
        } catch (InvalidDiscoveryRequestException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST
                    : "Error code should be DISCOVERY_INVALID_REQUEST";
        }
    }

    /**
     * Verifies that invalid ContractId (null) is rejected.
     */
    public void testInvalidContractIdRejected() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByContract(null);
            throw new AssertionError("Should have thrown InvalidDiscoveryRequestException");
        } catch (InvalidDiscoveryRequestException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST
                    : "Error code should be DISCOVERY_INVALID_REQUEST";
        }
    }

    /**
     * Verifies that invalid CapabilityId format is rejected.
     */
    public void testInvalidCapabilityIdFormatRejected() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            CapabilityId capabilityId = new CapabilityId("invalid id with spaces");
            service.resolveByCapability(capabilityId);
            throw new AssertionError("Should have thrown InvalidDiscoveryRequestException");
        } catch (InvalidDiscoveryRequestException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST
                    : "Error code should be DISCOVERY_INVALID_REQUEST";
        }
    }

    /**
     * Verifies that invalid ContractId format is rejected.
     */
    public void testInvalidContractIdFormatRejected() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            ContractId contractId = new ContractId("invalid id with spaces");
            service.resolveByContract(contractId);
            throw new AssertionError("Should have thrown InvalidDiscoveryRequestException");
        } catch (InvalidDiscoveryRequestException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST
                    : "Error code should be DISCOVERY_INVALID_REQUEST";
        }
    }

    /**
     * Verifies that DiscoveryValidator returns ValidationResult.
     */
    public void testValidatorReturnsValidationResult() {
        // Arrange
        DiscoveryValidator validator = new DiscoveryValidator();
        CapabilityId capabilityId = new CapabilityId("test-capability");

        // Act
        var result = validator.validateCapabilityId(capabilityId);

        // Assert
        assert result != null : "ValidationResult should not be null";
        assert result.isValid() : "Valid capability should pass validation";
    }

    /**
     * Verifies that DiscoveryValidator catches invalid CapabilityId.
     */
    public void testValidatorCatchesInvalidCapabilityId() {
        // Arrange
        DiscoveryValidator validator = new DiscoveryValidator();

        // Act
        var result = validator.validateCapabilityId(new CapabilityId("invalid id"));

        // Assert
        assert !result.isValid() : "Invalid capability should fail validation";
        assert !result.errors().isEmpty() : "Should have validation errors";
    }

    /**
     * Verifies that DiscoveryValidator catches invalid ContractId.
     */
    public void testValidatorCatchesInvalidContractId() {
        // Arrange
        DiscoveryValidator validator = new DiscoveryValidator();

        // Act
        var result = validator.validateContractId(new ContractId("invalid id"));

        // Assert
        assert !result.isValid() : "Invalid contract should fail validation";
        assert !result.errors().isEmpty() : "Should have validation errors";
    }

    /**
     * Verifies that DiscoveryValidator validates DiscoveryResult.
     */
    public void testValidatorValidatesDiscoveryResult() {
        // Arrange
        DiscoveryValidator validator = new DiscoveryValidator();
        DiscoveryResult result = new DiscoveryResult(
                new CapabilityId("cap"),
                new KernelId("kernel"),
                new ContractId("contract"),
                ResolutionStatus.FOUND
        );

        // Act
        var validationResult = validator.validateDiscoveryResult(result);

        // Assert
        assert validationResult != null : "ValidationResult should not be null";
        assert validationResult.isValid() : "Valid DiscoveryResult should pass validation";
    }

    /**
     * Verifies that DiscoveryValidator catches null DiscoveryResult.
     */
    public void testValidatorCatchesNullDiscoveryResult() {
        // Arrange
        DiscoveryValidator validator = new DiscoveryValidator();

        // Act & Assert
        try {
            validator.validateDiscoveryResult(null);
            throw new AssertionError("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}