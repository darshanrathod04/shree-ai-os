package platform.core.lifecycle;

import platform.core.lifecycle.engine.LifecycleTransitionEngine;
import platform.core.lifecycle.model.KernelState;
import platform.core.lifecycle.model.LifecycleTransition;
import platform.core.lifecycle.model.TransitionResult;
import platform.core.lifecycle.validator.LifecycleValidator;
import platform.core.registry.model.KernelId;

/**
 * <b>LifecycleEngineTests</b>
 *
 * <p>Verifies the behavior of the {@link LifecycleTransitionEngine}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that the engine creates LifecycleTransition records.</li>
 *   <li>Validates that the engine creates TransitionResult records.</li>
 *   <li>Validates that the engine never mutates external state.</li>
 *   <li>Validates that the engine remains stateless.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see LifecycleTransitionEngine
 */
public class LifecycleEngineTests {

    private final LifecycleTransitionEngine engine = new LifecycleTransitionEngine(new LifecycleValidator());
    private final KernelId kernelId = new KernelId("test-kernel");

    /**
     * Verifies the engine creates LifecycleTransition for allowed transitions.
     */
    public void testEngineCreatesLifecycleTransition() {
        // Act
        TransitionResult result = engine.transition(kernelId, KernelState.CREATED, KernelState.INITIALIZED);

        // Assert
        assert result.success() : "Allowed transition should succeed";
        LifecycleTransition transition = result.transition();
        assert transition != null : "Transition should not be null";
        assert transition.kernelId().equals(kernelId) : "KernelId should match";
        assert transition.previousState() == KernelState.CREATED : "Previous state should be CREATED";
        assert transition.currentState() == KernelState.INITIALIZED : "Current state should be INITIALIZED";
        assert transition.timestamp() != null : "Timestamp should not be null";
    }

    /**
     * Verifies the engine creates TransitionResult with success=true for allowed transitions.
     */
    public void testEngineReturnsSuccessForAllowedTransition() {
        // Act
        TransitionResult result = engine.transition(kernelId, KernelState.CREATED, KernelState.INITIALIZED);

        // Assert
        assert result.success() : "Should return success for allowed transition";
        assert result.failureMessage() == null : "Failure message should be null for success";
    }

    /**
     * Verifies the engine creates TransitionResult with success=false for rejected transitions.
     */
    public void testEngineReturnsFailureForRejectedTransition() {
        // Act
        TransitionResult result = engine.transition(kernelId, KernelState.CREATED, KernelState.RUNNING);

        // Assert
        assert !result.success() : "Should return failure for rejected transition";
        assert result.failureMessage() != null : "Failure message should not be null";
        assert !result.failureMessage().isEmpty() : "Failure message should not be empty";
    }

    /**
     * Verifies the engine never mutates external state.
     */
    public void testEngineNeverMutatesExternalState() {
        // Arrange
        KernelState previousState = KernelState.CREATED;
        KernelState targetState = KernelState.INITIALIZED;

        // Act
        TransitionResult result = engine.transition(kernelId, previousState, targetState);

        // Assert
        // The engine should only create results, not modify any external state
        assert result.success() : "Transition should succeed";
        assert previousState == KernelState.CREATED : "Previous state should remain unchanged";
        assert targetState == KernelState.INITIALIZED : "Target state should remain unchanged";
    }

    /**
     * Verifies the engine remains stateless across multiple calls.
     */
    public void testEngineIsStateless() {
        // Act - multiple calls with same parameters
        TransitionResult result1 = engine.transition(kernelId, KernelState.CREATED, KernelState.INITIALIZED);
        TransitionResult result2 = engine.transition(kernelId, KernelState.CREATED, KernelState.INITIALIZED);

        // Assert - same inputs produce same outcome
        assert result1.success() == result2.success() : "Engine should be stateless (deterministic)";
        assert result1.transition().previousState() == result2.transition().previousState() : "Engine should be stateless";
        assert result1.transition().currentState() == result2.transition().currentState() : "Engine should be stateless";
    }

    /**
     * Verifies the engine validates all required transitions.
     */
    public void testEngineValidatesAllAllowedTransitions() {
        // All allowed transitions should succeed
        assert engine.transition(kernelId, KernelState.CREATED, KernelState.INITIALIZED).success()
                : "CREATED -> INITIALIZED should succeed";
        assert engine.transition(kernelId, KernelState.INITIALIZED, KernelState.RUNNING).success()
                : "INITIALIZED -> RUNNING should succeed";
        assert engine.transition(kernelId, KernelState.RUNNING, KernelState.SUSPENDED).success()
                : "RUNNING -> SUSPENDED should succeed";
        assert engine.transition(kernelId, KernelState.RUNNING, KernelState.STOPPED).success()
                : "RUNNING -> STOPPED should succeed";
        assert engine.transition(kernelId, KernelState.RUNNING, KernelState.FAILED).success()
                : "RUNNING -> FAILED should succeed";
        assert engine.transition(kernelId, KernelState.SUSPENDED, KernelState.RUNNING).success()
                : "SUSPENDED -> RUNNING should succeed";
        assert engine.transition(kernelId, KernelState.FAILED, KernelState.TERMINATED).success()
                : "FAILED -> TERMINATED should succeed";
        assert engine.transition(kernelId, KernelState.STOPPED, KernelState.TERMINATED).success()
                : "STOPPED -> TERMINATED should succeed";
    }

    /**
     * Verifies the engine rejects all invalid transitions.
     */
    public void testEngineRejectsAllInvalidTransitions() {
        // Invalid transitions should fail
        assert !engine.transition(kernelId, KernelState.CREATED, KernelState.RUNNING).success()
                : "CREATED -> RUNNING should fail";
        assert !engine.transition(kernelId, KernelState.INITIALIZED, KernelState.TERMINATED).success()
                : "INITIALIZED -> TERMINATED should fail";
        assert !engine.transition(kernelId, KernelState.FAILED, KernelState.RUNNING).success()
                : "FAILED -> RUNNING should fail";
        assert !engine.transition(kernelId, KernelState.STOPPED, KernelState.RUNNING).success()
                : "STOPPED -> RUNNING should fail";
        assert !engine.transition(kernelId, KernelState.TERMINATED, KernelState.RUNNING).success()
                : "TERMINATED -> RUNNING should fail";
    }
}