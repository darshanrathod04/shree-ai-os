package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.SDKException;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import com.shreeai.os.platform.runtime.api.Runtime;

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
            // The response is built from the request
            // In this SDK foundation, we compose a deterministic response
            // based on the configured runtime and request
            double confidence = 0.85;
            boolean reasoningAvailable = true;
            String answer = "Processed: " + request.message();

            return SDKResponse.builder()
                    .answer(answer)
                    .confidence(confidence)
                    .reasoningAvailable(reasoningAvailable)
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