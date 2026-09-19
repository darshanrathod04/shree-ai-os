package com.shreeai.os.platform.llm.gemini;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttp-backed LlmProvider for Google Gemini API.
 */
public final class GeminiProvider implements LlmProvider {

    static final String DEFAULT_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;

    public GeminiProvider(String apiKey) {
        this(DEFAULT_BASE_URL, apiKey, createDefaultClient());
    }

    public GeminiProvider(String baseUrl, String apiKey, OkHttpClient client) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        this.apiKey = cleanApiKey(Objects.requireNonNull(apiKey, "apiKey must not be null"));
        if (this.apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    static String cleanApiKey(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            if (trimmed.length() >= 2) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }

    private static OkHttpClient createDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public Stream<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String safeModel = resolveModel(request.model());
        String effectiveBaseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
        System.out.println(">>> GEMINI CALLING URL: " + effectiveBaseUrl + safeModel + ":generateContent?key=MASKED");

        Request httpRequest = buildHttpRequest(request);

        Response response;
        try {
            response = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            System.err.println(">>> GEMINI NETWORK ERROR: " + e.getMessage());
            throw new IllegalStateException("Gemini request failed: " + e.getMessage(), e);
        }

        if (!response.isSuccessful()) {
            String errorBody = "no body";
            try {
                if (response.body() != null) {
                    errorBody = response.body().string();
                }
            } catch (IOException ignored) {}

            System.err.println(">>> GEMINI HTTP ERROR CODE: " + response.code());
            System.err.println(">>> GEMINI RAW ERROR TEXT: " + errorBody);
            response.close();
            throw new IllegalStateException("Gemini request failed with HTTP " + response.code() + ": " + errorBody);
        }

        try {
            String rawResponse = response.body() != null ? response.body().string() : "";
            response.close();
            String extracted = extractTextFromPayload(rawResponse);
            return extracted != null ? Stream.of(extracted) : Stream.empty();
        } catch (IOException e) {
            response.close();
            throw new IllegalStateException("Failed reading Gemini response: " + e.getMessage(), e);
        }
    }

    Request buildHttpRequest(LlmRequest request) {
        String safeModel = resolveModel(request.model());
        String effectiveBaseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
        String url = effectiveBaseUrl + safeModel + ":generateContent?key=" + this.apiKey.trim();
        String jsonBody = buildBody(request);

        return new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", this.apiKey.trim())
                .removeHeader("Authorization")
                .post(RequestBody.create(jsonBody, JSON))
                .build();
    }

    /**
     * Preserved for backward-compatibility and GeminiProviderParsingTest.
     */
    String streamUrl(String model) {
        String safeModel = resolveModel(model);
        String effectiveBaseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
        return effectiveBaseUrl + safeModel + ":streamGenerateContent?alt=sse&key=" + apiKey.trim();
    }

    private static String configuredDefaultModel() {
        String prop = System.getProperty("shree.llm.gemini.model");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        prop = System.getProperty("gemini.model");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        prop = System.getenv("SHREE_LLM_GEMINI_MODEL");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return "gemini-3.6-flash";
    }

    static String resolveModel(String model) {
        String defaultModel = configuredDefaultModel();
        if (model == null
                || model.isBlank()
                || "default".equalsIgnoreCase(model)) {
            return defaultModel;
        }
        String clean = model.trim();
        if (clean.startsWith("models/")) {
            clean = clean.substring("models/".length()).trim();
        }
        if (clean.equalsIgnoreCase("gemini-2.0-flash")
                || clean.toLowerCase(Locale.ROOT).startsWith("gemini-2.0-flash")
                || safeModelPrefix(clean)) {
            return defaultModel;
        }
        return clean;
    }

    private static boolean safeModelPrefix(String model) {
        return model.startsWith("shree-")
                || !model.startsWith("gemini");
    }

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
            if (!generationConfig.isEmpty()) {
                root.put("generationConfig", generationConfig);
            }

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Gemini request body", e);
        }
    }

    static String extractTextFromPayload(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(json);
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
        }
        return null;
    }

    /**
     * Extracts text from an SSE line (preserved for GeminiProviderParsingTest).
     */
    static String extractText(String sseLine) {
        String payload = stripDataPrefix(sseLine);
        if (payload == null) {
            return null;
        }
        return extractTextFromPayload(payload);
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
}