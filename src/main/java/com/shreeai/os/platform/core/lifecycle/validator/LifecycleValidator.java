package com.shreeai.os.platform.core.lifecycle.validator;

import com.shreeai.os.platform.core.lifecycle.model.KernelHealth;
import com.shreeai.os.platform.core.lifecycle.model.KernelState;
import com.shreeai.os.platform.core.lifecycle.model.LifecycleTransition;
import com.shreeai.os.platform.core.lifecycle.model.TransitionResult;
import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.validator.ValidationResult;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/**
 * <b>LifecycleValidator</b>
 *
 * <p>Stateless validator that enforces all lifecycle rules before any state transition occurs.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enforces all lifecycle rules before any state transition occurs.</li>
 *   <li>Answers the question: "Is this transition allowed?" — it never performs the transition.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors.</li>
 *   <li>Reuses the approved Registry validation architecture.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Deterministic — same inputs always produce the same result.</li>
 *   <li>Thread-safe — no mutable state.</li>
 *   <li>No state mutation — validation never changes state.</li>
 *   <li>Never performs transitions — validation only.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see ValidationResult
 * @see KernelState
 * @see KernelHealth
 * @see LifecycleTransition
 * @see TransitionResult
 */
public final class LifecycleValidator {

    // Allowed transitions: previous state -> set of allowed next states
    private static final EnumMap<KernelState, Set<KernelState>> ALLOWED_TRANSITIONS;
    
    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(KernelState.class);
        
        ALLOWED_TRANSITIONS.put(KernelState.CREATED, EnumSet.of(KernelState.INITIALIZED));
        
        ALLOWED_TRANSITIONS.put(KernelState.INITIALIZED, EnumSet.of(KernelState.RUNNING));
        
        ALLOWED_TRANSITIONS.put(KernelState.RUNNING, 
                EnumSet.of(KernelState.SUSPENDED, KernelState.STOPPED, KernelState.FAILED));
        
        ALLOWED_TRANSITIONS.put(KernelState.SUSPENDED, EnumSet.of(KernelState.RUNNING));
        
        ALLOWED_TRANSITIONS.put(KernelState.STOPPED, EnumSet.of(KernelState.TERMINATED));
        
        ALLOWED_TRANSITIONS.put(KernelState.FAILED, EnumSet.of(KernelState.TERMINATED));
        
        // TERMINATED has no allowed transitions
        ALLOWED_TRANSITIONS.put(KernelState.TERMINATED, EnumSet.noneOf(KernelState.class));
    }

    /**
     * Validates a {@link KernelId} for lifecycle operations.
     *
     * @param kernelId the kernel identifier to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code kernelId} is null
     */
    public ValidationResult validateKernelId(KernelId kernelId) {
        if (kernelId == null) {
            throw new NullPointerException("KernelId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // KernelId format validation
        String idValue = kernelId.value();
        if (idValue == null || idValue.isBlank()) {
            builder.addError("KernelId must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link KernelState} for lifecycle operations.
     *
     * @param state the state to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code state} is null
     */
    public ValidationResult validateState(KernelState state) {
        if (state == null) {
            throw new NullPointerException("KernelState must not be null");
        }

        // KernelState is an enum, so it's always valid if not null
        return ValidationResult.builder().build();
    }

    /**
     * Validates a {@link KernelHealth} for lifecycle operations.
     *
     * @param health the health to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code health} is null
     */
    public ValidationResult validateHealth(KernelHealth health) {
        if (health == null) {
            throw new NullPointerException("KernelHealth must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Validate health status
        String status = health.status();
        if (status == null || status.isBlank()) {
            builder.addError("KernelHealth status must not be null or blank");
        }

        // Validate health message
        String message = health.message();
        if (message == null || message.isBlank()) {
            builder.addError("KernelHealth message must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a state transition from previous state to next state.
     *
     * <p>Enforces the following allowed transitions:</p>
     * <ul>
     *   <li>CREATED → INITIALIZED</li>
     *   <li>INITIALIZED → RUNNING</li>
     *   <li>RUNNING → SUSPENDED</li>
     *   <li>RUNNING → STOPPED</li>
     *   <li>RUNNING → FAILED</li>
     *   <li>SUSPENDED → RUNNING</li>
     *   <li>FAILED → TERMINATED</li>
     *   <li>STOPPED → TERMINATED</li>
     * </ul>
     *
     * <p>The following transitions are rejected:</p>
     * <ul>
     *   <li>CREATED → RUNNING</li>
     *   <li>INITIALIZED → TERMINATED</li>
     *   <li>FAILED → RUNNING</li>
     *   <li>TERMINATED → ANY</li>
     *   <li>STOPPED → RUNNING</li>
     * </ul>
     *
     * @param previous the previous state (must not be null)
     * @param next     the next state (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if any parameter is null
     */
    public ValidationResult validateTransition(KernelState previous, KernelState next) {
        if (previous == null) {
            throw new NullPointerException("Previous state must not be null");
        }
        if (next == null) {
            throw new NullPointerException("Next state must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Check if transition is allowed
        Set<KernelState> allowedNextStates = ALLOWED_TRANSITIONS.get(previous);
        
        if (allowedNextStates == null || !allowedNextStates.contains(next)) {
            builder.addError("Invalid transition from " + previous + " to " + next
                    + ". Allowed transitions from " + previous + ": " + allowedNextStates);
        }

        return builder.build();
    }

    /**
     * Validates a {@link TransitionResult} for consistency.
     *
     * @param result the transition result to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code result} is null
     */
    public ValidationResult validateTransitionResult(TransitionResult result) {
        if (result == null) {
            throw new NullPointerException("TransitionResult must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Validate transition exists
        LifecycleTransition transition = result.transition();
        if (transition == null) {
            builder.addError("TransitionResult transition must not be null");
            return builder.build();
        }

        // Validate transition fields
        if (transition.kernelId() == null) {
            builder.addError("TransitionResult kernelId must not be null");
        }
        if (transition.previousState() == null) {
            builder.addError("TransitionResult previousState must not be null");
        }
        if (transition.currentState() == null) {
            builder.addError("TransitionResult currentState must not be null");
        }
        if (transition.timestamp() == null) {
            builder.addError("TransitionResult timestamp must not be null");
        }

        // Validate failure message for failed transitions
        if (!result.success() && (result.failureMessage() == null || result.failureMessage().isBlank())) {
            builder.addError("Failed TransitionResult must have a non-null, non-blank failure message");
        }

        return builder.build();
    }
}