package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>KnowledgeAcquisitionLiveIntegrationTest</b>
 *
 * <p>Integration test validating that K0.6 Knowledge Acquisition is wired into the live execution
 * pipeline. Confirms that when a query has missing knowledge, the orchestrator acquires
 * documents, KnowledgeStage retrieves them, and EvidenceAgent extracts real [KNOWLEDGE] evidence
 * into the final EvidenceBundle.</p>
 */
public class KnowledgeAcquisitionLiveIntegrationTest {

    private ShreeClient client;

    @BeforeEach
    void setUp() {
        client = ShreeClient.builder().build();
        assertNotNull(client, "ShreeClient must initialize cleanly");
    }

    @Test
    @DisplayName("Verify K0.6 Knowledge Acquisition provides real [KNOWLEDGE] evidence for Java query")
    public void testJavaQueryKnowledgeAcquisition() {
        System.out.println("===============================================================");
        System.out.println("TEST: Live Knowledge Acquisition for Java Query");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("Explain the Java memory model and collections framework.");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");
        assertTrue(response.confidence() > 0.0, "Confidence should be positive: " + response.confidence());

        System.out.println("[Chat Answer]\n" + response.answer());
        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload must not be null");

        // Verify evidence bundle contains KNOWLEDGE evidence
        Object evidenceRaw = payload.get("evidence");
        assertNotNull(evidenceRaw, "Evidence list must be present in structured payload");
        assertInstanceOf(List.class, evidenceRaw, "Evidence must be a List");

        List<?> evidenceList = (List<?>) evidenceRaw;
        assertFalse(evidenceList.isEmpty(), "Evidence list must not be empty");

        boolean hasKnowledgeEvidence = false;
        String knowledgeTitle = null;
        String knowledgeContent = null;

        for (Object item : evidenceList) {
            if (item instanceof Map<?, ?> itemMap) {
                String sourceType = String.valueOf(itemMap.get("sourceType"));
                if ("KNOWLEDGE".equalsIgnoreCase(sourceType)) {
                    hasKnowledgeEvidence = true;
                    knowledgeTitle = String.valueOf(itemMap.get("title"));
                    knowledgeContent = String.valueOf(itemMap.get("content"));
                    System.out.println("[Found KNOWLEDGE Evidence] Title: " + knowledgeTitle);
                    System.out.println("[Found KNOWLEDGE Evidence] Content: " + knowledgeContent);
                    break;
                }
            }
        }

        assertTrue(hasKnowledgeEvidence, "EvidenceBundle must contain at least one item with sourceType KNOWLEDGE");
        assertNotNull(knowledgeTitle, "Knowledge evidence title must not be null");
        assertFalse(knowledgeTitle.isBlank(), "Knowledge evidence title must not be blank");

        // Verify reflection outcome does not complain about missing knowledge
        Object reflectionRaw = payload.get("reflection");
        if (reflectionRaw instanceof Map<?, ?> reflectionMap) {
            Object lessonsRaw = reflectionMap.get("lessons");
            if (lessonsRaw instanceof List<?> lessonsList) {
                for (Object lesson : lessonsList) {
                    assertFalse(String.valueOf(lesson).contains("No knowledge or evidence was attached"),
                            "Reflection must not report missing knowledge when knowledge was acquired: " + lesson);
                }
            }
        }

        System.out.println("[SUCCESS] Verified real KNOWLEDGE evidence present in EvidenceBundle!");
    }

    @Test
    @DisplayName("Verify K0.6 Knowledge Acquisition provides real [KNOWLEDGE] evidence for Architecture query")
    public void testArchitectureQueryKnowledgeAcquisition() {
        System.out.println("===============================================================");
        System.out.println("TEST: Live Knowledge Acquisition for System Architecture Query");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("Analyze the system architecture for high throughput and consistency.");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload must not be null");

        Object evidenceRaw = payload.get("evidence");
        assertNotNull(evidenceRaw, "Evidence must be present");
        List<?> evidenceList = (List<?>) evidenceRaw;

        boolean hasKnowledgeEvidence = false;
        for (Object item : evidenceList) {
            if (item instanceof Map<?, ?> itemMap) {
                String sourceType = String.valueOf(itemMap.get("sourceType"));
                if ("KNOWLEDGE".equalsIgnoreCase(sourceType)) {
                    hasKnowledgeEvidence = true;
                    System.out.println("[Found KNOWLEDGE Evidence] Title: " + itemMap.get("title"));
                    break;
                }
            }
        }

        assertTrue(hasKnowledgeEvidence, "EvidenceBundle must contain KNOWLEDGE evidence for architecture query");
        System.out.println("[SUCCESS] Architecture query successfully acquired and grounded knowledge!");
    }

    @Test
    @DisplayName("Verify Python query does not inject Java Platform Architecture or Spring Boot docs")
    public void testPythonQueryDoesNotInjectJavaKnowledge() {
        System.out.println("===============================================================");
        System.out.println("TEST: Verify Python Query Does Not Inject Java Knowledge");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("whats is python");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        System.out.println("[Python Query Answer]\n" + response.answer());

        // Assert answer does NOT contain Java Platform Architecture or Spring Boot
        assertFalse(response.answer().contains("Java Platform Architecture and Specifications"),
                "Python query answer must NOT contain Java Platform Architecture");
        assertFalse(response.answer().contains("Spring Framework and Spring Boot Architecture"),
                "Python query answer must NOT contain Spring Boot Architecture");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload must not be null");

        Object evidenceRaw = payload.get("evidence");
        if (evidenceRaw instanceof List<?> evidenceList) {
            for (Object item : evidenceList) {
                if (item instanceof Map<?, ?> itemMap) {
                    String title = String.valueOf(itemMap.get("title"));
                    String content = String.valueOf(itemMap.get("content"));
                    assertFalse(title.contains("Java Platform Architecture"),
                            "Evidence title must not be Java for Python query: " + title);
                    assertFalse(content.contains("JVM Execution: Bytecode compilation"),
                            "Evidence content must not contain JVM bytecode for Python query");
                }
            }
        }
        System.out.println("[SUCCESS] Python query is clean of Java Platform Architecture injection!");
    }

