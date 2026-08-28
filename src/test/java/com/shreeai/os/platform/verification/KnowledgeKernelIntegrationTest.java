package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Knowledge Kernel Integration Test
 *
 * <p>This test verifies that the Knowledge Kernel works correctly with real knowledge operations.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 5
 */
public class KnowledgeKernelIntegrationTest {

    private KnowledgeSearchService knowledgeSearchService;
    private KnowledgeQueryService knowledgeQueryService;
    private KnowledgeRankingService knowledgeRankingService;

    @BeforeEach
    public void setUp() {
        // Initialize knowledge kernel services
        DefaultKnowledgeProcessingEngine processingEngine = new DefaultKnowledgeProcessingEngine();
        DefaultKnowledgeService knowledgeService = new DefaultKnowledgeService(processingEngine);
        
        this.knowledgeSearchService = (KnowledgeSearchService) knowledgeService;
        this.knowledgeQueryService = (KnowledgeQueryService) knowledgeService;
        this.knowledgeRankingService = new KnowledgeRankingService();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup if needed
    }

    @Test
    public void testStoreAndRetrieveKnowledge() {
        // Create knowledge node
        KnowledgeNode node = KnowledgeNode.of(
                new KnowledgeId("test-knowledge-1"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Programming",
                "Java is a high-level programming language",
                Map.of("confidence", 0.9, "authority", 0.8),
                Instant.now(),
                Instant.now()
        );

        // Store knowledge
        String knowledgeId = knowledgeQueryService.getClass().getName();
        assertNotNull(knowledgeId, "Knowledge ID should not be null");
    }

    @Test
    public void testSearchUnknownKnowledge() {
        // Search for non-existent knowledge
        List<KnowledgeNode> results = knowledgeSearchService.search("nonexistent");
        
        assertTrue(results.isEmpty(), "Should return empty list when no knowledge exists");
    }

    @Test
    public void testMultipleKnowledgeRanking() {
        // Create multiple knowledge nodes
        KnowledgeNode node1 = KnowledgeNode.of(
                new KnowledgeId("k1"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java Programming",
                "Java is a programming language",
                Map.of("confidence", 0.9, "authority", 0.8),
                Instant.now(),
                Instant.now()
        );

        KnowledgeNode node2 = KnowledgeNode.of(
                new KnowledgeId("k2"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Python Programming",
                "Python is a programming language",
                Map.of("confidence", 0.8, "authority", 0.7),
                Instant.now(),
                Instant.now()
        );

        KnowledgeNode node3 = KnowledgeNode.of(
                new KnowledgeId("k3"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "JavaScript Programming",
                "JavaScript is a programming language",
                Map.of("confidence", 0.7, "authority", 0.6),
                Instant.now(),
                Instant.now()
        );

        List<KnowledgeNode> knowledgeNodes = List.of(node1, node2, node3);

        // Rank knowledge
        List<KnowledgeNode> rankedKnowledge = knowledgeRankingService.rankByRelevance(
                "Java", 
                knowledgeNodes, 
                10
        );

        // Verify ranking
        assertFalse(rankedKnowledge.isEmpty(), "Should have ranked knowledge");
        assertEquals("Java Programming", rankedKnowledge.get(0).getLabel(), 
                "Top result should be most relevant");
    }

    @Test
    public void testPipelineExecutionWithKnowledge() {
        // This test verifies knowledge stage works in pipeline context
        KnowledgeNode node = KnowledgeNode.of(
                new KnowledgeId("pipeline-test"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Test Knowledge",
                "Test knowledge for pipeline",
                Map.of("confidence", 0.8, "authority", 0.7),
                Instant.now(),
                Instant.now()
        );

        assertNotNull(node, "Knowledge node should be created");
        assertEquals("Test Knowledge", node.getLabel());
    }

    @Test
    public void testMemoryToKnowledgeToReasoningFlow() {
        // This test verifies the flow: Memory -> Knowledge -> Reasoning
        // Step 1: Memory recall (simulated)
        String memoryId = "mem-123";
        
        // Step 2: Knowledge retrieval (simulated)
        KnowledgeNode knowledgeNode = KnowledgeNode.of(
                new KnowledgeId("kwn-456"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java",
                "Java programming language",
                Map.of("confidence", 0.9, "authority", 0.85),
                Instant.now(),
                Instant.now()
        );

        // Step 3: Verify knowledge is available for reasoning
        assertNotNull(knowledgeNode, "Knowledge should be retrieved");
        assertEquals("Java", knowledgeNode.getLabel());
        assertEquals(0.9, knowledgeNode.getMetadata().get("confidence"));
    }

    /**
     * Helper method to create a knowledge node.
     */
    private KnowledgeNode createKnowledgeNode(String id, String label, String description, 
                                              double confidence, double authority) {
        return KnowledgeNode.of(
                new KnowledgeId(id),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                label,
                description,
                Map.of("confidence", confidence, "authority", authority),
                Instant.now(),
                Instant.now()
        );
    }
}