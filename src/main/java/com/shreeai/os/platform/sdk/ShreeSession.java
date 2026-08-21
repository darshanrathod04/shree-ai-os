package com.shreeai.os.platform.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ShreeSession {

    private final String sessionId;
    private final ShreeClient client;

    private final Map<String, Object> metadata = new HashMap<>();

    ShreeSession(String sessionId, ShreeClient client) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.client = Objects.requireNonNull(client);
    }

    public static ShreeSession create(ShreeClient client) {
        return new ShreeSession(
                UUID.randomUUID().toString(),
                client
        );
    }

    public String sessionId() {
        return sessionId;
    }

    public ShreeSession metadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    public SDKResponse chat(String message) {

        SDKRequest request =
                SDKRequest.builder()
                        .sessionId(sessionId)
                        .message(message)
                        .metadata(metadata)
                        .build();

        return client.chat(request);
    }

    /**
     * Continue the conversation asynchronously.
     */
    public CompletableFuture<SDKResponse> chatAsync(String message) {

        SDKRequest request = SDKRequest.builder()
                .sessionId(sessionId)
                .message(message)
                .metadata(metadata)
                .build();

        return client.chatAsync(request);
    }
}