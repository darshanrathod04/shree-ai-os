package platform.core.lifecycle;

import com.shreeai.os.platform.core.lifecycle.error.InvalidTransitionException;
import com.shreeai.os.platform.core.lifecycle.error.KernelNotInitializedException;
import com.shreeai.os.platform.core.lifecycle.model.KernelState;
import com.shreeai.os.platform.core.lifecycle.service.DefaultLifecycleService;
import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;
import com.shreeai.os.platform.core.lifecycle.validator.LifecycleValidator;
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
 * <b>LifecycleTransitionTests</b>
 *
 * <p>Verifies all allowed and rejected lifecycle state transitions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates all allowed transitions execute successfully.</li>
 *   <li>Validates all illegal transitions are rejected.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultLifecycleService
 */
public class LifecycleTransitionTests {

    private DefaultLifecycleService createReadyService() {
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
        service.initialize(kernelId);
        service.start(kernelId);
        return service;
    }

    // ===== Allowed Transitions =====

    /**
     * Verifies CREATED → INITIALIZED is allowed.
     */
    public void testCreatedToInitialized() {
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
        assert result : "CREATED -> INITIALIZED should be allowed";
        assert service.state(kernelId) == KernelState.INITIALIZED : "State should be INITIALIZED";
    }

    /**
     * Verifies INITIALIZED → RUNNING is allowed.
     */
    public void testInitializedToRunning() {
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
        service.initialize(kernelId);

        // Act
        boolean result = service.start(kernelId);

        // Assert
        assert result : "INITIALIZED -> RUNNING should be allowed";
        assert service.state(kernelId) == KernelState.RUNNING : "State should be RUNNING";
    }

    /**
     * Verifies RUNNING → SUSPENDED is allowed.
     */
    public void testRunningToSuspended() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act
        boolean result = service.suspend(kernelId);

        // Assert
        assert result : "RUNNING -> SUSPENDED should be allowed";
        assert service.state(kernelId) == KernelState.SUSPENDED : "State should be SUSPENDED";
    }

    /**
     * Verifies SUSPENDED → RUNNING is allowed.
     */
    public void testSuspendedToRunning() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");
        service.suspend(kernelId);

        // Act
        boolean result = service.resume(kernelId);

        // Assert
        assert result : "SUSPENDED -> RUNNING should be allowed";
        assert service.state(kernelId) == KernelState.RUNNING : "State should be RUNNING";
    }

    /**
     * Verifies RUNNING → STOPPED is allowed.
     */
    public void testRunningToStopped() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act
        boolean result = service.stop(kernelId);

        // Assert
        assert result : "RUNNING -> STOPPED should be allowed";
        assert service.state(kernelId) == KernelState.STOPPED : "State should be STOPPED";
    }

    // ===== Rejected Transitions =====

    /**
     * Verifies CREATED → RUNNING is rejected.
     */
    public void testCreatedToRunningRejected() {
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

        // Act & Assert
        try {
            service.start(kernelId);
            throw new AssertionError("Should have thrown KernelNotInitializedException");
        } catch (KernelNotInitializedException e) {
            // Expected
        }
    }

    /**
     * Verifies STOPPED → RUNNING is rejected.
     */
    public void testStoppedToRunningRejected() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");
        service.stop(kernelId);

        // Act & Assert
        try {
            service.start(kernelId);
            throw new AssertionError("Should have thrown InvalidTransitionException");
        } catch (InvalidTransitionException e) {
            // Expected
        }
    }

    /**
     * Verifies SUSPENDED → SUSPENDED is idempotent.
     */
    public void testSuspendedToSuspendedIdempotent() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");
        service.suspend(kernelId);

        // Act
        boolean result = service.suspend(kernelId);

        // Assert
        assert result : "SUSPENDED -> SUSPENDED should be idempotent (return true)";
        assert service.state(kernelId) == KernelState.SUSPENDED : "State should remain SUSPENDED";
    }

    /**
     * Verifies RUNNING → RUNNING is idempotent.
     */
    public void testRunningToRunningIdempotent() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act
        boolean result = service.start(kernelId);

        // Assert
        assert result : "RUNNING -> RUNNING should be idempotent (return true)";
        assert service.state(kernelId) == KernelState.RUNNING : "State should remain RUNNING";
    }

    /**
     * Verifies STOPPED → STOPPED is idempotent.
     */
    public void testStoppedToStoppedIdempotent() {
        var service = createReadyService();
        KernelId kernelId = new KernelId("test-kernel");
        service.stop(kernelId);

        // Act
        boolean result = service.stop(kernelId);

        // Assert
        assert result : "STOPPED -> STOPPED should be idempotent (return true)";
        assert service.state(kernelId) == KernelState.STOPPED : "State should remain STOPPED";
    }
}