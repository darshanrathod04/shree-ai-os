package com.shreeai.os.platform.core.registry;

import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.model.KernelMetadata;
import com.shreeai.os.platform.core.registry.model.KernelVersion;
import com.shreeai.os.platform.core.registry.model.RegisteredKernel;
import com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry;
import com.shreeai.os.platform.core.registry.validator.KernelRegistrationValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <b>KernelRegistryLookupTests</b>
 *
 * <p>Verifies the lookup behavior of the {@link DefaultKernelRegistry}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that registered kernels can be found by identifier.</li>
 *   <li>Validates that missing kernels return empty Optional.</li>
 *   <li>Validates that findAll() returns an immutable collection.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKernelRegistry
 * @see KernelRegistrationValidator
 */
public class KernelRegistryLookupTests {

    private final DefaultKernelRegistry registry;
    private final KernelRegistrationValidator validator;

    public KernelRegistryLookupTests() {
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
     * Verifies that an existing kernel can be found by identifier.
     */
    public void testFindExistingKernel() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-find", "Find Test", "test");
        registry.register("kernel-find", kernel);

        // Act
        var found = registry.find("kernel-find");

        // Assert
        assert found.isPresent() : "Kernel should be found";
        assert found.get().kernelId().value().equals("kernel-find") : "Found kernel should have correct id";
        assert found.get().metadata().name().equals("Find Test") : "Found kernel should have correct name";
    }

    /**
     * Verifies that finding a missing kernel returns Optional.empty().
     */
    public void testFindMissingKernelReturnsEmpty() {
        // Arrange - no kernel registered with this id

        // Act
        var found = registry.find("nonexistent-kernel");

        // Assert
        assert found.isEmpty() : "Missing kernel should return empty Optional";
    }

    /**
     * Verifies that findAll() returns an immutable collection.
     */
    public void testFindAllReturnsImmutableCollection() {
        // Arrange
        RegisteredKernel kernel = createValidKernel("kernel-immutable", "Immutable Test", "test");
        registry.register("kernel-immutable", kernel);

        // Act
        Collection<RegisteredKernel> all = registry.findAll();

        // Assert
        assert all.size() == 1 : "Should contain 1 kernel";

        // Verify immutability by attempting to modify
        try {
            all.clear();
            throw new AssertionError("findAll() should return an immutable collection");
        } catch (UnsupportedOperationException e) {
            // Expected - collection is immutable
        }
    }

    /**
     * Verifies that findAll() returns an empty collection when no kernels are registered.
     */
    public void testFindAllReturnsEmptyWhenNoKernels() {
        // Arrange - fresh registry with no kernels

        // Act
        Collection<RegisteredKernel> all = registry.findAll();

        // Assert
        assert all.isEmpty() : "Should return empty collection when no kernels registered";
    }

    /**
     * Verifies that find() returns the correct kernel when multiple kernels are registered.
     */
    public void testFindReturnsCorrectKernelAmongMultiple() {
        // Arrange
        RegisteredKernel kernel1 = createValidKernel("kernel-a", "Kernel A", "test");
        RegisteredKernel kernel2 = createValidKernel("kernel-b", "Kernel B", "test");
        RegisteredKernel kernel3 = createValidKernel("kernel-c", "Kernel C", "test");

        registry.register("kernel-a", kernel1);
        registry.register("kernel-b", kernel2);
        registry.register("kernel-c", kernel3);

        // Act
        var foundB = registry.find("kernel-b");

        // Assert
        assert foundB.isPresent() : "Kernel B should be found";
        assert foundB.get().kernelId().value().equals("kernel-b") : "Should find kernel-b";
        assert foundB.get().metadata().name().equals("Kernel B") : "Should have correct name";
    }
}