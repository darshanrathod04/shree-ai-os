package platform.core.lifecycle;

import platform.core.lifecycle.api.LifecycleService;
import platform.core.lifecycle.engine.LifecycleTransitionEngine;
import platform.core.lifecycle.model.KernelState;
import platform.core.lifecycle.service.DefaultLifecycleService;
import platform.core.lifecycle.validator.LifecycleValidator;
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
 * <b>LifecycleInitializationTests</b>
 *
 * <p>Verifies the initialization behavior of the {@link DefaultLifecycleService}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that initialize() transitions from CREATED to INITIALIZED.</li>
 *   <li>Validates that default state is CREATED.</li>
 *   <li>Validates that duplicate initialization is idempotent.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultLifecycleService
 */
public class LifecycleInitializationTests {

    private LifecycleService createLifecycleService() {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        LifecycleValidator validator = new LifecycleValidator();
        LifecycleTransitionEngine engine = new LifecycleTransitionEngine(validator);
        return new DefaultLifecycleService(registry, validator, engine);
    }

    private KernelId registerKernel(LifecycleService service, String id) {
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId(id);
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel " + id, "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register(id, kernel);
        return kernelId;
    }

    /**
     * Verifies that a kernel starts in CREATED state.
     */
    public void testDefaultStateIsCreated() {
        // Arrange
        var service = createLifecycleService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act
        KernelState state = service.state(kernelId);

        // Assert
        assert state == KernelState.CREATED : "Default state should be CREATED";
    }

    /**
     * Verifies that initialize() transitions from CREATED to INITIALIZED.
     */
    public void testInitializeTransitionsToInitialized() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId("test-kernel");
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel", "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register("test-kernel", kernel);

        LifecycleValidator validator = new LifecycleValidator();
        LifecycleTransitionEngine engine = new LifecycleTransitionEngine(validator);
        DefaultLifecycleService service = new DefaultLifecycleService(registry, validator, engine);

        // Act
        boolean result = service.initialize(kernelId);

        // Assert
        assert result : "initialize() should return true";
        assert service.state(kernelId) == KernelState.INITIALIZED : "State should be INITIALIZED";
    }

    /**
     * Verifies that duplicate initialization is idempotent.
     */
    public void testDuplicateInitializationIsIdempotent() {
        // Arrange
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        KernelRegistry registry = new DefaultKernelRegistry(registryValidator);
        KernelId kernelId = new KernelId("test-kernel");
        KernelVersion version = new KernelVersion(1, 0, 0);
        Set<String> tags = new HashSet<>();
        tags.add("test");
        KernelMetadata metadata = new KernelMetadata(
                "Test Kernel", "Test description", "Test Author", tags, "test-category", Instant.now()
        );
        RegisteredKernel kernel = new RegisteredKernel(kernelId, version, metadata);
        registry.register("test-kernel", kernel);

        LifecycleValidator validator = new LifecycleValidator();
        LifecycleTransitionEngine engine = new LifecycleTransitionEngine(validator);
        DefaultLifecycleService service = new DefaultLifecycleService(registry, validator, engine);

        // Act
        boolean first = service.initialize(kernelId);
        boolean second = service.initialize(kernelId);

        // Assert
        assert first : "First initialize() should return true";
        assert second : "Second initialize() should return true (idempotent)";
        assert service.state(kernelId) == KernelState.INITIALIZED : "State should remain INITIALIZED";
    }

    /**
     * Verifies that initialize() returns false for unregistered kernels.
     */
    public void testInitializeReturnsFalseForUnregisteredKernel() {
        // Arrange
        var service = createLifecycleService();
        KernelId kernelId = new KernelId("unregistered-kernel");

        // Act
        boolean result = service.initialize(kernelId);

        // Assert
        assert !result : "initialize() should return false for unregistered kernel";
    }

    /**
     * Verifies that state() returns CREATED for uninitialized kernels.
     */
    public void testStateReturnsCreatedForUninitializedKernel() {
        // Arrange
        var service = createLifecycleService();
        KernelId kernelId = new KernelId("uninitialized-kernel");

        // Act
        KernelState state = service.state(kernelId);

        // Assert
        assert state == KernelState.CREATED : "State should be CREATED for uninitialized kernel";
    }
}