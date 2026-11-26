package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live end-to-end smoke test verifying real LLM output quality,
 * Hybrid RAG grounding, streaming, and episodic memory.
 *
 * Run with local Ollama:
 *   .\mvnw.cmd test -Dtest=LiveEndToEndSmokeTest#testLiveEndToEndWithOllama -Dshree.llm.chain=ollama,in-memory
 */
@Tag("live")
class LiveEndToEndSmokeTest {

    @Test
    @EnabledIfSystemProperty(named = "shree.llm.chain", matches = ".*ollama.*")
    @DisplayName("Live E2E: General Chat, Streaming, Hybrid RAG, and Memory with Local Ollama")
    void testLiveEndToEndWithOllama() throws Exception {
        try (ShreeAI shree = ShreeAI.builder().apiKey("local").build()) {
            runAllEndToEndAssertions(shree);
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    @EnabledIfSystemProperty(named = "shree.llm.chain", matches = ".*openai.*")
    @DisplayName("Live E2E: General Chat, Streaming, Hybrid RAG, and Memory with OpenAI")
    void testLiveEndToEndWithOpenAi() throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        try (ShreeAI shree = ShreeAI.builder().apiKey(apiKey).build()) {
            runAllEndToEndAssertions(shree);
        }
    }

    private static void runAllEndToEndAssertions(ShreeAI shree) throws Exception {
        // 1. General Chat (no KB needed -> Mode B General Assistance)
        SDKResponse general = shree.chat("What are Java 21 virtual threads in 2 concise sentences?");
        System.out.println("=== [LIVE 1: GENERAL CHAT] ===\n" + general.answer());
        assertNotNull(general.answer());
        assertFalse(general.answer().contains("Insufficient Evidence"),
                "Real LLM should answer general Java question instead of Insufficient Evidence");
        assertFalse(general.answer().contains(" echoes: "));

        // 2. Grounded Hybrid RAG (ingest -> query)
        shree.knowledge().ingest(
                "Shree RRF Architecture",
                "Shree AI OS combines HNSW vector similarity and PostgreSQL GIN full-text search using Reciprocal Rank Fusion (RRF) with k=60."
        );
        SDKResponse rag = shree.chat("How does Shree AI OS combine vector similarity and full-text search?");
        System.out.println("=== [LIVE 2: HYBRID RAG] ===\n" + rag.answer());
        assertNotNull(rag.answer());
        assertTrue(rag.answer().toLowerCase().contains("reciprocal rank fusion")
                || rag.answer().toLowerCase().contains("rrf")
                || rag.answer().toLowerCase().contains("hnsw"));

        // 3. Episodic Memory Store + Search
        shree.memory().store("Database Preference", "Production cluster uses PostgreSQL 16 with pgvector.");
        SDKResponse mem = shree.memory().search("PostgreSQL pgvector");
        System.out.println("=== [LIVE 3: MEMORY SEARCH] ===\n" + mem.answer());
        assertNotNull(mem.answer());

        // 4. Streaming Chat
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> streamed = new AtomicReference<>();
        AtomicReference<Throwable> streamErr = new AtomicReference<>();
        shree.chatStream("Explain fail-closed security in one short sentence.", new StreamingListener() {
            @Override public void onStart() {}
            @Override public void onToken(String token) {}
            @Override public void onComplete(String fullText) {
                streamed.set(fullText);
                latch.countDown();
            }
            @Override public void onError(Throwable error) {
                streamErr.set(error);
                latch.countDown();
            }
        });
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Streaming timed out");
        assertNull(streamErr.get(), "Streaming failed: " + streamErr.get());
        System.out.println("=== [LIVE 4: STREAMING CHAT] ===\n" + streamed.get());
        assertNotNull(streamed.get());
        assertFalse(streamed.get().isBlank());
        assertFalse(streamed.get().contains(" echoes: "), "Expected real LLM stream, got in-memory echo");
    }
}
