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
import okhttp3.ResponseBody;

/**
 * OkHttp-backed LlmProvider for Google Gemini API.
 */
public final class GeminiProvider implements LlmProvider {

    static final String DEFAULT_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    public static final int DEFAULT_MAX_RETRIES = 2;
    public static final long DEFAULT_RETRY_BACKOFF_MS = 1000L;

    public static final int MIN_MAX_OUTPUT_TOKENS = 2048;
    public static final int DEFAULT_MAX_OUTPUT_TOKENS = 2048;
    public static final double DEFAULT_TEMPERATURE = 0.4;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private final int maxRetries;
    private final long retryBackoffMs;

    public GeminiProvider(String apiKey) {
        this(DEFAULT_BASE_URL, apiKey, createDefaultClient(), DEFAULT_MAX_RETRIES, DEFAULT_RETRY_BACKOFF_MS);
    }

    public GeminiProvider(String baseUrl, String apiKey, OkHttpClient client) {
        this(baseUrl, apiKey, client, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_BACKOFF_MS);
    }

    public GeminiProvider(String baseUrl, String apiKey, OkHttpClient client, int maxRetries, long retryBackoffMs) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        this.apiKey = cleanApiKey(Objects.requireNonNull(apiKey, "apiKey must not be null"));
        if (this.apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.maxRetries = Math.max(0, maxRetries);
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
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

        int attempts = 0;
        while (true) {
            attempts++;
            Response response;
            try {
                response = client.newCall(httpRequest).execute();
            } catch (IOException e) {
                System.err.println(">>> GEMINI NETWORK ERROR (attempt " + attempts + "): " + e.getMessage());
                if (attempts <= maxRetries) {
                    sleepBackoff(retryBackoffMs);
                    continue;
                }
                throw new IllegalStateException("Gemini request failed: " + e.getMessage(), e);
            }

            int statusCode = response.code();
            if (response.isSuccessful()) {
                try {
                    String rawResponse = readFullResponseBody(response);
                    response.close();
                    String extracted = extractTextFromPayload(rawResponse);
                    return extracted != null ? Stream.of(extracted) : Stream.empty();
                } catch (IOException e) {
                    response.close();
                    throw new IllegalStateException("Failed reading Gemini response: " + e.getMessage(), e);
                }
            }

            String errorBody = "no body";
            try {
                errorBody = readFullResponseBody(response);
            } catch (IOException ignored) {}
            response.close();

            // Lightweight retry for HTTP 503 (Model High Demand / Unavailable) or HTTP 429 (Rate Limit)
            if ((statusCode == 503 || statusCode == 429) && attempts <= maxRetries) {
                System.out.println(">>> GEMINI RETRY: Received HTTP " + statusCode + " (High Demand/Rate Limit). Retrying attempt "
                        + attempts + "/" + maxRetries + " after " + retryBackoffMs + "ms backoff...");
                sleepBackoff(retryBackoffMs);
                continue;
            }

            System.err.println(">>> GEMINI HTTP ERROR CODE: " + statusCode + (attempts > 1 ? " after " + attempts + " attempts" : ""));
            System.err.println(">>> GEMINI RAW ERROR TEXT: " + errorBody);
            throw new IllegalStateException("Gemini request failed with HTTP " + statusCode + ": " + errorBody);
        }
    }

    private static void sleepBackoff(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request retry backoff interrupted", ie);
        }
    }

    private static String readFullResponseBody(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            return "";
        }
        try (java.io.Reader charReader = body.charStream();
             java.io.BufferedReader reader = new java.io.BufferedReader(charReader)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
                sb.append(buffer, 0, read);
            }
            return sb.toString();
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
            double temperature = request.temperature() != null ? request.temperature() : DEFAULT_TEMPERATURE;
            generationConfig.put("temperature", temperature);

            int maxOutputTokens = request.maxTokens() != null
                    ? Math.max(MIN_MAX_OUTPUT_TOKENS, request.maxTokens())
                    : DEFAULT_MAX_OUTPUT_TOKENS;
            generationConfig.put("maxOutputTokens", maxOutputTokens);

            root.put("generationConfig", generationConfig);

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Gemini request body", e);
        }
    }

    static String extractTextFromPayload(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        String cleanJson = json.trim();

        // Handle SSE stream or multi-chunk data: payload
        if (cleanJson.contains("data:")) {
            StringBuilder sseCombined = new StringBuilder();
            for (String line : cleanJson.split("\\r?\\n")) {
                String stripped = stripDataPrefix(line);
                if (stripped != null && !stripped.isBlank()) {
                    try {
                        JsonNode node = MAPPER.readTree(stripped);
                        extractAllTextFromNode(node, sseCombined);
                    } catch (Exception ignored) {}
                }
            }
            if (sseCombined.length() > 0) {
                return sseCombined.toString();
            }
        }

        // Standard JSON payload (single object, JSON array, or concatenated JSON chunks)
        StringBuilder combined = new StringBuilder();
        try (com.fasterxml.jackson.core.JsonParser parser = MAPPER.createParser(cleanJson)) {
            while (parser.nextToken() != null) {
                JsonNode node = MAPPER.readTree(parser);
                if (node != null) {
                    extractAllTextFromNode(node, combined);
                }
            }
        } catch (Exception ignored) {
        }

        if (combined.length() > 0) {
            return combined.toString();
        }

        return null;
    }

    private static void extractAllTextFromNode(JsonNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                extractAllTextFromNode(child, sb);
            }
            return;
        }

        JsonNode candidates = node.path("candidates");
        if (candidates.isArray()) {
            for (JsonNode candidate : candidates) {
                JsonNode parts = candidate.path("content").path("parts");
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        JsonNode textNode = part.path("text");
                        if (!textNode.isMissingNode() && !textNode.isNull()) {
                            sb.append(textNode.asText());
                        }
                    }
                }
            }
        }
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