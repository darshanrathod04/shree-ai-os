package com.shreeai.os.platform.runtime.exceptions;

import com.shreeai.os.platform.runtime.lifecycle.RuntimeState;

/**
 * <b>InvalidRuntimeStateException</b>
 *
 * <p>Thrown when an operation is attempted on a Runtime that is in an invalid state.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides specific exception for state transition violations.</li>
 *   <li>Carries the actual and expected states for diagnostic purposes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 */
public class InvalidRuntimeStateException extends RuntimeException {

    private final RuntimeState actualState;
    private final RuntimeState expectedState;

    /**
     * Creates a new InvalidRuntimeStateException.
     *
     * @param actualState   the actual RuntimeState at the time of the violation
     * @param expectedState the expected RuntimeState for the attempted operation
     */
    public InvalidRuntimeStateException(RuntimeState actualState, RuntimeState expectedState) {
        super("Invalid runtime state: actual=" + actualState + ", expected=" + expectedState);
        this.actualState = actualState;
        this.expectedState = expectedState;
    }

    /**
     * Returns the actual RuntimeState at the time of the violation.
     *
     * @return the actual state
     */
    public RuntimeState actualState() {
        return actualState;
    }

    /**
     * Returns the expected RuntimeState for the attempted operation.
     *
     * @return the expected state
     */
    public RuntimeState expectedState() {
        return expectedState;
    }
}