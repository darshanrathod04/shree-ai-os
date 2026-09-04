package com.shreeai.os.platform.services;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>SdkDiagnosticsService</b>
 *
 * <p>Aggregates runtime diagnostics for the SDK: active LLM provider, model,
 * kernel in use, latency, knowledge hits, memory usage, and routing source.</p>
 *
 * <p><b>Output shape:</b></p>
 * <pre>
 * Provider         OpenAI
 * Model            gpt-5
 * Kernel           Knowledge Kernel
 * Latency          41 ms
 * Knowledge Hits   3
 * Memory Used      true
 * Routing Source   Runtime
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
@Service
public class SdkDiagnosticsService {

    /** Identifies the active kernel being inspected. */
    public enum Kernel { KNOWLEDGE, MEMORY, IDENTITY, PLANNING, EXECUTION, REFLECTION, PROJECT, WORKFLOW }

    /** Source of routing for the current request. */
    public enum RoutingSource { RUNTIME, SDK, LLM_ROUTER }

    private volatile String provider = "in-memory";
    private volatile String model = "default";
    private volatile Kernel activeKernel = Kernel.KNOWLEDGE;
    private volatile long latencyMs = 0L;
    private volatile int knowledgeHits = 0;
    private volatile boolean memoryUsed = true;
    private volatile RoutingSource routingSource = RoutingSource.RUNTIME;

    public SdkDiagnosticsService() {}

    public SdkDiagnosticsService provider(String v) { this.provider = v; return this; }
    public SdkDiagnosticsService model(String v) { this.model = v; return this; }
    public SdkDiagnosticsService activeKernel(Kernel v) { this.activeKernel = v; return this; }
    public SdkDiagnosticsService latencyMs(long v) { this.latencyMs = v; return this; }
    public SdkDiagnosticsService knowledgeHits(int v) { this.knowledgeHits = v; return this; }
    public SdkDiagnosticsService memoryUsed(boolean v) { this.memoryUsed = v; return this; }
    public SdkDiagnosticsService routingSource(RoutingSource v) { this.routingSource = v; return this; }

    public String provider() { return provider; }
    public String model() { return model; }
    public Kernel activeKernel() { return activeKernel; }
    public long latencyMs() { return latencyMs; }
    public int knowledgeHits() { return knowledgeHits; }
    public boolean memoryUsed() { return memoryUsed; }
    public RoutingSource routingSource() { return routingSource; }

    /**
     * Generates the full diagnostics report.
     */
    public Map<String, Object> report() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("provider", provider);
        m.put("model", model);
        m.put("kernel", activeKernel.name() + " Kernel");
        m.put("latencyMs", latencyMs);
        m.put("latencyDisplay", latencyMs + " ms");
        m.put("knowledgeHits", knowledgeHits);
        m.put("memoryUsed", memoryUsed);
        m.put("routingSource", routingSource.name());
        return m;
    }

    /**
     * Returns a formatted multi-line report.
     */
    public String reportAsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : report().entrySet()) {
            sb.append(pad(e.getKey())).append(" ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Records a knowledge hit.
     */
    public void recordKnowledgeHit() {
        knowledgeHits++;
    }

    /**
     * Starts a latency timer. Returns a Runnable that, when called, sets
     * the latency.
     */
    public Runnable startTimer() {
        long start = System.nanoTime();
        return () -> this.latencyMs = (System.nanoTime() - start) / 1_000_000L;
    }

    /**
     * Resets all runtime metrics.
     */
    public void reset() {
        this.latencyMs = 0L;
        this.knowledgeHits = 0;
        this.memoryUsed = true;
    }

    private static String pad(String s) {
        Objects.requireNonNull(s, "s");
        if (s.length() >= 16) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < 16) sb.append(' ');
        return sb.toString();
    }
}
