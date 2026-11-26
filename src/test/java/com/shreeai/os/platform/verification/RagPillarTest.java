package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.engine.DefaultDocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.engine.SlidingWindowChunker;
import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagPillarTest {

    @Test
    @DisplayName("SlidingWindowChunker splits long sections with overlap while preserving short sections")
    void testSlidingWindowChunkerAndIngestion() {
        SlidingWindowChunker chunker = new SlidingWindowChunker(20, 5);
        assertEquals(1, chunker.chunk("short sentence here").size());

        StringBuilder longText = new StringBuilder();
        for (int i = 1; i <= 50; i++) {
            longText.append("word").append(i).append(' ');
        }
        List<String> windows = chunker.chunk(longText.toString().trim());
        assertTrue(windows.size() >= 3);
        // Verify overlap: window 1 ends with word16..word20 and window 2 starts with word16..
        assertTrue(windows.get(0).contains("word20"));
        assertTrue(windows.get(1).contains("word16"));

        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        String sourceId = registry.register(
                KnowledgeSourceType.TEXT, "Long Spec", "spec.txt", null, Map.of()).sourceId();
        DefaultDocumentIngestionEngine engine = new DefaultDocumentIngestionEngine(registry);

        StringBuilder hugeSection = new StringBuilder();
        for (int i = 1; i <= 400; i++) {
            hugeSection.append("token").append(i).append(' ');
        }
        IngestionResult result = engine.ingest(sourceId, "Long Spec", ParserType.TEXT, hugeSection.toString());
        assertTrue(result.totalChunks() >= 2, "400-word section should split into multiple overlapping chunks");
    }

    @Test
    @DisplayName("KnowledgeRankingService reranks exact-phrase matches above loose unigram matches")
    void testPhraseRerankingPrecision() {
        Instant now = Instant.now();
        KnowledgeNode looseNode = KnowledgeNode.of(
                new KnowledgeId("node-loose"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Search and Rank",
                "Uses fusion for rank and reciprocal logic across search",
                Map.of("confidence", 0.9, "authority", 0.9),
                now,
                now
        );
        KnowledgeNode exactPhraseNode = KnowledgeNode.of(
                new KnowledgeId("node-exact"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Reciprocal Rank Fusion",
                "Shree AI OS uses reciprocal rank fusion with k=60",
                Map.of("confidence", 0.9, "authority", 0.9),
                now,
                now
        );

        KnowledgeRankingService ranking = new KnowledgeRankingService();
        List<KnowledgeNode> ranked = ranking.rankByRelevance(
                "reciprocal rank fusion", List.of(looseNode, exactPhraseNode), 2);
        assertEquals(2, ranked.size());
        assertEquals("node-exact", ranked.get(0).getId().value());
    }
}
