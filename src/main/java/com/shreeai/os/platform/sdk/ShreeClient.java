package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;
import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import com.shreeai.os.platform.sdk.events.EventManager;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

    ShreeClient(SDKConfiguration configuration, Runtime runtime, RuntimeEventBus eventBus) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null"
        );
        this.runtime = runtime;
        this.eventBus = Objects.requireNonNull(eventBus);
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

            // Preserve SDK metadata
            Map<String, Object> metadata =
                    new HashMap<>(request.metadata());

            metadata.put("intelligenceContext", intelligenceContext);
            if (request.sessionId() != null && !request.sessionId().isBlank()) {
                metadata.put("sessionId", request.sessionId());
            }

            ExecutionRequest executionRequest =
                    ExecutionRequest.builder()
                            .requestId(request.sessionId() != null && !request.sessionId().isBlank()
                                    ? request.sessionId()
                                    : java.util.UUID.randomUUID().toString())
                            .requestType("CHAT")
                            .payload(request.message())
                            .context(request.context())
                            .metadata(metadata)
                            .build();

            ExecutionResult executionResult;

            if (runtime != null) {

                ExecutionSession session = runtime.submit(executionRequest);

                executionResult = session.result();

                if (executionResult == null) {
                    throw new SDKException(
                            SDKErrorCode.RUNTIME_ERROR,
                            "Runtime",
                            executionRequest.requestId(),
                            "Runtime returned a session without an execution result"
                    );
                }

            } else {

                // Foundation mode fallback
                executionResult = ExecutionResult.success(
                        executionRequest.requestId(),
                        "Processed: " + request.message()
                );
            }

            // Structured failure
            if (!executionResult.isSuccess()) {

                throw new SDKException(
                        SDKErrorCode.RUNTIME_ERROR,
                        "Runtime",
                        executionRequest.requestId(),
                        executionResult.output()
                                .orElse("Runtime execution failed")
                );
            }

            // Success response
            String answer = executionResult.output().orElse("");
            double confidence = 1.0;

            Map<String, Object> payload = executionResult.structuredPayload();

            if (payload != null && payload.get("response") instanceof SynthesizedResponse response) {
                answer = response.answer();
                confidence = response.confidence();
            }

            return SDKResponse.builder()
                    .answer(answer)
                    .confidence(confidence)
                    .reasoningAvailable(true)
                    .metadata("sdk-version:" + configuration.version())
                    .structuredPayload(payload)
                    .build();

        } catch (SDKException e) {
            throw e;

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
     * <p>Current implementation streams the completed Runtime response in
     * incremental chunks. The public API will remain unchanged when the
     * Runtime later supports true token streaming.</p>
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

                SDKResponse response = chat(message);

                String answer = response.answer();

                if (answer == null) {
                    answer = "";
                }

                // Stream by words while preserving spaces
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

            } catch (Throwable throwable) {

                listener.onError(throwable);
            }

        });
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
}