package com.shreeai.os.platform.llm.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.LlmResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * OkHttp-backed, streaming-first {@link LlmProvider} for Ollama's
 * {@code /api/generate} endpoint.
 *
 * <p>This is the canonical replacement for the legacy {@code OllamaClient}. Per
 * the constitutional migration rules (R2: promote-and-delegate, no logic
 * duplication), this class does <strong>not</strong> extend or call the legacy
 * client; instead it re-implements the HTTP contract on the shared OkHttp +
 * Jackson stack already present in the project. The legacy client is wired to
 * delegate to a provider behind this interface in a later step, never the other
 * way around.</p>
 *
 * <p>Streaming: Ollama emits newline-delimited JSON objects when
 * {@code stream=true}. Each object carries a {@code "response"} delta fragment;
 * the final object carries {@code "done":true}. This provider exposes those
 * fragments as a {@link Stream} whose {@link Stream#onClose()} releases the
 * OkHttp call and response body.</p>
 *
 * @since Sprint 6.2A-P1
 */
public final class OllamaProvider implements LlmProvider {

    /** Default Ollama generate endpoint (mirrors legacy {@code OllamaClient}). */
    static final String DEFAULT_URL = "http://localhost:11434/api/generate";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String url;

    /** Default constructor: localhost Ollama + a fresh OkHttp client. */
    public OllamaProvider() {
        this(DEFAULT_URL, new OkHttpClient());
    }

    /**
     * @param url    full Ollama generate endpoint URL (e.g.
     *               {@code http://localhost:11434/api/generate})
     * @param client the OkHttp client used for the HTTP call
     */
    public OllamaProvider(String url, OkHttpClient client) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    @Override
    public String providerName() {
        return "ollama";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(RequestBody.create(buildBody(request), JSON))
                .build();

        Response response;
        try {
            response = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new IllegalStateException("Ollama request failed: " + e.getMessage(), e);
        }
        if (!response.isSuccessful()) {
            response.close();
            throw new IllegalStateException("Ollama returned HTTP " + response.code());
        }

        ResponseBody body = response.body();
        if (body == null) {
            response.close();
            throw new IllegalStateException("Ollama returned an empty response body");
        }

        return StreamSupport.stream(new ChunkSpliterator(body), false)
                .onClose(() -> {
                    try {
                        body.close();
                    } finally {
                        response.close();
                    }
                });
    }

    /* ==========================================================\
       Deterministic, unit-testable parsing helpers
       ========================================================== */

        /**
     * Build the JSON request body that mirrors the legacy {@code OllamaClient}
     * shape ({@code model}, {@code prompt}, {@code stream:true},
     * {@code options}) so the canonical provider is wire-compatible and the
     * legacy client can later delegate without behaviour drift.
     */
    String buildBody(LlmRequest request) {
        try {
            Map<String, Object> options = new LinkedHashMap<>();
            if (request.temperature() != null) {
                options.put("temperature", request.temperature());
            }
            if (request.maxTokens() != null) {
                options.put("num_predict", request.maxTokens());
            }
            if (request.options() != null) {
                for (Map.Entry<String, Object> e : request.options().entrySet()) {
                    if (!"temperature".equals(e.getKey()) && !"maxTokens".equals(e.getKey())) {
                        options.put(e.getKey(), e.getValue());
                    }
                }
            }

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("model", request.model());
            root.put("prompt", request.prompt());
            root.put("stream", Boolean.TRUE);
            if (!options.isEmpty()) {
                root.put("options", options);
            }
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Ollama request body", e);
        }
    }

    /**
     * Extract the {@code "response"} text fragment from a single newline-delimited
     * JSON object emitted by Ollama's streaming generate endpoint.
     *
     * @param ndjsonLine one JSON object line
     * @return the response fragment, or {@code null} if the line carries no
     *         {@code response} field (e.g. the terminating {@code done:true} line)
     */
    static String extractResponseToken(String ndjsonLine) {
        if (ndjsonLine == null || ndjsonLine.isEmpty()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(ndjsonLine);
            if (node.hasNonNull("response")) {
                return node.get("response").asText();
            }
        } catch (Exception ignored) {
            // Malformed lines are treated as non-fragments.
        }
        return null;
    }

    /**
     * @param ndjsonLine one JSON object line
     * @return {@code true} if the line marks completion ({@code "done":true})
     */
    static boolean isDone(String ndjsonLine) {
        if (ndjsonLine == null || ndjsonLine.isEmpty()) {
            return false;
        }
        try {
            JsonNode node = MAPPER.readTree(ndjsonLine);
            return node.hasNonNull("done") && node.get("done").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Splits the streaming response body into token fragments. Reads lazily:
     * each call to {@link #tryAdvance} pulls one NDJSON line, parses it through
     * {@link #extractResponseToken}, and stops at the {@code done:true} line.
     */
    private static final class ChunkSpliterator extends Spliterators.AbstractSpliterator<String> {

        private final BufferedSource source;
        private boolean finished = false;

        ChunkSpliterator(ResponseBody body) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            this.source = body.source();
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            if (finished) {
                return false;
            }
            try {
                String line = source.readUtf8Line();
                if (line == null) {
                    finished = true;
                    return false;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    return tryAdvance(action);
                }
                if (isDone(trimmed)) {
                    String token = extractResponseToken(trimmed);
                    finished = true;
                    if (token != null) {
                        action.accept(token);
                    }
                    return false;
                }
                String token = extractResponseToken(trimmed);
                if (token != null) {
                    action.accept(token);
                }
                return !finished;
            } catch (IOException e) {
                finished = true;
                throw new IllegalStateException("Failed reading Ollama stream", e);
            }
        }
    }
}
