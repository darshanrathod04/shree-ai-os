package com.shreeai.os.platform.sdk.events;

import java.util.Objects;

/**
 * Developer-facing event manager.
 *
 * Wraps the RuntimeEventBus and exposes a clean subscription API.
 */
public final class EventManager {

    private final RuntimeEventBus eventBus;

    public EventManager(RuntimeEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    /**
     * Subscribe to a runtime event.
     */
    public void on(
            EventType type,
            RuntimeEventListener listener
    ) {
        eventBus.subscribe(type, listener);
    }

    /**
     * Remove a runtime event listener.
     */
    public void off(
            EventType type,
            RuntimeEventListener listener
    ) {
        eventBus.unsubscribe(type, listener);
    }
}