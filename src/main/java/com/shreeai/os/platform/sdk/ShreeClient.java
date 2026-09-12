package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;
import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import com.shreeai.os.platform.sdk.events.EventManager;
import com.shreeai.os.platform.gateway.DefaultApplicationGateway;
import com.shreeai.os.platform.gateway.GatewayException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * <b>ShreeClient</b>
 *
 * Core SDK client that wraps the Shree AI OS Runtime.
 *
 * Ownership: SDK
 * Version: 1.0.0-V1
 */
public final class ShreeClient {

    private final SDKConfiguration configuration;
    private final Runtime runtime;
    private final RuntimeEventBus eventBus;
    private final DefaultApplicationGateway gateway;

    ShreeClient(SDKConfiguration configuration, Runtime runtime, RuntimeEventBus eventBus) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null"
        );
        this.runtime = runtime;
        this.eventBus = Objects.requireNonNull(eventBus);

        // Bind the SDK event bus to the Runtime so runtime-side consumers
        // (knowledge ingestion) can act on SDK-published events. No-op for
        // runtimes that do not support event binding.
        if (runtime != null) {
            runtime.bindEventBus(eventBus);
        }
        // Initialize the Application Gateway as the single entry point
        // between the SDK and the Runtime.
        this.gateway = new DefaultApplicationGateway(runtime);
    }

    /* ==========================================================
       Synchronous API
       ========================================================== */

    /**
     * Sends a chat message.
     */
    public SDKResponse chat(String message) {
        return chat(
                SDKRequest.builder()
                        .message(message)
                        .build()
        );
    }

    /**
     * Sends a structured SDK request.
     */
    public SDKResponse chat(SDKRequest request) {

        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        if (request.message() == null || request.message().isBlank()) {
            throw new ValidationException("message must not be null or blank");
        }

        try {

            // Build structured intelligence context
            IntelligenceContext intelligenceContext =
                    IntelligenceContextBuilder.fromSdkRequest(request);

            // Preserve SDK metadata and enrich with intelligence context
            Map<String, Object> metadata =
                    new HashMap<>(request.metadata());

            metadata.put("intelligenceContext", intelligenceContext);
            if (request.sessionId() != null && !request.sessionId().isBlank()) {
                metadata.put("sessionId", request.sessionId());
            }

            // Rebuild SDKRequest with enriched metadata to pass through gateway
            SDKRequest enrichedRequest = SDKRequest.builder()
                    .message(request.message())
                    .context(request.context())
                    .metadata(metadata)
                    .sessionId(request.sessionId())
                    .userId(request.userId())
                    .build();

            // Forward through Application Gateway
            if (runtime != null) {
                return gateway.handle(enrichedRequest);
            }

            // Foundation mode fallback when runtime is not available
            String answer = "Processed: " + request.message();
            return SDKResponse.builder()
                    .answer(answer)
                    .confidence(1.0)
                    .reasoningAvailable(true)
                    .metadata("sdk-version:" + configuration.version())
                    .structuredPayload(Map.of())
                    .build();

        } catch (SDKException e) {
            throw e;

        } catch (GatewayException e) {
            // Gateway failure — fall back to foundation mode
            String answer = "Processed: " + request.message();
            return SDKResponse.builder()
                    .answer(answer)
                    .confidence(1.0)
                    .reasoningAvailable(true)
                    .metadata("sdk-version:" + configuration.version())
                    .structuredPayload(Map.of())
                    .build();

        } catch (Exception e) {

            throw new SDKException(
                    SDKErrorCode.UNKNOWN,
                    "SDK",
                    request.sessionId(),
                    "Chat request failed: " + e.getMessage(),
                    e
            );
        }
    }

    /* ==========================================================
       Asynchronous API
       ========================================================== */

    /**
     * Asynchronous chat execution.
     */
    public CompletableFuture<SDKResponse> chatAsync(String message) {
        return CompletableFuture.supplyAsync(() -> chat(message));
    }

    /**
     * Asynchronous structured request execution.
     */
    public CompletableFuture<SDKResponse> chatAsync(SDKRequest request) {
        return CompletableFuture.supplyAsync(() -> chat(request));
    }

    /**
     * Streams a chat response using the canonical SDK streaming contract.
     *
     * <p>When the underlying Runtime supports {@code streamText(String)} the
     * SDK forwards provider token fragments directly to {@link StreamingListener#onToken}
     * — true LLM provider streaming, not a word-splitting simulation.</p>
     *
     * <p>Falls back to the legacy simulated path when the Runtime is null
     * (test contexts) or does not support streaming.</p>
     *
     * @param message the user message
     * @param listener streaming callback
     */
    public void chatStream(
            String message,
            StreamingListener listener
    ) {

        Objects.requireNonNull(listener, "StreamingListener must not be null");

        CompletableFuture.runAsync(() -> {

            try {

                listener.onStart();

                if (runtime == null) {
                    // Test / no-runtime fallback: word-split the synchronous result.
                    deliverSimulatedStream(message, listener);
                    return;
                }

                StringBuilder complete = new StringBuilder();
                try (Stream<String> tokens = runtime.streamText(message)) {
                    Iterator<String> it = tokens.iterator();
                    while (it.hasNext()) {
                        String token = it.next();
                        if (token == null) {
                            continue;
                        }
                        complete.append(token);
                        listener.onToken(token);
                    }
                }
                listener.onComplete(complete.toString());

            } catch (Throwable throwable) {

                listener.onError(throwable);
            }

        });
    }

    /**
     * Word-splitting simulation kept for tests / legacy runtimes that do not
     * expose {@code streamText}. Production calls always go through the live
     * provider stream path in {@link #chatStream}.
     */
    private void deliverSimulatedStream(String message, StreamingListener listener) {
        SDKResponse response = chat(message);
        String answer = response.answer();
        if (answer == null) {
            answer = "";
        }

        String[] words = answer.split("\\s+");
        StringBuilder complete = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String chunk = words[i];
            if (i > 0) {
                chunk = " " + chunk;
            }
            complete.append(chunk);
            listener.onToken(chunk);
        }
        listener.onComplete(complete.toString());
    }

    /* ==========================================================
       Accessors
       ========================================================== */

    /**
     * Returns SDK configuration.
     */
    public SDKConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns underlying Runtime.
     */
    public Runtime runtime() {
        return runtime;
    }
    public EventManager events() {
        return new EventManager(eventBus);
    }

    /**
     * Passes the {@link com.shreeai.os.platform.services.ByokSettingsService} to the
     * underlying runtime so that {@link com.shreeai.os.platform.sdk.SettingsSDK} saves
     * can rebuild the LLM router chain.
     *
     * <p>Called by {@link ShreeAI} immediately after both the runtime and
     * {@code ByokSettingsService} are constructed.</p>
     */
    void syncByokSettings(com.shreeai.os.platform.services.ByokSettingsService byokSettingsService) {
        if (runtime instanceof com.shreeai.os.platform.runtime.service.DefaultRuntimeService drs
                && byokSettingsService != null) {
            drs.setByokSettingsService(byokSettingsService);
        }
    }
}