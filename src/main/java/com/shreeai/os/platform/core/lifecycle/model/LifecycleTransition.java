package com.shreeai.os.platform.core.lifecycle.model;

import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.registry.model.KernelId;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>LifecycleTransition</b>
 *
 * <p>Represents one state transition of a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Records a single state transition from previous state to current state.</li>
 *   <li>Provides an immutable audit trail of lifecycle changes.</li>
 *   <li>Contains no business logic — pure data carrier.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null and validated at construction time.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-011</p>
 *
 * @see LifecycleService
 * @see KernelId
 */
public final class LifecycleTransition {

    private final KernelId kernelId;
    private final KernelState previousState;
    private final KernelState currentState;
    private final Instant timestamp;

    /**
     * Constructs a new {@code LifecycleTransition} with the given kernel, previous state, current state, and timestamp.
     *
     * @param kernelId      the kernel identifier (must not be null)
     * @param previousState the previous state (must not be null)
     * @param currentState  the current state (must not be null)
     * @param timestamp     the instant when the transition occurred (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public LifecycleTransition(KernelId kernelId, KernelState previousState, KernelState currentState, Instant timestamp) {
        this.kernelId = Objects.requireNonNull(kernelId, "KernelId must not be null");
        this.previousState = Objects.requireNonNull(previousState, "Previous state must not be null");
        this.currentState = Objects.requireNonNull(currentState, "Current state must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
    }

    /**
     * Returns the kernel identifier.
     *
     * @return the kernel identifier
     */
    public KernelId kernelId() {
        return kernelId;
    }

    /**
     * Returns the previous state.
     *
     * @return the previous state
     */
    public KernelState previousState() {
        return previousState;
    }

    /**
     * Returns the current state.
     *
     * @return the current state
     */
    public KernelState currentState() {
        return currentState;
    }

    /**
     * Returns the instant when the transition occurred.
     *
     * @return the transition timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Compares this {@code LifecycleTransition} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code LifecycleTransition} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LifecycleTransition that = (LifecycleTransition) o;
        return kernelId.equals(that.kernelId)
                && previousState == that.previousState
                && currentState == that.currentState
                && timestamp.equals(that.timestamp);
    }

    /**
     * Returns the hash code for this {@code LifecycleTransition}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(kernelId, previousState, currentState, timestamp);
    }

    /**
     * Returns a string representation of this {@code LifecycleTransition}.
     *
     * @return a string containing the kernel ID, states, and timestamp
     */
    @Override
    public String toString() {
        return "LifecycleTransition{"
                + "kernelId=" + kernelId
                + ", previousState=" + previousState
                + ", currentState=" + currentState
                + ", timestamp=" + timestamp
                + '}';
    }
}