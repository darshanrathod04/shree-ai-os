package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.sdk.exceptions.ConfigurationException;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.sdk.observability.GenAiTracer;
import com.shreeai.os.platform.sdk.structured.StructuredOutputConverter;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;
import com.shreeai.os.platform.services.ByokSettingsService;
import com.shreeai.os.platform.services.SdkDiagnosticsService;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import com.shreeai.os.platform.sdk.events.EventManager;

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
public final class ShreeAI implements AutoCloseable {

    private final ExecutionSDK execution;
    private final PlanningSDK planning;
    private final KnowledgeSDK knowledge;
    private final MemorySDK memory;
    private final IdentitySDK identity;
    private final ReflectionSDK reflection;
    private final ProjectSDK project;
    private final SDKConfiguration configuration;
    private final ShreeClient client;
    private final SettingsSDK settings;
    private final DiagnosticsSDK diagnostics;
    private final AgentSDK agent;
    private final GenAiTracer tracer = new GenAiTracer();

    ShreeAI(SDKConfiguration configuration, Runtime runtime) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.client = new ShreeClient(
                configuration,
                runtime,
                new RuntimeEventBus()
        );
        this.identity = new IdentitySDK(client, client.runtime());
        this.memory = new MemorySDK(client);
        this.knowledge = new KnowledgeSDK(client);
        this.planning = new PlanningSDK(client);
        this.execution = new ExecutionSDK(client);
        this.reflection = new ReflectionSDK(client, client.runtime());
        this.project = new ProjectSDK();
        // Single ByokSettingsService instance is shared by SettingsSDK (writes)
        // and DefaultRuntimeService (LLM router rebuilds on write).
        com.shreeai.os.platform.services.ByokSettingsService byok = new ByokSettingsService();
        this.settings = new SettingsSDK(byok);
        configureInitialApiKey(byok, configuration.apiKey());
        this.diagnostics = new DiagnosticsSDK(new SdkDiagnosticsService());
        // Wire BYOK → runtime so SettingsSDK.save() and builder apiKey() rebuild the LLM router.
        this.client.syncByokSettings(byok);
        this.agent = new AgentSDK(client.runtime());
    }

    private static void configureInitialApiKey(ByokSettingsService byok, String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return;
        }
        String trimmed = rawKey.trim();
        if ("local".equalsIgnoreCase(trimmed) || trimmed.length() < 8) {
            return;
        }
        com.shreeai.os.platform.services.ProviderType provider = trimmed.startsWith("sk-")
                ? com.shreeai.os.platform.services.ProviderType.OPENAI
                : com.shreeai.os.platform.services.ProviderType.GEMINI;
        byok.save(new com.shreeai.os.platform.services.ProviderSettings(
                provider, true, trimmed, "", trimmed));
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

    /**
     * Reflection Kernel SDK (Phase 1.5).
     */
    public ReflectionSDK reflection() {
        return reflection;
    }

    /**
     * Project Intelligence Kernel SDK (Sprint-13).
     *
     * <p>Analyzes a software project structurally and provides impact
     * analysis, class discovery, and architecture intelligence.</p>
     */
    public ProjectSDK project() {
        return project;
    }

    /**
     * BYOK Settings SDK (Platform Services v1.0).
     *
     * <p>Manages per-provider LLM credentials. Keys are masked before
     * being returned. Used for Bring-Your-Own-Key configuration.</p>
     */
    public SettingsSDK settings() {
        return settings;
    }

    /**
     * SDK Diagnostics (Platform Services v1.0).
     *
     * <p>Provides runtime diagnostics: active provider, model, kernel,
     * latency, knowledge hits, and routing source.</p>
     */
    public DiagnosticsSDK diagnostics() {
        return diagnostics;
    }

    /**
     * Autonomous Tool-Calling Agent SDK.
     */
    public AgentSDK agent() {
        return agent;
    }

    /**
     * GenAI Span & Trace Recorder.
     */
    public GenAiTracer tracer() {
        return tracer;
    }

    /**
     * Type-safe structured output chat (Spring AI BeanOutputConverter / LangChain4j style).
     */
    public <T> T chat(String message, Class<T> responseType) {
        var trace = tracer.startTrace("chat.structured", responseType.getSimpleName());
        long t0 = System.nanoTime();
        try {
            T result = StructuredOutputConverter.convertWithRetry(
                    message,
                    responseType,
                    prompt -> chat(prompt).answer()
            );
            trace.addSpan("structured-output-parse", "structured-output",
                    (System.nanoTime() - t0) / 1_000_000L,
                    Map.of("targetType", responseType.getName()));
            trace.finish("OK");
            return result;
        } catch (RuntimeException e) {
            trace.addSpan("structured-output-error", "structured-output",
                    (System.nanoTime() - t0) / 1_000_000L,
                    Map.of("error", String.valueOf(e.getMessage())));
            trace.finish("ERROR");
            throw e;
        }
    }

    /**
     * Asynchronous chat.
     */
    public CompletableFuture<SDKResponse> chatAsync(String message) {
        return client.chatAsync(message);
    }

    /**
     * Asynchronous structured request.
     */
    public CompletableFuture<SDKResponse> chatAsync(SDKRequest request) {
        return client.chatAsync(request);
    }

    /**
     * Streams a chat response.
     *
     * @param message user message
     * @param listener streaming callback
     */
    public void chatStream(
            String message,
            StreamingListener listener
    ) {
        client.chatStream(message, listener);
    }

    public EventManager events() {
        return client.events();
    }

    /**
     * Stops the underlying runtime and releases resources.
     */
    @Override
    public void close() {
        Runtime runtime = client.runtime();
        if (runtime != null) {
            runtime.stop();
        }
    }
}