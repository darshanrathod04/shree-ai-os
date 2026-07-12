package platform.core.lifecycle.service;

import platform.core.lifecycle.api.LifecycleService;
import platform.core.lifecycle.engine.LifecycleTransitionEngine;
import platform.core.lifecycle.error.InvalidTransitionException;
import platform.core.lifecycle.error.KernelNotInitializedException;
import platform.core.lifecycle.error.LifecycleError;
import platform.core.lifecycle.error.LifecycleErrorCode;
import platform.core.lifecycle.error.LifecycleException;
import platform.core.lifecycle.model.KernelHealth;
import platform.core.lifecycle.model.KernelState;
import platform.core.lifecycle.model.TransitionResult;
import platform.core.lifecycle.validator.LifecycleValidator;
import platform.core.registry.api.KernelRegistry;
import platform.core.registry.model.KernelId;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <b>DefaultLifecycleService</b>
 *
 * <p>Reference implementation of the Lifecycle Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the reference implementation of lifecycle orchestration.</li>
 *   <li>Coordinates lifecycle operations by delegating validation to LifecycleValidator.</li>
 *   <li>Delegates all transition execution to LifecycleTransitionEngine.</li>
 *   <li>Maintains current lifecycle state for registered kernels.</li>
 *   <li>Never owns lifecycle transition rules — validation and execution are delegated.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Lifecycle Principle:</b> LifecycleService coordinates. LifecycleValidator validates.
 * LifecycleTransitionEngine executes transitions. Responsibilities SHALL remain separated.</p>
 *
 * @see LifecycleService
 * @see LifecycleTransitionEngine
 * @see LifecycleValidator
 * @see KernelRegistry
 */
public final class DefaultLifecycleService implements LifecycleService {

    private final KernelRegistry kernelRegistry;
    private final LifecycleValidator validator;
    private final LifecycleTransitionEngine transitionEngine;
    private final ConcurrentMap<KernelId, KernelState> states;
    private final ConcurrentMap<KernelId, KernelHealth> healthStates;

    /**
     * Constructs a new {@code DefaultLifecycleService} with the given dependencies.
     *
     * <p>Uses constructor injection only — no setter injection.</p>
     *
     * @param kernelRegistry    the kernel registry (must not be null)
     * @param validator         the lifecycle validator (must not be null)
     * @param transitionEngine  the lifecycle transition engine (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DefaultLifecycleService(KernelRegistry kernelRegistry, LifecycleValidator validator, LifecycleTransitionEngine transitionEngine) {
        this.kernelRegistry = java.util.Objects.requireNonNull(kernelRegistry, "KernelRegistry must not be null");
        this.validator = java.util.Objects.requireNonNull(validator, "LifecycleValidator must not be null");
        this.transitionEngine = java.util.Objects.requireNonNull(transitionEngine, "LifecycleTransitionEngine must not be null");
        this.states = new ConcurrentHashMap<>();
        this.healthStates = new ConcurrentHashMap<>();
    }

    /**
     * Initializes a kernel for operation.
     *
     * <p>Idempotent — calling it multiple times on an already initialized kernel
     * returns {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to initialize (must not be null)
     * @return {@code true} if initialization succeeded or kernel was already initialized,
     *         {@code false} if initialization failed
     * @throws LifecycleException if initialization fails
     */
    @Override
    public boolean initialize(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        validateKernelId(kernelId);

        KernelState currentState = states.get(kernelId);

        // If already initialized or running, return true (idempotent)
        if (currentState == KernelState.INITIALIZED || currentState == KernelState.RUNNING) {
            return true;
        }

        // Check if kernel exists in registry
        boolean kernelExists = kernelRegistry.exists(kernelId.value());
        if (!kernelExists) {
            return false;
        }

        // Delegate transition execution to LifecycleTransitionEngine
        KernelState actualPrev = currentState != null ? currentState : KernelState.CREATED;
        TransitionResult result = transitionEngine.transition(kernelId, actualPrev, KernelState.INITIALIZED);

        if (!result.success()) {
            throw new InvalidTransitionException(actualPrev, KernelState.INITIALIZED, result.failureMessage());
        }

        // Update state
        states.put(kernelId, KernelState.INITIALIZED);
        return true;
    }

    /**
     * Starts a kernel.
     *
     * <p>Idempotent — calling it on an already running kernel returns {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to start (must not be null)
     * @return {@code true} if the kernel was started successfully or was already running,
     *         {@code false} if the start operation failed
     * @throws KernelNotInitializedException if the kernel has not been initialized
     */
    @Override
    public boolean start(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        validateKernelId(kernelId);

        KernelState currentState = states.get(kernelId);

        // If already running, return true (idempotent)
        if (currentState == KernelState.RUNNING) {
            return true;
        }

        // Must be initialized before starting
        if (currentState == null || currentState == KernelState.CREATED) {
            throw new KernelNotInitializedException(kernelId);
        }

        // Delegate transition execution to LifecycleTransitionEngine
        TransitionResult result = transitionEngine.transition(kernelId, currentState, KernelState.RUNNING);

        if (!result.success()) {
            throw new InvalidTransitionException(currentState, KernelState.RUNNING, result.failureMessage());
        }

        // Update state
        states.put(kernelId, KernelState.RUNNING);
        return true;
    }

