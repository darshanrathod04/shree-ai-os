package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.ConfigurationException;
import com.shreeai.os.platform.runtime.api.Runtime;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ShreeAI</b>
 *
 * <p>Main entry point for the Shree AI OS SDK.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * ShreeAI ai = ShreeAI.builder()
 *         .apiKey("local")
 *         .build();
 *
 * SDKResponse response = ai.chat("Hello");
 * </pre>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class ShreeAI {

    private final ExecutionSDK execution;
    private final PlanningSDK planning;
    private final KnowledgeSDK knowledge;
    private final MemorySDK memory;
    private final IdentitySDK identity;
    private final SDKConfiguration configuration;
    private final ShreeClient client;

    ShreeAI(SDKConfiguration configuration, Runtime runtime) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.client = new ShreeClient(configuration, runtime);
        this.identity = new IdentitySDK(client);
        this.memory = new MemorySDK(client);
        this.knowledge = new KnowledgeSDK(client);
        this.planning = new PlanningSDK(client);
        this.execution = new ExecutionSDK(client);
    }

    /**
     * Creates a new builder for ShreeAI.
     *
     * @return a new builder
     */
    public static ShreeBuilder builder() {
        return new ShreeBuilder();
    }

    /**
     * Sends a chat message to the platform.
     *
     * @param message the user message
     * @return SDKResponse with the answer
     */
    public SDKResponse chat(String message) {
        return client.chat(message);
    }

    /**
     * Sends a chat request to the platform.
     *
     * @param request the SDK request
     * @return SDKResponse with the answer
     */
    public SDKResponse chat(SDKRequest request) {
        return client.chat(request);
    }

    /**
     * Returns the SDK configuration.
     *
     * @return the configuration
     */
    public SDKConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns the underlying client.
     *
     * @return the client
     */
    public ShreeClient client() {
        return client;
    }

    /**
     * Creates a persistent SDK conversation session.
     */
    public ShreeSession createSession() {
        return ShreeSession.create(client);
    }

    /**
     * Opens an existing session.
     */
    public ShreeSession openSession(String sessionId) {
        return new ShreeSession(sessionId, client);
    }

    public SDKResponse chat(
            String message,
            Map<String, Object> metadata
    ) {

        SDKRequest request = SDKRequest.builder()
                .message(message)
                .metadata(metadata)
                .build();

        return client.chat(request);
    }

    /**
     * Identity Kernel SDK
     */
    public IdentitySDK identity() {
        return identity;
    }

    /**
     * Memory Kernel SDK.
     */
    public MemorySDK memory() {
        return memory;
    }

    /**
     * Knowledge Kernel SDK.
     */
    public KnowledgeSDK knowledge() {
        return knowledge;
    }

    /**
     * Planning Kernel SDK.
     */
    public PlanningSDK planning() {
        return planning;
    }

    /**
     * Execution Kernel SDK.
     */
    public ExecutionSDK execution() {
        return execution;
    }
}