package com.shreeai.os.platform.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.shreeai.os.platform.llm.inmemory.InMemoryLlmProvider;

/**
 * Unit tests for the deterministic {@link InMemoryLlmProvider}.
 *
 * @since Sprint 6.2A-P1
 */
class InMemoryLlmProviderTest {

    private final InMemoryLlmProvider provider = new InMemoryLlmProvider();

    @Test
    void providerName() {
        assertEquals("in-memory", provider.providerName());
    }

    @Test
    void completeReproducesDeterministicEcho() {
        LlmRequest request = LlmRequest.builder().prompt("hello world").build();
        LlmResponse response = provider.complete(request);

        assertEquals("default echoes: hello world", response.content());
        assertEquals("default", response.model());
        assertEquals("stop", response.finishReason());
        assertFalse(response.usage().isEmpty());
    }

    @Test
    void streamTokensReconstructEchoExactly() {
        LlmRequest request = LlmRequest.builder().model("phi3").prompt("hi").build();
        List<String> tokens = provider.stream(request).toList();

        // Tokens are space-delimited fragments; joining them reproduces the echo.
        String joined = String.join("", tokens);
        assertEquals("phi3 echoes: hi", joined);
        assertEquals("phi3 echoes: hi", provider.echo(request));
    }

    @Test
    void streamIsFiniteAndCloseable() {
        LlmRequest request = LlmRequest.builder().prompt("x").build();
        List<String> collected = provider.stream(request).toList();
        assertFalse(collected.isEmpty());
    }

    @Test
    void emptyPromptStillEchoesModel() {
        LlmRequest request = LlmRequest.builder().prompt("").build();
        LlmResponse response = provider.complete(request);
        assertEquals("default echoes: ", response.content());
    }
}
