package platform.runtime.lifecycle;

/**
 * <b>RuntimeLifecycleListener</b>
 *
 * <p>Listener interface for observing Runtime lifecycle state transitions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides notification hooks for state transitions.</li>
 *   <li>Enables components to react to Runtime lifecycle events.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 *
 * @see RuntimeLifecycle
 * @see RuntimeState
 */
@FunctionalInterface
public interface RuntimeLifecycleListener {

    /**
     * Invoked when the Runtime transitions to a new state.
     *
     * @param oldState the previous state
     * @param newState the new state
     * @param cause    the cause of the transition, or null if normal
     */
    void onStateChanged(RuntimeState oldState, RuntimeState newState, Throwable cause);
}