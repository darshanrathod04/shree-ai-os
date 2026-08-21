package com.shreeai.os.platform.sdk;

import java.util.Objects;
import java.util.UUID;

/**
 * Persistent SDK conversation session.
 *
 * One session = one conversation.
 */
public final class ShreeSession {

    private final String sessionId;
    private final ShreeClient client;

    ShreeSession(String sessionId, ShreeClient client) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.client = Objects.requireNonNull(client);
    }

    public String sessionId() {
        return sessionId;
    }

    public SDKResponse chat(String message) {

        SDKRequest request = SDKRequest.builder()
                .sessionId(sessionId)
                .message(message)
                .build();

        return client.chat(request);
    }

    public static ShreeSession create(ShreeClient client) {
        return new ShreeSession(UUID.randomUUID().toString(), client);
    }
}