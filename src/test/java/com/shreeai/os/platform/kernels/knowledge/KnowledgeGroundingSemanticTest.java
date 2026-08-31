package com.shreeai.os.platform.kernels.knowledge;

import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeGroundingService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgePayload;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PHASE-1 acceptance: semantic grounding reaches {@code groundingScore >= 0.90}
 * for evidence retrieved from ingested documents, while the legacy lexical
 * model (no-arg constructor) remains unchanged.
 */
class KnowledgeGroundingSemanticTest {

    private static final String CHUNK =
            "Vertical scaling adds more CPU and memory to a single machine. "
                    + "Vertical scaling makes a server stronger but has a hard limit.";

    @Test
    void ingestedEvidenceGroundsAtOrAboveZeroNinety() {
        DefaultKnowledgeService service = DefaultKnowledgeService.withInMemoryDefaults(
                new DefaultKnowledgeProcessingEngine());
        LocalDeterministicEmbedder embedder = new LocalDeterministicEmbedder();
        KnowledgeGroundingService grounding = new KnowledgeGroundingService(embedder);

        service.ingest("Scaling Guide", CHUNK, null);

        List<KnowledgeNode> evidence = service.searchBySimilarity(CHUNK);
        assertTrue(!evidence.isEmpty(), "ingested chunk must be retrievable");

        KnowledgePayload payload = grounding.ground(CHUNK, evidence, null);

        assertTrue(payload.getGroundingScore() >= 0.90,
                "grounding score must be >= 0.90 but was " + payload.getGroundingScore());
        assertFalse(payload.getCitations().isEmpty());
    }

    @Test
    void emptyEvidenceScoresZero() {
        KnowledgeGroundingService grounding =
                new KnowledgeGroundingService(new LocalDeterministicEmbedder());
        assertEquals(0.0, grounding.groundingScore("anything", List.of()));
    }

    @Test
    void noArgConstructorKeepsLexicalModel() {
        KnowledgeGroundingService lexical = new KnowledgeGroundingService();
        KnowledgeNode node = nodeWith("Postgres replication", "Streams WAL to a standby.", 1.0, 1.0);

        // Full coverage (1.0) + full evidence (1.0) → legacy model returns 1.0.
        assertEquals(1.0, lexical.groundingScore("Postgres replication standby", List.of(node)), 1e-9);

        // Partial coverage → exactly the legacy 50/50 blend.
        KnowledgeNode partial = nodeWith("Redis cache", "In-memory key value store.", 1.0, 1.0);
        double score = lexical.groundingScore("Postgres replication standby", List.of(partial));
        assertEquals(0.5 * 0.0 + 0.5 * 1.0, score, 1e-9);
    }

    private KnowledgeNode nodeWith(String label, String description, double confidence, double authority) {
        return KnowledgeNode.of(
                new com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId("node-" + System.nanoTime()),
                com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType.CONCEPT,
                com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState.ACTIVE,
                com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope.GLOBAL,
                label,
                description,
                java.util.Map.of("confidence", confidence, "authority", authority),
                java.time.Instant.now(),
                java.time.Instant.now());
    }
}
