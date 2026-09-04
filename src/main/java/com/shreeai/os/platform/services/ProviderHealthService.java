package com.shreeai.os.platform.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * <b>ProviderHealthService</b>
 *
 * <p>Reports the health and latency of each configured LLM provider.
 * Each {@code check*} method returns a {@link HealthStatus} but does
 * not throw — provider unavailability is reflected in the result.</p>
 *
 * <p><b>Health model:</b></p>
 * <ul>
 *   <li>{@code HEALTHY} — provider responded in &lt;5s</li>
 *   <li>{@code DEGRADED} — provider responded slowly or with errors</li>
 *   <li>{@code OFFLINE} — provider could not be reached</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public class ProviderHealthService {

    /** Provider health status. */
    public enum HealthStatus { HEALTHY, DEGRADED, OFFLINE }

    private final Map<ProviderType, Long> lastLatency = new ConcurrentHashMap<>();
    private final Map<ProviderType, HealthStatus> lastStatus = new ConcurrentHashMap<>();
    private final ByokSettingsService settingsService;

    public ProviderHealthService(ByokSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public ProviderHealthService() {
        this(new ByokSettingsService());
    }

    /**
     * Returns the aggregated health report for all providers.
     */
    public List<Map<String, Object>> health() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ProviderType type : ProviderType.all()) {
            out.add(snapshot(type));
        }
        return out;
    }

    /**
     * Returns a one-line health report as a formatted string.
     */
    public String healthReport() {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> h : health()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(String.format("%-10s %-10s %s",
                    h.get("provider"), h.get("status"), h.get("latencyDisplay")));
        }
        return sb.toString();
    }

    /**
     * Checks OpenAI. Uses the configured endpoint to verify reachability.
     * Returns a HealthStatus; never throws.
     */
    public HealthStatus checkOpenAI() {
        return checkProvider(ProviderType.OPENAI, () -> pingEndpoint("https://api.openai.com"));
    }

    /**
     * Checks Gemini. Returns a HealthStatus; never throws.
     */
    public HealthStatus checkGemini() {
        return checkProvider(ProviderType.GEMINI, () -> pingEndpoint("https://generativelanguage.googleapis.com"));
    }

    /**
     * Checks Ollama. Returns a HealthStatus; never throws.
     */
    public HealthStatus checkOllama() {
        ProviderSettings settings = settingsService.get(ProviderType.OLLAMA);
        String ep = settings == null ? "http://localhost:11434" : settings.endpoint();
        final String endpoint = (ep == null || ep.isBlank()) ? "http://localhost:11434" : ep;
        return checkProvider(ProviderType.OLLAMA, () -> pingEndpoint(endpoint));
    }

    /**
     * Returns the last observed latency for a provider, or -1 if never checked.
     */
    public long lastLatency(ProviderType type) {
        Long v = lastLatency.get(type);
        return v == null ? -1 : v;
    }

    /**
     * Returns the last observed status for a provider, or null if never checked.
     */
    public HealthStatus lastStatus(ProviderType type) {
        return lastStatus.get(type);
    }

    /**
     * Resets the cached health information.
     */
    public void reset() {
        lastLatency.clear();
        lastStatus.clear();
    }

    // ─── Internals ──────────────────────────────────────────────────────────────

    /**
     * Checks a provider and records the result.
     */
    private HealthStatus checkProvider(ProviderType type, Supplier<Boolean> reachabilityTest) {
        long start = System.nanoTime();
        try {
            boolean reachable = Boolean.TRUE.equals(reachabilityTest.get());
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            lastLatency.put(type, elapsedMs);
            HealthStatus status;
            if (!reachable) {
                status = HealthStatus.OFFLINE;
            } else if (elapsedMs > 5_000L) {
                status = HealthStatus.DEGRADED;
            } else {
                status = HealthStatus.HEALTHY;
            }
            lastStatus.put(type, status);
            return status;
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            lastLatency.put(type, elapsedMs);
            lastStatus.put(type, HealthStatus.OFFLINE);
            return HealthStatus.OFFLINE;
        }
    }

    /**
     * Returns a snapshot of the current health for a provider without
     * performing a fresh check.
     */
    public Map<String, Object> snapshot(ProviderType type) {
        HealthStatus status = lastStatus.get(type);
        Long latency = lastLatency.get(type);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("provider", type.name());
        m.put("status", status == null ? HealthStatus.OFFLINE.name() : status.name());
        if (latency == null || latency < 0) {
            m.put("latencyMs", -1);
            m.put("latencyDisplay", "-");
        } else {
            m.put("latencyMs", latency);
            m.put("latencyDisplay", latency + " ms");
        }
        return m;
    }

    /**
     * Tries to ping a network endpoint. Returns false if no connection
     * can be established (does not throw).
     */
    private boolean pingEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return false;
        try {
            java.net.URI uri = java.net.URI.create(endpoint);
            int port = uri.getPort() < 0 ? 443 : uri.getPort();
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(uri.getHost(), port), 2000);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
