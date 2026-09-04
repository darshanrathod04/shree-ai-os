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
    private final com.shreeai.os.platform.runtime.api.Runtime runtime;

    ReflectionSDK(ShreeClient client) {
        this(client, client != null ? client.runtime() : null);
    }

    ReflectionSDK(ShreeClient client, com.shreeai.os.platform.runtime.api.Runtime runtime) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.runtime = runtime;
    }

    /**
     * Triggers reflection on a prior execution.
     *
     * <p>Phase 1.5: when a {@code Runtime} is available, this method delegates
     * directly to the typed {@link com.shreeai.os.platform.runtime.api.Runtime#reflectOnExecution}
     * interface for in-process reflection. When no runtime is present it falls
     * back to the legacy string-routing path (backward-compatible).</p>
     *
     * @param executionId the execution identifier to reflect on
     * @return SDKResponse with the reflection verdict, score, and lessons
     * @throws IllegalArgumentException if executionId is null or blank
     */
    public SDKResponse reflect(String executionId) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be null or blank");
        }

        if (runtime != null) {
            // Sprint-Phase 1.5: typed in-process reflection path.
            // reflectOnExecution requires additional parameters; derive sensible
            // defaults from what the SDK can provide without a full execution record.
            ReflectionReport report = runtime.reflectOnExecution(
                    executionId,
                    null,                   // requestText — not available from SDK surface
                    0,                      // planStepCount — not available
                    null,                   // actionStatus — not available
                    true,                   // executionSuccess — assume true
                    null,                   // responseSummary — not available
                    0.5                     // confidence — neutral default
            );

            if (report != null) {
                // Build a synthetic SDKResponse from the typed reflection report.
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("executionId", report.executionId());
                payload.put("verdict", report.verdict());
                payload.put("score", report.score());
                payload.put("importanceScore", report.importanceScore());
                payload.put("lessons", report.lessons());
                payload.put("memoryWorthy", report.memoryWorthy());
                payload.put("retryAdvised", report.retryAdvised());
                payload.put("evaluatedAt", report.evaluatedAt().toString());
                payload.put("_reflectionSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer(report.summary())
                        .structuredPayload(payload)
                        .build();
            }
            // Fall through to legacy path when runtime returns null.
        }

        // Legacy string-routing fallback (backward-compatible with existing deployments).
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
     * <p>Phase 1.5: delegates to {@link com.shreeai.os.platform.runtime.api.Runtime#recentReflections}
     * when a runtime is available. Falls back to legacy string routing otherwise.</p>
     *
     * @param tenantId the tenant identifier
     * @param limit    maximum records to return
     * @return SDKResponse with reflection history payload
     */
    public SDKResponse getHistory(String tenantId, int limit) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        if (runtime != null) {
            java.util.List<com.shreeai.os.platform.runtime.reflection.ReflectionHistory> records =
                    runtime.recentReflections(tenantId, limit);
            if (records != null) {
                java.util.List<Map<String, Object>> recordsList = new java.util.ArrayList<>(records.size());
                for (com.shreeai.os.platform.runtime.reflection.ReflectionHistory h : records) {
                    java.util.Map<String, Object> entry = new java.util.LinkedHashMap<>();
                    entry.put("executionId", h.executionId());
                    entry.put("verdict", h.verdict());
                    entry.put("score", h.score());
                    entry.put("importanceScore", h.importanceScore());
                    entry.put("lessons", h.lessons());
                    entry.put("rootCause", h.rootCause() == null ? "" : h.rootCause());
                    entry.put("retryAdvised", h.retryAdvised());
                    entry.put("evaluatedAt", h.evaluatedAt().toString());
                    recordsList.add(entry);
                }
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("tenantId", tenantId);
                payload.put("limit", limit);
                payload.put("count", recordsList.size());
                payload.put("records", recordsList);
                payload.put("_reflectionSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Reflection history retrieved.")
                        .structuredPayload(payload)
                        .build();
            }
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
     * <p>Phase 1.5: delegates to {@link com.shreeai.os.platform.runtime.api.Runtime#reflectionStatistics}
     * when a runtime is available. Falls back to legacy string routing otherwise.</p>
     *
     * @param tenantId the tenant identifier
     * @param window   analysis window size
     * @return SDKResponse with analytics summary payload
     */
    public SDKResponse getAnalytics(String tenantId, int window) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        if (runtime != null) {
            ReflectionStatistics stats = runtime.reflectionStatistics(tenantId, window);
            if (stats != null) {
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("tenantId", stats.tenantId());
                payload.put("totalRecords", stats.totalRecords());
                payload.put("successCount", stats.successCount());
                payload.put("partialCount", stats.partialCount());
                payload.put("failureCount", stats.failureCount());
                payload.put("successRate", stats.successRate());
                payload.put("averageScore", stats.averageScore());
                payload.put("averageImportance", stats.averageImportance());
                payload.put("totalLessons", stats.totalLessons());
                payload.put("rootCauseFrequency", stats.rootCauseFrequency());
                payload.put("computedAt", stats.computedAt().toString());
                payload.put("_reflectionSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Reflection statistics retrieved.")
                        .structuredPayload(payload)
                        .build();
            }
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

    /**
     * Returns typed reflection statistics for a tenant.
     *
     * <p>Phase 1.5 typed path. Returns {@code null} when no runtime is available
     * or when the runtime does not support analytics.</p>
     *
     * @param tenantId the tenant identifier (never null)
     * @param window   number of recent records to analyze (must be ≥ 1)
     * @return the analytics summary, or null if unavailable
     * @throws IllegalArgumentException if tenantId is null/blank or window < 1
     */
    public ReflectionStatistics statistics(String tenantId, int window) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }
        if (window < 1) {
            throw new IllegalArgumentException("window must be ≥ 1");
        }
        if (runtime == null) {
            return null;
        }
        return runtime.reflectionStatistics(tenantId, window);
    }
}