package com.shreeai.os.platform.verification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.LlmResponse;
import com.shreeai.os.platform.llm.gemini.GeminiProvider;
import com.shreeai.os.platform.llm.inmemory.InMemoryLlmProvider;
import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.ShreeClient;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>PlatformAdversarialChaosIntegrationTest</b>
 *
 * <p>Comprehensive Adversarial, Edge-Case, and Chaos Verification Suite for Shree AI OS.
 * Hardens production-readiness before distribution across five core dimensions:</p>
 *
 * <ol>
 *   <li><b>Query Edge-Cases & Fuzz Testing:</b> Special characters, noise, whitespace,
 *       Unicode/emojis, mixed domain / token collision, and extreme query lengths.</li>
 *   <li><b>Ingestion & Vector Deduplication Loop:</b> Repeated query stress in a single
 *       session; guarantees active knowledge nodes and citations do not unboundedly duplicate.</li>
 *   <li><b>Token Budget & Stream Completion Guard:</b> Validates Gemini token budgeting
 *       (&ge; 2048) and chunked response assembly until EOF without truncation.</li>
 *   <li><b>Failover & Resilience (Fault Injection):</b> Simulates transient HTTP 503 and
 *       HTTP 429 errors; asserts backoff retries and transparent delegation to in-memory LLM.</li>
 *   <li><b>Domain Isolation Sweep:</b> Validates that out-of-domain queries never force-inject
 *       mismatched Java, Spring, or Healthcare specifications.</li>
 * </ol>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlatformAdversarialChaosIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ShreeClient client;
    private ShreeAI ai;

    @BeforeEach
    void setUp() {
        ai = ShreeAI.builder().apiKey("local").build();
        client = ai.client();
        assertNotNull(client, "ShreeClient must initialize cleanly");
    }

    // =========================================================================
    // 1. QUERY EDGE-CASES & FUZZ TESTING
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("1.1 Fuzz Testing: Special Characters, Punctuation Noise, and Emojis")
    void testSpecialCharactersAndNoiseFuzzing() {
        // Noisy punctuation queries
        List<String> noisyQueries = List.of(
                "what is java???",
                "build app... #!",
                "what is spring boot?!?!?!",
                "java???...!!!@@@###$$$%%%"
        );

        for (String query : noisyQueries) {
            assertDoesNotThrow(() -> {
                SDKResponse response = client.chat(query);
                assertNotNull(response, "Response must not be null for query: " + query);
                assertNotNull(response.answer(), "Answer must not be null for query: " + query);
                assertFalse(response.answer().isBlank(), "Answer must not be blank for query: " + query);
            }, "Query with punctuation noise must not throw: " + query);
        }

        // Multilingual & Unicode / Emoji inputs
        List<String> unicodeQueries = List.of(
                "¿Cómo aprender Java? 🚀 💻 汉字 ñ",
                "Spring Boot ☕ \u00E9\u00E8\u00E0 \u4F60\u597D",
                "Artificial Intelligence 🤖 Vector embeddings \uD83E\uDDE0"
        );

        for (String query : unicodeQueries) {
            assertDoesNotThrow(() -> {
                SDKResponse response = client.chat(query);
                assertNotNull(response, "Response must not be null for unicode query: " + query);
                assertNotNull(response.answer(), "Answer must not be null for unicode query: " + query);
            }, "Unicode / Emoji query must execute without error: " + query);
        }
    }

    @Test
    @Order(2)
    @DisplayName("1.2 Edge-Cases: Whitespace and Blank Inputs Graceful Handling")
    void testWhitespaceAndBlankInputsHandling() {
        // 1. Verify QueryNormalizer handles blank/whitespace safely without throwing
        assertEquals("", QueryNormalizer.normalize("   "));
        assertEquals("", QueryNormalizer.normalize("\n\t"));
        assertEquals("", QueryNormalizer.normalize(null));
        assertEquals("", QueryNormalizer.stripStopWords("   "));
        assertEquals("", QueryNormalizer.stripStopWords(null));

        // 2. Verify ShreeClient rejects blank queries with ValidationException (never NPE or crash)
        List<String> blankInputs = List.of("   ", "\n\t", "\r\n   \t");
        for (String blank : blankInputs) {
            ValidationException ex = assertThrows(ValidationException.class, () -> client.chat(blank),
                    "Blank input must throw ValidationException, not NPE/crash");
            assertTrue(ex.getMessage().contains("must not be null or blank"),
                    "Expected validation error message, got: " + ex.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("1.3 Mixed Domain & Token Collision: JavaScript vs Java and Cross-Domain Queries")
    void testMixedDomainAndTokenCollision() {
        // Query A: Pure JavaScript query with space "java script" - must NOT dump Java Platform docs
        SDKResponse jsResponse = client.chat("can you help me to learn java script");
        assertNotNull(jsResponse);
        assertNotNull(jsResponse.answer());
        String jsAnswer = jsResponse.answer().toLowerCase();
        assertFalse(jsAnswer.contains("jvm execution"),
                "JavaScript query must not dump Java JVM architecture. Got: " + jsResponse.answer());
        assertFalse(jsAnswer.contains("java virtual machine"),
                "JavaScript query must not dump Java Virtual Machine architecture. Got: " + jsResponse.answer());

        // Query B: Java vs JavaScript comparison - tokenizer and pipeline must handle both cleanly
        SDKResponse comparisonResponse = client.chat("Java vs JavaScript comparison");
        assertNotNull(comparisonResponse);
        assertNotNull(comparisonResponse.answer());
        assertFalse(comparisonResponse.answer().isBlank());

        // Query C: Python in Java JVM - cross-domain query must execute without collision crash
        SDKResponse crossDomainResponse = client.chat("Python in Java JVM");
        assertNotNull(crossDomainResponse);
        assertNotNull(crossDomainResponse.answer());
        assertFalse(crossDomainResponse.answer().isBlank());
    }

    @Test
    @Order(4)
    @DisplayName("1.4 Length Extremes: Very Short Inputs and Very Long Prompts (>1,500 chars)")
    void testLengthExtremes() {
        // Very short inputs: single characters, punctuation
        List<String> shortInputs = List.of("a", "?", "x", "!");
        for (String shortQuery : shortInputs) {
            assertDoesNotThrow(() -> {
                SDKResponse response = client.chat(shortQuery);
                assertNotNull(response, "Response must not be null for short query: " + shortQuery);
                assertNotNull(response.answer(), "Answer must not be null for short query: " + shortQuery);
            }, "Single-character input must execute safely without crash: " + shortQuery);
        }

        // Very long prompt (>1,500 characters)
        StringBuilder longPrompt = new StringBuilder();
        longPrompt.append("We are designing a mission-critical distributed cognitive operating system platform. ");
        longPrompt.append("The platform requires multi-tier caching, distributed consensus via Raft or Paxos, ");
        longPrompt.append("and deterministic transaction serialization across heterogeneous relational databases. ");
        while (longPrompt.length() < 2000) {
            longPrompt.append("Furthermore, the execution pipeline coordinates autonomous kernel agents across ")
                    .append("memory ingestion, vector grounding, reflection metrics, and LLM synthesis under high concurrency. ");
        }

        assertTrue(longPrompt.length() > 1500, "Long prompt must exceed 1,500 characters");

        assertDoesNotThrow(() -> {
            SDKResponse response = client.chat(longPrompt.toString());
            assertNotNull(response, "Response must not be null for long prompt");
            assertNotNull(response.answer(), "Answer must not be null for long prompt");
            assertFalse(response.answer().isBlank(), "Answer must not be blank for long prompt");
        }, "Multi-paragraph prompt >1,500 chars must execute cleanly without buffer overflow or crash");
    }

    // =========================================================================
    // 2. INGESTION & VECTOR DEDUPLICATION LOOP
    // =========================================================================

    @Test
    @Order(5)
    @DisplayName("2. Ingestion & Vector Deduplication Loop: 5 Sequential Queries in Same Session")
    void testIngestionAndVectorDeduplicationLoop() {
        final String query = "what is java .";
        final int iterations = 5;

        for (int i = 1; i <= iterations; i++) {
            SDKResponse response = client.chat(query);
            assertNotNull(response, "Response at iteration " + i + " must not be null");
            String answer = response.answer();
            assertNotNull(answer, "Answer at iteration " + i + " must not be null");
            assertFalse(answer.isBlank(), "Answer at iteration " + i + " must not be blank");

            // Verify citation formatting: no repeating duplicate indices like [1], [1]
            Pattern citationPattern = Pattern.compile("\\[(\\d+)\\]");
            Matcher matcher = citationPattern.matcher(answer);
            List<Integer> citationIndices = new ArrayList<>();
            while (matcher.find()) {
                citationIndices.add(Integer.parseInt(matcher.group(1)));
            }

            // Citations count must remain bounded (<= 5 distinct items)
            Set<Integer> distinctIndices = new HashSet<>(citationIndices);
            assertTrue(distinctIndices.size() <= 5,
                    "Distinct citations must not exceed 5 items. Got: " + distinctIndices.size());

            // In structured payload, verify evidence items and citations list
            Map<String, Object> payload = response.structuredPayload();
            if (payload != null && payload.get("citations") instanceof List<?> citationsList) {
                Set<?> distinctCitations = new HashSet<>(citationsList);
                assertEquals(citationsList.size(), distinctCitations.size(),
                        "Structured citations must not contain duplicate entries at iteration " + i);
                assertTrue(distinctCitations.size() <= 5,
                        "Structured citations count must be <= 5. Got: " + distinctCitations.size());
            }
        }
    }

    // =========================================================================
    // 3. TOKEN BUDGET & STREAM COMPLETION GUARD
    // =========================================================================

    @Test
    @Order(6)
    @DisplayName("3.1 Token Budget Guard: GeminiProvider Enforces maxOutputTokens >= 2048")
    void testTokenBudgetGuard() throws Exception {
        // Case A: Request without maxTokens -> defaults to 2048
        LlmRequest requestDefault = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("test prompt")
                .build();
        JsonNode jsonDefault = MAPPER.readTree(GeminiProvider.buildBody(requestDefault));
        assertNotNull(jsonDefault.get("generationConfig"), "generationConfig must be present");
        assertEquals(2048, jsonDefault.get("generationConfig").get("maxOutputTokens").asInt(),
                "Default maxOutputTokens must be 2048");

        // Case B: Request with small maxTokens (e.g. 64, 128) -> clamped to at least 2048
        LlmRequest requestSmall = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("test prompt")
                .maxTokens(64)
                .build();
        JsonNode jsonSmall = MAPPER.readTree(GeminiProvider.buildBody(requestSmall));
        assertEquals(2048, jsonSmall.get("generationConfig").get("maxOutputTokens").asInt(),
                "Small maxTokens must be clamped to MIN_MAX_OUTPUT_TOKENS (2048)");

        // Case C: Request with large maxTokens (e.g. 4096) -> preserved
        LlmRequest requestLarge = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("test prompt")
                .maxTokens(4096)
                .build();
        JsonNode jsonLarge = MAPPER.readTree(GeminiProvider.buildBody(requestLarge));
        assertEquals(4096, jsonLarge.get("generationConfig").get("maxOutputTokens").asInt(),
                "Large maxTokens (4096) must be preserved");
    }

    @Test
    @Order(7)
    @DisplayName("3.2 Stream Completion Guard: Multi-Chunk SSE and Array Payloads Joined to EOF")
    void testStreamCompletionGuard() {
        // Multi-chunk SSE stream payload
        String ssePayload = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Part 1: In-process runtime. \"}]}}]}\n\n"
                + "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Part 2: Deterministic reasoning. \"}]}}]}\n\n"
                + "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Part 3: Verified citations.\"}]}}]}";

        String extractedSse = GeminiProvider.extractTextFromPayload(ssePayload);
        assertNotNull(extractedSse);
        assertEquals("Part 1: In-process runtime. Part 2: Deterministic reasoning. Part 3: Verified citations.",
                extractedSse, "All SSE stream chunks must be joined to EOF without premature cutoff");

        // JSON Array payload with multiple chunk parts
        String jsonArrayPayload = "["
                + "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Alpha \"}]}}]},"
                + "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Beta \"},{\"text\":\"Gamma \"}]}}]},"
                + "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Delta.\"}]}}]}"
                + "]";

        String extractedArray = GeminiProvider.extractTextFromPayload(jsonArrayPayload);
        assertNotNull(extractedArray);
        assertEquals("Alpha Beta Gamma Delta.", extractedArray,
                "All JSON array chunks must be concatenated without loss");

        // Large streamed buffer test (>10,000 characters across 10 chunks)
        StringBuilder largeSse = new StringBuilder();
        StringBuilder expectedLarge = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            String chunkText = "Chunk[" + i + "] " + "A".repeat(1000) + " ";
            expectedLarge.append(chunkText);
            largeSse.append("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"")
                    .append(chunkText)
                    .append("\"}]}}]}\n\n");
        }
        String extractedLarge = GeminiProvider.extractTextFromPayload(largeSse.toString());
        assertNotNull(extractedLarge);
        assertEquals(expectedLarge.toString().trim(), extractedLarge.trim(),
                "Large payload buffer (>10KB) must assemble completely to EOF");
    }

    // =========================================================================
    // 4. FAILOVER & RESILIENCE (FAULT INJECTION)
    // =========================================================================

    @Test
    @Order(8)
    @DisplayName("4.1 Failover & Resilience: Transient HTTP 503 Retries and Succeeds")
    void testTransientHttp503RetriesAndSucceeds() {
        AtomicInteger callCount = new AtomicInteger(0);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    int count = callCount.incrementAndGet();
                    if (count < 3) {
                        return new Response.Builder()
                                .request(chain.request())
                                .protocol(Protocol.HTTP_1_1)
                                .code(503)
                                .message("Service Unavailable (High Demand)")
                                .body(ResponseBody.create("{\"error\": \"high demand\"}", MediaType.parse("application/json")))
                                .build();
                    }
                    return new Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(ResponseBody.create(
                                    "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Gemini recovered after retry!\"}]}}]}",
                                    MediaType.parse("application/json")))
                            .build();
                })
                .build();

        GeminiProvider provider = new GeminiProvider(
                "https://generativelanguage.googleapis.com/v1beta/models",
                "test-api-key",
                client,
                2,  // 2 retries
                10L // 10ms backoff
        );

        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("test retry")
                .build();

        Stream<String> responseStream = provider.stream(request);
        List<String> results = responseStream.toList();
        assertEquals(1, results.size());
        assertEquals("Gemini recovered after retry!", results.get(0));
        assertEquals(3, callCount.get(), "Must have made exactly 3 attempts (1 initial + 2 retries)");
    }

    @Test
    @Order(9)
    @DisplayName("4.2 Failover & Resilience: Persistent HTTP 429 Exhausts Retries and Falls Back to In-Memory")
    void testPersistentHttp429FallsBackToInMemoryRouter() {
        AtomicInteger callCount = new AtomicInteger(0);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    callCount.incrementAndGet();
                    return new Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(429)
                            .message("Too Many Requests (Quota Exceeded)")
                            .body(ResponseBody.create("{\"error\": \"quota exceeded\"}", MediaType.parse("application/json")))
                            .build();
                })
                .build();

        GeminiProvider failingGemini = new GeminiProvider(
                "https://generativelanguage.googleapis.com/v1beta/models",
                "test-api-key",
                client,
                2,  // 2 retries
                5L  // 5ms backoff
        );

        InMemoryLlmProvider inMemory = new InMemoryLlmProvider();

        // Build Router with failing Gemini preferred, InMemory fallback
        LlmRouter router = new LlmRouter(List.of(failingGemini, inMemory));

        LlmRequest request = LlmRequest.builder()
                .model("gemini-3.6-flash")
                .prompt("test fallback")
                .build();

        // Assert Router executes without throwing pipeline exception
        assertDoesNotThrow(() -> {
            LlmResponse response = router.complete(request);
            assertNotNull(response, "Response must not be null");
            assertNotNull(response.content(), "Content must not be null");
            assertTrue(response.content().contains("echoes: test fallback"),
                    "Response must be successfully served by fallback InMemory provider");
        });

        // Verify that Gemini was attempted 3 times (1 initial + 2 retries) before failover
        assertEquals(3, callCount.get(), "Failing provider must have attempted all retries before failover");
    }

    // =========================================================================
    // 5. DOMAIN ISOLATION SWEEP
    // =========================================================================

    @Test
    @Order(10)
    @DisplayName("5. Domain Isolation Sweep: Out-of-Domain Queries Do NOT Inject Java/Spring Specs")
    void testDomainIsolationSweep() {
        List<String> outOfDomainQueries = List.of(
                "how to become a chef",
                "history of the Roman empire",
                "best recipe for chocolate cake",
                "how to change a flat tire on a bicycle"
        );

        for (String query : outOfDomainQueries) {
            SDKResponse response = client.chat(query);
            assertNotNull(response, "Response must not be null for: " + query);
            String answer = response.answer();
            assertNotNull(answer, "Answer must not be null for: " + query);

            String answerLower = answer.toLowerCase();

            // Strict domain isolation assertions: Java, Spring, and Healthcare specs must NOT be injected
            assertFalse(answerLower.contains("java platform architecture"),
                    "Out-of-domain query '" + query + "' must not contain Java Platform Architecture");
            assertFalse(answerLower.contains("spring framework"),
                    "Out-of-domain query '" + query + "' must not contain Spring Framework specs");
            assertFalse(answerLower.contains("spring boot"),
                    "Out-of-domain query '" + query + "' must not contain Spring Boot specs");
            assertFalse(answerLower.contains("jvm execution"),
                    "Out-of-domain query '" + query + "' must not contain JVM Execution specs");
            assertFalse(answerLower.contains("healthcare and hospital management"),
                    "Out-of-domain query '" + query + "' must not contain Healthcare specs");
            assertFalse(answerLower.contains("patient administration"),
                    "Out-of-domain query '" + query + "' must not contain Hospital Patient Administration");

            // Verify evidence bundle in structured payload does not carry mismatched documents
            Map<String, Object> payload = response.structuredPayload();
            if (payload != null && payload.get("evidence") instanceof List<?> evidenceList) {
                for (Object item : evidenceList) {
                    String itemString = String.valueOf(item).toLowerCase();
                    assertFalse(itemString.contains("java platform architecture"),
                            "Evidence bundle must not contain Java Platform Architecture for: " + query);
                    assertFalse(itemString.contains("spring framework"),
                            "Evidence bundle must not contain Spring Framework for: " + query);
                    assertFalse(itemString.contains("healthcare and hospital"),
                            "Evidence bundle must not contain Healthcare specs for: " + query);
                }
            }
        }
    }
}
