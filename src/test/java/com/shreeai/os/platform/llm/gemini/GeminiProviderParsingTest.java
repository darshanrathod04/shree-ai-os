package com.shreeai.os.platform.llm.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        String url = provider.streamUrl("gemini-2.0-flash");

        assertTrue(url.startsWith(GeminiProvider.DEFAULT_BASE_URL + "gemini-2.0-flash"));
        assertTrue(url.contains(":streamGenerateContent?alt=sse"));
        assertTrue(url.endsWith("key=key-123"));
    }

    @Test
    void streamUrlFallsBackToDefaultModel() {
        GeminiProvider provider = new GeminiProvider("key-123");
        assertTrue(provider.streamUrl("default").contains("gemini-2.0-flash"));
    }

    @Test
    void buildBodyCarriesPromptAndGenerationConfig() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("gemini-2.0-flash")
                .prompt("hello")
                .temperature(0.5)
                .maxTokens(64)
                .build();

        JsonNode root = MAPPER.readTree(GeminiProvider.buildBody(request));

        assertEquals("hello", root.get("contents").get(0).get("parts").get(0).get("text").asText());
        assertEquals(0.5, root.get("generationConfig").get("temperature").asDouble());
        assertEquals(64, root.get("generationConfig").get("maxOutputTokens").asInt());
    }

    @Test
    void extractTextReadsCandidateParts() {
        String line = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hi\"},{\"text\":\" there\"}]}}]}";
        assertEquals("Hi there", GeminiProvider.extractText(line));
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
        assertTrue(urlString.contains("/models/gemini-2.0-flash:generateContent"), "URL must contain correct model and action: " + urlString);

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
}