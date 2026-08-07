package platform.core.registry;

import com.shreeai.os.platform.core.registry.error.RegistryErrorCode;
import com.shreeai.os.platform.core.registry.error.InvalidKernelException;
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
 * <b>KernelRegistryRegistrationTests</b>
 *
 * <p>Verifies the registration behavior of the {@link DefaultKernelRegistry}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that kernels can be registered successfully.</li>
 *   <li>Validates that registration enforces validation and duplicate detection.</li>
 *   <li>Ensures the registry size increases with each successful registration.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKernelRegistry
 * @see KernelRegistrationValidator
 */
public class KernelRegistryRegistrationTests {

    private final DefaultKernelRegistry registry;
    private final KernelRegistrationValidator validator;

    public KernelRegistryRegistrationTests() {
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
     * Verifies that a valid kernel can be registered successfully.
     */
    public void testRegisterValidKernel() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-1", "Test Kernel", "test");

        // Act
        boolean result = registry.register("kernel-1", kernel);

        // Assert
        assert result : "Registration should succeed";
        assert registry.exists("kernel-1") : "Kernel should exist after registration";
    }

    /**
     * Verifies that multiple kernels can be registered.
     */
    public void testRegisterMultipleKernels() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-1", "Kernel One", "test");
        RegisteredKernel kernel2 = createValidKernel("kernel-2", "Kernel Two", "test");
        RegisteredKernel kernel3 = createValidKernel("kernel-3", "Kernel Three", "test");

        // Act
        registry.register("kernel-1", kernel1);
        registry.register("kernel-2", kernel2);
        registry.register("kernel-3", kernel3);

        // Assert
        assert registry.exists("kernel-1") : "Kernel 1 should exist";
        assert registry.exists("kernel-2") : "Kernel 2 should exist";
        assert registry.exists("kernel-3") : "Kernel 3 should exist";
        assert registry.findAll().size() == 3 : "Registry should contain 3 kernels";
    }

    /**
     * Verifies that the registry size increases with each successful registration.
     */
    public void testRegistrySizeIncreases() {
        // Arrange
        int initialSize = registry.findAll().size();

        // Act
        RegisteredKernel kernel = createValidKernel("kernel-size", "Size Test", "test");
        registry.register("kernel-size", kernel);

        // Assert
        assert registry.findAll().size() == initialSize + 1 : "Registry size should increase by 1";
    }

    /**
     * Verifies that exists() returns true after registration.
     */
    public void testExistsReturnsTrueAfterRegistration() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-exists", "Exists Test", "test");

        // Act
        registry.register("kernel-exists", kernel);
        boolean exists = registry.exists("kernel-exists");

        // Assert
        assert exists : "exists() should return true for registered kernel";
    }

    /**
     * Verifies that duplicate registration throws DuplicateKernelException.
     */
    public void testDuplicateRegistrationThrowsException() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-dup", "Duplicate Test", "test");
        registry.register("kernel-dup", kernel);

        // Act & Assert
        try {
            RegisteredKernel kernel2 = createValidKernel("kernel-dup", "Duplicate Test 2", "test");
            registry.register("kernel-dup", kernel2);
            throw new AssertionError("Should have thrown InvalidKernelException for duplicate");
        } catch (InvalidKernelException e) {
            // Expected
            assert e.code() == RegistryErrorCode.REGISTRY_DUPLICATE_KERNEL
                    : "Error code should be REGISTRY_DUPLICATE_KERNEL";
        }
    }
}