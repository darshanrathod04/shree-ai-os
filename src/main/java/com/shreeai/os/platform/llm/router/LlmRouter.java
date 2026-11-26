package com.shreeai.os.platform.llm.router;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * <b>LlmRouter</b>
 *
 * <p>The interchangeable provider architecture of Shree AI OS: routes
 * {@link LlmRequest}s across an ordered, failure-tolerant chain of
 * {@link LlmProvider}s (OpenAI, Gemini, Anthropic, Ollama, in-memory)
 * with per-provider circuit breaking and latency/token telemetry.</p>
 *
 * <p><b>Thread Safety:</b> Thread-safe after construction; safe to share.</p>
 *
 * @since Sprint 6.3A-P4
 */
public final class LlmRouter implements LlmProvider {

    /** Name reported by {@link #providerName()} for logs and tracing. */
    public static final String ROUTER_NAME = "llm-router";

    private static final int DEFAULT_FAILURE_THRESHOLD = 3;
    private static final long DEFAULT_COOLDOWN_MS = 30_000L;

    private final List<LlmProvider> chain;
    private final int failureThreshold;
    private final long cooldownMs;
    private final Map<String, MutableTelemetry> telemetryMap = new ConcurrentHashMap<>();

    public LlmRouter(List<LlmProvider> chain) {
        this(chain, DEFAULT_FAILURE_THRESHOLD, DEFAULT_COOLDOWN_MS);
    }

