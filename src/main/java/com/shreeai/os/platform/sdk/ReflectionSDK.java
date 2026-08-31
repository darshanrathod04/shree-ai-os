package com.shreeai.os.platform.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ReflectionSDK</b>
 *
 * <p>Developer-facing entry point for the Reflection Kernel. Provides a
 * non-breaking API to trigger reflection on a prior execution and query
 * reflection history.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ReflectionSDK {

    private final ShreeClient client;

    ReflectionSDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Triggers reflection on a prior execution.
     *
     * <p>This is the non-breaking Phase 1.5 API. The execution is scored,
     * lessons are extracted and stored in memory, and a REFLECTION_COMPLETED
     * event is published.</p>
     *
     * @param executionId the execution identifier to reflect on
     * @return SDKResponse with the reflection verdict, score, and lessons
     * @throws IllegalArgumentException if executionId is null or blank
     */
    public SDKResponse reflect(String executionId) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be null or blank");
        }

        SDKRequest request = SDKRequest.builder()
                .message("REFLECTION_RUN")
                .metadata(Map.of(
                        "operation", "REFLECT_EXECUTION",
                        "executionId", executionId
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Queries reflection history for analysis.
     *
     * @param tenantId the tenant identifier
     * @param limit    maximum records to return
     * @return SDKResponse with reflection history payload
     */
    public SDKResponse getHistory(String tenantId, int limit) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        SDKRequest request = SDKRequest.builder()
                .message("REFLECTION_HISTORY")
                .metadata(Map.of(
                        "operation", "GET_REFLECTION_HISTORY",
                        "tenantId", tenantId,
                        "limit", limit
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Queries reflection analytics for a tenant.
     *
     * @param tenantId the tenant identifier
     * @param window   analysis window size
     * @return SDKResponse with analytics summary payload
     */
    public SDKResponse getAnalytics(String tenantId, int window) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        SDKRequest request = SDKRequest.builder()
                .message("REFLECTION_ANALYTICS")
                .metadata(Map.of(
                        "operation", "GET_REFLECTION_ANALYTICS",
                        "tenantId", tenantId,
                        "window", window
                ))
                .build();

        return client.chat(request);
    }
}