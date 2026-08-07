package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reasoning Kernel Integration Test
 *
 * <p>This test verifies that the Cognitive Kernel works correctly with real reasoning operations.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 6
 */
public class ReasoningKernelIntegrationTest {

    private DefaultReasoningEngine reasoningEngine;

    @BeforeEach
    public void setUp() {
        this.reasoningEngine = new DefaultReasoningEngine();
    }

    @Test
    public void testMemoryAndKnowledgeToReasoning() {
        // Create memory
        MemoryContent memoryContent = new MemoryContent(
                "User previously asked about Java programming",
                null,
                Map.of("requestId", "req-001"),
                Instant.now()
        );
        MemoryId memoryId = new MemoryId("mem-001");
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata memoryMetadata = new MemoryMetadata(
                memoryId,
                MemoryType.EPISODIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("java", "programming"),
                0.8,
                0.9,
                "test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );
        Memory memory = new Memory(memoryId, memoryContent, memoryMetadata, Instant.now(), Instant.now());

        // Create knowledge
        KnowledgeNode knowledgeNode = KnowledgeNode.of(
                new KnowledgeId("kwn-001"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Programming",
                "Java is a high-level programming language",
                Map.of("confidence", 0.9, "authority", 0.85),
                Instant.now(),
                Instant.now()
        );

        // Run reasoning
        ReasoningResult result = reasoningEngine.reason(
                "What is Java?",
                List.of(memory),
                List.of(knowledgeNode)
        );

        // Verify results
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.reasoningId(), "Reasoning ID should not be null");
        assertFalse(result.findings().isEmpty(), "Should have findings");
        assertFalse(result.evidence().isEmpty(), "Should have evidence");
        assertNotNull(result.conclusion(), "Conclusion should not be null");
        assertTrue(result.confidence() >= 0.0 && result.confidence() <= 1.0, "Confidence should be 0-1");
        assertNotNull(result.risks(), "Risks should not be null");
        assertFalse(result.alternatives().isEmpty(), "Should have alternatives");
    }

    @Test
    public void testUnknownKnowledgeGracefulDegradation() {
        // Run reasoning with no knowledge or memory
        ReasoningResult result = reasoningEngine.reason(
                "What is quantum computing?",
                List.of(),
                List.of()
        );

        // Verify graceful degradation
        assertNotNull(result, "Result should not be null");
        assertFalse(result.findings().isEmpty(), "Should have findings");
        assertNotNull(result.conclusion(), "Conclusion should not be null");
        assertFalse(result.risks().isEmpty(), "Should identify risks for insufficient evidence");
        assertTrue(result.confidence() < 0.5, "Confidence should be minimal without evidence");
    }

    @Test
    public void testMultipleEvidenceCorrectConclusion() {
        // Create multiple knowledge nodes
        KnowledgeNode node1 = KnowledgeNode.of(
                new KnowledgeId("kwn-001"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Programming",
                "Java is a programming language used for enterprise applications",
                Map.of("confidence", 0.9, "authority", 0.85),
                Instant.now(),
                Instant.now()
        );
        KnowledgeNode node2 = KnowledgeNode.of(
                new KnowledgeId("kwn-002"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Virtual Machine",
                "JVM is the runtime environment that executes Java bytecode",
                Map.of("confidence", 0.85, "authority", 0.8),
                Instant.now(),
                Instant.now()
        );
        KnowledgeNode node3 = KnowledgeNode.of(
                new KnowledgeId("kwn-003"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Standard Library",
                "Provides pre-built classes and functions for Java developers",
                Map.of("confidence", 0.8, "authority", 0.75),
                Instant.now(),
                Instant.now()
        );

        // Run reasoning with multiple evidence
        ReasoningResult result = reasoningEngine.reason(
                "What is Java?",
                List.of(),
                List.of(node1, node2, node3)
        );

        // Verify conclusion uses top knowledge
        assertNotNull(result.conclusion(), "Conclusion should not be null");
        assertTrue(result.conclusion().contains("Java Programming"), 
                "Conclusion should reference top knowledge node");
        assertTrue(result.evidence().size() >= 3, "Should have evidence from all knowledge nodes");
        assertTrue(result.confidence() >= 0.4, "Confidence should be higher with more evidence");
    }

    @Test
    public void testPipelineMetadataUpdated() {
        // Create memory and knowledge
        MemoryContent memoryContent = new MemoryContent(
                "Memory about Java",
                null,
                Map.of(),
                Instant.now()
        );
        MemoryId memoryId = new MemoryId("mem-002");
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata memoryMetadata = new MemoryMetadata(
                memoryId,
                MemoryType.EPISODIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("java"),
                0.7,
                0.8,
                "test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );
        Memory memory = new Memory(memoryId, memoryContent, memoryMetadata, Instant.now(), Instant.now());

        KnowledgeNode knowledgeNode = KnowledgeNode.of(
                new KnowledgeId("kwn-004"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java",
                "Java programming language",
                Map.of("confidence", 0.9, "authority", 0.8),
                Instant.now(),
                Instant.now()
        );

        // Run reasoning
        ReasoningResult result = reasoningEngine.reason(
                "Explain Java",
                List.of(memory),
                List.of(knowledgeNode)
        );

        // Verify all required metadata fields
        assertNotNull(result.reasoningId());
        assertNotNull(result.conclusion());
        assertNotNull(result.summary());
        assertNotNull(result.findings());
        assertNotNull(result.evidence());
        assertNotNull(result.risks());
        assertNotNull(result.alternatives());
        assertNotNull(result.scope());
        assertNotNull(result.reasoningType());
        assertTrue(result.reasoningSteps() > 0);
        assertNotNull(result.completedAt());
    }

    @Test
    public void testReasoningDeterministic() {
        // Create knowledge
        KnowledgeNode knowledgeNode = KnowledgeNode.of(
                new KnowledgeId("kwn-005"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java",
                "Java is a programming language",
                Map.of("confidence", 0.9, "authority", 0.85),
                Instant.now(),
                Instant.now()
        );

        // Run reasoning twice with same inputs
        ReasoningResult result1 = reasoningEngine.reason(
                "What is Java?",
                List.of(),
                List.of(knowledgeNode)
        );
        ReasoningResult result2 = reasoningEngine.reason(
                "What is Java?",
                List.of(),
                List.of(knowledgeNode)
        );

        // Verify deterministic behavior (same conclusion, same confidence)
        assertEquals(result1.conclusion(), result2.conclusion(), 
                "Conclusions should be deterministic");
        assertEquals(result1.confidence(), result2.confidence(), 
                "Confidence should be deterministic");
        assertEquals(result1.findings(), result2.findings(), 
                "Findings should be deterministic");
        assertEquals(result1.alternatives(), result2.alternatives(), 
                "Alternatives should be deterministic");
        assertEquals(result1.risks(), result2.risks(), 
                "Risks should be deterministic");
        assertEquals(result1.reasoningSteps(), result2.reasoningSteps(), 
                "Reasoning steps should be deterministic");
    }
}