package com.shreeai.os.platform.llm.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.LlmResponse;

import java.io.IOException;
import java.time.Duration;
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
 * Streaming-first {@link LlmProvider} for local Ollama instances.
 *
 * <p>Ollama streams line-delimited JSON objects over {@code /api/generate} with
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

    private static OkHttpClient createDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofSeconds(60))
                .build();
    }

    /** Default constructor: localhost Ollama + configured OkHttp client with timeouts. */
    public OllamaProvider() {
        this(DEFAULT_URL, createDefaultClient());
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
            if (e instanceof java.io.InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
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

        return StreamSupport.stream(new ChunkSpliterator(response, body), false)
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

        private final Response response;
        private final ResponseBody body;
        private final BufferedSource source;
        private boolean finished = false;

        ChunkSpliterator(Response response, ResponseBody body) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            this.response = response;
            this.body = body;
            this.source = body.source();
        }

        private void closeQuietly() {
            try {
                if (body != null) {
                    body.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            Objects.requireNonNull(action, "action must not be null");
            if (finished) {
                return false;
            }
            try {
                String line;
                while ((line = source.readUtf8Line()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    if (isDone(trimmed)) {
                        finished = true;
                        closeQuietly();
                        String token = extractResponseToken(trimmed);
                        if (token != null) {
                            action.accept(token);
                            return true;
                        }
                        return false;
                    }
                    String token = extractResponseToken(trimmed);
                    if (token != null) {
                        action.accept(token);
                        return true;
                    }
                }
                finished = true;
                closeQuietly();
                return false;
            } catch (IOException e) {
                finished = true;
                closeQuietly();
                if (e instanceof java.io.InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                throw new IllegalStateException("Failed reading Ollama stream", e);
            }
        }
    }
}
