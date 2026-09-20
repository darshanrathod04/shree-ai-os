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
        if (type == null || listener == null) {
            return;
        }
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
        if (type == null || listener == null) {
            return;
        }
        List<RuntimeEventListener> list = listeners.get(type);

        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Publish an event.
     */
    public void publish(RuntimeEvent event) {
        if (event == null || event.type() == null) {
            return;
        }

        List<RuntimeEventListener> list =
                listeners.get(event.type());

        if (list == null) {
            return;
        }

        for (RuntimeEventListener listener : list) {
            try {
                listener.onEvent(event);
            } catch (Throwable ignored) {
                // Isolate listener failures so one subscriber does not starve others or abort caller
            }
        }
    }

    /**
     * Remove every registered listener.
     */
    public void clear() {
        listeners.clear();
    }
}