package com.shreeai.os.platform.llm.ollama;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import static com.shreeai.os.platform.llm.ollama.OllamaProvider.DEFAULT_URL;
import static com.shreeai.os.platform.llm.ollama.OllamaProvider.extractResponseToken;
import static com.shreeai.os.platform.llm.ollama.OllamaProvider.isDone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmRequest;

/**
 * Unit tests for the deterministic, network-free parsing logic of
 * {@link OllamaProvider}. No live Ollama server is required.
 *
 * <p>Hosted in the same package as {@link OllamaProvider} so the package-private
 * parsing/body helpers ({@code extractResponseToken}, {@code isDone},
 * {@code buildBody}) are exercised without widening the public API.</p>
 *
 * @since Sprint 6.2A-P1
 */
class OllamaProviderParsingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void providerName() {
        assertEquals("ollama", new OllamaProvider().providerName());
    }

    @Test
    void defaultUrlPointsAtGenerateEndpoint() {
        assertEquals("http://localhost:11434/api/generate", DEFAULT_URL);
    }

    @Test
    void extractResponseTokenReadsDelta() {
        String line = "{\"model\":\"phi3\",\"response\":\"hel\",\"done\":false}";
        assertEquals("hel", extractResponseToken(line));
    }

    @Test
    void extractResponseTokenReturnsNullForDoneOnlyLine() {
        String line = "{\"model\":\"phi3\",\"done\":true,\"total duration\":123}";
        assertNull(extractResponseToken(line));
        assertTrue(isDone(line));
    }

    @Test
    void isDoneFlagsTerminalLine() {
        assertTrue(isDone("{\"done\":true}"));
        assertFalse(isDone("{\"done\":false}"));
        assertFalse(isDone("{\"response\":\"partial\"}"));
    }

    @Test
    void extractResponseTokenIsRobustToMalformedInput() {
        assertNull(extractResponseToken("not json"));
        assertNull(extractResponseToken(""));
        assertNull(extractResponseToken(null));
        assertFalse(isDone("not json"));
        assertFalse(isDone(null));
    }

    @Test
    void buildBodyMirrorsLegacyShape() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("phi3")
                .prompt("explain quantum")
                .temperature(0.3)
                .maxTokens(64)
                .build();

        JsonNode root = MAPPER.readTree(new OllamaProvider().buildBody(request));

        assertEquals("phi3", root.get("model").asText());
        assertEquals("explain quantum", root.get("prompt").asText());
        assertTrue(root.get("stream").asBoolean());
        assertNotNull(root.get("options"));
        assertEquals(0.3, root.get("options").get("temperature").asDouble(), 0.0001);
        assertEquals(64, root.get("options").get("num_predict").asInt());
    }

    @Test
    void buildBodyOmitsOptionsWhenUnused() throws Exception {
        LlmRequest request = LlmRequest.builder().model("m").prompt("p").build();
        JsonNode root = MAPPER.readTree(new OllamaProvider().buildBody(request));

        assertFalse(root.has("options"));
        assertEquals("m", root.get("model").asText());
        assertEquals("p", root.get("prompt").asText());
        assertTrue(root.get("stream").asBoolean());
    }

    @Test
    void buildBodyForwardsCustomOptions() throws Exception {
        LlmRequest request = LlmRequest.builder()
                .model("m")
                .prompt("p")
                .option("top_k", 40)
                .build();
        JsonNode root = MAPPER.readTree(new OllamaProvider().buildBody(request));

        assertTrue(root.has("options"));
        assertEquals(40, root.get("options").get("top_k").asInt());
        // temperature / num_predict absent because not set.
        assertFalse(root.get("options").has("temperature"));
        assertFalse(root.get("options").has("num_predict"));
    }
}
