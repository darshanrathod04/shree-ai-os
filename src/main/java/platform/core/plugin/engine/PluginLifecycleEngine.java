package platform.core.plugin.engine;

import platform.core.plugin.model.PluginDescriptor;
import platform.core.plugin.model.PluginState;

import java.util.Objects;

/**
 * <b>PluginLifecycleEngine</b>
 *
 * <p>Stateless engine that governs plugin lifecycle transitions within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Decides lifecycle transitions only.</li>
 *   <li>Answers the question: "Is this transition allowed?"</li>
 *   <li>Returns {@link PluginTransitionResult} for every transition attempt.</li>
 *   <li>Never throws exceptions for expected transition failures.</li>
 * </ul>
 *
 * <p>The engine does <strong>not</strong>:</p>
 * <ul>
 *   <li>Store plugins — that is the service's job.</li>
 *   <li>Validate plugins — that is the validator's job.</li>
 *   <li>Load JAR files.</li>
 *   <li>Execute plugin code.</li>
 *   <li>Publish events.</li>
 * </ul>
 *
 * <h2>Transition Rules</h2>
 * <pre>
 *   Allowed:
 *     UNLOADED  ──load──→  LOADED
 *     LOADED    ──start─→  STARTED
 *     STARTED   ──stop──→  STOPPED
 *     STOPPED   ──start─→  STARTED
 *     STOPPED   ──unload→  UNLOADED
 *
 *   Rejected:
 *     STARTED   ──→  LOADED
 *     UNLOADED  ──→  STARTED
 *     FAILED    ──→  STARTED
 *     FAILED    ──→  LOADED
 * </pre>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Thread-safe — no mutable state.</li>
 *   <li>Deterministic — same inputs always produce same outputs.</li>
 *   <li>No persistence.</li>
 *   <li>No plugin loading.</li>
 *   <li>No business logic.</li>
 *   <li>No Spring, Lombok, or JPA.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginTransitionResult
 * @see PluginState
 * @see PluginDescriptor
 */
public final class PluginLifecycleEngine {

    /**
     * Constructs a new {@code PluginLifecycleEngine}.
     * Public to allow test instantiation.
     */
    public PluginLifecycleEngine() {
    }

    /**
     * Attempts to transition a plugin from {@link PluginState#LOADED}
     * to {@link PluginState#STARTED}.
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult start(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return transition(descriptor, PluginState.LOADED, PluginState.STARTED);
    }

    /**
     * Attempts to transition a plugin from {@link PluginState#STARTED}
     * to {@link PluginState#STOPPED}.
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult stop(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return transition(descriptor, PluginState.STARTED, PluginState.STOPPED);
    }

    /**
     * Attempts to transition a plugin from {@link PluginState#UNLOADED}
     * to {@link PluginState#LOADED}.
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult load(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return transition(descriptor, PluginState.UNLOADED, PluginState.LOADED);
    }

    /**
     * Attempts to transition a plugin from {@link PluginState#STOPPED}
     * to {@link PluginState#UNLOADED}.
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult unload(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return transition(descriptor, PluginState.STOPPED, PluginState.UNLOADED);
    }

    /**
     * Core transition logic. Validates that the transition from the given
     * current state to the target state is allowed according to the
     * lifecycle state machine.
     *
     * @param descriptor   the plugin descriptor
     * @param currentState the expected current state of the plugin
     * @param targetState  the desired target state
     * @return a transition result indicating success or failure
     */
    private PluginTransitionResult transition(
            PluginDescriptor descriptor,
            PluginState currentState,
            PluginState targetState
    ) {
        if (isAllowed(currentState, targetState)) {
            return PluginTransitionResult.success(descriptor, currentState, targetState);
        }
        return PluginTransitionResult.failure(
                descriptor,
                currentState,
                currentState,
                String.format(
                        "Transition from %s to %s is not allowed for plugin '%s'",
                        currentState, targetState, descriptor.plugin().id()
                )
        );
    }

    /**
     * Returns {@code true} if the given state transition is allowed.
     *
     * <p>Allowed transitions:
     * <ul>
     *   <li>{@code UNLOADED → LOADED}</li>
     *   <li>{@code LOADED → STARTED}</li>
     *   <li>{@code STARTED → STOPPED}</li>
     *   <li>{@code STOPPED → STARTED}</li>
     *   <li>{@code STOPPED → UNLOADED}</li>
     * </ul>
     *
     * <p>All other transitions are rejected, including:
     * <ul>
     *   <li>{@code STARTED → LOADED}</li>
     *   <li>{@code UNLOADED → STARTED}</li>
     *   <li>{@code FAILED → STARTED}</li>
     *   <li>{@code FAILED → LOADED}</li>
     *   <li>{@code FAILED} → any state</li>
     * </ul>
     */
    private static boolean isAllowed(PluginState from, PluginState to) {
        return switch (from) {
            case UNLOADED -> to == PluginState.LOADED;
            case LOADED   -> to == PluginState.STARTED;
            case STARTED  -> to == PluginState.STOPPED;
            case STOPPED  -> to == PluginState.STARTED || to == PluginState.UNLOADED;
            case FAILED   -> false;
        };
    }
}