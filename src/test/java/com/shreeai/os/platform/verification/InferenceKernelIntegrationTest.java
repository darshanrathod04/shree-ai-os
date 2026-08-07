package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.inference.model.Hypothesis;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Inference Kernel Integration Test
 *
 * <p>This test verifies that the Inference Kernel works correctly with real inference operations.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 7
 */
public class InferenceKernelIntegrationTest {

    private DefaultInferenceEngine inferenceEngine;

    @BeforeEach
    public void setUp() {
        this.inferenceEngine = new DefaultInferenceEngine();
    }

    private ReasoningResult createReasoningResult(String conclusion, double confidence) {
        return new ReasoningResult(
                "rsn-test",
                "Test reasoning summary",
                List.of("Test finding"),
                List.of("Test evidence"),
                conclusion,
                confidence,
                List.of(),
                List.of(),
                "test",
                "EVIDENCE_BASED_REASONING",
                5,
                Map.of(),
                Instant.now()
        );
    }

    private Memory createMemory(String text, double importance) {
        MemoryContent content = new MemoryContent(text, null, Map.of(), Instant.now());
        MemoryId id = new MemoryId("mem-" + System.nanoTime());
        IdentityId owner = new IdentityId("test-owner");
        MemoryMetadata metadata = new MemoryMetadata(
                id, MemoryType.EPISODIC, MemoryStatus.ACTIVE, MemoryVisibility.PRIVATE,
                owner, Set.of("test"), importance, 0.8,
                "test", Instant.now(), Instant.now(), Instant.now(), 0L
        );
        return new Memory(id, content, metadata, Instant.now(), Instant.now());
    }

    private KnowledgeNode createKnowledge(String label, String desc, double confidence) {
        return KnowledgeNode.of(
                new KnowledgeId("kwn-" + System.nanoTime()),
                KnowledgeType.CONCEPT, KnowledgeState.ACTIVE, KnowledgeScope.GLOBAL,
                label, desc, Map.of("confidence", confidence, "authority", 0.8),
                Instant.now(), Instant.now()
        );
    }

