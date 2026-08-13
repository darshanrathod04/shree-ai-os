package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.exceptions.SDKException;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.util.Objects;

/**
 * <b>ShreeClient</b>
 *
 * <p>Core SDK client that wraps the Shree AI OS Runtime.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class ShreeClient {

    private final SDKConfiguration configuration;
    private final Runtime runtime;

    ShreeClient(SDKConfiguration configuration, Runtime runtime) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.runtime = runtime; // May be null for SDK foundation mode
    }

    /**
     * Sends a chat message to the platform.
     *
     * @param message the user message
     * @return SDKResponse with the answer
     * @throws SDKException if the request fails
     */
    public SDKResponse chat(String message) {
        SDKRequest request = SDKRequest.builder()
                .message(message)
                .build();
        return chat(request);
    }

    /**
     * Sends a chat request to the platform.
     *
     * @param request the SDK request
     * @return SDKResponse with the answer
     * @throws ValidationException if the request is invalid
     * @throws SDKException if the execution fails
     */
    public SDKResponse chat(SDKRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new ValidationException("message must not be null or blank");
        }

        try {
            // Create ExecutionRequest from SDKRequest
            ExecutionRequest executionRequest = ExecutionRequest.builder()
                    .requestId(request.sessionId())
                    .requestType("CHAT")
                    .payload(request.message())
                    .build();

            // Submit to Runtime if available
            ExecutionResult executionResult;
            if (runtime != null) {
                ExecutionSession session = runtime.submit(executionRequest);
                // Use the actual execution result from the Runtime session
                executionResult = session.result();
                if (executionResult == null) {
                    throw new SDKException("Runtime returned a session without an execution result");
                }
            } else {
                // Fallback only if Runtime is not available (should not happen with new ShreeBuilder)
                executionResult = ExecutionResult.success(
                        executionRequest.requestId(),
                        "Processed: " + request.message()
                );
            }

            // Convert ExecutionResult to SDKResponse
            return SDKResponse.builder()
                    .answer(executionResult.output().orElse("No output"))
                    .confidence(executionResult.isSuccess() ? 1.0 : 0.0)
                    .reasoningAvailable(executionResult.isSuccess())
                    .metadata("sdk-version:" + configuration.version())
                    .build();
        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            throw new SDKException("Chat request failed: " + e.getMessage(), e);
        }
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
     * Returns the underlying runtime.
     *
     * @return the runtime
     */
    public Runtime runtime() {
        return runtime;
    }
}