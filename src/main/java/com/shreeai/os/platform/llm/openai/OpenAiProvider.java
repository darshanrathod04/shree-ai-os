package com.shreeai.os.platform.llm.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
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
 * OkHttp-backed, streaming-first {@link LlmProvider} for OpenAI-compatible
 * chat-completion endpoints (GPT family and any OpenAI-compatible gateway).
 *
 * <p>Streaming: OpenAI emits server-sent events ({@code data: {...}} lines).
 * Each event carries a {@code choices[0].delta.content} fragment; the stream
 * terminates with a {@code data: [DONE]} sentinel. Fragments are exposed as a
 * closeable {@link Stream} whose {@code onClose} releases the HTTP response.</p>
 *
 * <p>Same constitutional pattern as {@code OllamaProvider}: shared OkHttp +
 * Jackson stack, no legacy dependencies, network-free unit tests for parsing.</p>
 */
public final class OpenAiProvider implements LlmProvider {

    static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final String DONE_SENTINEL = "[DONE]";

    private final OkHttpClient client;
    private final String url;
    private final String apiKey;

    /** Creates a provider against the public OpenAI endpoint with a fresh OkHttp client. */
    public OpenAiProvider(String apiKey) {
        this(DEFAULT_URL, apiKey, new OkHttpClient());
    }

    /**
     * @param url    full chat-completion endpoint URL
     * @param apiKey bearer API key (must not be null or blank)
     * @param client the OkHttp client used for the HTTP call
     */
    public OpenAiProvider(String url, String apiKey, OkHttpClient client) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    @Override
    public String providerName() {
        return "openai";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Request httpRequest = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(buildBody(request), JSON))
                .build();

        Response response;
        try {
            response = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new IllegalStateException("OpenAI request failed: " + e.getMessage(), e);
        }
        if (!response.isSuccessful()) {
            response.close();
            throw new IllegalStateException("OpenAI request failed with HTTP " + response.code());
        }

        ResponseBody body = response.body();
        if (body == null) {
            response.close();
            throw new IllegalStateException("OpenAI request returned an empty body");
        }

        ChunkSpliterator spliterator = new ChunkSpliterator(body);
        Stream<String> stream = StreamSupport.stream(spliterator, false);
        return stream.onClose(() -> {
            try {
                response.close();
            } catch (Exception ignored) {
                // release is best-effort
            }
        });
    }

    /**
     * Serialises an {@link LlmRequest} into OpenAI's chat-completion request body.
     *
     * @param request the request
     * @return the JSON body (never null)
     */
    static String buildBody(LlmRequest request) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("model", request.model());
            root.put("stream", true);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", request.prompt());
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);
            root.put("messages", messages);

            Map<String, Object> options = new LinkedHashMap<>();
            if (request.temperature() != null) {
                options.put("temperature", request.temperature());
            }
            if (request.maxTokens() != null) {
                options.put("max_tokens", request.maxTokens());
            }
            for (Map.Entry<String, Object> entry : request.options().entrySet()) {
                options.put(entry.getKey(), entry.getValue());
            }
            if (!options.isEmpty()) {
                root.putAll(options);
            }

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise OpenAI request body", e);
        }
    }

    /**
     * Extracts the {@code choices[0].delta.content} fragment from a single SSE
     * {@code data:} line.
     *
     * @param sseLine one SSE line (e.g. {@code data: {...}} or {@code data: [DONE]})
     * @return the content fragment, or {@code null} if the line carries no delta
     *         content (comment lines, keep-alives, the DONE sentinel, etc.)
     */
    static String extractDelta(String sseLine) {
        String payload = stripDataPrefix(sseLine);
        if (payload == null || payload.equals(DONE_SENTINEL)) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(payload);
            JsonNode choices = node.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode content = choices.get(0).path("delta").path("content");
                if (!content.isMissingNode() && !content.isNull()) {
                    return content.asText();
                }
            }
        } catch (Exception ignored) {
            // Malformed lines are treated as non-fragments.
        }
        return null;
    }

    /**
     * @param sseLine one SSE line
     * @return {@code true} if the line is the {@code data: [DONE]} completion sentinel
     */
    static boolean isDoneLine(String sseLine) {
        String payload = stripDataPrefix(sseLine);
        return DONE_SENTINEL.equals(payload);
    }

    private static String stripDataPrefix(String sseLine) {
        if (sseLine == null) {
            return null;
        }
        String trimmed = sseLine.trim();
        if (trimmed.isEmpty() || trimmed.startsWith(":")) {
            return null; // comment / keep-alive
        }
        if (!trimmed.startsWith("data:")) {
            return null;
        }
        return trimmed.substring("data:".length()).trim();
    }

    /**
     * Reads OpenAI's SSE stream lazily: each {@link #tryAdvance} pulls one line,
     * parses it through {@link #extractDelta}, and stops at {@code [DONE]}.
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
                if (isDoneLine(trimmed)) {
                    finished = true;
                    return false;
                }
                String token = extractDelta(trimmed);
                if (token != null) {
                    action.accept(token);
                }
                return !finished;
            } catch (IOException e) {
                finished = true;
                throw new IllegalStateException("Failed reading OpenAI stream", e);
            }
        }
    }
}