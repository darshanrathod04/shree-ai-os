package com.shreeai.os.platform.llm.gemini;

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
 * OkHttp-backed, streaming-first {@link LlmProvider} for Google Gemini's
 * {@code streamGenerateContent} endpoint.
 *
 * <p>Streaming: Gemini (with {@code alt=sse}) emits server-sent events whose
 * payload carries the text at {@code candidates[0].content.parts[*].text}.
 * The stream ends at EOF (Gemini has no DONE sentinel). Fragments are exposed
 * as a closeable {@link Stream} whose {@code onClose} releases the response.</p>
 *
 * <p>Same constitutional pattern as {@code OllamaProvider} / {@code
 * OpenAiProvider}: shared OkHttp + Jackson stack, network-free unit tests.</p>
 */
public final class GeminiProvider implements LlmProvider {

    static final String DEFAULT_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;

    /** Creates a provider against the public Gemini endpoint with a fresh OkHttp client. */
    public GeminiProvider(String apiKey) {
        this(DEFAULT_BASE_URL, apiKey, new OkHttpClient());
    }

    /**
     * @param baseUrl base URL ending at {@code .../models/}
     * @param apiKey  Gemini API key (must not be null or blank)
     * @param client  the OkHttp client used for the HTTP call
     */
    public GeminiProvider(String baseUrl, String apiKey, OkHttpClient client) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Request httpRequest = new Request.Builder()
                .url(streamUrl(request.model()))
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(buildBody(request), JSON))
                .build();

        Response response;
        try {
            response = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new IllegalStateException("Gemini request failed: " + e.getMessage(), e);
        }
        if (!response.isSuccessful()) {
            response.close();
            throw new IllegalStateException("Gemini request failed with HTTP " + response.code());
        }

        ResponseBody body = response.body();
        if (body == null) {
            response.close();
            throw new IllegalStateException("Gemini request returned an empty body");
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
     * Builds the streaming endpoint URL for a model.
     *
     * @param model the Gemini model id (e.g. {@code gemini-2.0-flash})
     * @return the full streaming URL
     */
    String streamUrl(String model) {
        String safeModel = model == null || model.isBlank() || "default".equals(model)
                ? "gemini-2.0-flash"
                : model;
        return baseUrl + safeModel + ":streamGenerateContent?alt=sse&key=" + apiKey;
    }

    /**
     * Serialises an {@link LlmRequest} into Gemini's generate-content body.
     *
     * @param request the request
     * @return the JSON body (never null)
     */
    static String buildBody(LlmRequest request) {
        try {
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("text", request.prompt());
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("parts", parts);
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(content);

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("contents", contents);

            Map<String, Object> generationConfig = new LinkedHashMap<>();
            if (request.temperature() != null) {
                generationConfig.put("temperature", request.temperature());
            }
            if (request.maxTokens() != null) {
                generationConfig.put("maxOutputTokens", request.maxTokens());
            }
            for (Map.Entry<String, Object> entry : request.options().entrySet()) {
                generationConfig.put(entry.getKey(), entry.getValue());
            }
            if (!generationConfig.isEmpty()) {
                root.put("generationConfig", generationConfig);
            }

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Gemini request body", e);
        }
    }

    /**
     * Extracts the concatenated {@code candidates[0].content.parts[*].text}
     * from a single SSE {@code data:} line.
     *
     * @param sseLine one SSE line
     * @return the text fragment, or {@code null} if the line carries no text
     */
    static String extractText(String sseLine) {
        String payload = stripDataPrefix(sseLine);
        if (payload == null) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(payload);
            JsonNode candidates = node.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    StringBuilder text = new StringBuilder();
                    for (JsonNode part : parts) {
                        JsonNode value = part.path("text");
                        if (!value.isMissingNode() && !value.isNull()) {
                            text.append(value.asText());
                        }
                    }
                    return text.length() > 0 ? text.toString() : null;
                }
            }
        } catch (Exception ignored) {
            // Malformed lines are treated as non-fragments.
        }
        return null;
    }

    private static String stripDataPrefix(String sseLine) {
        if (sseLine == null) {
            return null;
        }
        String trimmed = sseLine.trim();
        if (trimmed.isEmpty() || trimmed.startsWith(":") || !trimmed.startsWith("data:")) {
            return null;
        }
        return trimmed.substring("data:".length()).trim();
    }

    /**
     * Reads Gemini's SSE stream lazily: each {@link #tryAdvance} pulls one line
     * and parses it through {@link #extractText}; the stream ends at EOF.
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
                String token = extractText(trimmed);
                if (token != null) {
                    action.accept(token);
                }
                return true;
            } catch (IOException e) {
                finished = true;
                throw new IllegalStateException("Failed reading Gemini stream", e);
            }
        }
    }
}