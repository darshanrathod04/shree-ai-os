package platform.core.registry;

import platform.core.registry.error.DuplicateKernelException;
import platform.core.registry.error.InvalidKernelException;
import platform.core.registry.error.KernelNotFoundException;
import platform.core.registry.error.RegistryErrorCode;
import platform.core.registry.error.RegistryException;
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
 * <b>KernelRegistryErrorTests</b>
 *
 * <p>Verifies the error handling behavior of the {@link DefaultKernelRegistry}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that duplicate registration throws DuplicateKernelException.</li>
 *   <li>Validates that invalid registration throws InvalidKernelException.</li>
 *   <li>Validates that RegistryErrorCode is correct for each error type.</li>
 *   <li>Ensures all registry errors extend RegistryException.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKernelRegistry
 * @see RegistryException
 * @see DuplicateKernelException
 * @see InvalidKernelException
 */
public class KernelRegistryErrorTests {

    private final DefaultKernelRegistry registry;
    private final KernelRegistrationValidator validator;

    public KernelRegistryErrorTests() {
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
     * Verifies that duplicate registration throws DuplicateKernelException.
     */
    public void testDuplicateRegistrationThrowsDuplicateKernelException() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-dup", "Duplicate Test", "test");
        registry.register("kernel-dup", kernel1);

        // Act & Assert
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-dup", "Duplicate Test 2", "test");
            registry.register("kernel-dup", kernel2);
            throw new AssertionError("Should have thrown DuplicateKernelException");
        } catch (DuplicateKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_DUPLICATE_KERNEL
                    : "Error code should be REGISTRY_DUPLICATE_KERNEL";
            assert e.getMessage().contains("kernel-dup")
                    : "Error message should contain kernel id";
        }
    }

    /**
     * Verifies that invalid registration throws InvalidKernelException.
     */
    public void testInvalidRegistrationThrowsInvalidKernelException() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-invalid", "Invalid Test", "test");
        // Create invalid metadata with blank name
        KernelMetadata invalidMetadata = new KernelMetadata(
                "",
                "Test description",
                "Test Author",
                new HashSet<>(),
                "test",
                Instant.now()
        );
        RegisteredKernel invalidKernel = new RegisteredKernel(
                kernel.kernelId(),
                kernel.version(),
                invalidMetadata
        );

        // Act & Assert
        try {
            registry.register("kernel-invalid", invalidKernel);
            throw new AssertionError("Should have thrown InvalidKernelException");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
        }
    }

    /**
     * Verifies that RegistryErrorCode is correct for duplicate registration.
     */
    public void testDuplicateRegistrationHasCorrectErrorCode() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-code-dup", "Code Test", "test");
        registry.register("kernel-code-dup", kernel1);

        // Act & Assert
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-code-dup", "Code Test 2", "test");
            registry.register("kernel-code-dup", kernel2);
            throw new AssertionError("Should have thrown DuplicateKernelException");
        } catch (DuplicateKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_DUPLICATE_KERNEL
                    : "Error code should be REGISTRY_DUPLICATE_KERNEL";
            assert e.error() != null : "Error object should not be null";
            assert e.error().code() == RegistryErrorCode.REGISTRY_DUPLICATE_KERNEL
                    : "Nested error code should match";
        }
    }

    /**
     * Verifies that RegistryErrorCode is correct for invalid registration.
     */
    public void testInvalidRegistrationHasCorrectErrorCode() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-code-invalid");
        KernelMetadata metadata = new KernelMetadata(
                "",
                "Test description",
                "Test Author",
                new HashSet<>(),
                "test",
                Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(
                kernelId,
                new KernelVersion(1, 0, 0),
                metadata
        );

        // Act & Assert
        try {
            registry.register("kernel-code-invalid", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.error() != null : "Error object should not be null";
            assert e.error().code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Nested error code should match";
        }
    }

    /**
     * Verifies that all registry exceptions extend RegistryException.
     */
    public void testAllExceptionsExtendRegistryException() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-exc-1", "Exception Test 1", "test");
        registry.register("kernel-exc-1", kernel1);

        // Act & Assert - DuplicateKernelException
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-exc-1", "Exception Test 2", "test");
            registry.register("kernel-exc-1", kernel2);
            throw new AssertionError("Should have thrown exception");
        } catch (RegistryException e) {
            assert e instanceof DuplicateKernelException
                    : "Should be DuplicateKernelException";
        }

        // Act & Assert - InvalidKernelException
        KernelId kernelId = new KernelId("kernel-exc-2");
        KernelMetadata metadata = new KernelMetadata(
                "",
                "Test description",
                "Test Author",
                new HashSet<>(),
                "test",
                Instant.now()
        );
        RegisteredKernel invalidKernel = new RegisteredKernel(
                kernelId,
                new KernelVersion(1, 0, 0),
                metadata
        );

        try {
            registry.register("kernel-exc-2", invalidKernel);
            throw new AssertionError("Should have thrown exception");
        } catch (RegistryException e) {
            assert e instanceof InvalidKernelException
                    : "Should be InvalidKernelException";
        }
    }

    /**
     * Verifies that RegistryError contains all required fields.
     */
    public void testRegistryErrorContainsRequiredFields() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-error-fields", "Error Fields Test", "test");
        registry.register("kernel-error-fields", kernel1);

        // Act & Assert
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-error-fields", "Error Fields Test 2", "test");
            registry.register("kernel-error-fields", kernel2);
            throw new AssertionError("Should have thrown DuplicateKernelException");
        } catch (DuplicateKernelException e) {
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
        RegisteredKernel kernel1 = createValidKernel("kernel-msg", "Message Test", "test");
        registry.register("kernel-msg", kernel1);

        // Act & Assert
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-msg", "Message Test 2", "test");
            registry.register("kernel-msg", kernel2);
            throw new AssertionError("Should have thrown DuplicateKernelException");
        } catch (DuplicateKernelException e) {
            String message = e.getMessage();
            assert message != null && !message.isEmpty() : "Error message should not be empty";
            assert message.contains("kernel-msg") : "Error message should contain kernel id";
        }
    }
}