    @Test
    public void testReasoningToInference() {
        // Create reasoning result
        ReasoningResult reasoningResult = createReasoningResult(
                "Java is a programming language used for enterprise applications", 0.85);

        // Run inference
        InferenceResult result = inferenceEngine.infer(
                "What is Java?",
                reasoningResult,
                List.of(createMemory("User is learning Java", 0.7)),
                List.of(createKnowledge("Java Programming", "Java is a programming language", 0.9)),
                "test-context"
        );

        // Verify
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.inferenceId(), "Inference ID should not be null");
        assertFalse(result.hypotheses().isEmpty(), "Should have hypotheses");
        assertNotNull(result.bestHypothesis(), "Should have best hypothesis");
        assertFalse(result.supportingEvidence().isEmpty(), "Should have supporting evidence");
        assertNotNull(result.recommendedNextInvestigation(), "Should have next investigation");
    }

    @Test
    public void testMultipleHypothesesGenerated() {
        ReasoningResult reasoningResult = createReasoningResult("User is preparing for job interviews", 0.8);

        InferenceResult result = inferenceEngine.infer(
                "Tell me about Java",
                reasoningResult,
                List.of(createMemory("Learning Java", 0.8), createMemory("Learning DSA", 0.7)),
                List.of(createKnowledge("Java Programming", "Java programming", 0.9), createKnowledge("Interview Roadmap", "Interview preparation", 0.8)),
                "test-context"
        );

        assertTrue(result.hypotheses().size() >= 3, "Should generate multiple hypotheses");
        assertTrue(result.hypotheses().size() >= 2, "Should have at least 2 hypotheses");
    }

    @Test
    public void testBestHypothesisSelected() {
        ReasoningResult reasoningResult = createReasoningResult("User is preparing for interviews", 0.9);

        InferenceResult result = inferenceEngine.infer(
                "What is Java?",
                reasoningResult,
                List.of(createMemory("Learning Java", 0.9), createMemory("Resume updated", 0.8)),
                List.of(createKnowledge("Interview Roadmap", "Interview preparation", 0.9)),
                "test-context"
        );

        Hypothesis best = result.bestHypothesis();
        assertNotNull(best, "Best hypothesis should not be null");
        assertTrue(best.confidence() >= 0.5, "Best hypothesis should have decent confidence");
        assertEquals("LIKELY", best.status(), "Best hypothesis should be LIKELY with high evidence");
    }

    @Test
    public void testUnknownInformationDetected() {
        // No memory, no knowledge
        ReasoningResult reasoningResult = createReasoningResult("Insufficient evidence", 0.1);

        InferenceResult result = inferenceEngine.infer(
                "What is quantum computing?",
                reasoningResult,
                List.of(),
                List.of(),
                "test-context"
        );

        assertFalse(result.unknownInformation().isEmpty(), "Should detect unknown information");
        assertTrue(result.unknownInformation().contains("No relevant memories available") ||
                   result.unknownInformation().size() >= 1, "Should list unknown items");
    }

    @Test
    public void testContradictoryEvidenceHandled() {
        // Low confidence reasoning + low importance memory
        ReasoningResult reasoningResult = createReasoningResult("Uncertain conclusion", 0.2);

        InferenceResult result = inferenceEngine.infer(
                "What is Java?",
                reasoningResult,
                List.of(createMemory("Irrelevant memory", 0.2)),
                List.of(),
                "test-context"
        );

        assertNotNull(result, "Should not throw on contradiction");
        assertFalse(result.contradictingEvidence().isEmpty(), "Should identify contradicting evidence");
    }

    @Test
    public void testDeterministicOutput() {
        // Same inputs twice
        ReasoningResult reasoningResult = createReasoningResult("Java is a programming language", 0.85);
        List<Memory> memories = List.of(createMemory("User is learning Java", 0.8));
        List<KnowledgeNode> knowledge = List.of(createKnowledge("Java", "Java programming", 0.9));

        InferenceResult result1 = inferenceEngine.infer("What is Java?", reasoningResult, memories, knowledge, "test");
        InferenceResult result2 = inferenceEngine.infer("What is Java?", reasoningResult, memories, knowledge, "test");

        assertEquals(result1.bestHypothesis().description(), result2.bestHypothesis().description(),
                "Best hypothesis should be deterministic");
        assertEquals(result1.bestHypothesis().confidence(), result2.bestHypothesis().confidence(),
                "Confidence should be deterministic");
        assertEquals(result1.hypotheses().size(), result2.hypotheses().size(),
                "Number of hypotheses should be deterministic");
        assertEquals(result1.recommendedNextInvestigation(), result2.recommendedNextInvestigation(),
                "Next investigation should be deterministic");
    }

    @Test
    public void testPipelineMetadataUpdated() {
        ReasoningResult reasoningResult = createReasoningResult("Java is a programming language", 0.85);

        InferenceResult result = inferenceEngine.infer(
                "What is Java?",
                reasoningResult,
                List.of(createMemory("User learning Java", 0.7)),
                List.of(createKnowledge("Java", "Java programming", 0.9)),
                "request-test"
        );

        // Verify all metadata fields are present
        assertNotNull(result.inferenceId(), "inferenceId");
        assertNotNull(result.hypotheses(), "hypotheses");
        assertNotNull(result.bestHypothesis(), "bestHypothesis");
        assertNotNull(result.supportingEvidence(), "supportingEvidence");
        assertNotNull(result.contradictingEvidence(), "contradictingEvidence");
        assertNotNull(result.unknownInformation(), "unknowns");
        assertNotNull(result.recommendedNextInvestigation(), "nextInvestigation");
        assertNotNull(result.generatedAt(), "generatedAt");
        assertTrue(result.confidence() >= 0.0 && result.confidence() <= 1.0, "confidence");
    }
}