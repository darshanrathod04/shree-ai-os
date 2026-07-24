package platform.kernels.execution.model;

import java.util.Objects;

/**
 * <b>ActionState</b>
 *
 * <p>Represents immutable action state.
 * This value object encapsulates the state of an action execution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents action lifecycle states.</li>
 *   <li>Provides clear action semantics.</li>
 *   <li>Enables state-based action management.</li>
 *   <li>Contains no execution behavior.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param actionId    the action identifier (must not be {@code null})
 * @param lifecycleState the lifecycle state (must not be {@code null})
 * @param timestamps  the timestamps map (must not be {@code null})
 * @param metadata    additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ActionState {

    private final String actionId;
    private final ExecutionStatus lifecycleState;
    private final java.util.Map<String, java.time.Instant> timestamps;
    private final java.util.Map<String, Object> metadata;

    /**
     * Constructs an {@code ActionState} with the specified parameters.
     *
     * @param actionId       the action identifier (must not be {@code null} or empty)
     * @param lifecycleState the lifecycle state (must not be {@code null})
     * @param timestamps     the timestamps map (must not be {@code null})
     * @param metadata       additional metadata (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    public ActionState(
            String actionId,
            ExecutionStatus lifecycleState,
            java.util.Map<String, java.time.Instant> timestamps,
            java.util.Map<String, Object> metadata) {
        if (actionId == null || actionId.trim().isEmpty()) {
            throw new IllegalArgumentException("ActionState actionId must not be null or empty");
        }
        if (lifecycleState == null) {
            throw new IllegalArgumentException("ActionState lifecycleState must not be null");
        }
        if (timestamps == null) {
            throw new IllegalArgumentException("ActionState timestamps must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ActionState metadata must not be null");
        }

        this.actionId = actionId;
        this.lifecycleState = lifecycleState;
        this.timestamps = java.util.Collections.unmodifiableMap(new java.util.HashMap<>(timestamps));
        this.metadata = java.util.Collections.unmodifiableMap(new java.util.HashMap<>(metadata));
    }

    /**
     * Returns the action identifier.
     *
     * @return the action identifier
     */
    public String actionId() {
        return actionId;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return the lifecycle state
     */
    public ExecutionStatus lifecycleState() {
        return lifecycleState;
    }

    /**
     * Returns an unmodifiable view of the timestamps.
     *
     * <p>The returned map is unmodifiable and reflects the timestamps at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of timestamps
     */
    public java.util.Map<String, java.time.Instant> timestamps() {
        return timestamps;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * <p>The returned map is unmodifiable and reflects the metadata at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public java.util.Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ActionState} instances are equal if they have the same
     * action identifier, lifecycle state, timestamps, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ActionState that = (ActionState) obj;
        return Objects.equals(actionId, that.actionId) &&
                lifecycleState == that.lifecycleState &&
                Objects.equals(timestamps, that.timestamps) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for this {@code ActionState}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(actionId, lifecycleState, timestamps, metadata);
    }

    /**
     * Returns a string representation of this {@code ActionState}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ActionState{" +
                "actionId='" + actionId + '\'' +
                ", lifecycleState=" + lifecycleState +
                ", timestamps=" + timestamps +
                ", metadata=" + metadata +
                '}';
    }
}