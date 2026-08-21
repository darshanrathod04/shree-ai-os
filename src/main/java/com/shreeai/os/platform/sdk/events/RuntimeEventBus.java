package com.shreeai.os.platform.sdk.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Canonical SDK Runtime Event Bus.
 *
 * Thread-safe publish/subscribe implementation.
 */
public final class RuntimeEventBus {

    private final Map<EventType, List<RuntimeEventListener>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Subscribe to an event type.
     */
    public void subscribe(
            EventType type,
            RuntimeEventListener listener
    ) {
        listeners
                .computeIfAbsent(type, t -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    /**
     * Remove a listener.
     */
    public void unsubscribe(
            EventType type,
            RuntimeEventListener listener
    ) {
        List<RuntimeEventListener> list = listeners.get(type);

        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Publish an event.
     */
    public void publish(RuntimeEvent event) {

        List<RuntimeEventListener> list =
                listeners.get(event.type());

        if (list == null) {
            return;
        }

        for (RuntimeEventListener listener : list) {
            listener.onEvent(event);
        }
    }

    /**
     * Remove every registered listener.
     */
    public void clear() {
        listeners.clear();
    }
}