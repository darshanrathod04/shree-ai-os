package platform.core.lifecycle.model;

import java.util.Objects;

/**
 * <b>TransitionResult</b>
 *
 * <p>Represents the result of a lifecycle transition within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of a lifecycle state transition.</li>
 *   <li>Provides success status, the transition details, and optional failure message.</li>
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
 * @see platform.core.lifecycle.api.LifecycleService
 * @see LifecycleTransition
 */
public final class TransitionResult {

    private final boolean success;
    private final LifecycleTransition transition;
    private final String failureMessage;

    /**
     * Constructs a successful {@code TransitionResult}.
     *
     * @param transition the lifecycle transition (must not be null)
     * @throws NullPointerException if {@code transition} is null
     */
    public TransitionResult(LifecycleTransition transition) {
        this(true, transition, null);
    }

    /**
     * Constructs a failed {@code TransitionResult}.
     *
     * @param transition     the lifecycle transition (must not be null)
     * @param failureMessage the failure message (must not be null or blank)
     * @throws NullPointerException if {@code transition} is null
     * @throws IllegalArgumentException if {@code failureMessage} is null or blank
     */
    public TransitionResult(LifecycleTransition transition, String failureMessage) {
        this(false, transition, failureMessage);
    }

    /**
     * Constructs a new {@code TransitionResult} with the given success, transition, and failure message.
     *
     * @param success        whether the transition succeeded
     * @param transition     the lifecycle transition (must not be null)
     * @param failureMessage the failure message (may be null if successful)
     * @throws NullPointerException if {@code transition} is null
     * @throws IllegalArgumentException if {@code success} is false and {@code failureMessage} is null or blank
     */
    public TransitionResult(boolean success, LifecycleTransition transition, String failureMessage) {
        this.success = success;
        this.transition = Objects.requireNonNull(transition, "Transition must not be null");
        if (!success && (failureMessage == null || failureMessage.isBlank())) {
            throw new IllegalArgumentException("Failure message must not be null or blank for failed transitions");
        }
        this.failureMessage = failureMessage;
    }

    /**
     * Returns whether the transition succeeded.
     *
     * @return {@code true} if the transition succeeded, {@code false} otherwise
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the lifecycle transition.
     *
     * @return the transition
     */
    public LifecycleTransition transition() {
        return transition;
    }

    /**
     * Returns the failure message, or {@code null} if the transition succeeded.
     *
     * @return the failure message, or {@code null} if successful
     */
    public String failureMessage() {
        return failureMessage;
    }

    /**
     * Compares this {@code TransitionResult} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code TransitionResult} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionResult that = (TransitionResult) o;
        return success == that.success
                && transition.equals(that.transition)
                && Objects.equals(failureMessage, that.failureMessage);
    }

    /**
     * Returns the hash code for this {@code TransitionResult}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(success, transition, failureMessage);
    }

    /**
     * Returns a string representation of this {@code TransitionResult}.
     *
     * @return a string containing the success status, transition, and failure message
     */
    @Override
    public String toString() {
        return "TransitionResult{"
                + "success=" + success
                + ", transition=" + transition
                + ", failureMessage='" + failureMessage + '\''
                + '}';
    }
}