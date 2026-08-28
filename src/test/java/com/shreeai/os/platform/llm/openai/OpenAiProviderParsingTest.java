package com.shreeai.os.platform.llm.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Network-free parsing tests for {@link OpenAiProvider}. */
class OpenAiProviderParsingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void providerName() {
        assertEquals("openai", new OpenAiProvider("sk-test").providerName());
    }

    @Test
    void buildBodyCarriesModelPromptAndOptions() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("gpt-4o-mini")
                .prompt("hello")
                .temperature(0.2)
                .maxTokens(128)
                .build();

        JsonNode root = MAPPER.readTree(OpenAiProvider.buildBody(request));

        assertEquals("gpt-4o-mini", root.get("model").asText());
        assertTrue(root.get("stream").asBoolean());
        assertEquals("hello", root.get("messages").get(0).get("content").asText());
        assertEquals(0.2, root.get("temperature").asDouble());
        assertEquals(128, root.get("max_tokens").asInt());
    }

    @Test
    void extractDeltaReadsChoiceDeltaContent() {
        String line = "data: {\"choices\":[{\"delta\":{\"content\":\"Hel\"}}]}";
        assertEquals("Hel", OpenAiProvider.extractDelta(line));
    }

    @Test
    void extractDeltaIgnoresNonContentLines() {
        assertNull(OpenAiProvider.extractDelta(": keep-alive"));
        assertNull(OpenAiProvider.extractDelta("data: {\"choices\":[{\"finish_reason\":\"stop\"}]}"));
        assertNull(OpenAiProvider.extractDelta("not-data"));
        assertNull(OpenAiProvider.extractDelta(null));
    }

    @Test
    void doneSentinelDetection() {
        assertTrue(OpenAiProvider.isDoneLine("data: [DONE]"));
        assertFalse(OpenAiProvider.isDoneLine("data: {\"choices\":[]}"));
    }
}