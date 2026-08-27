package com.shreeai.os.platform.llm;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Streaming-first provider SPI for Shree AI OS large-language-model calls.
 *
 * <p>The contract is intentionally minimal and synchronous at the boundary: a
 * provider turns an {@link LlmRequest} into a {@link Stream Stream&lt;String&gt;}
 * of token (or delta) fragments. Every other convenience method is derived
 * from {@link #stream(LlmRequest)} so implementations only ever implement one
 * primitive. This keeps the Runtime as the single source of truth and lets the
 * legacy {@code OllamaClient} delegate to any provider without logic duplication.
 * </p>
 *
 * <p>Resource handling: the returned {@link Stream} is closeable. Callers that
 * fully consume the stream (e.g. {@link #complete(LlmRequest)}) MUST close it
 * so that underlying HTTP connections / calls are released. Provider
 * implementations are expected to honour {@link Stream#onClose}.</p>
 *
 * <p>Conventions:</p>
 * <ul>
 *   <li>{@link #providerName()} returns a stable, log-friendly identifier.</li>
 *   <li>{@link #stream(LlmRequest)} never blocks the calling thread on network
 *       I/O before returning; bytes are read lazily as the stream is pulled.</li>
 *   <li>Errors surfacing from the remote are wrapped in a {@link RuntimeException}
 *       so callers are not forced onto a checked-exception path.</li>
 * </ul>
 *
 * @since Sprint 6.2A-P1
 */
public interface LlmProvider {

    /**
     * Stable identifier (e.g. {@code "in-memory"}, {@code "ollama"}) used in
     * logs, tracing and runtime wiring.
     *
     * @return the provider name, never {@code null}
     */
    String providerName();

    /**
     * Materialise the completion for {@code request} as a blocking
     * {@link LlmResponse} by fully consuming {@link #stream(LlmRequest)}.
     *
     * <p>The underlying token stream is closed before this returns.</p>
     *
     * @param request the request, must not be {@code null}
     * @return the completed response, never {@code null}
     */
    default LlmResponse complete(LlmRequest request) {
        StringBuilder content = new StringBuilder();
        try (Stream<String> tokens = stream(request)) {
            tokens.forEach(content::append);
        }
        return LlmResponse.builder()
                .model(request.model())
                .content(content.toString())
                .done(true)
                .finishReason("stop")
                .usage(Map.of("contentLength", content.length()))
                .build();
    }

    /**
     * Streaming-first primitive: turn {@code request} into a live stream of
     * token/delta fragments.
     *
     * <p>Implementations <strong>must</strong> register an
     * {@link Stream#onClose()} handler that releases any backing resource
     * (HTTP call, connection, etc.).</p>
     *
     * @param request the request, must not be {@code null}
     * @return a finite, closeable stream of content fragments
     */
    Stream<String> stream(LlmRequest request);
}
