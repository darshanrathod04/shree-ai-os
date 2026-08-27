package com.shreeai.os.platform.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the canonical {@link LlmRequest} value object.
 *
 * @since Sprint 6.2A-P1
 */
class LlmRequestTest {

    @Test
    void defaultsAreSensible() {
        LlmRequest r = LlmRequest.builder().build();
        assertEquals("default", r.model());
        assertEquals("", r.prompt());
        assertTrue(r.stream());
        assertEquals(Map.of(), r.options());
    }

    @Test
    void builderSetsAllFields() {
        LlmRequest r = LlmRequest.builder()
                .model("phi3")
                .prompt("hello")
                .temperature(0.2)
                .maxTokens(64)
                .stream(true)
                .option("top_k", 40)
                .build();
        assertEquals("phi3", r.model());
        assertEquals("hello", r.prompt());
        assertEquals(0.2, r.temperature());
        assertEquals(64, r.maxTokens());
        assertTrue(r.stream());
        assertEquals(40, r.options().get("top_k"));
    }

    @Test
    void builtInstanceIsImmutableAndBuilderIsReusable() {
        LlmRequest.Builder b = LlmRequest.builder().model("m1").prompt("p1");
        LlmRequest r1 = b.build();
        LlmRequest r2 = b.model("m2").build();

        // First instance unaffected by later builder mutation.
        assertEquals("m1", r1.model());
        assertEquals("m2", r2.model());
    }

    @Test
    void optionsMapIsUnmodifiable() {
        LlmRequest r = LlmRequest.builder().option("k", "v").build();
        assertThrows(UnsupportedOperationException.class, () -> r.options().put("x", "y"));
    }

    @Test
    void equalityIsContentBased() {
        LlmRequest a = LlmRequest.builder().model("m").prompt("p").build();
        LlmRequest b = LlmRequest.builder().model("m").prompt("p").build();
        LlmRequest c = LlmRequest.builder().model("m").prompt("other").build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void nullPromptBecomesEmptyString() {
        LlmRequest r = LlmRequest.builder().prompt(null).build();
        assertEquals("", r.prompt());
    }

    @Test
    void nullStreamBecomesTrue() {
        LlmRequest r = LlmRequest.builder().stream(null).build();
        assertTrue(r.stream());
    }
}
