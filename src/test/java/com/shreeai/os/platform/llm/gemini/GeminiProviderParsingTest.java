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
}