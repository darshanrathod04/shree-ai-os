package com.shreeai.os.platform.intelligence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.intelligence.context.IntelligenceRequest;
import com.shreeai.os.platform.intelligence.context.ProjectProfile;
import com.shreeai.os.platform.intelligence.evidence.EvidenceItem;
import com.shreeai.os.platform.intelligence.evidence.EvidenceProvenance;
import com.shreeai.os.platform.intelligence.evidence.EvidenceSource;
import com.shreeai.os.platform.intelligence.evidence.EvidenceType;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 — Structured Context & Evidence Intelligence semantic preservation tests.
 *
 * <p>These tests prove that important structured information survives the
 * SDK → Runtime → Pipeline → SDK path without truncation, flattening, or loss.
 * They assert actual values, not merely object non-nullness.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since EO-V1-INTEL-READINESS-001 Phase 1
 */
public class IntelligenceFoundationPreservationTest {

    private static final String LONG_EVIDENCE =
            "This is a deliberately long evidence payload exceeding one hundred characters "
            + "to prove that the structured intelligence context does not truncate evidence "
            + "at any boundary. The complete text must survive end-to-end.";

    // ========================================================================
    // Test A — Evidence preservation
    // ========================================================================

    @Test
    public void testA_EvidencePreservation() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "College Management System");
        metadata.put("technologies", List.of("Java", "Maven"));
        metadata.put("totalFiles", 38);
        metadata.put("sourceFiles", 30);
        metadata.put("testFiles", 4);
        metadata.put("evidence", List.of(
                Map.of("id", "ev-1", "type", "FACT", "value", "Java",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "cms", "confidence", 0.9),
                Map.of("id", "ev-2", "type", "FACT", "value", "Maven",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "cms", "confidence", 0.9),
                Map.of("id", "ev-3", "type", "OBSERVATION", "value", "38 total files",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "cms", "confidence", 0.8),
                Map.of("id", "ev-4", "type", "TEST_RESULT", "value", "4 tests",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "cms", "confidence", 0.7)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze this project")
                .sessionId("session-A")
                .metadata(metadata)
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        // Project identity survives
        assertNotNull(context.project(), "Project profile must be present");
        assertEquals("College Management System", context.project().projectName());
        assertEquals(List.of("Java", "Maven"), context.project().technologies());
        assertEquals(38, context.project().totalFiles());
        assertEquals(30, context.project().sourceFiles());
        assertEquals(4, context.project().testFiles());

        // All evidence survives
        assertEquals(4, context.evidence().size(), "All 4 evidence items must survive");
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("Java")));
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("Maven")));
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("38 total files")));
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("4 tests")));
    }

    // ========================================================================
    // Test B — No truncation
    // ========================================================================

    @Test
    public void testB_NoTruncation() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Long Evidence Project");
        metadata.put("evidence", List.of(
                Map.of("id", "ev-long", "type", "OBSERVATION", "value", LONG_EVIDENCE,
                        "sourceType", "FILE", "sourceId", "src/main/README.md", "confidence", 0.9)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze")
                .sessionId("session-B")
                .metadata(metadata)
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        assertEquals(1, context.evidence().size());
        assertEquals(LONG_EVIDENCE, context.evidence().get(0).value(),
                "Evidence longer than 100 chars must survive completely");
        assertTrue(context.evidence().get(0).value().length() > 100);
    }

    // ========================================================================
    // Test C — Provenance
    // ========================================================================

    @Test
    public void testC_Provenance() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Provenance Project");
        metadata.put("evidence", List.of(
                Map.of("id", "ev-prov", "type", "CODE", "value", "public class App {}",
                        "sourceType", "FILE", "sourceId", "src/main/java/App.java",
                        "sourceLocation", "App.java:1-10", "confidence", 0.95)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze")
                .sessionId("session-C")
                .metadata(metadata)
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        EvidenceItem item = context.evidence().get(0);
        assertNotNull(item.provenance(), "Provenance must be present");
        assertEquals("FILE", item.provenance().sourceType());
        assertEquals("src/main/java/App.java", item.provenance().sourceId());
        assertNotNull(item.provenance().capturedAt(), "Capture time must be present");
        assertEquals("SDK_METADATA_EXTRACTION", item.provenance().extractionMethod());
        assertEquals("src/main/java/App.java", item.source().sourceId());
    }

    // ========================================================================
    // Test D — Multiple evidence sources remain distinguishable
    // ========================================================================

    @Test
    public void testD_MultipleEvidenceSourcesDistinguishable() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Multi Source Project");
        metadata.put("evidence", List.of(
                Map.of("id", "ev-project", "type", "PROJECT_ARTIFACT", "value", "Spring Boot app",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "project-1", "confidence", 0.9),
                Map.of("id", "ev-memory", "type", "MEMORY", "value", "User prefers REST",
                        "sourceType", "MEMORY", "sourceId", "mem-42", "confidence", 0.6),
                Map.of("id", "ev-knowledge", "type", "KNOWLEDGE", "value", "Spring Boot uses embedded Tomcat",
                        "sourceType", "KNOWLEDGE", "sourceId", "kwn-7", "confidence", 0.85)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze")
                .sessionId("session-D")
                .metadata(metadata)
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        assertEquals(3, context.evidence().size());
        assertTrue(context.evidence().stream().anyMatch(e -> e.type() == EvidenceType.PROJECT_ARTIFACT));
        assertTrue(context.evidence().stream().anyMatch(e -> e.type() == EvidenceType.MEMORY));
        assertTrue(context.evidence().stream().anyMatch(e -> e.type() == EvidenceType.KNOWLEDGE));

        // Each evidence item retains its distinct source
        EvidenceItem memoryItem = context.evidence().stream()
                .filter(e -> e.type() == EvidenceType.MEMORY).findFirst().orElseThrow();
        assertEquals("mem-42", memoryItem.source().sourceId());
    }

    // ========================================================================
    // Test E — Contradictory evidence coexists
    // ========================================================================

    @Test
    public void testE_ContradictoryEvidenceCoexists() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Contradiction Project");
        metadata.put("evidence", List.of(
                Map.of("id", "ev-java", "type", "FACT", "value", "Java",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "project-1", "confidence", 0.9),
                Map.of("id", "ev-python", "type", "FACT", "value", "Python",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "project-1", "confidence", 0.7)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze")
                .sessionId("session-E")
                .metadata(metadata)
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        // Both conflicting facts survive — neither overwrites the other
        assertEquals(2, context.evidence().size());
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("Java")));
        assertTrue(context.evidence().stream().anyMatch(e -> e.value().equals("Python")));
    }

    // ========================================================================
    // Test F — Context isolation
    // ========================================================================

    @Test
    public void testF_ContextIsolation() {
        Map<String, Object> projectAMetadata = new HashMap<>();
        projectAMetadata.put("projectName", "Project A");
        projectAMetadata.put("technologies", List.of("Java"));
        projectAMetadata.put("evidence", List.of(
                Map.of("id", "ev-a", "type", "FACT", "value", "Java",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "proj-a", "confidence", 0.9)
        ));

        Map<String, Object> projectBMetadata = new HashMap<>();
        projectBMetadata.put("projectName", "Project B");
        projectBMetadata.put("technologies", List.of("Python"));
        projectBMetadata.put("evidence", List.of(
                Map.of("id", "ev-b", "type", "FACT", "value", "Python",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "proj-b", "confidence", 0.9)
        ));

        SDKRequest requestA = SDKRequest.builder()
                .message("Analyze A").sessionId("session-A").metadata(projectAMetadata).build();
        SDKRequest requestB = SDKRequest.builder()
                .message("Analyze B").sessionId("session-B").metadata(projectBMetadata).build();

        IntelligenceContext contextA = IntelligenceContextBuilder.fromSdkRequest(requestA);
        IntelligenceContext contextB = IntelligenceContextBuilder.fromSdkRequest(requestB);

        // Project A evidence never appears in Project B context
        assertEquals("Project A", contextA.project().projectName());
        assertEquals("Project B", contextB.project().projectName());
        assertTrue(contextA.evidence().stream().allMatch(e -> e.source().sourceId().equals("proj-a")));
        assertTrue(contextB.evidence().stream().allMatch(e -> e.source().sourceId().equals("proj-b")));
        assertFalse(contextB.evidence().stream().anyMatch(e -> e.value().equals("Java")));
        assertFalse(contextA.evidence().stream().anyMatch(e -> e.value().equals("Python")));
    }

    // ========================================================================
    // Test G — Backward compatibility
    // ========================================================================

    @Test
    public void testG_BackwardCompatibility() {
        // Existing V1 SDK usage must still work
        ShreeAI shree = ShreeAI.builder().apiKey("local").build();
        SDKResponse response = shree.chat("Hello Shree");

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");
        assertTrue(response.confidence() >= 0.0 && response.confidence() <= 1.0);
        assertNotNull(response.timestamp());
        assertNotNull(response.structuredPayload(), "Structured payload must never be null");
    }

    // ========================================================================
    // Test H — Serialization round-trip
    // ========================================================================

    @Test
    public void testH_SerializationRoundTrip() throws Exception {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Serialization Project");
        metadata.put("technologies", List.of("Java", "Spring"));
        metadata.put("totalFiles", 100);
        metadata.put("evidence", List.of(
                Map.of("id", "ev-s1", "type", "FACT", "value", "Java",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "proj-s", "confidence", 0.9)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze")
                .sessionId("session-H")
                .metadata(metadata)
                .build();

        IntelligenceContext original = IntelligenceContextBuilder.fromSdkRequest(request);

        // Serialize to JSON and back
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(original);
        IntelligenceContext restored = mapper.readValue(json, IntelligenceContext.class);

        // Important fields survive round-trip
        assertEquals(original.contextId(), restored.contextId());
        assertEquals(original.request().requestId(), restored.request().requestId());
        assertEquals(original.request().userInput(), restored.request().userInput());
        assertEquals(original.project().projectName(), restored.project().projectName());
        assertEquals(original.project().technologies(), restored.project().technologies());
        assertEquals(original.project().totalFiles(), restored.project().totalFiles());
        assertEquals(original.evidence().size(), restored.evidence().size());
        assertEquals(original.evidence().get(0).value(), restored.evidence().get(0).value());
        assertEquals(original.evidence().get(0).provenance().sourceId(),
                restored.evidence().get(0).provenance().sourceId());
    }

    // ========================================================================
    // Test I — Rich context preservation SDK → Runtime → Pipeline
    // ========================================================================

    @Test
    public void testI_RichContextPreservationThroughRuntime() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectName", "Runtime Project");
        metadata.put("technologies", List.of("Java", "Maven"));
        metadata.put("totalFiles", 38);
        metadata.put("evidence", List.of(
                Map.of("id", "ev-r1", "type", "FACT", "value", "Java",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "proj-r", "confidence", 0.9),
                Map.of("id", "ev-r2", "type", "OBSERVATION", "value", "38 total files",
                        "sourceType", "PROJECT_ARTIFACT", "sourceId", "proj-r", "confidence", 0.8)
        ));

        SDKRequest request = SDKRequest.builder()
                .message("Analyze this project")
                .sessionId("session-I")
                .metadata(metadata)
                .build();

        ShreeAI shree = ShreeAI.builder().apiKey("local").build();
        SDKResponse response = shree.chat(request);

        // The structured payload must carry the intelligence context back to the developer
        assertNotNull(response.structuredPayload(), "Structured payload must be present");
        Object contextValue = response.structuredPayload().get("intelligenceContext");
        assertNotNull(contextValue, "IntelligenceContext must survive SDK → Runtime → SDK");
        assertTrue(contextValue instanceof IntelligenceContext,
                "Structured context must be a typed IntelligenceContext, not a flattened string");

        IntelligenceContext returned = (IntelligenceContext) contextValue;
        assertEquals("Runtime Project", returned.project().projectName());
        assertEquals(List.of("Java", "Maven"), returned.project().technologies());
        assertEquals(38, returned.project().totalFiles());
        assertEquals(2, returned.evidence().size());
        assertTrue(returned.evidence().stream().anyMatch(e -> e.value().equals("Java")));
        assertTrue(returned.evidence().stream().anyMatch(e -> e.value().equals("38 total files")));
    }

    // ========================================================================
    // Test J — Empty/minimal context
    // ========================================================================

    @Test
    public void testJ_EmptyMinimalContext() {
        SDKRequest request = SDKRequest.builder()
                .message("Hello")
                .sessionId("session-J")
                .build();

        IntelligenceContext context = IntelligenceContextBuilder.fromSdkRequest(request);

        // Graceful handling with no additional evidence
        assertNotNull(context, "Context must not be null");
        assertNull(context.project(), "No project profile when none supplied");
        assertTrue(context.evidence().isEmpty(), "No evidence when none supplied");
        assertEquals("Hello", context.request().userInput());
        assertEquals("session-J", context.request().sessionId());
    }

    // ========================================================================
    // Test K — ExecutionResult structured payload (runtime contract)
    // ========================================================================

    @Test
    public void testK_ExecutionResultStructuredPayload() {
        Map<String, Object> payload = Map.of("intelligenceContext", "test-context");
        ExecutionResult result = ExecutionResult.builder()
                .requestId("req-k")
                .success(true)
                .output("done")
                .structuredPayload(payload)
                .build();

        assertTrue(result.isSuccess());
        assertEquals("done", result.output().orElse(""));
        assertEquals("test-context", result.structuredPayload().get("intelligenceContext"));
        assertTrue(result.structuredPayload().isEmpty() == false);
    }
}