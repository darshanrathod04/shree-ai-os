package platform.core.lifecycle;

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
 * <b>LifecycleIntegrationTests</b>
 *
 * <p>Verifies the complete integration of the Lifecycle subsystem.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates the complete flow: LifecycleService → LifecycleValidator → TransitionEngine → TransitionResult → State Update.</li>
 *   <li>Validates that all components work together correctly.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultLifecycleService
 * @see LifecycleValidator
 * @see LifecycleTransitionEngine
 */
public class LifecycleIntegrationTests {

    private DefaultLifecycleService createFullService() {
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
        return new DefaultLifecycleService(registry, validator, engine);
    }

    /**
     * Verifies the complete lifecycle flow: CREATED → INITIALIZED → RUNNING → STOPPED.
     */
    public void testCompleteLifecycleFlow() {
        // Arrange
        var service = createFullService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act & Assert - CREATED
        assert service.state(kernelId) == KernelState.CREATED : "Should start in CREATED";

        // Act & Assert - CREATED → INITIALIZED
        boolean initResult = service.initialize(kernelId);
        assert initResult : "initialize() should succeed";
        assert service.state(kernelId) == KernelState.INITIALIZED : "Should be INITIALIZED";

        // Act & Assert - INITIALIZED → RUNNING
        boolean startResult = service.start(kernelId);
        assert startResult : "start() should succeed";
        assert service.state(kernelId) == KernelState.RUNNING : "Should be RUNNING";

        // Act & Assert - RUNNING → STOPPED
        boolean stopResult = service.stop(kernelId);
        assert stopResult : "stop() should succeed";
        assert service.state(kernelId) == KernelState.STOPPED : "Should be STOPPED";
    }

    /**
     * Verifies the complete lifecycle flow with suspend/resume.
     */
    public void testLifecycleFlowWithSuspendResume() {
        // Arrange
        var service = createFullService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act - Full flow
        service.initialize(kernelId);
        service.start(kernelId);
        assert service.state(kernelId) == KernelState.RUNNING : "Should be RUNNING";

        // RUNNING → SUSPENDED
        boolean suspendResult = service.suspend(kernelId);
        assert suspendResult : "suspend() should succeed";
        assert service.state(kernelId) == KernelState.SUSPENDED : "Should be SUSPENDED";

        // SUSPENDED → RUNNING
        boolean resumeResult = service.resume(kernelId);
        assert resumeResult : "resume() should succeed";
        assert service.state(kernelId) == KernelState.RUNNING : "Should be RUNNING again";
    }

    /**
     * Verifies that the service correctly delegates to the validator and engine.
     */
    public void testServiceDelegatesToValidatorAndEngine() {
        // Arrange
        var service = createFullService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act - Attempt invalid transition (CREATED → RUNNING without initialize)
        try {
            service.start(kernelId);
            throw new AssertionError("Should have thrown exception for invalid transition");
        } catch (Exception e) {
            // Assert - exception indicates validator and engine were consulted
            assert e instanceof platform.core.lifecycle.error.KernelNotInitializedException
                    : "Should throw KernelNotInitializedException";
        }
    }

    /**
     * Verifies that health() returns correct status based on state.
     */
    public void testHealthReflectsState() {
        // Arrange
        var service = createFullService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act & Assert - CREATED state
        var healthCreated = service.health(kernelId);
        assert "UNKNOWN".equals(healthCreated.status()) : "CREATED state should have UNKNOWN health";

        // Initialize and start
        service.initialize(kernelId);
        service.start(kernelId);

        // Act & Assert - RUNNING state
        var healthRunning = service.health(kernelId);
        assert "HEALTHY".equals(healthRunning.status()) : "RUNNING state should have HEALTHY health";
    }

    /**
     * Verifies that the service, validator, and engine work together for all allowed transitions.
     */
    public void testAllComponentsWorkTogether() {
        // Arrange
        var service = createFullService();
        KernelId kernelId = new KernelId("test-kernel");

        // Act - Execute all allowed transitions
        service.initialize(kernelId);
        service.start(kernelId);
        service.suspend(kernelId);
        service.resume(kernelId);
        service.stop(kernelId);

        // Assert - Final state should be STOPPED
        assert service.state(kernelId) == KernelState.STOPPED : "Final state should be STOPPED";
    }

    /**
     * Verifies that the service correctly handles the Registry integration.
     */
    public void testServiceIntegratesWithRegistry() {
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

        // Act - Initialize (checks registry for kernel existence)
        boolean result = service.initialize(kernelId);

        // Assert
        assert result : "initialize() should succeed for registered kernel";
        assert service.state(kernelId) == KernelState.INITIALIZED : "State should be INITIALIZED";
    }

    /**
     * Verifies that the service handles unregistered kernels correctly.
     */
    public void testServiceHandlesUnregisteredKernel() {
        // Arrange
        var service = createFullService();
        KernelId unregisteredKernel = new KernelId("unregistered-kernel");

        // Act
        boolean result = service.initialize(unregisteredKernel);

        // Assert
        assert !result : "initialize() should return false for unregistered kernel";
        assert service.state(unregisteredKernel) == KernelState.CREATED : "State should remain CREATED";
    }
}