    /**
     * Stops a kernel.
     *
     * <p>Idempotent — calling it on an already stopped kernel returns {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to stop (must not be null)
     * @return {@code true} if the kernel was stopped successfully or was already stopped,
     *         {@code false} if the stop operation failed
     * @throws InvalidTransitionException if the kernel is not in a state that can be stopped
     */
    @Override
    public boolean stop(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        validateKernelId(kernelId);

        KernelState currentState = states.get(kernelId);

        // If already stopped, return true (idempotent)
        if (currentState == KernelState.STOPPED) {
            return true;
        }

        // Must be running to stop
        if (currentState != KernelState.RUNNING) {
            throw new InvalidTransitionException(currentState, KernelState.STOPPED);
        }

        // Delegate transition execution to LifecycleTransitionEngine
        TransitionResult result = transitionEngine.transition(kernelId, currentState, KernelState.STOPPED);

        if (!result.success()) {
            throw new InvalidTransitionException(currentState, KernelState.STOPPED, result.failureMessage());
        }

        // Update state
        states.put(kernelId, KernelState.STOPPED);
        return true;
    }

    /**
     * Suspends a kernel.
     *
     * <p>Idempotent — calling it on an already suspended kernel returns {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to suspend (must not be null)
     * @return {@code true} if the kernel was suspended successfully or was already suspended,
     *         {@code false} if the suspend operation failed
     * @throws InvalidTransitionException if the kernel is not in a state that can be suspended
     */
    @Override
    public boolean suspend(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        validateKernelId(kernelId);

        KernelState currentState = states.get(kernelId);

        // If already suspended, return true (idempotent)
        if (currentState == KernelState.SUSPENDED) {
            return true;
        }

        // Must be running to suspend
        if (currentState != KernelState.RUNNING) {
            throw new InvalidTransitionException(currentState, KernelState.SUSPENDED);
        }

        // Delegate transition execution to LifecycleTransitionEngine
        TransitionResult result = transitionEngine.transition(kernelId, currentState, KernelState.SUSPENDED);

        if (!result.success()) {
            throw new InvalidTransitionException(currentState, KernelState.SUSPENDED, result.failureMessage());
        }

        // Update state
        states.put(kernelId, KernelState.SUSPENDED);
        return true;
    }

    /**
     * Resumes a suspended kernel.
     *
     * <p>Idempotent — calling it on a running kernel returns {@code true} without side effects.</p>
     *
     * @param kernelId the identifier of the kernel to resume (must not be null)
     * @return {@code true} if the kernel was resumed successfully or was already running,
     *         {@code false} if the resume operation failed
     * @throws InvalidTransitionException if the kernel is not in a state that can be resumed
     */
    @Override
    public boolean resume(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        validateKernelId(kernelId);

        KernelState currentState = states.get(kernelId);

        // If already running, return true (idempotent)
        if (currentState == KernelState.RUNNING) {
            return true;
        }

        // Must be suspended to resume
        if (currentState != KernelState.SUSPENDED) {
            throw new InvalidTransitionException(currentState, KernelState.RUNNING);
        }

        // Delegate transition execution to LifecycleTransitionEngine
        TransitionResult result = transitionEngine.transition(kernelId, currentState, KernelState.RUNNING);

        if (!result.success()) {
            throw new InvalidTransitionException(currentState, KernelState.RUNNING, result.failureMessage());
        }

        // Update state
        states.put(kernelId, KernelState.RUNNING);
        return true;
    }

    /**
     * Returns the current state of a kernel.
     *
     * @param kernelId the identifier of the kernel to query (must not be null)
     * @return the current {@link KernelState} of the kernel
     */
    @Override
    public KernelState state(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        KernelState currentState = states.get(kernelId);
        return currentState != null ? currentState : KernelState.CREATED;
    }

    /**
     * Returns the current health status of a kernel.
     *
     * @param kernelId the identifier of the kernel to query (must not be null)
     * @return the current {@link KernelHealth} of the kernel
     */
    @Override
    public KernelHealth health(KernelId kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        KernelHealth health = healthStates.get(kernelId);
        if (health != null) {
            return health;
        }

        // Default healthy status
        KernelState currentState = state(kernelId);
        String status = currentState == KernelState.RUNNING ? "HEALTHY" : "UNKNOWN";
        return new KernelHealth(status, "Kernel is in " + currentState + " state", Instant.now());
    }

    private void validateKernelId(KernelId kernelId) {
        var validationResult = validator.validateKernelId(kernelId);
        if (!validationResult.isValid()) {
            throw new LifecycleException(
                    new LifecycleError(
                            LifecycleErrorCode.LIFECYCLE_VALIDATION_FAILED,
                            "KernelId validation failed: " + validationResult.errors()
                    )
            );
        }
    }
}