package com.shreeai.os.platform.runtime.internal;

import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycleListener;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <b>DefaultRuntimeLifecycle</b>
 *
 * <p>Default implementation of the {@link RuntimeLifecycle} interface.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Manages Runtime state transitions with thread-safe state tracking.</li>
 *   <li>Validates all state transitions against the approved state machine.</li>
 *   <li>Notifies registered listeners on every state change.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Internal)</p>
 * <p><b>Invariant:</b> State transitions MUST follow the approved state machine.</p>
 */
public final class DefaultRuntimeLifecycle implements RuntimeLifecycle {

    private final AtomicReference<RuntimeState> currentState;
    private final List<RuntimeLifecycleListener> listeners;

    /**
     * Constructs a new DefaultRuntimeLifecycle in INITIALIZING state.
     */
    public DefaultRuntimeLifecycle() {
        this.currentState = new AtomicReference<>(RuntimeState.INITIALIZING);
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public RuntimeState currentState() {
        return currentState.get();
    }

    @Override
    public void start() {
        RuntimeState current = currentState.get();
        if (current == RuntimeState.INITIALIZING) {
            transitionTo(RuntimeState.READY, null);
        } else {
            throw new IllegalStateException(
                    "Cannot start from state: " + current + ". Expected: INITIALIZING");
        }
    }

    @Override
    public void stop() {
        RuntimeState current = currentState.get();
        if (current == RuntimeState.READY || current == RuntimeState.ACTIVE || current == RuntimeState.IDLE) {
            transitionTo(RuntimeState.DRAINING, null);
            transitionTo(RuntimeState.STOPPED, null);
        } else {
            throw new IllegalStateException(
                    "Cannot stop from state: " + current + ". Expected: READY, ACTIVE, or IDLE");
        }
    }

    @Override
    public void shutdown() {
        RuntimeState current = currentState.get();
        if (current != RuntimeState.STOPPED && current != RuntimeState.FAILED) {
            transitionTo(RuntimeState.STOPPED, null);
        } else {
            throw new IllegalStateException(
                    "Cannot shutdown from state: " + current + ". Expected: any active state");
        }
    }

    @Override
    public void fail(Throwable cause) {
        transitionTo(RuntimeState.FAILED, cause);
    }

    @Override
    public boolean isAcceptingRequests() {
        RuntimeState state = currentState.get();
        return state == RuntimeState.READY || state == RuntimeState.IDLE;
    }

    @Override
    public void addListener(RuntimeLifecycleListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RuntimeLifecycleListener listener) {
        listeners.remove(listener);
    }

    private void transitionTo(RuntimeState newState, Throwable cause) {
        RuntimeState oldState = currentState.getAndSet(newState);
        for (RuntimeLifecycleListener listener : listeners) {
            listener.onStateChanged(oldState, newState, cause);
        }
    }
}