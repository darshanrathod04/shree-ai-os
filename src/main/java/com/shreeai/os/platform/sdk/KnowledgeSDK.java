package com.shreeai.os.platform.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * Knowledge SDK Facade
 *
 * Thin developer-facing wrapper over the Knowledge Kernel.
 */
public final class KnowledgeSDK {

    private final ShreeClient client;

    KnowledgeSDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Semantic knowledge query.
     */
    public SDKResponse query(String question) {

        SDKRequest request = SDKRequest.builder()
                .message("KNOWLEDGE_QUERY")
                .metadata(Map.of(
                        "operation", "QUERY_KNOWLEDGE",
                        "question", question
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Retrieve a knowledge entity.
     */
    public SDKResponse retrieve(String entityId) {

        SDKRequest request = SDKRequest.builder()
                .message("KNOWLEDGE_RETRIEVE")
                .metadata(Map.of(
                        "operation", "RETRIEVE_ENTITY",
                        "entityId", entityId
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Search the knowledge graph.
     */
    public SDKResponse search(String keyword) {

        SDKRequest request = SDKRequest.builder()
                .message("KNOWLEDGE_SEARCH")
                .metadata(Map.of(
                        "operation", "SEARCH_KNOWLEDGE",
                        "keyword", keyword
                ))
                .build();

        return client.chat(request);
    }
}