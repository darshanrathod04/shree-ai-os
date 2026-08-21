package com.shreeai.os.platform.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;

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

    /**
     * Streams a response while preserving the current session.
     *
     * @param message user message
     * @param listener streaming callback
     */
    public void chatStream(
            String message,
            StreamingListener listener
    ) {

        SDKRequest request = SDKRequest.builder()
                .sessionId(sessionId)
                .message(message)
                .metadata(metadata)
                .build();

        client.chatStream(request.message(), listener);
    }
}