package com.shreeai.os.platform.llm.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <b>AnthropicProvider</b>
 *
 * <p>Streaming {@link LlmProvider} implementation targeting Anthropic's
 * {@code /v1/messages} SSE endpoint ({@code content_block_delta} / {@code delta.text}).</p>
 */
public final class AnthropicProvider implements LlmProvider {

    static final String DEFAULT_URL = "https://api.anthropic.com/v1/messages";
    static final String ANTHROPIC_VERSION = "2023-06-01";
    static final String DEFAULT_MODEL = "claude-3-5-sonnet-latest";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String url;
    private final String apiKey;

    private static OkHttpClient createDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofSeconds(60))
                .build();
    }

    public AnthropicProvider(String apiKey) {
        this(DEFAULT_URL, apiKey, createDefaultClient());
    }

    public AnthropicProvider(String url, String apiKey, OkHttpClient client) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    @Override
    public String providerName() {
        return "anthropic";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Request httpRequest = new Request.Builder()
                .url(url)
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(buildBody(request), JSON))
                .build();

        Response response;
        try {
            response = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            if (e instanceof java.io.InterruptedIOException
                    && !(e instanceof java.net.SocketTimeoutException)) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Anthropic request failed: " + e.getMessage(), e);
        }
        if (!response.isSuccessful()) {
            response.close();
            throw new IllegalStateException("Anthropic request failed with HTTP " + response.code());
        }

        ResponseBody body = response.body();
        if (body == null) {
            response.close();
            throw new IllegalStateException("Anthropic request returned an empty body");
        }

        ChunkSpliterator spliterator = new ChunkSpliterator(response, body);
        Stream<String> stream = StreamSupport.stream(spliterator, false);
        return stream.onClose(() -> {
            try {
                response.close();
            } catch (Exception ignored) {
                // release is best-effort
            }
        });
    }

    static String buildBody(LlmRequest request) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("model", resolveModel(request.model()));
            root.put("max_tokens", request.maxTokens() != null ? request.maxTokens() : 2048);
            root.put("stream", true);
            if (request.temperature() != null) {
                root.put("temperature", request.temperature());
            }
            root.put("messages", List.of(Map.of("role", "user", "content", request.prompt())));
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Anthropic request body", e);
        }
    }

    static String resolveModel(String model) {
        if (model == null
                || model.isBlank()
                || "default".equalsIgnoreCase(model.trim())
                || model.trim().startsWith("shree-")
                || model.trim().startsWith("gemini")
                || model.trim().startsWith("gpt-")) {
            String prop = System.getProperty("shree.llm.anthropic.model");
            if (prop != null && !prop.isBlank()) {
                return prop.trim();
            }
            String env = System.getenv("SHREE_LLM_ANTHROPIC_MODEL");
            if (env != null && !env.isBlank()) {
                return env.trim();
            }
            return DEFAULT_MODEL;
        }
        return model.trim();
    }

    static String extractTokenFromEvent(String jsonLine) {
        if (jsonLine == null || jsonLine.isBlank()) {
            return "";
        }
        try {
            JsonNode root = MAPPER.readTree(jsonLine);
            if ("content_block_delta".equals(root.path("type").asText())) {
                JsonNode delta = root.path("delta");
                if ("text_delta".equals(delta.path("type").asText())) {
                    return delta.path("text").asText("");
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static final class ChunkSpliterator extends Spliterators.AbstractSpliterator<String> {
        private final Response response;
        private final BufferedSource source;
        private boolean finished = false;

        ChunkSpliterator(Response response, ResponseBody body) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            this.response = response;
            this.source = body.source();
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            if (finished) {
                return false;
            }
            try {
                while (true) {
                    String line = source.readUtf8Line();
                    if (line == null) {
                        finished = true;
                        closeQuietly();
                        return false;
                    }
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String payload = line.substring("data:".length()).trim();
                    if (payload.contains("\"message_stop\"")) {
                        finished = true;
                        closeQuietly();
                        return false;
                    }
                    String token = extractTokenFromEvent(payload);
                    if (!token.isEmpty()) {
                        action.accept(token);
                        return true;
                    }
                }
            } catch (IOException e) {
                finished = true;
                closeQuietly();
                throw new IllegalStateException("Anthropic stream read failed: " + e.getMessage(), e);
            }
        }

        private void closeQuietly() {
            try {
                response.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }
}
