package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

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

    ShreeClient(SDKConfiguration configuration, Runtime runtime) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null"
        );
        this.runtime = runtime;
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

            ExecutionRequest executionRequest =
                    ExecutionRequest.builder()
                            .requestId(request.sessionId())
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
            return SDKResponse.builder()
                    .answer(executionResult.output().orElse(""))
                    .confidence(1.0)
                    .reasoningAvailable(true)
                    .metadata("sdk-version:" + configuration.version())
                    .structuredPayload(executionResult.structuredPayload())
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
}