    @Test
    @DisplayName("Verify Hospital Management query does not inject Java Platform Architecture or JVM specs")
    public void testHospitalManagementDoesNotInjectJavaKnowledge() {
        System.out.println("===============================================================");
        System.out.println("TEST: Verify Hospital Management Query Does Not Inject Java Knowledge");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("Create a hospital management system");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        System.out.println("[Hospital Management Answer]\n" + response.answer());

        // Assert answer does NOT contain Java JVM / Spring Boot specs
        assertFalse(response.answer().contains("Java Platform Architecture and Specifications"),
                "Hospital management answer must NOT contain Java Platform Architecture");
        assertFalse(response.answer().contains("JVM Execution: Bytecode compilation"),
                "Hospital management answer must NOT contain JVM specs");

        Map<String, Object> payload = response.structuredPayload();
        if (payload != null && payload.get("evidence") instanceof List<?> evidenceList) {
            for (Object item : evidenceList) {
                if (item instanceof Map<?, ?> itemMap) {
                    String title = String.valueOf(itemMap.get("title"));
                    String content = String.valueOf(itemMap.get("content"));
                    assertFalse(title.contains("Java Platform Architecture"),
                            "Evidence title must not be Java for hospital management query: " + title);
                    assertFalse(content.contains("JVM Execution: Bytecode compilation"),
                            "Evidence content must not contain JVM bytecode for hospital management query");
                }
            }
        }
        System.out.println("[SUCCESS] Hospital management query is clean of Java Platform Architecture injection!");
    }

    @Test
    @DisplayName("Verify 'how to become java developer in 30 days .' acquires and attaches Java knowledge")
    public void testJavaQueryWithStopWordsAndTrailingDot() {
        System.out.println("===============================================================");
        System.out.println("TEST: 'how to become java developer in 30 days .' Knowledge Retrieval");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("how to become java developer in 30 days .");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload must not be null");

        Object evidenceRaw = payload.get("evidence");
        assertNotNull(evidenceRaw, "Evidence must be present for natural language java query");
        List<?> evidenceList = (List<?>) evidenceRaw;
        assertFalse(evidenceList.isEmpty(), "Evidence list must not be empty");

        boolean hasJavaKnowledge = false;
        for (Object item : evidenceList) {
            if (item instanceof Map<?, ?> itemMap) {
                String sourceType = String.valueOf(itemMap.get("sourceType"));
                String title = String.valueOf(itemMap.get("title"));
                String content = String.valueOf(itemMap.get("content"));
                if ("KNOWLEDGE".equalsIgnoreCase(sourceType) && (title.contains("Java") || content.contains("Java"))) {
                    hasJavaKnowledge = true;
                    System.out.println("[Found Java KNOWLEDGE Evidence] Title: " + title);
                    break;
                }
            }
        }

        assertTrue(hasJavaKnowledge, "EvidenceBundle must contain Java knowledge for 'how to become java developer in 30 days .'");
        System.out.println("[SUCCESS] Java knowledge successfully attached for natural query with stop-words and trailing dot!");
    }

    @Test
    @DisplayName("Verify 'build hospital management system' acquires and attaches Healthcare Architecture knowledge")
    public void testBuildHospitalManagementAcquiresHealthcareKnowledge() {
        System.out.println("===============================================================");
        System.out.println("TEST: 'build hospital management system' Healthcare Knowledge Retrieval");
        System.out.println("===============================================================");

        SDKResponse response = client.chat("build hospital management system");

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.answer(), "Answer must not be null");
        assertFalse(response.answer().isBlank(), "Answer must not be blank");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload must not be null");

        Object evidenceRaw = payload.get("evidence");
        assertNotNull(evidenceRaw, "Evidence must be present for hospital management query");
        List<?> evidenceList = (List<?>) evidenceRaw;
        assertFalse(evidenceList.isEmpty(), "Evidence list must not be empty");

        boolean hasHealthcareKnowledge = false;
        for (Object item : evidenceList) {
            if (item instanceof Map<?, ?> itemMap) {
                String sourceType = String.valueOf(itemMap.get("sourceType"));
                String title = String.valueOf(itemMap.get("title"));
                String content = String.valueOf(itemMap.get("content"));
                System.out.println("  [DEBUG EVIDENCE] type=" + sourceType + ", title=" + title + ", contentSnippet=" + (content.length() > 60 ? content.substring(0, 60) : content));
                if ("KNOWLEDGE".equalsIgnoreCase(sourceType)
                        && (title.contains("Healthcare") || title.contains("Hospital") || content.contains("Hospital"))) {
                    hasHealthcareKnowledge = true;
                    System.out.println("[Found Healthcare KNOWLEDGE Evidence] Title: " + title);
                    break;
                }
            }
        }

        assertTrue(hasHealthcareKnowledge, "EvidenceBundle must contain Healthcare/Hospital knowledge for 'build hospital management system'");
        System.out.println("[SUCCESS] Healthcare Architecture successfully attached for 'build hospital management system'!");
    }
}
