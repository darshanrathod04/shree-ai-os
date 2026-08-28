package com.shreeai.os.platform.core.registry;

import com.shreeai.os.platform.core.registry.error.InvalidKernelException;
import com.shreeai.os.platform.core.registry.error.RegistryErrorCode;
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
 * <b>KernelRegistryValidationTests</b>
 *
 * <p>Verifies the validation behavior of the {@link DefaultKernelRegistry}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that invalid kernels are rejected during registration.</li>
 *   <li>Validates that validation errors are reported via InvalidKernelException.</li>
 *   <li>Ensures the registry never bypasses validation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKernelRegistry
 * @see KernelRegistrationValidator
 * @see InvalidKernelException
 */
public class KernelRegistryValidationTests {

    private final DefaultKernelRegistry registry;
    private final KernelRegistrationValidator validator;

    public KernelRegistryValidationTests() {
        this.validator = new KernelRegistrationValidator();
        this.registry = new DefaultKernelRegistry(validator);
    }

    private RegisteredKernel createKernelWithId(KernelId kernelId, String name, String category) {
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                name,
                "Test description",
                "Test Author",
                tags,
                category,
                Instant.now()
        );
        return new RegisteredKernel(kernelId, version, metadata);
    }

    /**
     * Verifies that invalid KernelId (null) is rejected.
     */
    public void testInvalidKernelIdRejected() {
        // Arrange
        RegisteredKernel kernel = createKernelWithId(null, "Test", "test");

        // Act & Assert
        try {
            registry.register("kernel-null-id", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException for null KernelId");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
        }
    }

    /**
     * Verifies that invalid metadata (blank name) is rejected.
     */
    public void testInvalidMetadataRejected() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-blank-name");
        RegisteredKernel kernel = createKernelWithId(kernelId, "", "test");

        // Act & Assert
        try {
            registry.register("kernel-blank-name", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException for blank name");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("name") || e.getMessage().contains("blank")
                    : "Error message should mention name or blank";
        }
    }

    /**
     * Verifies that missing version (null) is rejected.
     */
    public void testMissingVersionRejected() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-no-version");
        KernelMetadata metadata = new KernelMetadata(
                "Test",
                "Test description",
                "Test Author",
                new HashSet<>(),
                "test",
                Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, null, metadata);

        // Act & Assert
        try {
            registry.register("kernel-no-version", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException for null version");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("Version")
                    : "Error message should mention Version";
        }
    }

    /**
     * Verifies that missing metadata (null) is rejected.
     */
    public void testMissingMetadataRejected() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-no-metadata");
        KernelVersion version = new KernelVersion(1, 0, 0);
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, null);

        // Act & Assert
        try {
            registry.register("kernel-no-metadata", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException for null metadata");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("Metadata")
                    : "Error message should mention Metadata";
        }
    }

    /**
     * Verifies that invalid KernelId format is rejected.
     */
    public void testInvalidKernelIdFormatRejected() {
        // Arrange
        KernelId kernelId = new KernelId("invalid id with spaces");
        RegisteredKernel kernel = createKernelWithId(kernelId, "Test", "test");

        // Act & Assert
        try {
            registry.register("invalid id with spaces", kernel);
            throw new AssertionError("Should have thrown InvalidKernelException for invalid format");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("format")
                    : "Error message should mention format";
        }
    }

    /**
     * Verifies that blank description is rejected.
     */
    public void testBlankDescriptionRejected() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-blank-desc");
        KernelMetadata metadata = new KernelMetadata(
                "Test",
                "",
                "Test Author",
                new HashSet<>(),
                "test",
                Instant.now()
        );
        RegisteredKernel kernel = createKernelWithId(kernelId, "Test", "test");
        // Override metadata with blank description
        RegisteredKernel kernelWithBlankDesc = new RegisteredKernel(
                kernelId,
                kernel.version(),
                metadata
        );

        // Act & Assert
        try {
            registry.register("kernel-blank-desc", kernelWithBlankDesc);
            throw new AssertionError("Should have thrown InvalidKernelException for blank description");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("description")
                    : "Error message should mention description";
        }
    }

    /**
     * Verifies that blank category is rejected.
     */
    public void testBlankCategoryRejected() {
        // Arrange
        KernelId kernelId = new KernelId("kernel-blank-cat");
        KernelMetadata metadata = new KernelMetadata(
                "Test",
                "Test description",
                "Test Author",
                new HashSet<>(),
                "",
                Instant.now()
        );
        RegisteredKernel kernel = createKernelWithId(kernelId, "Test", "test");
        // Override metadata with blank category
        RegisteredKernel kernelWithBlankCat = new RegisteredKernel(
                kernelId,
                kernel.version(),
                metadata
        );

        // Act & Assert
        try {
            registry.register("kernel-blank-cat", kernelWithBlankCat);
            throw new AssertionError("Should have thrown InvalidKernelException for blank category");
        } catch (InvalidKernelException e) {
            assert e.code() == RegistryErrorCode.REGISTRY_VALIDATION_FAILED
                    : "Error code should be REGISTRY_VALIDATION_FAILED";
            assert e.getMessage().contains("category")
                    : "Error message should mention category";
        }
    }
}