package com.shreeai.os.platform.core.lifecycle.error;

import com.shreeai.os.platform.core.lifecycle.model.KernelState;

/**
 * <b>InvalidTransitionException</b>
 *
 * <p>Thrown when a state transition violates the Lifecycle State Model.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a requested state transition is not allowed.</li>
 *   <li>Extends {@link LifecycleException} to maintain the single base exception hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see LifecycleException
 * @see LifecycleErrorCode#LIFECYCLE_INVALID_TRANSITION
 */
public class InvalidTransitionException extends LifecycleException {

    /**
     * Constructs a new {@code InvalidTransitionException} with the given previous and next states.
     *
     * @param previousState the previous state (must not be null)
     * @param nextState     the next state (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public InvalidTransitionException(KernelState previousState, KernelState nextState) {
        this(previousState, nextState, (String) null);
    }

    /**
     * Constructs a new {@code InvalidTransitionException} with the given previous state, next state, and message.
     *
     * @param previousState the previous state (must not be null)
     * @param nextState     the next state (must not be null)
     * @param message       the detail message (may be null)
     * @throws NullPointerException if any parameter is null
     */
    public InvalidTransitionException(KernelState previousState, KernelState nextState, String message) {
        super(createError(previousState, nextState, message));
    }

    /**
     * Constructs a new {@code InvalidTransitionException} with the given previous state, next state, and cause.
     *
     * @param previousState the previous state (must not be null)
     * @param nextState     the next state (must not be null)
     * @param cause         the underlying cause (may be null)
     * @throws NullPointerException if any parameter is null
     */
    public InvalidTransitionException(KernelState previousState, KernelState nextState, Throwable cause) {
        super(createError(previousState, nextState, null), cause);
    }

    /**
     * Constructs a new {@code InvalidTransitionException} with the given previous state, next state, message, and cause.
     *
     * @param previousState the previous state (must not be null)
     * @param nextState     the next state (must not be null)
     * @param message       the detail message (may be null)
     * @param cause         the underlying cause (may be null)
     * @throws NullPointerException if any parameter is null
     */
    public InvalidTransitionException(KernelState previousState, KernelState nextState, String message, Throwable cause) {
        super(createError(previousState, nextState, message), cause);
    }

    private static LifecycleError createError(KernelState previousState, KernelState nextState, String message) {
        String errorMessage = message != null ? message
                : "Invalid transition from " + previousState + " to " + nextState;
        return new LifecycleError(
                LifecycleErrorCode.LIFECYCLE_INVALID_TRANSITION,
                errorMessage
        );
    }
}