package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.RetrievalQuery;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;

/**
 * <b>KnowledgeRetrievalEngine</b>
 *
 * <p>The K4 contract that answers exactly one question: given a user's
 * cognitive context, which knowledge is most relevant? It retrieves and ranks
 * evidence deterministically - it does not reason, generate answers, embed or
 * approximate.</p>
 *
 * <p><b>Pipeline (locked):</b> query construction (from Context Intelligence)
 * → candidate search over titles, sections, contents and graph concepts →
 * one-hop graph expansion → weighted deterministic ranking → stable ordering.
 * The produced {@link RetrievalResult} is the permanent foundation that
 * Reasoning will consume.</p>
 *
 * <p><b>Contract:</b> implementations must be stateless with respect to a
 * single retrieval, thread-safe, immutable in their artifacts, and free of
 * embeddings, vector databases, NLP libraries and LLMs.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K4 Retrieval and Ranking</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKnowledgeRetrievalEngine
 */
public interface KnowledgeRetrievalEngine {

    /**
     * Executes the deterministic retrieval pipeline for the given query.
     *
     * @param query the canonical cognitive search request (must not be null)
     * @return the immutable, stably ordered retrieval result (never null;
     *         empty evidence when nothing matches)
     */
    RetrievalResult retrieve(RetrievalQuery query);
}
