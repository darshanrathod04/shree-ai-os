package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;

/**
 * <b>KnowledgeGraphBuilder</b>
 *
 * <p>The K3 contract that converts the isolated knowledge chunks of a
 * {@link KnowledgeDocument} into a connected, immutable graph of concepts.
 * It is a deterministic construction engine - not AI generation: concept
 * discovery, canonicalization and relationship detection are dictionary and
 * rule driven, and the produced graph is identical for identical input.</p>
 *
 * <p><b>Pipeline position:</b> {@code KnowledgeSourceRegistry} →
 * {@code DocumentIngestionEngine} → {@code KnowledgeDocument} → this builder
 * → {@link ConceptGraph}. Retrieval, ranking, embeddings and reasoning are
 * explicitly out of scope.</p>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe and
 * deterministic: the same document always produces the same concepts, the
 * same relationships, the same ids and the same ordering. No embeddings, no
 * LLM, no NLP libraries.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKnowledgeGraphBuilder
 */
public interface KnowledgeGraphBuilder {

    /**
     * Builds the canonical concept graph of the given document.
     *
     * @param document the canonical document to analyze (must not be null)
     * @return the immutable, deterministically ordered concept graph (never
     *         null; empty when the dictionary discovers no concepts)
     */
    ConceptGraph build(KnowledgeDocument document);
}
