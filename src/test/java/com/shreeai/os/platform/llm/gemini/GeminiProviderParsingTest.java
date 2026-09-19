package com.shreeai.os.platform.llm.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Network-free parsing tests for {@link GeminiProvider}. */
class GeminiProviderParsingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void providerName() {
        assertEquals("gemini", new GeminiProvider("key-test").providerName());
    }

    @Test
    void streamUrlTargetsModelAndKey() {
        GeminiProvider provider = new GeminiProvider("key-123");
        String url = provider.streamUrl("gemini-3.6-flash");

        assertTrue(url.startsWith(GeminiProvider.DEFAULT_BASE_URL + "gemini-3.6-flash"));
        assertTrue(url.contains(":streamGenerateContent?alt=sse"));
        assertTrue(url.endsWith("key=key-123"));
    }

    @Test
    void streamUrlFallsBackToDefaultModel() {
        GeminiProvider provider = new GeminiProvider("key-123");
        assertTrue(provider.streamUrl("default").contains("gemini-3.6-flash"));
    }

    @Test
    void resolveModelReplacesLegacyGemini20Flash() {
        assertEquals("gemini-3.6-flash", GeminiProvider.resolveModel("gemini-2.0-flash"));
        assertEquals("gemini-3.6-flash", GeminiProvider.resolveModel("models/gemini-2.0-flash"));
        assertEquals("gemini-3.6-flash", GeminiProvider.resolveModel(null));
        assertEquals("gemini-3.6-flash", GeminiProvider.resolveModel("default"));
        assertEquals("gemini-3.6-flash", GeminiProvider.resolveModel("shree-default"));

        GeminiProvider provider = new GeminiProvider("key-123");
        assertTrue(provider.streamUrl("gemini-2.0-flash").contains("gemini-3.6-flash"));
        assertTrue(provider.streamUrl("models/gemini-2.0-flash").contains("gemini-3.6-flash"));
    }

    @Test
    void buildBodyCarriesPromptAndGenerationConfig() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("gemini-2.0-flash")
                .prompt("hello")
                .temperature(0.5)
                .maxTokens(4096)
                .build();

        JsonNode root = MAPPER.readTree(GeminiProvider.buildBody(request));

        assertEquals("hello", root.get("contents").get(0).get("parts").get(0).get("text").asText());
        assertEquals(0.5, root.get("generationConfig").get("temperature").asDouble());
        assertEquals(4096, root.get("generationConfig").get("maxOutputTokens").asInt());
    }

    @Test
    void buildBodyClampsSmallMaxTokensToMinimum2048() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("hello")
                .maxTokens(64)
                .build();

        JsonNode root = MAPPER.readTree(GeminiProvider.buildBody(request));

        assertEquals(2048, root.get("generationConfig").get("maxOutputTokens").asInt());
        assertEquals(0.4, root.get("generationConfig").get("temperature").asDouble());
    }

    @Test
    void buildBodyDefaultsMaxOutputTokensWhenNull() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("hello")
                .build();

        JsonNode root = MAPPER.readTree(GeminiProvider.buildBody(request));

        assertEquals(2048, root.get("generationConfig").get("maxOutputTokens").asInt());
        assertEquals(0.4, root.get("generationConfig").get("temperature").asDouble());
    }

    @Test
    void extractTextReadsCandidateParts() {
        String line = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hi\"},{\"text\":\" there\"}]}}]}";
        assertEquals("Hi there", GeminiProvider.extractText(line));
    }

    @Test
    void extractTextJoinsMultipleCandidatePartsAcrossChunks() {
        // Multi-line SSE with multiple chunks
        String multiChunkSse = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Part 1 \"}]}}]}\n\n"
                + "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Part 2 \"},{\"text\":\"Part 3\"}]}}]}";
        assertEquals("Part 1 Part 2 Part 3", GeminiProvider.extractTextFromPayload(multiChunkSse));

        // JSON array of chunks
        String jsonArray = "[{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello \"}]}}]},"
                + "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"world!\"}]}}]}]";
        assertEquals("Hello world!", GeminiProvider.extractTextFromPayload(jsonArray));
    }

    @Test
    void extractTextIgnoresNonContentLines() {
        assertNull(GeminiProvider.extractText(": keep-alive"));
        assertNull(GeminiProvider.extractText("data: {\"candidates\":[]}"));
        assertNull(GeminiProvider.extractText(null));
    }

    @Test
    void cleanApiKeyStripsWhitespaceAndQuotes() {
        assertEquals("my-key", GeminiProvider.cleanApiKey("  my-key  "));
        assertEquals("my-key", GeminiProvider.cleanApiKey("\"my-key\""));
        assertEquals("my-key", GeminiProvider.cleanApiKey("'my-key'"));
        assertEquals("my-key", GeminiProvider.cleanApiKey("  \"my-key\"  "));
        assertEquals("", GeminiProvider.cleanApiKey(null));
    }

    @Test
    void buildHttpRequestHasQueryKeyAndNoAuthorizationHeader() {
        GeminiProvider provider = new GeminiProvider("  \"AIzaSyTest123\"  ");
        LlmRequest request = LlmRequest.builder()
                .model("gemini-2.0-flash")
                .prompt("ping")
                .build();

        okhttp3.Request httpRequest = provider.buildHttpRequest(request);

        // Verify URL contains the cleaned key
        String urlString = httpRequest.url().toString();
        assertTrue(urlString.contains("key=AIzaSyTest123"), "URL must contain ?key= parameter with cleaned key: " + urlString);
        assertTrue(urlString.contains("/models/gemini-3.6-flash:generateContent"), "URL must contain correct model and action: " + urlString);

        // Verify headers
        assertEquals("AIzaSyTest123", httpRequest.header("x-goog-api-key"), "x-goog-api-key header must match cleaned key");
        assertNull(httpRequest.header("Authorization"), "Authorization header must NOT be present on Gemini request");
    }

    @Test
    void buildHttpRequestHandlesBaseUrlWithoutTrailingSlash() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        GeminiProvider provider = new GeminiProvider("https://generativelanguage.googleapis.com/v1beta/models", "key-xyz", client);
        LlmRequest request = LlmRequest.builder()
                .model("gemini-1.5-pro")
                .prompt("test")
                .build();

        okhttp3.Request httpRequest = provider.buildHttpRequest(request);
        String urlString = httpRequest.url().toString();
        assertTrue(urlString.contains("/models/gemini-1.5-pro:generateContent?key=key-xyz"),
                "URL must format slash cleanly when baseUrl has no trailing slash: " + urlString);
    }

    @Test
    void retriesTransientHttp503ThenSucceeds() {
        AtomicInteger callCount = new AtomicInteger(0);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    int count = callCount.incrementAndGet();
                    if (count < 3) {
                        return new Response.Builder()
                                .request(chain.request())
                                .protocol(Protocol.HTTP_1_1)
                                .code(503)
                                .message("Service Unavailable")
                                .body(ResponseBody.create("{\"error\": \"high demand\"}", MediaType.parse("application/json")))
                                .build();
                    }
                    return new Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(ResponseBody.create("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"success after retry\"}]}}]}", MediaType.parse("application/json")))
                            .build();
                })
                .build();

        GeminiProvider provider = new GeminiProvider(
                "https://generativelanguage.googleapis.com/v1beta/models",
                "key-test",
                client,
                2,
                10L
        );

        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("hello")
                .build();

        Stream<String> responseStream = provider.stream(request);
        List<String> results = responseStream.toList();
        assertEquals(1, results.size());
        assertEquals("success after retry", results.get(0));
        assertEquals(3, callCount.get());
    }

    @Test
    void exhaustsRetriesOnHttp429AndThrowsForRouterFallback() {
        AtomicInteger callCount = new AtomicInteger(0);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    callCount.incrementAndGet();
                    return new Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(429)
                            .message("Too Many Requests")
                            .body(ResponseBody.create("{\"error\": \"quota exceeded\"}", MediaType.parse("application/json")))
                            .build();
                })
                .build();

        GeminiProvider provider = new GeminiProvider(
                "https://generativelanguage.googleapis.com/v1beta/models",
                "key-test",
                client,
                2,
                10L
        );

        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("hello")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> provider.stream(request));
        assertTrue(ex.getMessage().contains("HTTP 429"));
        assertEquals(3, callCount.get());
    }
}