package com.shreeai.os.platform.llm.router;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <b>LlmRouter</b>
 *
 * <p>The interchangeable provider architecture of Shree AI OS: routes
 * {@link LlmRequest}s across an ordered, failure-tolerant chain of
 * {@link LlmProvider}s (GPT / OpenAI, Gemini, Ollama, in-memory, ...).</p>
 *
 * <p><b>Routing model:</b></p>
 * <ul>
 *   <li><b>Priority order</b> — the first provider in the chain is preferred;
 *       later providers are fallbacks.</li>
 *   <li><b>Fail-over</b> — if a provider fails before producing a usable
 *       stream (unavailable, HTTP error, bad credentials), the router retries
 *       with the next provider automatically.</li>
 *   <li><b>Composable</b> — the router itself implements {@link LlmProvider},
 *       so callers (runtime stages, legacy clients, SDKs) consume it exactly
 *       like any single provider. The LLM stays replaceable end to end.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Immutable after construction; safe to share.</p>
 *
 * @since Sprint 6.3A-P4
 */
public final class LlmRouter implements LlmProvider {

    /** Name reported by {@link #providerName()} for logs and tracing. */
    public static final String ROUTER_NAME = "llm-router";

    private final List<LlmProvider> chain;

    /**
     * Creates a router over an ordered provider chain.
     *
     * @param chain ordered providers, first = preferred (must not be null or empty)
     */
    public LlmRouter(List<LlmProvider> chain) {
        Objects.requireNonNull(chain, "chain must not be null");
        if (chain.isEmpty()) {
            throw new IllegalArgumentException("chain must contain at least one provider");
        }
        this.chain = List.copyOf(chain);
    }

    /**
     * Convenience factory building a router from a comma-separated chain spec
     * (e.g. {@code "openai,gemini,ollama"}) resolved against a provider registry.
     *
     * @param chainSpec comma-separated provider names, first = preferred
     * @param registry  available providers keyed by {@link LlmProvider#providerName()}
     * @return a router over the resolved providers (never null)
     * @throws IllegalArgumentException if no requested provider resolves
     */
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

    /**
     * Returns the ordered provider chain.
     *
     * @return unmodifiable ordered provider list (never null)
     */
    public List<LlmProvider> providers() {
        return chain;
    }

    /**
     * Returns the names of the providers in routing order.
     *
     * @return unmodifiable ordered provider-name list (never null)
     */
    public List<String> providerNames() {
        List<String> names = new ArrayList<>();
        for (LlmProvider provider : chain) {
            names.add(provider.providerName());
        }
        return Collections.unmodifiableList(names);
    }

    /**
     * Selects the first provider that can serve {@code request} without
     * throwing (preferred provider first, fallbacks after).
     *
     * @param request the request to route (must not be null)
     * @return the selected provider, or empty when every provider failed
     */
    public Optional<LlmProvider> route(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        for (LlmProvider provider : chain) {
            try {
                // Probe availability without consuming: opening a stream is the
                // cheapest reliable health check for HTTP-backed providers.
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

    /**
     * Streams from the first provider that succeeds; if a provider fails
     * before emitting any fragment, the next one is tried.
     *
     * @param request the request (must not be null)
     * @return a live token stream from the selected provider
     * @throws IllegalStateException when every provider in the chain fails
     */
    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        IllegalStateException lastFailure = new IllegalStateException(
                "No provider in chain could serve the request: " + providerNames());

        for (LlmProvider provider : chain) {
            try {
                return provider.stream(request);
            } catch (RuntimeException failure) {
                lastFailure.addSuppressed(failure);
            }
        }
        throw lastFailure;
    }
}