package com.shreeai.os.platform.kernels.knowledge.engine.search;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.List;

/**
 * Enterprise Knowledge Search Engine.
 *
 * <p>Provides lexical-semantic retrieval over a KnowledgeGraph.
 * This interface is intentionally independent from storage and ranking
 * implementations so different search engines (BM25, Vector, Hybrid)
 * can be plugged in without changing the Knowledge Kernel.</p>
 *
 * <p><b>Design Principles</b></p>
 * <ul>
 *     <li>Stateless</li>
 *     <li>Thread-safe</li>
 *     <li>Read-only operations</li>
 *     <li>Immutable results</li>
 * </ul>
 *
 * Constitutional Authority:
 * EIO-KNW-101
 */
public interface KnowledgeSearchEngine {

    /**
     * Performs lexical-semantic search across the knowledge graph.
     *
     * <p>This is the primary retrieval method used by the Knowledge Kernel.
     * Implementations may combine token overlap, metadata relevance,
     * synonym expansion, or future vector similarity while preserving
     * the same contract.</p>
     *
     * @param graph knowledge graph (never null)
     * @param query natural language query
     * @return ranked immutable knowledge nodes
     */
    List<KnowledgeNode> semanticSearch(
            KnowledgeGraph graph,
            String query
    );

    /**
     * Performs exact keyword retrieval.
     *
     * @param graph knowledge graph
     * @param keyword keyword to search
     * @return ranked matching nodes
     */
    List<KnowledgeNode> keywordSearch(
            KnowledgeGraph graph,
            String keyword
    );

    /**
     * Retrieves knowledge by topic.
     *
     * Example:
     * "Nutrition", "Recovery", "Hypertrophy"
     *
     * @param graph knowledge graph
     * @param topic topic name
     * @return matching nodes
     */
    List<KnowledgeNode> topicSearch(
            KnowledgeGraph graph,
            String topic
    );

    /**
     * Retrieves knowledge matching one or more tags.
     *
     * Example tags:
     * muscle, recovery, protein
     *
     * @param graph knowledge graph
     * @param tags tag collection
     * @return ranked matching nodes
     */
    List<KnowledgeNode> tagSearch(
            KnowledgeGraph graph,
            Iterable<String> tags
    );
}