    public LlmRouter(List<LlmProvider> chain, int failureThreshold, long cooldownMs) {
        Objects.requireNonNull(chain, "chain must not be null");
        if (chain.isEmpty()) {
            throw new IllegalArgumentException("chain must contain at least one provider");
        }
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold must be >= 1");
        }
        if (cooldownMs < 0) {
            throw new IllegalArgumentException("cooldownMs must be >= 0");
        }
        this.chain = List.copyOf(chain);
        this.failureThreshold = failureThreshold;
        this.cooldownMs = cooldownMs;
        for (LlmProvider p : this.chain) {
            this.telemetryMap.computeIfAbsent(p.providerName(), k -> new MutableTelemetry());
        }
    }

    public static LlmRouter fromChain(String chainSpec, Map<String, LlmProvider> registry) {
        Objects.requireNonNull(chainSpec, "chainSpec must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        Set<LlmProvider> resolved = new LinkedHashSet<>();
        for (String name : chainSpec.split(",")) {
            String key = name.trim().toLowerCase();
            if (key.isEmpty()) {
                continue;
            }
            Object provider = registry.get(key);
            if (provider instanceof LlmProvider llm) {
                resolved.add(llm);
            }
        }
        if (resolved.isEmpty()) {
            throw new IllegalArgumentException(
                    "No provider in chain spec resolved: " + chainSpec);
        }
        return new LlmRouter(new ArrayList<>(resolved));
    }

    public List<LlmProvider> providers() {
        return chain;
    }

    public List<String> providerNames() {
        List<String> names = new ArrayList<>();
        for (LlmProvider provider : chain) {
            names.add(provider.providerName());
        }
        return Collections.unmodifiableList(names);
    }

    public Map<String, ProviderTelemetry> telemetry() {
        Map<String, ProviderTelemetry> snapshot = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (LlmProvider provider : chain) {
            MutableTelemetry m = telemetryMap.computeIfAbsent(provider.providerName(), k -> new MutableTelemetry());
            snapshot.put(provider.providerName(), m.snapshot(now));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public Optional<LlmProvider> route(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        long now = System.currentTimeMillis();
        for (LlmProvider provider : chain) {
            MutableTelemetry m = telemetryMap.computeIfAbsent(provider.providerName(), k -> new MutableTelemetry());
            if (m.isCircuitOpen(now)) {
                continue;
            }
            try {
                Stream<String> probe = provider.stream(request);
                return Optional.of(provider).map(p -> {
                    probe.close();
                    return p;
                });
            } catch (RuntimeException ignored) {
                // Provider unavailable — fall through to the next one.
            }
        }
        return Optional.empty();
    }

    @Override
    public String providerName() {
        return ROUTER_NAME;
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        IllegalStateException lastFailure = new IllegalStateException(
                "No provider in chain could serve the request: " + providerNames());

        long now = System.currentTimeMillis();
        boolean anyAttempted = false;

        for (LlmProvider provider : chain) {
            MutableTelemetry metrics = telemetryMap.computeIfAbsent(provider.providerName(), k -> new MutableTelemetry());
            if (metrics.isCircuitOpen(now)) {
                continue;
            }
            anyAttempted = true;
            Stream<String> stream = tryInvoke(provider, request, metrics, lastFailure);
            if (stream != null) {
                return stream;
            }
        }

        // If every provider's circuit was open, probe all of them as a last resort.
        if (!anyAttempted) {
            for (LlmProvider provider : chain) {
                MutableTelemetry metrics = telemetryMap.computeIfAbsent(provider.providerName(), k -> new MutableTelemetry());
                Stream<String> stream = tryInvoke(provider, request, metrics, lastFailure);
                if (stream != null) {
                    return stream;
                }
            }
        }

        throw lastFailure;
    }

    private Stream<String> tryInvoke(
            LlmProvider provider,
            LlmRequest request,
            MutableTelemetry metrics,
            IllegalStateException lastFailure) {
        long startNs = System.nanoTime();
        try {
            System.out.println(">>> [LLM ROUTER] Invoking provider: " + provider.providerName());
            Stream<String> rawStream = provider.stream(request);
            long connectLatencyMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            metrics.recordSuccess(connectLatencyMs);
            System.out.println(">>> [LLM ROUTER] Provider '" + provider.providerName() + "' is ACTIVE and serving response.");
            return rawStream.peek(chunk -> metrics.addEstimatedTokens(estimateTokens(chunk)));
        } catch (RuntimeException failure) {
            long failLatencyMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            metrics.recordFailure(failLatencyMs, failure.getMessage(), failureThreshold, cooldownMs);
            System.err.println(">>> [LLM ROUTER] Provider '" + provider.providerName() + "' FAILED ("
                    + failure.getMessage() + "). Falling back to next provider in chain...");
            lastFailure.addSuppressed(failure);
            return null;
        }
    }

    private static long estimateTokens(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return 0L;
        }
        return Math.max(1L, chunk.length() / 4L);
    }

    public record ProviderTelemetry(
            long successCount,
            long failureCount,
            int consecutiveFailures,
            boolean circuitOpen,
            long lastLatencyMs,
            long totalLatencyMs,
            long estimatedOutputTokens,
            String lastError
    ) {}

    private static final class MutableTelemetry {
        private final AtomicLong successCount = new AtomicLong();
        private final AtomicLong failureCount = new AtomicLong();
        private final AtomicInteger consecutiveFailures = new AtomicInteger();
        private final AtomicLong circuitOpenUntilMs = new AtomicLong();
        private final AtomicLong lastLatencyMs = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong estimatedOutputTokens = new AtomicLong();
        private volatile String lastError = "";

        boolean isCircuitOpen(long nowMs) {
            long until = circuitOpenUntilMs.get();
            return until > nowMs;
        }

        void recordSuccess(long latencyMs) {
            successCount.incrementAndGet();
            consecutiveFailures.set(0);
            circuitOpenUntilMs.set(0L);
            lastLatencyMs.set(latencyMs);
            totalLatencyMs.addAndGet(latencyMs);
            lastError = "";
        }

        void recordFailure(long latencyMs, String error, int threshold, long cooldownMs) {
            failureCount.incrementAndGet();
            lastLatencyMs.set(latencyMs);
            totalLatencyMs.addAndGet(latencyMs);
            lastError = error == null ? "unknown" : error;
            int streak = consecutiveFailures.incrementAndGet();
            if (streak >= threshold && cooldownMs > 0L) {
                circuitOpenUntilMs.set(System.currentTimeMillis() + cooldownMs);
            }
        }

        void addEstimatedTokens(long tokens) {
            if (tokens > 0L) {
                estimatedOutputTokens.addAndGet(tokens);
            }
        }

        ProviderTelemetry snapshot(long nowMs) {
            return new ProviderTelemetry(
                    successCount.get(),
                    failureCount.get(),
                    consecutiveFailures.get(),
                    isCircuitOpen(nowMs),
                    lastLatencyMs.get(),
                    totalLatencyMs.get(),
                    estimatedOutputTokens.get(),
                    lastError
            );
        }
    }
}
