package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeCitation;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgePayload;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link KnowledgeGroundingService}: citation extraction,
 * structured payload construction and grounding score behaviour.
 */
class KnowledgeGroundingServiceTest {

    private final KnowledgeGroundingService groundingService = new KnowledgeGroundingService();

    @Test
    void emptyNodeListProducesEmptyPayload() {
        KnowledgePayload payload = groundingService.ground("jvm memory", List.of(), "fallback");

        assertEquals(0, payload.getCitations().size());
        assertEquals(0.0, payload.getGroundingScore());
        assertEquals("fallback", payload.getTitle());
        assertEquals(0, payload.getMatchedNodeCount());
    }

    @Test
    void citationsAreNumberedAndCarryProvenance() {
        KnowledgeNode node = node(
                "JVM Memory",
                "The JVM manages memory through generational garbage collection.",
                Map.of("source", "oracle-docs", "confidence", 0.9, "authority", 0.8));

        KnowledgePayload payload =
                groundingService.ground("jvm memory", List.of(node), null);

        assertEquals(1, payload.getCitations().size());

        KnowledgeCitation citation = payload.getCitations().getFirst();

        assertEquals(1, citation.getIndex());
        assertEquals("JVM Memory", citation.getLabel());
        assertEquals("oracle-docs", citation.getSource());
        assertEquals(0.9, citation.getConfidence());
        assertEquals(0.8, citation.getAuthority());
        assertTrue(citation.getSnippet().contains("generational garbage collection"));
        assertEquals("JVM Memory", payload.getTitle());
    }

    @Test
    void groundingScoreRewardsTermCoverageAndEvidence() {
        KnowledgeNode coveringNode = node(
                "JVM Memory Model",
                "The JVM memory model defines heap and stack behaviour for threads.",
                Map.of("confidence", 1.0, "authority", 1.0));

        double score = groundingService.groundingScore(
                "jvm memory model", List.of(coveringNode));

        assertTrue(score > 0.8, "expected high grounding score, got " + score);

        KnowledgeNode unrelatedNode = node(
                "Cooking Pasta",
                "Boil water and add salt before the pasta.",
                Map.of());

        double unrelatedScore = groundingService.groundingScore(
                "jvm memory model", List.of(unrelatedNode));

        assertTrue(unrelatedScore < 0.6, "expected low grounding score, got " + unrelatedScore);
    }

    @Test
    void markdownLineContainsIndexLabelAndSource() {
        KnowledgeNode node = node(
                "Kernel",
                "A kernel is an isolated subsystem.",
                Map.of("source", "architecture-docs"));

        KnowledgeCitation citation = KnowledgeCitation.fromNode(1, node);

        String markdown = citation.toMarkdownLine();

        assertTrue(markdown.startsWith("[1] **Kernel**"));
        assertTrue(markdown.contains("architecture-docs"));
        assertFalse(markdown.isBlank());
    }

    @Test
    void toMapExposesStructuredCitations() {
        KnowledgeNode node = node(
                "Kernel",
                "A kernel is an isolated subsystem.",
                Map.of("source", "architecture-docs", "confidence", 0.7));

        KnowledgePayload payload = groundingService.ground("kernel", List.of(node), null);

        Map<String, Object> map = payload.toMap();

        assertEquals("kernel", map.get("query"));
        assertEquals(1, ((Number) map.get("matchedNodeCount")).intValue());
        assertTrue(map.get("groundingScore") instanceof Number);
        assertTrue(map.containsKey("citations"));
    }

    private KnowledgeNode node(String label, String description, Map<String, Object> metadata) {
        return KnowledgeNode.of(
                new KnowledgeId("kwn-" + label.toLowerCase().replace(' ', '-')),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                label,
                description,
                metadata,
                Instant.now(),
                Instant.now());
    }
}