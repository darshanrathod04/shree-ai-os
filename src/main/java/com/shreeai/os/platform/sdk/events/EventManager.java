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

    /**
     * Publishes a runtime event to all subscribers of its type.
     *
     * <p>Dispatch is synchronous: by the time this method returns, every
     * subscriber has processed the event. This is what makes the event-driven
     * {@code KnowledgeSDK.ingest(...)} contract deterministic — ingestion
     * completes before the SDK call returns.</p>
     *
     * @param event the event to publish (must not be null)
     */
    public void publish(RuntimeEvent event) {
        eventBus.publish(Objects.requireNonNull(event, "event must not be null"));
    }
}