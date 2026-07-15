package platform.core.plugin.engine;

import platform.core.plugin.model.PluginDescriptor;
import platform.core.plugin.model.PluginState;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>PluginTransitionResult</b>
 *
 * <p>Immutable result of a plugin lifecycle transition within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Captures the outcome of a lifecycle state transition.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Fields:</b></p>
 * <ul>
 *   <li>{@code success} — whether the transition was successful</li>
 *   <li>{@code descriptor} — the plugin descriptor involved in the transition</li>
 *   <li>{@code previousState} — the state before the transition</li>
 *   <li>{@code currentState} — the state after the transition</li>
 *   <li>{@code failureMessage} — a description of why the transition failed (empty on success)</li>
 *   <li>{@code timestamp} — the instant at which the transition was attempted</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301, STD-003</p>
 *
 * @see PluginDescriptor
 * @see PluginState
 * @see PluginLifecycleEngine
 */
public final class PluginTransitionResult {

    private final boolean success;
    private final PluginDescriptor descriptor;
    private final PluginState previousState;
    private final PluginState currentState;
    private final String failureMessage;
    private final Instant timestamp;

    private PluginTransitionResult(
            boolean success,
            PluginDescriptor descriptor,
            PluginState previousState,
            PluginState currentState,
            String failureMessage,
            Instant timestamp
    ) {
        this.success = success;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
        this.previousState = Objects.requireNonNull(previousState, "previousState must not be null");
        this.currentState = Objects.requireNonNull(currentState, "currentState must not be null");
        this.failureMessage = failureMessage != null ? failureMessage : "";
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /**
     * Creates a successful transition result.
     *
     * @param descriptor    the plugin descriptor
     * @param previousState the state before the transition
     * @param currentState  the state after the transition
     * @return a new {@code PluginTransitionResult} with {@code success = true}
     */
    public static PluginTransitionResult success(
            PluginDescriptor descriptor,
            PluginState previousState,
            PluginState currentState
    ) {
        return new PluginTransitionResult(true, descriptor, previousState, currentState, "", Instant.now());
    }

    /**
     * Creates a failed transition result.
     *
     * @param descriptor     the plugin descriptor
     * @param previousState  the state before the attempted transition
     * @param currentState   the state after the failed transition (typically same as previous)
     * @param failureMessage a description of why the transition failed
     * @return a new {@code PluginTransitionResult} with {@code success = false}
     */
    public static PluginTransitionResult failure(
            PluginDescriptor descriptor,
            PluginState previousState,
            PluginState currentState,
            String failureMessage
    ) {
        return new PluginTransitionResult(
                false,
                descriptor,
                previousState,
                currentState,
                Objects.requireNonNull(failureMessage, "failureMessage must not be null"),
                Instant.now()
        );
    }

    // --- Accessors ---

    public boolean success() {
        return success;
    }

    public PluginDescriptor descriptor() {
        return descriptor;
    }

    public PluginState previousState() {
        return previousState;
    }

    public PluginState currentState() {
        return currentState;
    }

    public String failureMessage() {
        return failureMessage;
    }

    public Instant timestamp() {
        return timestamp;
    }

    // --- Equality and hashing ---

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PluginTransitionResult that = (PluginTransitionResult) obj;
        return success == that.success
                && descriptor.equals(that.descriptor)
                && previousState == that.previousState
                && currentState == that.currentState
                && failureMessage.equals(that.failureMessage)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(success);
        result = 31 * result + descriptor.hashCode();
        result = 31 * result + previousState.hashCode();
        result = 31 * result + currentState.hashCode();
        result = 31 * result + failureMessage.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PluginTransitionResult{" +
                "success=" + success +
                ", descriptor=" + descriptor +
                ", previousState=" + previousState +
                ", currentState=" + currentState +
                ", failureMessage='" + failureMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}