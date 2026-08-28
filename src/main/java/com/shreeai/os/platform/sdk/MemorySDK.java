package com.shreeai.os.platform.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * Memory SDK Facade
 *
 * Developer-facing entry point for the Memory Kernel.
 * Contains no business logic; delegates to ShreeClient.
 */
public final class MemorySDK {

    private final ShreeClient client;

    MemorySDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Searches semantic memory.
     */
    public SDKResponse search(String query) {

        SDKRequest request = SDKRequest.builder()
                .message("MEMORY_SEARCH")
                .metadata(Map.of(
                        "operation", "SEARCH_MEMORY",
                        "query", query
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Stores a memory entry.
     */
    public SDKResponse store(
            String title,
            String content
    ) {

        SDKRequest request = SDKRequest.builder()
                .message("MEMORY_STORE")
                .metadata(Map.of(
                        "operation", "STORE_MEMORY",
                        "title", title,
                        "content", content
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Recalls memories using a semantic query.
     */
    public SDKResponse recall(String query) {

        SDKRequest request = SDKRequest.builder()
                .message("MEMORY_RECALL")
                .metadata(Map.of(
                        "operation", "RECALL_MEMORY",
                        "query", query
                ))
                .build();

        return client.chat(request);
    }
}