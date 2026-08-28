package com.shreeai.os.platform.sdk.events;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable SDK runtime event.
 */
public final class RuntimeEvent {

    private final EventType type;
    private final String requestId;
    private final String stage;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    public RuntimeEvent(
            EventType type,
            String requestId,
            String stage,
            Instant timestamp,
            Map<String, Object> metadata
    ) {
        this.type = Objects.requireNonNull(type);
        this.requestId = requestId;
        this.stage = stage;
        this.timestamp = Objects.requireNonNull(timestamp);
        this.metadata = Collections.unmodifiableMap(
                new HashMap<>(Objects.requireNonNull(metadata))
        );
    }

    public EventType type() {
        return type;
    }

    public String requestId() {
        return requestId;
    }

    public String stage() {
        return stage;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}