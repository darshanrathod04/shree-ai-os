package com.shreeai.os.platform.core.lifecycle.engine;

import com.shreeai.os.platform.core.lifecycle.model.KernelState;
import com.shreeai.os.platform.core.lifecycle.model.LifecycleTransition;
import com.shreeai.os.platform.core.lifecycle.model.TransitionResult;
import com.shreeai.os.platform.core.lifecycle.validator.LifecycleValidator;
import com.shreeai.os.platform.core.registry.model.KernelId;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>LifecycleTransitionEngine</b>
 *
 * <p>The Transition Engine is the ONLY component responsible for executing
 * Lifecycle State Transitions within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes validated lifecycle state transitions.</li>
 *   <li>Creates {@link LifecycleTransition} and {@link TransitionResult} records.</li>
 *   <li>Never mutates external state — returns results for the caller to apply.</li>
 *   <li>Remains completely stateless — all state is passed as parameters.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Lifecycle Principle:</b> LifecycleValidator decides legality.
 * LifecycleTransitionEngine executes transitions.
 * LifecycleService coordinates orchestration.
 * These responsibilities SHALL remain independent forever.</p>
 *
 * @see LifecycleValidator
 * @see LifecycleTransition
 * @see TransitionResult
 */
public final class LifecycleTransitionEngine {

    private final LifecycleValidator validator;

    /**
     * Constructs a new {@code LifecycleTransitionEngine} with the given validator.
     *
     * <p>Uses constructor injection only — no setter injection.</p>
     *
     * @param validator the lifecycle validator (must not be null)
     * @throws NullPointerException if {@code validator} is null
     */
    public LifecycleTransitionEngine(LifecycleValidator validator) {
        this.validator = Objects.requireNonNull(validator, "LifecycleValidator must not be null");
    }

    /**
     * Executes a state transition from the current state to the target state.
     *
     * <p>Validates the transition legality via {@link LifecycleValidator#validateTransition}
     * and produces a {@link TransitionResult} describing the outcome.</p>
     *
     * <p>Allowed transitions produce {@code TransitionResult(success = true)}.</p>
     * <p>Rejected transitions produce {@code TransitionResult(success = false)}
     * with a descriptive failure message. Exceptions are NOT thrown for expected
     * transition failures.</p>
     *
     * <p>This method never mutates external state — it only creates results.
     * The caller is responsible for applying the transition outcome.</p>
     *
     * @param kernelId     the kernel identifier (must not be null)
     * @param currentState the current kernel state (must not be null)
     * @param targetState  the target kernel state (must not be null)
     * @return a {@link TransitionResult} describing the transition outcome
     * @throws NullPointerException if any parameter is null
     */
    public TransitionResult transition(KernelId kernelId, KernelState currentState, KernelState targetState) {
        Objects.requireNonNull(kernelId, "KernelId must not be null");
        Objects.requireNonNull(currentState, "Current state must not be null");
        Objects.requireNonNull(targetState, "Target state must not be null");

        // Validate the transition via LifecycleValidator
        var validationResult = validator.validateTransition(currentState, targetState);

        // Create LifecycleTransition record
        LifecycleTransition transition = new LifecycleTransition(
                kernelId,
                currentState,
                targetState,
                Instant.now()
        );

        // Create TransitionResult based on validation outcome
        if (validationResult.isValid()) {
            // Allowed transition
            return new TransitionResult(transition);
        } else {
            // Rejected transition
            String failureMessage = validationResult.errors().isEmpty()
                    ? "Transition from " + currentState + " to " + targetState + " is not allowed"
                    : validationResult.errors().iterator().next();
            return new TransitionResult(transition, failureMessage);
        }
    }
}