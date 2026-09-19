package com.shreeai.os.platform;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>DeveloperSmokeTest</b>
 *
 * <p>End-to-End Developer Verification & Smoke Test validating that Shree AI OS
 * is 100% operational for external developers using the canonical ShreeClient.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeveloperSmokeTest {

    private ShreeClient client;

    @BeforeEach
    void setUp() {
        // Initialize the in-process runtime client
        client = ShreeClient.builder().build();
        assertNotNull(client, "ShreeClient must initialize cleanly");
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Memory Ingestion and Grounding")
    public void testMemoryIngestionAndGrounding() {
        System.out.println("===================================================");
        System.out.println("TEST 1: Memory Ingestion and Grounding");
        System.out.println("===================================================");
        System.out.println("Storing memory: developer_role -> Principal Software Architect");

        // 1. Ingest/Store Memory
        SDKResponse storeResponse = client.memory().store("developer_role", "Principal Software Architect");
        assertNotNull(storeResponse, "Store response must not be null");
        System.out.println("[Memory Store] Response Answer: " + storeResponse.answer());
        System.out.println("[Memory Store] Structured Payload: " + storeResponse.structuredPayload());

        // 2. Query Memory Grounding via Chat
        System.out.println("Querying: What is my professional role?");
        SDKResponse chatResponse = client.chat("What is my professional role?");
        assertNotNull(chatResponse, "Chat response must not be null");

        String answer = chatResponse.answer();
        Map<String, Object> payload = chatResponse.structuredPayload();

        System.out.println("[Chat Query] Answer:\n" + answer);
        System.out.println("[Chat Query] Confidence: " + chatResponse.confidence());
        System.out.println("[Chat Query] Reasoning Available: " + chatResponse.reasoningAvailable());
        System.out.println("[Chat Query] Structured Payload Keys: " + (payload != null ? payload.keySet() : "none"));

        // 3. Verify Memory Grounding in Answer or Grounding Metadata
        boolean reflectedInAnswer = answer != null && answer.contains("Principal Software Architect");

        boolean reflectedInGrounding = false;
        if (payload != null) {
            String groundingAnswer = (String) payload.get("groundingAnswer");
            if (groundingAnswer != null && groundingAnswer.contains("Principal Software Architect")) {
                reflectedInGrounding = true;
            }

            Object evidenceRaw = payload.get("evidence");
            if (evidenceRaw instanceof List<?> evidenceList) {
                for (Object item : evidenceList) {
                    if (item instanceof Map<?, ?> itemMap) {
                        String content = String.valueOf(itemMap.get("content"));
                        if (content.contains("Principal Software Architect")) {
                            reflectedInGrounding = true;
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("[Verification] Reflected in Answer: " + reflectedInAnswer);
        System.out.println("[Verification] Reflected in Grounding Metadata: " + reflectedInGrounding);

        trueAssert(reflectedInAnswer || reflectedInGrounding,
                "The answer or grounding metadata must reflect 'Principal Software Architect'");
    }

    private void trueAssert(boolean cond, String msg) {
        trueCheck(condtrue(cond), msg);
    }
    private boolean condtrue(boolean b) { return b; }
    private void trueCheck(boolean c, String m) { assertTrue(c, m); }

    @Test
    @Order(2)
    @DisplayName("Test 2: Hybrid Knowledge Ingestion and Search")
    public void testHybridKnowledgeIngestionAndSearch() {
        System.out.println("===================================================");
        System.out.println("TEST 2: Hybrid Knowledge Ingestion and Search");
        System.out.println("===================================================");
        System.out.println("Ingesting: Shree AI OS is an in-process deterministic cognitive runtime for Java 21.");

        // 1. Ingest Knowledge Document
        SDKResponse ingestResponse = client.knowledge().ingest(
                "Shree AI OS is an in-process deterministic cognitive runtime for Java 21."
        );
        assertNotNull(ingestResponse, "Ingest response must not be null");
        System.out.println("[Knowledge Ingest] Answer: " + ingestResponse.answer());
        System.out.println("[Knowledge Ingest] Payload: " + ingestResponse.structuredPayload());

        // 2. Search Knowledge
        System.out.println("Searching: deterministic runtime");
        SDKResponse searchResponse = client.knowledge().search("deterministic runtime");
        assertNotNull(searchResponse, "Search response must not be null");

        String answer = searchResponse.answer();
        double confidence = searchResponse.confidence();
        Map<String, Object> payload = searchResponse.structuredPayload();

        System.out.println("[Knowledge Search] Answer:\n" + answer);
        System.out.println("[Knowledge Search] Confidence: " + confidence);
        System.out.println("[Knowledge Search] Payload Keys: " + (payload != null ? payload.keySet() : "none"));

        // 3. Assert search results non-empty and score/confidence > 0
        assertNotNull(answer, "Search answer must not be null");
        assertFalse(answer.isBlank(), "Search answer must not be blank");
        assertTrue(confidence > 0.0, "Confidence/score must be greater than 0: " + confidence);

        if (payload != null) {
            Object knowledgeCount = payload.get("knowledgeCount");
            if (knowledgeCount instanceof Number) {
                Number count = (Number) knowledgeCount;
                System.out.println("[Knowledge Search] Knowledge Count: " + count);
                assertTrue(count.intValue() > 0, "Knowledge count should be greater than 0");
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Unlocked Reasoning Pipeline")
    public void testUnlockedReasoningPipeline() {
        System.out.println("===================================================");
        System.out.println("TEST 3: Unlocked Reasoning Pipeline");
        System.out.println("===================================================");
        System.out.println("Querying: Analyze the system architecture for high throughput and consistency.");

        // Submit query passing through ReasoningStage
        SDKResponse response = client.chat("Analyze the system architecture for high throughput and consistency.");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        System.out.println("[Reasoning Query] Answer:\n" + response.answer());
        System.out.println("[Reasoning Query] Confidence: " + response.confidence());
        System.out.println("[Reasoning Query] Reasoning Available: " + response.reasoningAvailable());

        // Assert reasoning available is true and execution completed without NPE
        assertTrue(response.reasoningAvailable(), "Reasoning pipeline must be available and unlocked");
        assertTrue(response.confidence() > 0.0, "Confidence should be positive");
    }
}

