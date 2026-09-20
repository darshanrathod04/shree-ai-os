package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.util.Map;
import java.util.Objects;

/**
 * Execution SDK Facade
 *
 * Thin developer-facing wrapper over the Execution Kernel.
 * Contains no execution logic; delegates through ShreeClient.
 */
public final class ExecutionSDK {

    private final ShreeClient client;

    ExecutionSDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Execute a task.
     */
    public SDKResponse execute(
            String capability,
            String input
    ) {
        if (capability == null || capability.isBlank()) {
            throw new ValidationException("capability must not be null or blank");
        }
        if (input == null) {
            throw new ValidationException("input must not be null");
        }

        SDKRequest request = SDKRequest.builder()
                .message("EXECUTION_RUN")
                .metadata(Map.of(
                        "operation", "EXECUTE_TASK",
                        "capability", capability,
                        "input", input
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Execute using structured parameters.
     */
    public SDKResponse execute(
            String capability,
            Map<String, Object> parameters
    ) {
        if (capability == null || capability.isBlank()) {
            throw new ValidationException("capability must not be null or blank");
        }
        if (parameters == null) {
            throw new ValidationException("parameters must not be null");
        }

        SDKRequest request = SDKRequest.builder()
                .message("EXECUTION_RUN")
                .metadata(Map.of(
                        "operation", "EXECUTE_TASK",
                        "capability", capability,
                        "parameters", parameters
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Verify an execution result.
     */
    public SDKResponse verify(String executionId) {
        if (executionId == null || executionId.isBlank()) {
            throw new ValidationException("executionId must not be null or blank");
        }

        SDKRequest request = SDKRequest.builder()
                .message("EXECUTION_VERIFY")
                .metadata(Map.of(
                        "operation", "VERIFY_EXECUTION",
                        "executionId", executionId
                ))
                .build();

        return client.chat(request);
    }
}