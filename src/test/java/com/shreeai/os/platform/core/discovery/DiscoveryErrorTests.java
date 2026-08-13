package com.shreeai.os.platform.core.discovery;

import com.shreeai.os.platform.core.discovery.error.CapabilityNotFoundException;
import com.shreeai.os.platform.core.discovery.error.ContractNotFoundException;
import com.shreeai.os.platform.core.discovery.error.DiscoveryErrorCode;
import com.shreeai.os.platform.core.discovery.error.DiscoveryException;
import com.shreeai.os.platform.core.discovery.error.InvalidDiscoveryRequestException;
import com.shreeai.os.platform.core.discovery.model.CapabilityId;
import com.shreeai.os.platform.core.discovery.model.ContractId;
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
 * <b>DiscoveryErrorTests</b>
 *
 * <p>Verifies the error handling behavior of the {@link DefaultDiscoveryService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that CapabilityNotFoundException is thrown for missing capabilities.</li>
 *   <li>Validates that ContractNotFoundException is thrown for missing contracts.</li>
 *   <li>Validates that InvalidDiscoveryRequestException is thrown for invalid requests.</li>
 *   <li>Validates that DiscoveryErrorCode is correct for each error type.</li>
 *   <li>Validates that all exceptions extend DiscoveryException.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDiscoveryService
 * @see DiscoveryException
 */
public class DiscoveryErrorTests {

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
     * Verifies that CapabilityNotFoundException is thrown for missing capability.
     */
    public void testCapabilityNotFoundExceptionThrown() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown CapabilityNotFoundException");
        } catch (CapabilityNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CAPABILITY_NOT_FOUND
                    : "Error code should be DISCOVERY_CAPABILITY_NOT_FOUND";
        }
    }

    /**
     * Verifies that ContractNotFoundException is thrown for missing contract.
     */
    public void testContractNotFoundExceptionThrown() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByContract(new ContractId("nonexistent-contract"));
            throw new AssertionError("Should have thrown ContractNotFoundException");
        } catch (ContractNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CONTRACT_NOT_FOUND
                    : "Error code should be DISCOVERY_CONTRACT_NOT_FOUND";
        }
    }

    /**
     * Verifies that InvalidDiscoveryRequestException is thrown for invalid request.
     */
    public void testInvalidDiscoveryRequestExceptionThrown() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("invalid id with spaces"));
            throw new AssertionError("Should have thrown InvalidDiscoveryRequestException");
        } catch (InvalidDiscoveryRequestException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_INVALID_REQUEST
                    : "Error code should be DISCOVERY_INVALID_REQUEST";
        }
    }

    /**
     * Verifies that DiscoveryErrorCode is correct for CapabilityNotFoundException.
     */
    public void testCapabilityNotFoundHasCorrectErrorCode() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown CapabilityNotFoundException");
        } catch (CapabilityNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CAPABILITY_NOT_FOUND
                    : "Error code should be DISCOVERY_CAPABILITY_NOT_FOUND";
            assert e.error() != null : "Error object should not be null";
            assert e.error().code() == DiscoveryErrorCode.DISCOVERY_CAPABILITY_NOT_FOUND
                    : "Nested error code should match";
        }
    }

    /**
     * Verifies that DiscoveryErrorCode is correct for ContractNotFoundException.
     */
    public void testContractNotFoundHasCorrectErrorCode() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByContract(new ContractId("nonexistent-contract"));
            throw new AssertionError("Should have thrown ContractNotFoundException");
        } catch (ContractNotFoundException e) {
            assert e.code() == DiscoveryErrorCode.DISCOVERY_CONTRACT_NOT_FOUND
                    : "Error code should be DISCOVERY_CONTRACT_NOT_FOUND";
            assert e.error() != null : "Error object should not be null";
            assert e.error().code() == DiscoveryErrorCode.DISCOVERY_CONTRACT_NOT_FOUND
                    : "Nested error code should match";
        }
    }

    /**
     * Verifies that all discovery exceptions extend DiscoveryException.
     */
    public void testAllExceptionsExtendDiscoveryException() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert - CapabilityNotFoundException
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown exception");
        } catch (DiscoveryException e) {
            assert e instanceof CapabilityNotFoundException
                    : "Should be CapabilityNotFoundException";
        }

        // Act & Assert - ContractNotFoundException
        try {
            service.resolveByContract(new ContractId("nonexistent-contract"));
            throw new AssertionError("Should have thrown exception");
        } catch (DiscoveryException e) {
            assert e instanceof ContractNotFoundException
                    : "Should be ContractNotFoundException";
        }

        // Act & Assert - InvalidDiscoveryRequestException
        try {
            service.resolveByCapability(new CapabilityId("invalid id"));
            throw new AssertionError("Should have thrown exception");
        } catch (DiscoveryException e) {
            assert e instanceof InvalidDiscoveryRequestException
                    : "Should be InvalidDiscoveryRequestException";
        }
    }

    /**
     * Verifies that DiscoveryError contains all required fields.
     */
    public void testDiscoveryErrorContainsRequiredFields() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown CapabilityNotFoundException");
        } catch (CapabilityNotFoundException e) {
            assert e.error() != null : "Error should not be null";
            assert e.error().code() != null : "Error code should not be null";
            assert e.error().message() != null : "Error message should not be null";
            assert e.error().timestamp() != null : "Error timestamp should not be null";
            assert e.error().details() != null : "Error details should not be null";
        }
    }

    /**
     * Verifies that error messages are descriptive.
     */
    public void testErrorMessagesAreDescriptive() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown CapabilityNotFoundException");
        } catch (CapabilityNotFoundException e) {
            String message = e.getMessage();
            assert message != null && !message.isEmpty() : "Error message should not be empty";
            assert message.contains("nonexistent-capability") : "Error message should contain capability id";
        }
    }

    /**
     * Verifies that DiscoveryException can be caught as RuntimeException.
     */
    public void testDiscoveryExceptionIsRuntimeException() {
        // Arrange
        var service = createDiscoveryService();

        // Act & Assert
        try {
            service.resolveByCapability(new CapabilityId("nonexistent-capability"));
            throw new AssertionError("Should have thrown exception");
        } catch (RuntimeException e) {
            assert e instanceof DiscoveryException : "Should be DiscoveryException";
        }
    }
}